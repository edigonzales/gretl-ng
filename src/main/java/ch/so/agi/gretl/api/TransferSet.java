package ch.so.agi.gretl.api;

import ch.so.agi.gretl.steps.GeometryTransform;
import ch.so.agi.gretl.util.GretlException;

import java.io.File;
import java.util.HashMap;

/**
 * A transferset is a container for the sql file containing the select statement 
 * for the source database, the qualified table name for the targe database
 * and a flag whether the target table should be emptied before the inserts. It 
 * is also responsible for dealing with specific geometry column handling (aka 
 * abstraction of the geometry columns by using wkb, wkt or geojson instead
 * of the original database vendor geometry data type). 
 */
public class TransferSet {
    private boolean deleteAllRows;
    private File inputSqlFile;
    private String outputQualifiedTableName;
    private HashMap<String, GeometryTransform> geoColumns;

    /**
     * Constructor for a transfer set. It initializes the geometry columns by 
     * creating a GeometryTransform object for each geometry column.
     * 
     * @param inputSqlFilePath                     the input sql file path
     * @param outputQualifiedSchemaAndTableName    the qualified target table name
     * @param outputDeleteAllRows                  a flag whether the target table should be emptied before 
     * @param geoColumns                           the geometry columns array. A geometry column is a string separated by colons "colname:geomtype[:options]" 
     */
    public TransferSet(String inputSqlFilePath, String outputQualifiedSchemaAndTableName, boolean outputDeleteAllRows,
            String[] geoColumns) {
        if (inputSqlFilePath == null || inputSqlFilePath.length() == 0)
            throw new IllegalArgumentException("inputSqlFilePath must not be null or empty");

        this.inputSqlFile = new File(inputSqlFilePath);

        if (outputQualifiedSchemaAndTableName == null || outputQualifiedSchemaAndTableName.length() == 0)
            throw new IllegalArgumentException("outputQualifiedTableName must not be null or empty");

        this.outputQualifiedTableName = outputQualifiedSchemaAndTableName;

        this.deleteAllRows = outputDeleteAllRows;

        initGeoColumnHash(geoColumns);
    }

    /**
     * Constructor for a transfer set.
     *  
     * @param inputSqlFilePath                     the input sql file path
     * @param outputQualifiedSchemaAndTableName    the qualified target table name
     * @param outputDeleteAllRows                  a flag whether the target table should be emptied before 
     */
    public TransferSet(String inputSqlFilePath, String outputQualifiedSchemaAndTableName, boolean outputDeleteAllRows) {
        this(inputSqlFilePath, outputQualifiedSchemaAndTableName, outputDeleteAllRows, null);
    }
    
    private void initGeoColumnHash(String[] colList) {
        geoColumns = new HashMap<String, GeometryTransform>();

        if (colList != null) {
            for (String colDef : colList) {
                if (colDef == null)
                    throw new GretlException("Geometry column definition array must not contain null values");

                GeometryTransform trans = GeometryTransform.createFromString(colDef);

                geoColumns.put(trans.getColNameUpperCase(), trans);
            }
        }
    }

    public boolean getDeleteAllRows() {
        return deleteAllRows;
    }

    public File getInputSqlFile() {
        return inputSqlFile;
    }

    public void setInputSqlFile(File inputSqlFile) {
        this.inputSqlFile = inputSqlFile;
    }

    public String getOutputQualifiedTableName() {
        return outputQualifiedTableName;
    }

    public boolean isGeoColumn(String colName) {
        return geoColumns.containsKey(colName.toUpperCase());
    }

    /**
     * Wraps a geometry column with a specific geometry function (wkb, wkt or 
     * geojson) for the insert statement, e.g. "perimeter" -> "ST_GeomFromText(\"perimeter\", 2056)".
     * 
     * @param colName             the geometry column name
     * @param valuePlaceHolder    the prepared statement value placeholder (e.g. "?")
     * @return the sql function string
     */
    public String wrapWithGeoTransformFunction(String colName, String valuePlaceHolder) {
        String res = null;

        GeometryTransform trans = geoColumns.get(colName.toUpperCase());

        if (trans == null)
            throw new GretlException("Given colName was not defined / configured as geometry column");

        res = trans.wrapWithGeoTransformFunction(valuePlaceHolder);

        return res;
    }

    public String toString() {
        String colString = String.join(",", geoColumns.keySet());

        String res = String.format(
                "TransferSet( SqlSelectFile: %s, TargetTable: %s, DeleteTargetRows: %s, GeoColumns: %s)",
                inputSqlFile.getAbsolutePath(), outputQualifiedTableName, deleteAllRows, colString);

        return res;
    }
}
