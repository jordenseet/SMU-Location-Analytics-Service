package DAO;

import Utilities.DAOUtility;
import Entity.LocationUpdate;
import Entity.User;
import Utilities.ConnectionManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;

/**
 *
 * @author G7T4
 */
public class LocationUpdateDAO {

    private static final String TBLNAME = "location";
    private static Connection conn;
    private static PreparedStatement stmt;
    private static ResultSet results;

    /**
     *get location by id
     * @param id
     * @return the location by ID
     */
    public static LocationUpdate getLocation(String id) {

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME + " where location_id = ?");
            stmt.setString(2, id);
            results = stmt.executeQuery();
            while (results.next()) {
                String timestamp = results.getString(1);//get all the columns needed
                String mac = results.getString(2);
                int locationId = results.getInt(3);
                return new LocationUpdate(timestamp, mac, locationId);
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
        return null;
    }

    /**
     *retrieve all locations 
     * @return arraylist of locations retrieved
     */
    public static ArrayList<String> retrieveAllLocations() {
        ArrayList<String> locationList = new ArrayList<String>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME);

            results = stmt.executeQuery();
            while (results.next()) {
                String locationId = results.getString(3);//get all the columns needed
                locationList.add(locationId);
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
        return locationList;
    }

    /**
     *retrieve all the location update
     * @return arraylist of locations update
     */
    public static ArrayList<LocationUpdate> retrieveAll() {
        ArrayList<LocationUpdate> locationList = new ArrayList<LocationUpdate>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME);

            results = stmt.executeQuery();
            while (results.next()) {
                locationList.add(new LocationUpdate(results.getString(1), results.getString(2), results.getInt(3)));
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
        return locationList;
    }

    /**
     *retrieve all validated locations details
     * Throws Exception
     * @return hashmap of validated locations detail
     */
    public static HashMap<String, Integer> retrieveAllValidatedLocationsDetails() {
        HashMap<String, Integer> allLocations = new HashMap<String, Integer>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME);
            results = stmt.executeQuery();
            while (results.next()) {
                String timeStamp = (results.getString(1)).substring(0, results.getString(1).length() - 2);//get all the columns needed
                String macAdd = results.getString(2);
                String key = timeStamp.trim() + macAdd.trim();
                int value = results.getInt(3);
                allLocations.put(key, value);
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
        return allLocations;
    }

