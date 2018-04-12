/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Json;

import DAO.LocationUpdateDAO;
import DAO.UserDAO;
import Entity.CoLocation;
import Entity.LocationUpdate;
import Entity.User;
import Utilities.DAOUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import servlet.TopKCompServlet;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "TopKCompanionsJsonServlet", urlPatterns = {"/json/top-k-companions"})
public class TopKCompJsonServlet extends HttpServlet {

    /**
     *display the output in json
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //get the parameter require for output
        String datetime = request.getParameter("datetime");
        String macAddress = request.getParameter("mac-address");
        int k = Integer.parseInt(request.getParameter("k"));
        JsonObject jsonOutput = new JsonObject();
        JsonArray errors = new JsonArray();

        String token = request.getParameter("token");
        String sharedSecret = "G7T4SHAREDSECRET";
        PrintWriter out = response.getWriter();

        JsonObject obj = new JsonObject();
        JsonArray topKCompanions = new JsonArray();

        //check for valid token
        try {
            String result = JWTUtility.verify(token, sharedSecret);
            if (result == null) {
                errors.add("Invalid token");
                out.println(jsonOutput);
                return;
            }
        } catch (Exception e) {
            errors.add(e.getMessage());
            errors.add("Invalid token");
            out.println(jsonOutput);
            return;
        }

        //check for mac address
        //get all the person updates in that time window within 15 mins
        ArrayList<LocationUpdate> updates = LocationUpdateDAO.retrieveUserUpdatesInWindow(macAddress, datetime);
        String windowEnd = DAOUtility.formatDatetime(datetime);

        TreeMap<String, Integer> trajMap = generateUserTrajectory(updates);
        // Bug: What if trajMap only one?
        if (!trajMap.isEmpty()) {
            Iterator<String> iterKeys = trajMap.keySet().iterator();
            String startTime;
            String endTime = iterKeys.next();
            HashMap<String, CoLocation> coLocMap = new HashMap<>();
            int locId;
            ArrayList<LocationUpdate> nearbyUpdates;

            if (trajMap.size() == 1) {
                startTime = endTime;
                endTime = updates.get(updates.size() - 1).getTimeStamp();
                locId = trajMap.get(startTime);
                // Everyone who updated at the same place while user was there
                nearbyUpdates = LocationUpdateDAO.retrieveNearbyUpdates(macAddress, startTime, endTime, locId);

                coLocMap = generateCoLocations(coLocMap, macAddress, nearbyUpdates, locId, windowEnd);
            } else {
                // For each change of location, see who is nearby
                while (iterKeys.hasNext()) {
                    startTime = endTime;
                    locId = trajMap.get(startTime);
                    endTime = iterKeys.next();
                    // Everyone who updated at the same place while user was there
                    nearbyUpdates = LocationUpdateDAO.retrieveNearbyUpdates(macAddress, startTime, endTime, locId);

                    coLocMap = generateCoLocations(coLocMap, macAddress, nearbyUpdates, locId, windowEnd);
                }
            }

            TreeMap<Long, ArrayList<User>> rankCompanions = getUserCompanions(groupByDuration(coLocMap));
            Iterator<Long> iterKeyRankMap = rankCompanions.descendingKeySet().iterator();
            int count = 1;

            while (iterKeyRankMap.hasNext() && count != k) {
                long coTime = iterKeyRankMap.next();
                ArrayList<User> companions = rankCompanions.get(coTime);
                int numCompanions = companions.size();
                for (int i = 0; i < numCompanions; i++) {
                    User c = companions.get(i);
                    long time = (coTime / 1000);
                    String email = c.getEmail();
                    obj.addProperty("rank", i);

                    if (email != null) {
                        obj.addProperty("companion", email);
                    } else {
                        obj.addProperty("companion", "");
                    }

                    obj.addProperty("mac-address:", c.getMacAddress());
                    obj.addProperty("time-together", time);
                    topKCompanions.add(obj);

                }

            }
        } else { //if the list is empty
            errors.add("none, User has no companions to display");
        }
        jsonOutput.addProperty("status", "success");
        jsonOutput.add("results", topKCompanions);
        out.println(jsonOutput);
        return;
    }

    //Track where and when the user went during the window

  /**
     *generate user trajectory 
     * @param updates
     * @return a list of user with new location details
     */
    protected TreeMap<String, Integer> generateUserTrajectory(ArrayList<LocationUpdate> updates) {
        // Timestamp : LocationId
        TreeMap<String, Integer> map = new TreeMap<>();
        for (LocationUpdate update : updates) {
            // if next locationid in updates is different from current locationid in map, new insert
            String timeStamp = update.getTimeStamp();
            int locationId = update.getLocationId();
            if (map.isEmpty()) {
                map.put(timeStamp, locationId);
            } else {
                String lastKey = map.lastKey();
                int lastLocId = map.get(lastKey);
                if (lastLocId != locationId) {
                    map.put(timeStamp, locationId);
                }
            }
        }
        return map;
    }

