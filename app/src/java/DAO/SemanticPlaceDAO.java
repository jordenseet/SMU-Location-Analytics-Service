package DAO;


import Entity.SemanticPlace;
import Utilities.ConnectionManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;

/**
 *
 * @author G7T4
 */
public class SemanticPlaceDAO {
    private static final String TBLNAME = "semanticplace";
    private static Connection conn;
    private static PreparedStatement stmt;
    private static ResultSet results;

    /**
     *
     * @param id
     * @return semantic place by id
     */
    public static SemanticPlace getSemanticPlace(String id) {
        try {
           Class.forName("com.mysql.jdbc.Driver").newInstance();
             conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME + " where location_id = ?");
            stmt.setString(1, id);

            results = stmt.executeQuery();
            while (results.next()) {
                int locationId = results.getInt(1);
                String name = results.getString(2);
                return new SemanticPlace(locationId, name);
            }
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return null;
    }
    
    /**
     *retrieve all semantic places
     * @return list of semantic places
     */
    public static ArrayList<String> retrieveAllSemanticPlaces() {
        ArrayList<String> spList = new ArrayList<String>();

        try {
           Class.forName("com.mysql.jdbc.Driver").newInstance();
             conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select distinct name from " + TBLNAME);

            results = stmt.executeQuery();
            while (results.next()) {
                String spName = results.getString(1);
                spList.add(spName);
            }
            
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return spList;
    }

    /**
     *delete table
     * @throws SQLException
     */
    public void deleteTable() throws SQLException{
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
     *insert path in database
     * @param path
     * @throws SQLException
     */
    public void insert(String path) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
             conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("load data local infile ? into table semanticplace fields terminated by ',' lines terminated by '\\n'");
            stmt.setString(1, path);
            stmt.execute();
            System.out.println("location-lookup.csv to sql successful");
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
                conn.close();
            } 
            catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }
    
    /**
     * retrieve all validated locations id
     * @return the validated locations ID
     */
    public static ArrayList<Integer> retrieveAllValidatedLocationIds() {
        ArrayList<Integer> allLocationIds = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
             conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from " + TBLNAME);
            results = stmt.executeQuery();
            while (results.next()) {
                String locationId = results.getString(1);//get all the columns needed
                allLocationIds.add(Integer.parseInt(locationId));
            }

        } catch (Exception e) {
            e.printStackTrace();//print error message
        }try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return allLocationIds;
    }
    
    /**
     *retrieve mac address at semantic places
     * @param place
     * @param datetime
     * @return list of mac address at that semantic place
     */
    public static ArrayList<String> retrieveMacsAtSemanticPlace(String place, String datetime) {
        ArrayList<String> macList = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select max(time_stamp) as latest, mac "
                    + "from location "
                    + "where location_id in ( "
                    + "	select location_id "
                    + "	from " + TBLNAME
                    + "	where name=? "
                    + ") "
                    + "and time_stamp >= ? - interval 15 minute "
                    + "and time_stamp < ? "
                    + "group by mac "
                    + "order by latest;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, place);
            stmt.setString(2, datetime);
            stmt.setString(3, datetime);
            results = stmt.executeQuery();
            while (results.next()) {
                macList.add(results.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return macList;
    }
    
    /**
     *retrieve user visits to the semantic places
     * @param mac
     * @param datetime
     * @return retrieve users visits to semantic places
     */
    public static ArrayList<String[]> retrieveUserVisitsToSemanticPlaces(String mac, String datetime) {
        ArrayList<String[]> visitList = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select mac, time_stamp, name "
                    + "from " + TBLNAME + " s, ( "
                    + "select * "
                    + "from location "
                    + "where mac = ? "
                    + "and time_stamp >= ? "
                    + "and time_stamp < ? + interval 15 minute "
                    + ") as A "
                    + "where s.location_id = A.location_id "
                    + "order by time_stamp;";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, mac);
            stmt.setString(2, datetime);
            stmt.setString(3, datetime);
            results = stmt.executeQuery();
            while (results.next()) {
                String[] visit = new String[3];
                visit[0] = results.getString(1); // mac address
                visit[1] = results.getString(2); // timestamp
                visit[2] = results.getString(3); // semantic place
                visitList.add(visit);
            }
        } catch (Exception e) {
            e.printStackTrace();//print error message
        }try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return visitList;
    }
}