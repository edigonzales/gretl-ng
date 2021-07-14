package ch.so.agi.gretl.util;

import java.sql.*;
import java.util.HashMap;

/**
 * Map containing the attribute (field) names of the corresponding database
 * table or view. The function getAttributeName(...) returns the exact attribute
 * name in the case of the underlying database.
 */
public class AttributeNameMap {

    private Connection con;
    private String tableName;

    private HashMap<String, String> attributeNames;

    private AttributeNameMap(Connection con, String tableName) {
        this.con = con;
        this.tableName = tableName;
        this.attributeNames = new HashMap<>();
    }

    /**
     * Creates a attribute name map for a given database connection and a
     * qualified table name. The keys are the lower case attribute names whereas
     * the values are the exact attribute names as received from 
     * ResultSetMetaData.
     * 
     * @param con          the database connection
     * @param tableName    the qualified table name (schema.table)
     * @return the attribute name map
     */
    public static AttributeNameMap createAttributeNameMap(Connection con, String tableName) {
        AttributeNameMap map = new AttributeNameMap(con, tableName);
        map.initializeFromDbSchema();

        return map;
    }

    private void initializeFromDbSchema() {
        // 0=1: No need for query to return row's as only the attribute names are of
        // interest.
        String sql = String.format("select * from %s where 0=1", tableName);

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String attrName = meta.getColumnName(i);
                attributeNames.put(attrName.toLowerCase(), attrName);
            }
        } catch (SQLException se) {
            throw new GretlException(se.getMessage(), se);
        }
    }

    /**
     * Returns the exact attribute name of a given column name.
     * 
     * @param nameInAnyCase a case insensitive column name
     * @return the exact (case sensitive) column name   
     */
    public String getAttributeName(String nameInAnyCase) {
        String nameLower = nameInAnyCase.toLowerCase();

        String nameInExactCase = attributeNames.get(nameLower);
        if (nameInExactCase == null)
            throw new GretlException(GretlException.TYPE_COLUMN_MISMATCH, String
                    .format("Requested attribute [%s] is not contained in this AttributeNameMap.", nameInAnyCase));

        return nameInExactCase;
    }
}
