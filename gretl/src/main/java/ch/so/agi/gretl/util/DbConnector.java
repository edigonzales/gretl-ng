package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

/** 
 * Utility class to open database connections. Database drivers / vendors are 
 * hardcoded. If you want to support more databases, add them here and in the
 * build file as dependency. 
 * @author agi.so.ch
 * @since 1.0.0
 */
public class DbConnector {

    private static HashMap<String, String> jdbcDriverClasses = null;

    private static GretlLogger log = LogEnvironment.getLogger(DbConnector.class);

    static {
        jdbcDriverClasses = new HashMap<String, String>();
        jdbcDriverClasses.put("postgresql", "org.postgresql.Driver");
        jdbcDriverClasses.put("sqlite", "org.sqlite.JDBC");
        jdbcDriverClasses.put("derby", "org.apache.derby.jdbc.EmbeddedDriver");
        jdbcDriverClasses.put("oracle", "oracle.jdbc.driver.OracleDriver");
    }

    /**
     * Returns the connection to a specific database. The database is specified by
     * the arguments connectionUrl, userName and password.
     *
     * @param connectionUrl database specific jdbc connection url
     * @param userName      database user
     * @param password      password of given database user
     * @return the connection to the specific database
     */
    public static Connection connect(String connectionUrl, String userName, String password) {
        Connection con = null;
        try {
            String[] splits = connectionUrl.split(":");
            if (splits.length < 3)
                throw new IllegalArgumentException("Connection url string is malformed: " + connectionUrl);

            String driverType = splits[1];
            String driverClassName = jdbcDriverClasses.get(driverType);
            if (driverClassName == null)
                throw new IllegalArgumentException(
                        "Configuration error. Connection url contains unsupported driver type: " + driverType + "("
                                + connectionUrl + ")");

            Driver driver = null;

            try {
                driver = (Driver) Class.forName(driverClassName).newInstance();
            } catch (Exception e) {
                throw new GretlException("Could not find and load jdbc driver class " + driverClassName, e);
            }

            DriverManager.registerDriver(driver);

            con = DriverManager.getConnection(connectionUrl, userName, password);
            con.setAutoCommit(false);

            log.debug("DB connected with these Parameters:  Connection url:" + connectionUrl + " Username: " + userName
                    + " Password: " + password);

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                    con.close();
                    con = null;
                } catch (SQLException f) {
                    log.info(f.toString());
                }
            }
            log.error("Could not connect to: " + connectionUrl, e);
            throw new GretlException("Could not connect to: " + connectionUrl, e);
        } catch (Exception e) {
            log.error("Connection URL is undefined: " + connectionUrl, e);
            throw new GretlException("Connection URL is undefined: " + connectionUrl, e);
        }
        return con;
    }
}
