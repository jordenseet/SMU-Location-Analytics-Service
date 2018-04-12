package Utilities;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;


/**
 * A class that manages connections to the database. It also has a utility
 * method that close connections, statements and resultsets
 */

/**
 *
 * @author G7T4
 */
public class ConnectionManager {

    private static final String RESOURCES = "/Utilities/connection.properties";
    private static String user;
    private static String password;
    private static String url;

    static {
        readDatabaseProperties();

        initDBDriver();
    }
   /**
     *read database properties
     * @throws Exception, SQL exceptions
     */
    private static void readDatabaseProperties() {
        InputStream is = null;
        try {
            // Retrieve properties from connection.properties via the CLASSPATH
            // WEB-INF/classes is on the CLASSPATH
            is = ConnectionManager.class.getResourceAsStream(RESOURCES);
            Properties props = new Properties();
            props.load(is);

            // load database connection details
            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String dbName = props.getProperty("db.name");
            user = props.getProperty("db.user");
           
            String osName = System.getProperty("os.name");
            if (osName.equals("Linux")) {

                password = props.getProperty("aws.db.password");
            } else {

                password = props.getProperty("db.password");
            }

            url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        } catch (Exception ex) {
  
            String message = "Unable to load '" + RESOURCES + "'.";

            System.out.println(message);
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionManager.class.getName()).log(Level.WARNING, "Unable to close connection.properties", ex);
                }
            }
        }
    }

    /**
     *get connections
     * @return get connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        String message = "url: " + url
                + "  , user: " + user
                + "  , password: " + password;
        Logger.getLogger(ConnectionManager.class.getName()).log(Level.INFO, message);

        return DriverManager.getConnection(url, user, password);

    }

    /**
     *close the connection
     * @param conn
     * @param stmt
     * @param rs
     * throws SQLException
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.WARNING,
                    "Unable to close ResultSet", ex);
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.WARNING,
                    "Unable to close Statement", ex);
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.WARNING,
                    "Unable to close Connection", ex);
        }
    }
    
      /**
     *
     * Manage Db connections providing shortcuts for Statements
     * throws RunTimeException
     */
    
    private static void initDBDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // unable to load properties file
            String message = "Unable to find JDBC driver for MySQL.";

            System.out.println(message);
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        }
    }


    
}
