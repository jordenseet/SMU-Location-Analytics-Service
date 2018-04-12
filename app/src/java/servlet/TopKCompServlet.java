package servlet;


import java.text.SimpleDateFormat;
import Utilities.DAOUtility;
import DAO.LocationUpdateDAO;
import DAO.UserDAO;
import Entity.LocationUpdate;
import Entity.User;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "TopKCompServlet", urlPatterns = {"/TopKCompServlet"})
public class TopKCompServlet extends HttpServlet {

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
        String datetime = DAOUtility.formatDatetime(request.getParameter("date"));
        String mac = request.getParameter("mac-address");
        int k = Integer.parseInt(request.getParameter("k"));
        request.setAttribute("k",k);
        TreeMap<Integer,HashMap<ArrayList<User>,Long>> rankMap = new TreeMap<>();
        
        String dateStr = request.getParameter("date");
        java.sql.Date date = null;
        try {
            date = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr).getTime());//convert to Date object for easy manipulation
        }
        catch (ParseException pe) {
            try {
                dateStr += ":00";
                date = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr).getTime());//convert to Date object for easy manipulation

            } catch (Exception error) {
                request.setAttribute("error","Invalid Top-k Companions input detected, try again");
                request.getRequestDispatcher("/basicLocationReport.jsp").forward(request, response);
            }
        } 

        if (datetime != null && mac != null) {
            
            UserDAO userDAO = new UserDAO();
            User theUser = userDAO.getUserWithMacAddress(mac);
            if (theUser == null){
                theUser = new User(mac,"nu","null","null",'M');
            }
            
            ArrayList<User> users = UserDAO.retrievePersonsInWindow(datetime);
            ArrayList<LocationUpdate> locations = LocationUpdateDAO.retrieveLocationsInWindow(datetime);

            for (User user : users) {
                List<LocationUpdate> userTimeline = new ArrayList<LocationUpdate>(); //timeline consists of where the user is at what time
                for (LocationUpdate locationUpdate : locations) {
                    if (user.getMacAddress().equals(locationUpdate.getMacAddress())) {
                        userTimeline.add(locationUpdate);
                        System.out.println(user.getName() + " found at " + locationUpdate.getLocationId() + " at " + locationUpdate.getTimeStamp());
                    }
                }
                if (user.getMacAddress().equals(mac)){
                    theUser.setTimeline(userTimeline);
                }
                else{
                    user.setTimeline(userTimeline);
                }
                
            }
            HashMap<User,Long> whoHowLong = new HashMap<>();

            for (int j =0; j <users.size(); j++) {
                if (!users.get(j).getMacAddress().equals(mac) && !users.get(j).getTimeline().isEmpty()){
                    User companion = users.get(j);//iterating through list of potential group members
                    long duration = 0;//how long the user stayed in the place
                    for (int i = 0; i < companion.getTimeline().size(); i++) { //taking only the smaller window where both are present
                        try{
                        MutablePair<LocationUpdate, Long> userData = theUser.getTimeline().get(i); //use pair class as HashMap has no equals method

                        LocationUpdate userLocation = userData.getLeft();
                        ArrayList<MutablePair<LocationUpdate, Long>> companionTimelines = companion.getTimeline();
                        ArrayList<MutablePair<LocationUpdate, Long>> commonTimelines = new ArrayList<>();

                        for (MutablePair<LocationUpdate, Long> companionTimeline : companionTimelines) { //iterate through potential member's timelines to find a match with the user's timeline
                            long lastUpdateSpan = companionTimeline.getLeft().getDate().getTime() + companionTimeline.getRight();
                            if (date.getTime() - lastUpdateSpan < 0) {
                                long timeDiff = date.getTime() - companionTimeline.getLeft().getDate().getTime();
                                companionTimeline.setRight(timeDiff);
                            }
                            if (companionTimeline.getLeft().getLocationId() == userLocation.getLocationId()) {
                                commonTimelines.add(companionTimeline);
                            }
                        }
                        for (int m = 0; m < commonTimelines.size(); m++) {
                            MutablePair<LocationUpdate, Long> timeline = commonTimelines.get(m);
                            LocationUpdate potentialLocation = timeline.getLeft();

                            long userStart = userLocation.getDate().getTime();
                            long userEnd = userStart + userData.getRight();
                            long companionStart = potentialLocation.getDate().getTime();
                            long companionEnd = companionStart + timeline.getRight();
                            if (userEnd >= companionStart) { //if there is an overlap

                                long max = Math.max(userStart, companionStart); //deriving the overlap duration
                                long min = Math.min(userEnd, companionEnd);

                                if (min - max > 0) {
                                    duration += (min - max);//remove negative values
                                }
                            }
                        }
                        }
                        catch(Exception e){
                            request.setAttribute("error","Invalid Top-k Companions input detected, try again");
                            request.getRequestDispatcher("/basicLocationReport.jsp").forward(request, response);
                        }
                    }
                    whoHowLong.put(companion, duration);
                }
            }
           LinkedHashMap<User,Long> rankedUsers = sorter(whoHowLong);
           
           boolean isFirst = true;
           int count = 1;
           long durationChecker = 0;
           ArrayList<User> thisRank = new ArrayList<User>();
           for (Map.Entry<User,Long> rankedUser: rankedUsers.entrySet()){
               User thisUser = rankedUser.getKey();
               long thisDuration = rankedUser.getValue();
               if (isFirst){
                   durationChecker = thisDuration;
                   thisRank.add(thisUser);
                   isFirst = false;
               }
               else{
                   if (thisDuration == durationChecker){
                       thisRank.add(thisUser);
                   }
                   else{
                       HashMap<ArrayList<User>,Long> group = new HashMap<>();
                       group.put(thisRank, durationChecker);
                       rankMap.put(count, group);
                       count++;
                       thisRank = new ArrayList<User>();
                       durationChecker = thisDuration;
                       thisRank.add(thisUser);
                   }
               }
           }
            HashMap<ArrayList<User>,Long> group = new HashMap<>();
            group.put(thisRank, durationChecker);
            rankMap.put(count, group);
            request.setAttribute("rankMap",rankMap);
            request.getRequestDispatcher("/topkcompanions.jsp").forward(request, response);
        } else {
            request.setAttribute("error","Invalid Top-k Companions input detected, try again");
            request.getRequestDispatcher("/basicLocationReport.jsp").forward(request, response);
        }
        
    }
    private static LinkedHashMap<User, Long> sorter (HashMap<User, Long> toSort) {
        LinkedHashMap<User, Long> sortedByOriginalKeys = new LinkedHashMap<>(); //maintains the order of putting
        TreeMap<User, Long> firstSorter = new TreeMap<>((u1, u2) -> u1.getMacAddress().compareTo(u2.getMacAddress()));
        firstSorter.putAll(toSort);//sorts based on keys
        for (Map.Entry<User, Long> map: firstSorter.entrySet()) {
            sortedByOriginalKeys.put(map.getKey(), map.getValue());
        }

        LinkedHashMap<Long, User> swappedMapping = new LinkedHashMap<>();
        for (Map.Entry<User, Long> map: sortedByOriginalKeys.entrySet()) {
            swappedMapping.put(map.getValue(), map.getKey());
        }

        LinkedHashMap<User, Long> toReturn = new LinkedHashMap<>();
        NavigableMap<Long, User> sortedByValue = new TreeMap<>(swappedMapping).descendingMap();
        for (Map.Entry<Long, User> map: sortedByValue.entrySet()) {
            toReturn.put(map.getValue(), map.getKey()); //sort and swap
        }
        return toReturn;
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
