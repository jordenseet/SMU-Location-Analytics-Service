/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DAO.UserDAO;
import Entity.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import DAO.LocationUpdateDAO;
import Entity.LocationUpdate;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author G7T4 BronKerboschClique finder is a utility that we got online.
 * Credit to Ewgenij Proschak and Contributors.
 */
@WebServlet(name = "/GroupDetectionServlet", urlPatterns = {"/GroupDetectionServlet"})
public class GroupDetectionServlet extends HttpServlet {

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

        String dateStr = request.getParameter("date");
        System.out.println(dateStr);
        Date date = null;
        try {
            date = new Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr).getTime());//convert to Date object for easy manipulation
        }
        catch (ParseException pe) {
            try {
                dateStr += ":00";
                date = new Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr).getTime());//convert to Date object for easy manipulation

            } catch (Exception error) {
                System.out.println("gg.com");
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Invalid date");
            request.getRequestDispatcher("/automaticGroupDetection.jsp").forward(request, response);
        }

        ArrayList<User> users = UserDAO.retrievePersonsInWindow(dateStr);
        request.setAttribute("total", users.size() + "");
        ArrayList<LocationUpdate> locations = LocationUpdateDAO.retrieveLocationsInWindow(dateStr);

        for (User user : users) {
            List<LocationUpdate> userTimeline = new ArrayList<LocationUpdate>(); //timeline consists of where the user is at what time
            for (LocationUpdate locationUpdate : locations) {
                if (user.getMacAddress().equals(locationUpdate.getMacAddress())) {
                    userTimeline.add(locationUpdate);
                }
            }
            user.setTimeline(userTimeline);
        }

        HashMap<User, List<User>> grouping = new HashMap();//parsing one user against a list of all users in the place at that time
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            List<User> potentialGroupMembers = new ArrayList();//getting list of potential group members

            for (int j = users.size() - 1; j >= i; j--) {
                User potentialMember = users.get(j);//iterating through list of potential group members

                long duration = 0;//how long the user stayed in the place
                for (int k = 0; k <= Math.min(user.getTimeline().size() - 1, potentialMember.getTimeline().size() - 1); k++) { //taking only the smaller window where both are present
                    MutablePair<LocationUpdate, Long> userData = user.getTimeline().get(k); //use pair class as HashMap has no equals method

                    LocationUpdate userLocation = userData.getLeft();
                    ArrayList<MutablePair<LocationUpdate, Long>> potentialTimelines = potentialMember.getTimeline();
                    ArrayList<MutablePair<LocationUpdate, Long>> commonTimelines = new ArrayList<>();

                    for (MutablePair<LocationUpdate, Long> potentialTimeline : potentialTimelines) { //iterate through potential member's timelines to find a match with the user's timeline
                        long lastUpdateSpan = potentialTimeline.getLeft().getDate().getTime() + potentialTimeline.getRight();
                        if (date.getTime() - lastUpdateSpan < 0) {
                            long timeDiff = date.getTime() - potentialTimeline.getLeft().getDate().getTime();
                            potentialTimeline.setRight(timeDiff);
                        }
                        if (potentialTimeline.getLeft().getLocationId() == userLocation.getLocationId()) {
                            commonTimelines.add(potentialTimeline);
                            //potentialData = potentialTimeline; //if match is found, return the timeline
                        }
                    }
                    for (int m = 0; m < commonTimelines.size(); m++) {
                        MutablePair<LocationUpdate, Long> timeline = commonTimelines.get(m);
                        LocationUpdate potentialLocation = timeline.getLeft();

                        long userStart = userLocation.getDate().getTime();
                        long userEnd = userStart + userData.getRight();

                        long potentialStart = potentialLocation.getDate().getTime();
                        long potentialEnd = potentialStart + timeline.getRight();
                        if (userEnd >= potentialStart) { //if there is an overlap

                            long max = Math.max(userStart, potentialStart); //deriving the overlap duration
                            long min = Math.min(userEnd, potentialEnd);

                            if (min - max > 0) {
                                duration += (min - max);//remove negative values
                                System.out.println("duration now is " + duration);
                            }
                        }
                    }
                }
                if (duration >= (1000 * 60 * 12)) {//if duration together is longer than 12 minutes, set as a possible group pairing

                    potentialMember.setDuration(duration);
                    potentialGroupMembers.add(potentialMember);
                }
            }
            if (potentialGroupMembers.size() > 0) {
                grouping.put(user, potentialGroupMembers); // prepare to cycle through pairings to form groups
            }
        }
        Set<ArrayList<User>> refinedGroups = new HashSet<ArrayList<User>>();
        SimpleGraph<User, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class); //draw graph to find groupings

        for (Map.Entry<User, List<User>> pair : grouping.entrySet()) { //iterate through beach User : member pairings
            User user = pair.getKey();
            List<User> compList = pair.getValue();
            ArrayList<User> compUserList = new ArrayList<>();
            for (User comp : compList) {
                if (!comp.equals(user)) {
                    compUserList.add(comp); //add all members into a list
                }
            }
            graph.addVertex(user); // add initial vertex which is the user

            for (int i = 0; i < compUserList.size(); i++) {
                User nextUser = compUserList.get(i);
                graph.addVertex(nextUser); // add vertexes for group members

                if (!nextUser.equals(user)) {
                    graph.addEdge(user, nextUser); //draw the linkages
                }

            }

        }

        BronKerboschCliqueFinder<User, DefaultEdge> groupFormer = new BronKerboschCliqueFinder<>(graph);
        Collection<Set<User>> maxCliques = groupFormer.getAllMaximalCliques(); //form the big groups in a Collection of Set of users

        for (Set<User> bigGroups : maxCliques) { //iterating through each set of users, or each refined group

            if (bigGroups.size() >= 2) {
                ArrayList<User> group = new ArrayList<>();
                group.addAll(bigGroups);//converting set to arraylist
                Collections.sort(group, (u1, u2) -> u1.getMacAddress().compareTo(u2.getMacAddress())); //sorting by mac address
                refinedGroups.add(group); //add group to list of groups
            }
        }
        request.setAttribute("numGroups", refinedGroups.size() + "");
        request.setAttribute("refinedGroups", refinedGroups);
        request.getRequestDispatcher("/AGDOutPut.jsp").forward(request, response);
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
