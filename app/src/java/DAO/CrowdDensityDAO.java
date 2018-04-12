/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import Entity.CrowdDensity;
import Utilities.ConnectionManager;
import Utilities.DAOUtility;
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
public class CrowdDensityDAO {

    private static Connection conn;
    private static PreparedStatement stmt;
    private static ResultSet results;

    /**
     *retrieve crowd densities in window
     * @param datetime
     * Throws Exception
     * @return list of crowd density list in that window
     */
    public static ArrayList<CrowdDensity> retrieveCrowdDensitiesInWindow(String datetime) {
        ArrayList<CrowdDensity> cdList = new ArrayList<>();
        datetime = DAOUtility.formatDatetime(datetime);

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select name, sum(people) as total "
                    + "from semanticplace s, ( "
                    + "select count(B.mac) as people, location_id "
                    + "from ( "
                    + "select time_stamp, l.mac, location_id "
                    + "from location l, ( "
                    + "select max(time_stamp) as latest, mac "
                    + "from location "
                    + "where time_stamp >= ? - interval 15 minute "
                    + "and time_stamp < ? "
                    + "group by mac) as A "
                    + "where time_stamp = latest "
                    + "and l.mac = A.mac "
                    + ") as B "
                    + "group by location_id) as C "
                    + "where s.location_id = C.location_id "
                    + "group by name "
                    + "order by total desc;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, datetime);
            stmt.setString(2, datetime);
            results = stmt.executeQuery();
            while (results.next()) {
                String name = results.getString(1);
                int numPeople = Integer.parseInt(results.getString(2));
                cdList.add(new CrowdDensity(name, numPeople));
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
        return cdList;
    }

    /**
     *retrieve crowd densities in that window by floor
     * @param datetime
     * @param floor
     * @return the list of crowd densities in that floor
     */
     public static ArrayList<CrowdDensity> retrieveCrowdDensitiesInWindowOnFloor(String datetime, String floor) {
        ArrayList<CrowdDensity> cdList = new ArrayList<>();
        datetime = DAOUtility.formatDatetime(datetime);

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            conn = ConnectionManager.getConnection();
            String query = "select name, sum(people) as total\n" +
                            "from semanticplace s\n" +
                            "left outer join (\n" +
                            "	select count(mac) as people, location_id\n" +
                            "	from (\n" +
                            "		select max(time_stamp), mac, location_id\n" +
                            "		from location\n" +
                            "		where time_stamp >= '" + datetime + "' - interval 15 minute\n" +
                            "		and time_stamp < '" + datetime + "'\n" +
                            "		group by mac\n" +
                            "	) as A\n" +
                            "	group by location_id\n" +
                            ") as B\n" +
                            "on s.location_id = B.location_id\n" +
                            "group by name\n" +
                            "having name like '%" + floor + "%'\n" +
                            "order by name;";
            stmt = conn.prepareStatement(query);

            results = stmt.executeQuery();
            while (results.next()) {
                String name = results.getString(1);
                String numPeople = results.getString(2);
                int num = 0;
                if (numPeople != null) {
                    num = Integer.parseInt(numPeople);
                }
                cdList.add(new CrowdDensity(name, num));
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
        return cdList;
    }
}
