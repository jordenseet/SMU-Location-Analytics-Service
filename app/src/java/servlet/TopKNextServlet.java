/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DAO.SemanticPlaceDAO;
import Utilities.DAOUtility;
import java.io.IOException;
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

/**
 *
 * @author G7T4
 */
@WebServlet(name = "TopKNextServlet", urlPatterns = {"/TopKNextServlet"})
public class TopKNextServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int k = Integer.parseInt(request.getParameter("k"));
        String semanticPlaceName = request.getParameter("origin");
        String datetime = request.getParameter("date");
        if (semanticPlaceName != null && datetime != null) {
            datetime = DAOUtility.formatDatetime(datetime);
            System.out.println("DATETIMEEEEEEEEE:" + datetime);
            SemanticPlaceDAO semanticPlaceDAO = new SemanticPlaceDAO();
            ArrayList<String> macs = semanticPlaceDAO.retrieveMacsAtSemanticPlace(semanticPlaceName, datetime);
            for (String mac: macs){
                System.out.println(mac);
            }
            int totalPplQueried = macs.size();
            int numPplWhoLeft = 0;
    
            if (totalPplQueried > 0) {
                HashMap<String, Integer> visitMap = new HashMap<>();
                for (String mac : macs) {
                    // For each macaddress, get their visits in next 15 minute window.
                    ArrayList<String[]> visits = semanticPlaceDAO.retrieveUserVisitsToSemanticPlaces(mac, datetime);
                    // Clean up in between updates when user staying at same location
                    TreeMap<Date, String> trajMap = generateUserTrajectory(visits);
                    // Store last eligible visit of at least 5 minutes for that mac address
                    String nextPlace = null;
                    Iterator<Date> iterKeys = trajMap.keySet().iterator();
                    Date nextTimestamp = iterKeys.next();
                    while (iterKeys.hasNext()) {
                        // Keep updating place as long as at least 5 minutes
                        Date timestamp = nextTimestamp;
                        String place = trajMap.get(timestamp);
                        nextTimestamp = iterKeys.next();
                        long duration = nextTimestamp.getTime() - timestamp.getTime();
                        if (duration >= 5 * 60 * 1000) {
                            nextPlace = place;
                        }
                    }
                    if (nextPlace != null) {
                        // Keep count of number of people at next place
                        int count = 0;
                        if (visitMap.containsKey(nextPlace)) {
                            count = visitMap.get(nextPlace);
                        }
                        visitMap.put(nextPlace, ++count);
                    } else {
                        numPplWhoLeft++;
                    }
                }
                TreeMap<Integer, ArrayList<String>> rankMap = rankNextPlaces(visitMap);
                request.setAttribute("numPplWhoLeft", numPplWhoLeft);
                request.setAttribute("rankMap", rankMap);
            } else {
                request.setAttribute("error", "There was no one at " + semanticPlaceName + ".");
            }
    
            request.setAttribute("k", k);
            request.setAttribute("origin", semanticPlaceName);
            request.setAttribute("totalPplQueried", totalPplQueried);
            request.getRequestDispatcher("/topknext.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Invalid Top-k Next input detected, try again");
            request.getRequestDispatcher("/basicLocationReport.jsp").forward(request, response);
        }
    }

     /**
     *generate user trajectory 
     * @param visits
     * @return a list of user with new location details
     */
    protected TreeMap<Date, String> generateUserTrajectory(ArrayList<String[]> visits) {
        // Similar to generateUserTrajectory() method in TopKCompServlet but specific to Top K Next.
        TreeMap<Date, String> map = new TreeMap<>();
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Key: Timestamp, Value: Semantic Place
            for (String[] visit : visits) {
                Date timestamp = dateFormatter.parse(visit[1]);
                String place = visit[2];
                if (map.isEmpty()) {
                    map.put(timestamp, place);
                } else {
                    Date lastKey = map.lastKey();
                    String lastPlace = map.get(lastKey);
                    if (!lastPlace.equals(place)) {
                        map.put(timestamp, place);
                    }
                }
            }

            // Last insert is to keep track of final update in window
            String[] lastVisit = visits.get(visits.size() - 1);
            Date lastTimestamp = dateFormatter.parse(lastVisit[1]);
            if (!map.containsKey(lastTimestamp)) {
                map.put(lastTimestamp, lastVisit[2]);
            }
        } catch (ParseException ex) {
            Logger.getLogger(TopKNextServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    /**
     *ranking by the amount of number of people going to the next places
     * @param map
     * @return a treemap of rank in next places
     */
    protected TreeMap<Integer, ArrayList<String>> rankNextPlaces(HashMap<String, Integer> map) {
        TreeMap<Integer, ArrayList<String>> rankMap = new TreeMap<>();
        Iterator<String> iterKeys = map.keySet().iterator();
        while (iterKeys.hasNext()) {
            String nextPlace = iterKeys.next();
            int num = map.get(nextPlace);
            ArrayList<String> nextPlaceList = new ArrayList<>();
            if (rankMap.containsKey(num)) {
                nextPlaceList = rankMap.get(num);
            }
            nextPlaceList.add(nextPlace);
            rankMap.put(num, nextPlaceList);
        }
        return rankMap;
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