    /**
     *retrieve user updates in that window
     * @param datetime
     * @return the users update in that window frame
     */
    public static ArrayList<LocationUpdate> retrieveUserUpdatesInWindow(String datetime) {
        ArrayList<LocationUpdate> updates = new ArrayList<>();
        datetime = DAOUtility.formatDatetime(datetime);
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select * from location "
                    + "where time_stamp >= ? - interval 15 minute"
                    + " and time_stamp < ?"
                    + " order by locationid, time_stamp;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, datetime);
            stmt.setString(2, datetime);
            results = stmt.executeQuery();
            while (results.next()) {
                String timeStamp = results.getString(1);
                String macAddress = results.getString(2);
                int locationId = results.getInt(3);
                updates.add(new LocationUpdate(timeStamp, macAddress, locationId));
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
        return updates;
    }
    
    /**
     *retrieve user update in that window frame
     * @param macAddress
     * @param datetime
     * @return arraylist of retrieved user updates in window
     */
    public static ArrayList<LocationUpdate> retrieveUserUpdatesInWindow(String macAddress, String datetime) {
        ArrayList<LocationUpdate> updates = new ArrayList<>();
        datetime = DAOUtility.formatDatetime(datetime);
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select time_stamp, location_id "
                    + "from location "
                    + "where mac = ? "
                    + " and time_stamp >= ? - interval 15 minute"
                    + " and time_stamp < ?"
                    + " order by time_stamp;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, macAddress);
            stmt.setString(2, datetime);
            stmt.setString(3, datetime);
            results = stmt.executeQuery();
            while (results.next()) {
                String timeStamp = results.getString(1);
                int locationId = results.getInt(2);
                updates.add(new LocationUpdate(timeStamp, macAddress, locationId));
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
        return updates;
    }

    /**
     *retrieve user updates in window
     * @param macAddress
     * @param start
     * @param end
     * @return arraylist of retrieved user updates in window
     */
    public static ArrayList<LocationUpdate> retrieveUserUpdatesInWindow(String macAddress, String start, String end) {
        ArrayList<LocationUpdate> updates = new ArrayList<>();
        // Start and end datetimes are already in the right format when this method is called.
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select time_stamp, location_id "
                    + "from location "
                    + "where mac = ? "
                    + "and time_stamp > ? "
                    + "and time_stamp < ? "
                    + "order by time_stamp;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, macAddress);
            stmt.setString(2, start);
            stmt.setString(3, end);
            results = stmt.executeQuery();
            while (results.next()) {
                String timeStamp = results.getString(1);
                int locationId = results.getInt(2);
                updates.add(new LocationUpdate(timeStamp, macAddress, locationId));
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
        return updates;
    }

    /**
     * retrieved nearby updates
     * @param macAddress
     * @param startTime
     * @param endTime
     * @param locationId
     * @return arraylist of nearby updates
     */
    public static ArrayList<LocationUpdate> retrieveNearbyUpdates(String macAddress, String startTime, String endTime, int locationId) {
        ArrayList<LocationUpdate> nearbyUpdates = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select min(time_stamp), max(time_stamp), mac "
                    + "from location "
                    + "where time_stamp >= ? "
                    + "and time_stamp < ? "
                    + "and location_id = ? "
                    + "and mac != ? "
                    + "group by mac;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, startTime);
            stmt.setString(2, endTime);
            stmt.setInt(3, locationId);
            stmt.setString(4, macAddress);
            results = stmt.executeQuery();
            while (results.next()) {
                String start = results.getString(1);
                String end = results.getString(2);
                String mac = results.getString(3);
                nearbyUpdates.add(new LocationUpdate(start, mac, locationId));
                nearbyUpdates.add(new LocationUpdate(end, mac, locationId));
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
        return nearbyUpdates;
    }

    /**
     *insert the path in database
     * @param path
     * @throws SQLException
     */
    public void insert(String path) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("load data local infile ? into table location fields terminated by ',' lines terminated by '\\n'");
            stmt.setString(1, path);
            stmt.execute();
            System.out.println("location.csv to sql successful");
            System.out.println("Inside location update dao 107, inserting");
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
     * @param timeStamp
     * @param macAddress
     * @return
     * @throws SQLException
     */
    public boolean deleteDuplicates(String timeStamp, String macAddress) throws SQLException {
        boolean deleted = false;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("DELETE FROM " + TBLNAME + " WHERE time_stamp=? AND mac=?");
            stmt.setString(1, timeStamp);
            stmt.setString(2, macAddress);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                deleted = true;
            }
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
        return deleted;
    }

    /**
     *
     * @param timeStamp
     * @param macAddress
     * @param location
     * @return
     * @throws SQLException
     */
    public boolean TryUpdate(String timeStamp, String macAddress, int location) throws SQLException {
        boolean updated = false;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("UPDATE " + TBLNAME + " SET time_stamp=? AND mac=? AND location_id=? WHERE time_stamp=? AND mac=? LIMIT 1");
            stmt.setString(1, timeStamp);
            stmt.setString(2, macAddress);
            stmt.setInt(3, location);
            stmt.setString(4, timeStamp);
            stmt.setString(5, macAddress);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                updated = true;
            }
            System.out.println("update executed");
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
        return updated;
    }
    
    /**
     *
     * @param date
     * @return
     */
    public static ArrayList<LocationUpdate> retrieveLocationsInWindow(String date) {
        ArrayList<LocationUpdate> locations = new ArrayList();
        
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "SELECT * FROM location WHERE time_stamp >= DATE_ADD(?, INTERVAL -15 MINUTE) AND time_stamp <= ? ORDER BY time_stamp;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            stmt.setString(2, date);

            results = stmt.executeQuery();
            while (results.next()) {
                String time_stamp = results.getString(1);
                String mac = results.getString(2);
                int locationid = results.getInt(3);
                LocationUpdate location = new LocationUpdate(time_stamp, mac, locationid);
                locations.add(location);
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

        return locations;
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
}
