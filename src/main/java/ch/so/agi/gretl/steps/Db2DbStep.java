package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Db2DbStep class is used as a step for transfer of tabulated data from one
 * to another database. It needs a sourceDb (Connector), a targetDb (Connector)
 * and a list of transferSet, containing 1. a boolean parameter concerning the
 * emptying of the target table, 2. a SQL-file containing a SELECT-statement and
 * 3. a qualified target schema and table name (schema.table).
 */
public class Db2DbStep {
    public static final String PREFIX = "ch.so.agi.gretl.steps.Db2DbStep";
    public static final String SETTING_BATCH_SIZE = PREFIX + ".batchSize";
    public static final String SETTING_FETCH_SIZE = PREFIX + ".fetchSize";
    private static GretlLogger log = LogEnvironment.getLogger(Db2DbStep.class);
    private String taskName;
    private int batchSize = 5000;
    private int fetchSize = 5000;

    public Db2DbStep() {
        this(null);
    }

    public Db2DbStep(String taskName) {
        if (taskName == null) {
            taskName = Db2DbStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
    }

    /**
     * Main method. Calls for each transferSet the private method 
     * processTransferSet.
     * 
     * @param sourceDb     The source database connection
     * @param targetDb     The target database connection
     * @param transferSets A list of transfer sets
     * @throws Exception todo
     */
    public void processAllTransferSets(Connector sourceDb, Connector targetDb, List<TransferSet> transferSets)
            throws Exception {
        processAllTransferSets(sourceDb, targetDb, transferSets, new Settings(),
                new java.util.HashMap<String, String>());
    }

    public void processAllTransferSets(Connector sourceDb, Connector targetDb, List<TransferSet> transferSets,
            Settings settings, Map<String, String> params) throws Exception {
        assertValidTransferSets(transferSets);

        String batchSizeStr = settings.getValue(SETTING_BATCH_SIZE);
        if (batchSizeStr != null) {
            try {
                int newBatchSize = Integer.parseInt(batchSizeStr);
                if (newBatchSize > 0) {
                    batchSize = newBatchSize;
                }
            } catch (NumberFormatException e) {

            }
        }

        String fetchSizeStr = settings.getValue(SETTING_FETCH_SIZE);
        if (fetchSizeStr != null) {
            try {
                int newFetchSize = Integer.parseInt(fetchSizeStr);
                if (newFetchSize >= 0) { // fetchSize 0 -> fetch all at once
                    fetchSize = newFetchSize;
                }
            } catch (NumberFormatException e) {

            }
        }

        log.lifecycle(String.format("Start Db2DbStep(Name: %s SourceDb: %s TargetDb: %s Transfers: %s)", taskName,
                sourceDb, targetDb, transferSets));

        ArrayList<String> rowCountStrings = new ArrayList<String>();

        try {
            Connection sourceDbConnection = sourceDb.connect();
            Connection targetDbConnection = targetDb.connect();
            for (TransferSet transferSet : transferSets) {
                // Check if file is readable
                if (!transferSet.getInputSqlFile().canRead()) {
                    throw new IllegalArgumentException(
                            "File" + transferSet.getInputSqlFile().getName() + " not found or not readable");
                }
                // Check if File is UTF8
                FileStylingDefinition.checkForUtf8(transferSet.getInputSqlFile());
                // Check if File contains no BOM. If File is Empty, there will be a
                // NullPointerException catched away.
                try {
                    FileStylingDefinition.checkForBOMInFile(transferSet.getInputSqlFile());
                } catch (NullPointerException e) {
                }
                ;
                int rowCount = processTransferSet(sourceDbConnection, targetDbConnection, transferSet, params);
                rowCountStrings.add(Integer.toString(rowCount));
            }
            sourceDbConnection.commit();
            targetDbConnection.commit();
            sourceDb.close();
            targetDb.close();

            String rowCountList = String.join(",", rowCountStrings);
            log.lifecycle(String.format(
                    "Db2DbStep %s: Transfered all Transfersets. Number of Transfersets: %s, transfered rows: [%s]",
                    taskName, rowCountStrings.size(), rowCountList));
        } catch (Exception e) {
            log.error("Exception while executing processAllTransferSets()", e);
            throw e;
        } finally {
            if (!sourceDb.isClosed()) {
                try {
                    sourceDb.connect().rollback();
                } catch (SQLException e) {
                    log.error("failed to rollback", e);
                } finally {
                    try {
                        sourceDb.close();
                    } catch (SQLException e) {
                        log.error("failed to close", e);
                    }
                }
            }
            if (!targetDb.isClosed()) {
                try {
                    targetDb.connect().rollback();
                } catch (SQLException e) {
                    log.error("failed to rollback", e);
                } finally {
                    try {
                        targetDb.close();
                    } catch (SQLException e) {
                        log.error("failed to close", e);
                    }
                }
            }
        }
    }

    /**
     * Controls the execution of a TransferSet
     * 
     * @param srcCon      source database connection
     * @param targetCon   target database connection
     * @param transferSet transferset
     * @throws SQLException if the resultset or the insert row statement could not 
     * be created or if the batch could not be executed 
     * @throws FileNotFoundException
     * @throws EmptyFileException
     * @throws NotAllowedSqlExpressionException
     * @returns The number of processed rows
     */
    private int processTransferSet(Connection srcCon, Connection targetCon, TransferSet transferSet,
            Map<String, String> params)
            throws SQLException, IOException, EmptyFileException, NotAllowedSqlExpressionException {
        if (transferSet.getDeleteAllRows()) {
            deleteDestTableContents(targetCon, transferSet.getOutputQualifiedTableName());
        }
        String selectStatement = extractSingleStatement(transferSet.getInputSqlFile(), params);
        log.debug("SQL statement: " + selectStatement);
        ResultSet rs = createResultSet(srcCon, selectStatement);
        PreparedStatement insertRowStatement = createInsertRowStatement(srcCon, targetCon, rs, transferSet);
        int columncount = rs.getMetaData().getColumnCount();
        int k = 0;
        while (rs.next()) {
            transferRow(rs, insertRowStatement, columncount);
            if (k % batchSize == 0) {
                log.debug("Batching next " + batchSize + " records. (Total: " + String.valueOf(k) + ")");
                insertRowStatement.executeBatch();
                insertRowStatement.clearBatch();
            }
            k += 1;
        }

        insertRowStatement.executeBatch();
        log.debug("Transfer " + k + " rows and " + columncount + " columns to table "
                + transferSet.getOutputQualifiedTableName());

        return k;
    }

    /**
     * Copies a row of the source ResultSet to the target table
     * 
     * @param rs                 ResultSet
     * @param insertRowStatement The prepared insert statement
     * @param columncount        the number of columns
     * @throws SQLException
     */
    private void transferRow(ResultSet rs, PreparedStatement insertRowStatement, int columncount) throws SQLException {
        // assign column wise values
        for (int j = 1; j <= columncount; j++) {
            insertRowStatement.setObject(j, rs.getObject(j));
        }
        // insertRowStatement.execute();
        insertRowStatement.addBatch();
    }

    /**
     * Delete the content of the target table
     * 
     * @param targetCon     target database connection
     * @param destTableName qualified target table name (schema.table)
     * @throws SQLException
     */
    private void deleteDestTableContents(Connection targetCon, String destTableName) throws SQLException {
        String sqltruncate = "DELETE FROM " + destTableName;
        try {
            PreparedStatement stmt = targetCon.prepareStatement(sqltruncate);
            stmt.execute();
            log.info("DELETE executed");
        } catch (SQLException e1) {
            log.error("DELETE FROM TABLE " + destTableName + " failed.", e1);
            throw e1;
        }
    }

    /**
     * Creates the resultset with the select statement from the input file
     * 
     * @param srcCon             source database connection
     * @param sqlSelectStatement The sql statement extract from the input file
     * @return rs Resultset
     * @throws SQLException
     */
    private ResultSet createResultSet(Connection srcCon, String sqlSelectStatement) throws SQLException {
        Statement SQLStatement = srcCon.createStatement();
        SQLStatement.setFetchSize(fetchSize);
        ResultSet rs = SQLStatement.executeQuery(sqlSelectStatement);

        return rs;
    }

    /**
     * Prepares the insert statement. Leaves the values as '?'
     * 
     * @param srcCon    source database connection
     * @param targetCon target database connection
     * @param rs        resultset
     * @param tSet      transferset
     * @return The InsertRowStatement
     * @throws SQLException
     */
    private PreparedStatement createInsertRowStatement(Connection srcCon, Connection targetCon, ResultSet rs,
            TransferSet tSet) {
        ResultSetMetaData meta = null;
        PreparedStatement insertRowStatement = null;

        try {
            meta = rs.getMetaData();

            String insertColNames = buildInsertColumnNames(meta, targetCon, tSet.getOutputQualifiedTableName());
            String valuesList = buildValuesList(meta, tSet);

            String sql = "INSERT INTO " + tSet.getOutputQualifiedTableName() + " (" + insertColNames + ") VALUES ("
                    + valuesList + ")";
            insertRowStatement = targetCon.prepareStatement(sql);

            log.info(String.format(taskName + ": Sql insert statement: [%s]", sql));

        } catch (SQLException g) {
            throw new GretlException(g);
        }

        return insertRowStatement;
    }

    private static String buildValuesList(ResultSetMetaData meta, TransferSet tSet) {
        StringBuffer valuesList = new StringBuffer();
        try {
            for (int j = 1; j <= meta.getColumnCount(); j++) {
                if (j > 1) {
                    valuesList.append(", ");
                }

                String colName = meta.getColumnName(j);

                if (tSet.isGeoColumn(colName)) {
                    String func = tSet.wrapWithGeoTransformFunction(colName, "?");
                    valuesList.append(func);
                } else {
                    valuesList.append("?");
                }
            }
        } catch (SQLException se) {
            throw new GretlException(se);
        }
        return valuesList.toString();
    }

    private static String buildInsertColumnNames(ResultSetMetaData sourceMeta, Connection targetCon,
            String targetTableName) {
        StringBuffer columnNames = new StringBuffer();
        AttributeNameMap colMap = AttributeNameMap.createAttributeNameMap(targetCon, targetTableName);
        try {
            for (int j = 1; j <= sourceMeta.getColumnCount(); j++) {
                if (j > 1) {
                    columnNames.append(", ");
                }

                String srcColName = sourceMeta.getColumnName(j);
                String targetColName = colMap.getAttributeName(srcColName);
                columnNames.append(targetColName);

            }
        } catch (SQLException se) {
            throw new GretlException(se);
        }
        return columnNames.toString();
    }

    /**
     * Extracts a single statement out of the sql file and checks if it fits the
     * conditions.
     * 
     * @param a file
     * @return select statement as string
     * @throws FileNotFoundException
     * @throws EmptyFileException
     */
    private String extractSingleStatement(File targetFile, Map<String, String> params) throws IOException {
        SqlReader reader = new SqlReader();
        String firstStmt = reader.readSqlStmt(targetFile, params);
        if (firstStmt == null) {
            log.info("Empty file. No statement to execute!");
            throw new EmptyFileException("Empty file: " + targetFile.getName());
        }
        String secondStmt = reader.nextSqlStmt();
        if (secondStmt != null) {
            log.info("There is more then one Statement in the file.");
            throw new IOException("There is more then one Statement in the file.");
        }
        reader.close();

        return firstStmt;
    }

    /**
     * Checks if the transferset list is not empty.
     * 
     * @param transferSets
     * @throws EmptyListException
     */
    private void assertValidTransferSets(List<TransferSet> transferSets) throws EmptyListException {
        if (transferSets.size() == 0) {
            throw new EmptyListException();
        }

        for (TransferSet ts : transferSets) {
            if (!ts.getInputSqlFile().canRead()) {
                throw new GretlException("Can not read input sql file at path: " + ts.getInputSqlFile().getPath());
            }
        }
    }

}