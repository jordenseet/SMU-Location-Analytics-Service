/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import Entity.Admin;
import Entity.User;
import Utilities.ConnectionManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author G7T4
 */
public class AdminDAO {

    private static final String TBLNAME = "admin";
    private static Connection conn = null;
    private static PreparedStatement stmt = null;
    private static ResultSet results = null;

    /**
     *Get admin using ID
     * @param id
     * throw Exception
     * @return admin object
     */
    public static Admin getAdmin(String id) {
        try {
            conn = ConnectionManager.getConnection();
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //String url = "jdbc:mysql://localhost/sloca";
            //conn = DriverManager.getConnection(url, "root", "");//open connection
            //String url = "jdbc:mysql://http://localhost:8888/sloca";
            stmt = conn.prepareStatement("select * from " + TBLNAME + " where name = ?");
            stmt.setString(1, id);

            results = stmt.executeQuery();
            while (results.next()) {
                String name = results.getString(1);
                String password = results.getString(2);
                return new Admin(name, password);
            }
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
     *retrieve all admins 
     * throws Exception, SQLException
     * @return all the admin list
     */
    public static ArrayList<String> retrieveAllAdmins() {
        ArrayList<String> adminIdList = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet results = null;

        try {
            conn = ConnectionManager.getConnection();
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //String url = "jdbc:mysql://localhost/sloca";
            //conn = DriverManager.getConnection(url, "root", "");//open connection
            //String url = "jdbc:mysql://http://localhost:8888/sloca";

            //String url = "jdbc:mysql://localhost/sloca";
            stmt = conn.prepareStatement("select * from " + TBLNAME);

            results = stmt.executeQuery();
            while (results.next()) {
                String adminId = results.getString(1);//get all the columns needed
                adminIdList.add(adminId);
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
        return adminIdList;
    }
}