    /**
     *group the user by duration
     * @param map
     * @return group by duration
     */
    protected HashMap<String, Long> groupByDuration(HashMap<String, CoLocation> map) {
        HashMap<String, Long> result = new HashMap<>();
        Iterator<String> iterKeys = map.keySet().iterator();
        while (iterKeys.hasNext()) {
            String macAndLocation = iterKeys.next();
            String mac = macAndLocation.substring(0, 40);
            CoLocation co = map.get(macAndLocation);
            long duration = co.getDuration();
            if (result.containsKey(mac)) {
                duration += result.get(mac);
            }
            result.put(mac, duration);
        }
        return result;
    }

    /**
     *get user companions
     * @param map
     * @return the time and people that spent with user
     */
    protected TreeMap<Long, ArrayList<User>> getUserCompanions(HashMap<String, Long> map) {
        // Key: Time spent with user
        // Value: People
        TreeMap<Long, ArrayList<User>> rankMap = new TreeMap<>();
        Iterator<String> iterKeys = map.keySet().iterator();
        while (iterKeys.hasNext()) {
            String mac = iterKeys.next();
            long duration = map.get(mac);
            ArrayList<User> companions = new ArrayList<>();
            if (rankMap.containsKey(duration)) {
                companions = rankMap.get(duration);
            }
            User companion = UserDAO.getUserWithMacAddress(mac);
            if (companion == null) {
                companion = new User(mac);
            }
            companions.add(companion);
            rankMap.put(duration, companions);
        }
        return rankMap;
    }

    /**
     *generate companions locations
     * @param map
     * @param macAddress
     * @param nearbyUpdates
     * @param locId
     * @param windowEnd
     * throws ParseException
     * @return generated CoLocations
     */
    protected HashMap<String, CoLocation> generateCoLocations(HashMap<String, CoLocation> map, String macAddress, ArrayList<LocationUpdate> nearbyUpdates, int locId, String windowEnd) {
        // For each nearby update, get their trajectory to see how long they were there
        int nearbyUpdatesSize = nearbyUpdates.size();
        int i = 0;
        while (i < nearbyUpdatesSize - 1) {
            LocationUpdate update1 = nearbyUpdates.get(i);
            LocationUpdate update2 = nearbyUpdates.get(i + 1);
            String compMac = update1.getMacAddress();
            String lastUpdateTimeStr = update2.getTimeStamp();
            String firstUpdateTimeStr = update1.getTimeStamp();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date lastUpdateTime;
            Date firstUpdateTime;
            /*If there is new update at new location after last update at current location within window,
                duration = new update at new location - first update
            else
                if time from last update to window end < 5min
                    duration = window end - first update
                else
                    duration = last update - first update + 5min*/
            try {
                lastUpdateTime = dateFormatter.parse(lastUpdateTimeStr);
                firstUpdateTime = dateFormatter.parse(firstUpdateTimeStr);
                long duration = lastUpdateTime.getTime() - firstUpdateTime.getTime();
                ArrayList<LocationUpdate> excludedUpdates = LocationUpdateDAO.retrieveUserUpdatesInWindow(compMac, lastUpdateTimeStr, windowEnd);
                if (excludedUpdates.size() > 0) {
                    String newLocTimeStr = excludedUpdates.get(0).getTimeStamp();
                    Date newLocTime = dateFormatter.parse(newLocTimeStr);
                    duration += newLocTime.getTime() - lastUpdateTime.getTime();
                } else {
                    Date windowEndTime = dateFormatter.parse(windowEnd);
                    long timeBetweenLastAndWindow = windowEndTime.getTime() - lastUpdateTime.getTime();
                    if (timeBetweenLastAndWindow < 5) {
                        duration += timeBetweenLastAndWindow;
                    } else {
                        duration += 5 * 60 * 1000;
                    }
                }
                // What if the companion moves with the user?
                CoLocation co = new CoLocation(macAddress, compMac, locId, duration);
                if (map.containsKey(compMac + locId)) {
                    co = map.get(compMac + locId);
                    long old = co.getDuration();
                    co.setDuration(old + duration);
                }
                map.put(compMac + locId, co);
            } catch (ParseException ex) {
                Logger.getLogger(TopKCompServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            i += 2;
        }
        return map;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     *
     * @return
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
