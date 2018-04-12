package DAO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author G7T4
 */
import Utilities.DAOUtility;
import Entity.User;
import Utilities.ConnectionManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author JaneSeah
 */
public class UserDAO {

    private static final String TBLNAME = "users";
    private static PreparedStatement stmt;
    private static ResultSet results;
    private static Connection conn;

    /**
     *
     * @param id
     * @return
     */
    public static User getUserWithEmail(String id) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME + " where email like ?");
            stmt.setString(1, id + "@%");

            results = stmt.executeQuery();
            while (results.next()) {
                String macAddress = results.getString(1);//get all the columns needed
                String name = results.getString(2);
                String password = results.getString(3);
                String email = results.getString(4);
                char gender = results.getString(5).charAt(0);
                return new User(macAddress, name, password, email, gender);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();//print error message
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * @param mac
     * @return
     */
    public static User getUserWithMacAddress(String mac) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME + " where mac = '" + mac + "'");

            results = stmt.executeQuery();
            if (results.next()) {
                String macAddress = results.getString(1);//get all the columns needed
                String name = results.getString(2);
                String password = results.getString(3);
                String email = results.getString(4);
                char gender = results.getString(5).charAt(0);
                return new User(macAddress, name, password, email, gender);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();//print error message
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param date
     * @return
     */
    public static ArrayList<User> retrieveUsersInWindow(String date) {
        ArrayList<User> users = new ArrayList();

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select distinct u.mac, name, password, email, gender "
                    + "from users u, ("
                    + "  select * "
                    + "  from location "
                    + "  where time_stamp >= ? - interval 15 minute "
                    + "  and time_stamp < ? "
                    + ") as A "
                    + "where u.mac = A.mac;";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            stmt.setString(2, date);
            //System.out.println(stmt);
            results = stmt.executeQuery();
            while (results.next()) {
                //System.out.println(results.toString());
                String mac = results.getString(1);
                String name = results.getString(2);
                String password = results.getString(3);
                String email = results.getString(4);
                char gender = (results.getString(5) == null ? 'M' : results.getString(5).charAt(0));
                users.add(new User(mac, name, password, email, gender));
            }
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     *
     * @param date
     * @return
     */
    public static ArrayList<User> retrievePersonsInWindow(String date) {
        ArrayList<User> users = new ArrayList();

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "SELECT DISTINCT location.mac, users.name, password, email, gender FROM location LEFT OUTER JOIN users ON users.mac = location.mac WHERE time_stamp >= DATE_ADD(?, INTERVAL -15 MINUTE) AND time_stamp <= ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            stmt.setString(2, date);

            results = stmt.executeQuery();
            while (results.next()) {
                String mac = results.getString(1);
                String name = results.getString(2);
                String password = results.getString(3);
                String email = results.getString(4);
                char gender = (results.getString(5) == null ? 'M' : results.getString(5).charAt(0));
                users.add(new User(mac, name, password, email, gender));
            }
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     *
     * @return
     */
    public static ArrayList<User> retrieveAllUsers() {
        ArrayList<User> userList = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME);

            results = stmt.executeQuery();
            while (results.next()) {
                String macAddress = results.getString(1);//get all the columns needed
                String name = results.getString(2);
                String password = results.getString(3);
                String email = results.getString(4);
                char gender = results.getString(5).charAt(0);
                userList.add(new User(macAddress, name, password, email, gender));
            }
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     *
     * @param path
     * @throws SQLException
     */
    public void insert(String path) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("load data local infile ? into table users fields terminated by ',' lines terminated by '\\n'");
            stmt.setString(1, path);
            stmt.execute();
            System.out.println("demographics.csv to sql successful");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     *
     * @throws SQLException
     */
    public void deleteTable() throws SQLException {

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            System.out.println(TBLNAME);
            stmt = conn.prepareStatement("TRUNCATE TABLE " + TBLNAME);
            stmt.executeUpdate();
            System.out.println("delete executed");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    public static boolean validate(String username, String password) {
        System.out.println(retrievePassword(username));
        System.out.println(password);
        if (password.equals(retrievePassword(username))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param username
     * @return
     */
    public static String retrievePassword(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {

            conn = ConnectionManager.getConnection();
            //Create a PreparedStatement with a SQL statement
            stmt = conn.prepareStatement("select u.password, from demographics where username = ?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString("user_password"));
                return rs.getString("user_password");
            }
            ConnectionManager.close(conn, stmt, rs);

        } catch (SQLException ex) {
            //Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

}
