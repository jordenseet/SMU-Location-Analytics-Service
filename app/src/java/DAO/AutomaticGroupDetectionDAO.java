package DAO;

import Entity.LocationUpdate;
import Entity.*;
import Utilities.ConnectionManager;
//import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author G7T4
 */
public class AutomaticGroupDetectionDAO {

    private static final String TBLNAME = "location";
    private static Connection conn;
    private static PreparedStatement stmt;
    private static ResultSet results;

    /**
     *retrieve user updates in that window
     * @param macAddress
     * @param start
     * @param end
     * @return users in the window frame without duplicates
     */
    public static LinkedHashMap<Integer, ArrayList<Date>> retrieveUserUpdatesInWindowForAGD(String macAddress, String start, String end) {
        LinkedHashMap<Integer, ArrayList<Date>> updatesWithoutDuplicates = new LinkedHashMap<>();
        //ArrayList<LocationUpdate> nonDuplicateLocations = new ArrayList<>();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<LocationUpdate> updates = new ArrayList<>();
        // Start and end datetimes are already in the right format when this method is called.
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = ConnectionManager.getConnection();
            String query = "select time_stamp, location_id\n"
                    + "from location"
                    + "where mac = '" + '?' + "'"
                    + "and time_stamp > '" + '?' + "'"
                    + "and time_stamp < '" + '?' + "'"
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
        // actually from within the hashmap can get the amount spent already sia, so this method is underutilised
        // I wanna store the first and last timestamp to get duration and to remove duplicates
        try {
            for (LocationUpdate locationUpdate : updates) {
                int locationId = locationUpdate.getLocationId();
                String timeStamp = locationUpdate.getTimeStamp();
                Date date = dateFormatter.parse(timeStamp);
                ArrayList<Date> dateList = new ArrayList<>();

                if (!updatesWithoutDuplicates.containsKey(locationId)) {
                    dateList.add(date);
                    //nonDuplicateLocations.add(locationUpdate);
                    updatesWithoutDuplicates.put(locationId, dateList);

                } else {
                    ArrayList<Date> retrievedDateList = updatesWithoutDuplicates.get(locationId);
                    if (retrievedDateList.size() == 1) {
                        retrievedDateList.add(date);
                        updatesWithoutDuplicates.put(locationId, retrievedDateList);

                    } else if (retrievedDateList.size() == 2) {
                        retrievedDateList.remove(1);
                        retrievedDateList.add(date);
                        updatesWithoutDuplicates.put(locationId, retrievedDateList);

                    }
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        //May i suggest that we utilise the hashmap to calculate the time spent in each location?
        //return nonDuplicateLocations;

        return updatesWithoutDuplicates;
    }

    /**
     *remove the user that stay in the window without 12 mins
     * @param userVisits
     * @param start
     * @param end
     * @return users that has 12 mins
     */
    public static HashMap<User, LinkedHashMap<Integer, ArrayList<Date>>> removeUsersWithout12MinWindows(HashMap<User, LinkedHashMap<Integer, ArrayList<Date>>> userVisits, String start, String end) {
        HashMap<User, LinkedHashMap<Integer, ArrayList<Date>>> allUsers = userVisits;
        HashMap<User, LinkedHashMap<Integer, ArrayList<Date>>> correctUsers = new HashMap<>();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date windowStart = dateFormatter.parse(start);
            Date windowEnd = dateFormatter.parse(end);
            for (User user : allUsers.keySet()) {
                LinkedHashMap<Integer, ArrayList<Date>> value = allUsers.get(user);
                int numberOfLocations = value.keySet().size();
                // If numberOfLocations is 1, just check for start and end, if there is one date then it won't be in 12 min
                //This is case 1-------------------------------------------------------------------------------------------

                if (numberOfLocations == 1) {
                    // User stayed at one location ID throughout
                    for (int locationId : value.keySet()) {
                        ArrayList<Date> dateList = value.get(locationId);

                        if (dateList.size() == 2) {
                            //If datelist is size 1, it will never hit 12 minutes

                            Date firstTime = dateList.get(0);

                            Date secondTime = dateList.get(1);

                            long checkFor5Minutes = windowEnd.getTime() - secondTime.getTime();

                            if (checkFor5Minutes > 300000) {
                                secondTime = new Date(300000 + (secondTime.getTime()));
                            }
                            if (checkFor5Minutes < 300000) {
                                secondTime = windowEnd;
                            }
                            long checkFor12Minutes = secondTime.getTime() - firstTime.getTime();
                            //User stayed at location 1 for >= 12 minutes because 720000ms = 12 mins
                            if (checkFor12Minutes >= 720000) {

                                ArrayList<Date> correctDateList = new ArrayList<>();
                                correctDateList.add(firstTime);
                                correctDateList.add(secondTime);
                                LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                innerMap.put(locationId, correctDateList);
                                correctUsers.put(user, innerMap);
                                System.out.println("Case 1 success");
                            }
                        }
                    }
                }
                if (numberOfLocations == 2) {
                    //This is case 2-------------------------------------------------------------------------------------------
                    //User went to two locations 
                    int firstLocationId = 0;
                    int secondLocationId = 0;
                    int counter = 0;
                    for (int locationId : value.keySet()) {
                        if (counter == 0) {
                            System.out.println("AGD DAO line 148");
                            firstLocationId = locationId;
                            counter++;
                        } else {
                            System.out.println("AGD DAO line 152");
                            secondLocationId = locationId;
                        }
                    }
                    System.out.println("First Location Id " + firstLocationId);
                    System.out.println("Second Location Id " + secondLocationId);
                    try {
                        ArrayList<Date> firstLocationDateList = value.get(firstLocationId);
                        System.out.println("firstLocationDateListSize " + firstLocationDateList.size());
                        ArrayList<Date> secondLocationDateList = value.get(secondLocationId);
                        System.out.println("secondLocationDateListSize " + secondLocationDateList.size());
                        if (firstLocationDateList.size() == 2 && secondLocationDateList.size() == 2) {
                            System.out.println("Inside same size arraylist loop");
                            Date firstLocationDate1 = firstLocationDateList.get(0);
                            Date firstLocationDate2 = firstLocationDateList.get(1);
                            Date secondLocationDate1 = secondLocationDateList.get(0);
                            Date secondLocationDate2 = secondLocationDateList.get(1);
                            System.out.println(secondLocationDate1.getTime() - firstLocationDate2.getTime());
                            if (secondLocationDate1.getTime() > firstLocationDate2.getTime()) {
                                //User went to both locations sequentially and both locations have at least two updates
                                //This is case 2A-----------------------------------------------------------------------------------------
                                System.out.println("First if loop when comparing time");
                                
                                firstLocationDate2 = secondLocationDate1;

                                long difference = windowEnd.getTime() - secondLocationDate2.getTime();
                                System.out.println(difference + " check for time difference");
                                if (difference > 300000) {
                                    secondLocationDate2 = new Date(secondLocationDate2.getTime() + 300000);
                                } else {
                                    secondLocationDate2 = windowEnd;
                                }
                                if ((firstLocationDate2.getTime() - firstLocationDate1.getTime()) >= 720000) {
                                    System.out.println("Case 2a success " + (firstLocationDate2.getTime() - firstLocationDate1.getTime()));
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(firstLocationDate1);
                                    correctDateList.add(firstLocationDate2);
                                    LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(firstLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                }
                                if ((secondLocationDate2.getTime() - secondLocationDate1.getTime()) >= 720000) {
                                    System.out.println("Case 2a success " + (secondLocationDate2.getTime() - secondLocationDate1.getTime()));
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(secondLocationDate1);
                                    correctDateList.add(secondLocationDate2);
                                    LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(secondLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                }
                                //Could have used an If but this else if explains things more clearly
                            } else if (firstLocationDate2.getTime() > secondLocationDate2.getTime()) {
                                // Case where secondLocation1 happens before firstlocation2, meaning that user went from location A to B and back to A
                                //This is case 2B-----------------------------------------------------------------------------------------------
                                Date firstLocationNewDate2 = secondLocationDate1;
                                //3 represents the 2nd time user went back to the same location
                                Date firstLocationNewDate3 = secondLocationDate2;
                                Date firstLocationNewDate4 = firstLocationDate2;
                                long difference = windowEnd.getTime() - firstLocationNewDate4.getTime();
                                if (difference > 30000) {
                                    firstLocationNewDate4 = new Date(firstLocationNewDate4.getTime() + difference);
                                } else {
                                    firstLocationNewDate4 = windowEnd;
                                }

                                long totalDuration = firstLocationNewDate4.getTime() - firstLocationDate1.getTime();
                                long innerDuration = firstLocationNewDate3.getTime() - firstLocationNewDate2.getTime();
                                if ((totalDuration - innerDuration) >= 720000) {
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(firstLocationDate1);
                                    correctDateList.add(firstLocationNewDate2);
                                    correctDateList.add(firstLocationNewDate3);
                                    correctDateList.add(firstLocationNewDate4);
                                    LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(firstLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                    System.out.println("Case 2b success " + (totalDuration - innerDuration));
                                }
                                if ((secondLocationDate2.getTime()-secondLocationDate1.getTime())>=720000){
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(secondLocationDate1);
                                    correctDateList.add(secondLocationDate2);
       
                                   LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(secondLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                    System.out.println("Case 2b success " + (secondLocationDate2.getTime()-secondLocationDate1.getTime()));
                                }
                            }
                        } else if (firstLocationDateList.size() == 2 && secondLocationDateList.size() == 1) {
                            //This is case 2 where the number of updates for both locations are not 2 each--------------------------
                            Date firstLocationDate1 = firstLocationDateList.get(0);
                            Date firstLocationDate2 = firstLocationDateList.get(1);
                            Date secondLocationDate1 = secondLocationDateList.get(0);
                            if (secondLocationDate1.getTime() > firstLocationDate2.getTime()) {
                                //This is case 2C---------------------------------------------------------------------------------------------
                                //When location A has at least 2 updates and location B has only 1 and it is still sequential
                                firstLocationDate2 = secondLocationDate1;
                                //No need to check for windowEnd cos the second location only appears once. It will never hit 12 mins
                                long duration = firstLocationDate2.getTime() - firstLocationDate1.getTime();
                                if (duration >= 720000) {
                                    System.out.println("Case 2C success " + (firstLocationDate2.getTime() - firstLocationDate1.getTime()));
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(firstLocationDate1);
                                    correctDateList.add(firstLocationDate2);
                                    LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(firstLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                }
                            } else {
                                //Case where secondLocation1 happens before firstlocation2, meaning that user went back and forth from location1 to location2 and location1 again
                                //This is case 2D
                                Date firstLocationNewDate2 = secondLocationDate1;
                                Date firstLocationNewDate3 = firstLocationDate2;
                                Date firstLocationNewDate4 = firstLocationDate2;
                                long difference = windowEnd.getTime() - firstLocationNewDate4.getTime();
                                if (difference > 30000) {
                                    firstLocationNewDate4 = new Date(firstLocationNewDate4.getTime() + 30000);
                                } else {
                                    firstLocationNewDate4 = windowEnd;
                                }
                                long duration1 = firstLocationNewDate2.getTime() - firstLocationDate1.getTime();
                                long duration2 = firstLocationNewDate4.getTime() - firstLocationNewDate3.getTime();
                                if ((duration1 + duration2) >= 720000) {
                                    System.out.println("Case 2D success " + (duration1 + duration2));
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(firstLocationDate1);
                                    correctDateList.add(firstLocationNewDate2);
                                    correctDateList.add(firstLocationNewDate3);
                                    correctDateList.add(firstLocationNewDate4);
                                    LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(firstLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                }
                                //In this scenario, Location A sandwiches Location B but Location B is larger than 12 mins
                                if ((firstLocationNewDate3.getTime()-firstLocationNewDate2.getTime())>=720000){
                                 System.out.println("Case 2D success " + (firstLocationNewDate3.getTime()-firstLocationNewDate2.getTime()));
                                    ArrayList<Date> correctDateList = new ArrayList<>();
                                    correctDateList.add(firstLocationNewDate2);
                                    correctDateList.add(firstLocationNewDate3);
      
                                    LinkedHashMap<Integer, ArrayList<Date>> innerMap = new LinkedHashMap<>();
                                    innerMap.put(firstLocationId, correctDateList);
                                    correctUsers.put(user, innerMap);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return correctUsers;
    }

    /**
     *update tracker for AGD
     * @param userVisits
     * throws Exception
     * @return the timestamp that equals to 12 mins
     */
    public static LinkedHashMap<ArrayList<Date>, Integer> updateTrackerForAGD(ArrayList<LocationUpdate> userVisits) {
        LinkedHashMap<ArrayList<Date>, Integer> timeStampEqual12Minutes = new LinkedHashMap<ArrayList<Date>, Integer>();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int endPlace = 0;
        int startPlace = 0;
        try {
            for (int j = 0; j < userVisits.size(); j++) {
                String startTimeStamp = userVisits.get(j).getTimeStamp();
                Date startDateTime = dateFormatter.parse(startTimeStamp);
                String startDateTimeString = dateFormatter.format(startDateTime);
                startPlace = userVisits.get(j).getLocationId();

                for (int k = userVisits.size() - 1; k >= 0; k--) {

                    /*String endTimeStamp = userVisits.get(k).getTimeStamp();
                    Date endDateTime = dateFormatter.parse(endTimeStamp);
                    int endPlace = userVisits.get(k).getLocationId();
                    if (endDateTime.getTime() - startDateTime.getTime() >= 12 * 60 * 1000) {
                        ArrayList<Date> toAdd = new ArrayList<Date>();
                        toAdd.add(endDateTime);
                        toAdd.add(startDateTime);
                        ArrayList<Integer> trackLocations = new ArrayList<Integer>();
                        trackLocations.add(endPlace);
                        trackLocations.add(startPlace);
                        timeStampEqual12Minutes.put(toAdd,trackLocations);
                    } 
                    else{
                        break;
                    }*/
                    String endTimeStamp = userVisits.get(k).getTimeStamp();
                    Date endDateTime = dateFormatter.parse(endTimeStamp);
                    endPlace = userVisits.get(k).getLocationId();
                    boolean correctDuration = (endDateTime.getTime() - startDateTime.getTime()) >= 12 * 60 * 1000;
                    if (endPlace == startPlace && (correctDuration)) {
                        ArrayList<Date> toAdd = new ArrayList<Date>();
                        toAdd.add(endDateTime);
                        toAdd.add(startDateTime);
                        timeStampEqual12Minutes.put(toAdd, startPlace);
                    }
                }
            }
            return timeStampEqual12Minutes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
