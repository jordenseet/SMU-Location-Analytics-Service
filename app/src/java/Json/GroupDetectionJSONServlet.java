package Json;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTException;
import is203.JWTUtility;
import java.io.PrintWriter;
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
* @author G7T4
* BronKerboschClique finder is a utility that we got online.
* Credit to Ewgenij Proschak and Contributors.
* 
* MutablePair is an Object from apache.commons api
*/
@WebServlet(name = "/GroupDetectionJSONServlet", urlPatterns = {"/json/group_detect"})
public class GroupDetectionJSONServlet extends HttpServlet {

    /**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
    * methods.
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        JsonObject results = new JsonObject();
        ArrayList<String> errorList = new ArrayList<>();

        PrintWriter out = response.getWriter();

        String token = request.getParameter("token");
        String sharedSecret = "G7T4SHAREDSECRET";
        String output = "";
          Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String result = JWTUtility.verify(token, sharedSecret);
            if (result == null) {
                errorList.add("Invalid token");
            }
        } catch (JWTException e) {
            errorList.add("Invalid token");
        }

        String dateStr = request.getParameter("datetime");
        Date date = null;
        try {
            date = new Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr).getTime());//convert to Date object for easy manipulation
        } 
        catch (ParseException pe){
            try{
                dateStr += ":00";
                date = new Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr).getTime());//convert to Date object for easy manipulation
            }
            catch(Exception error){
                errorList.add("Invalid date");
            }
        }
        catch (Exception e) {
            errorList.add("Invalid date");
        }
        
        if (errorList.size() > 0) {
            Collections.sort(errorList);
            results.addProperty("status", "error");
            JsonArray errors = new JsonArray();
            for (String error : errorList) {
                errors.add(error);
            }
            results.add("message", errors);
            out.println(results);
            return;
        }
        
        ArrayList<User> users = UserDAO.retrievePersonsInWindow(dateStr);
        results.addProperty("status", "success");
        results.addProperty("total-users", users.size());
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
            System.out.println("Current user is " + user.getName());
            MutablePair<LocationUpdate, Long> finalUserUpdate = user.getTimeline().get(user.getTimeline().size() - 1);
            int finalUserLocation = finalUserUpdate.getLeft().getLocationId();
            System.out.println("final location for user is " + finalUserLocation + " at " + finalUserUpdate.getLeft().getTimeStamp());
            List<User> potentialGroupMembers = new ArrayList();//getting list of potential group members

            for (int j = users.size() - 1; j >= i; j--) {
                User potentialMember = users.get(j);//iterating through list of potential group members
                System.out.println("Potential user is " + potentialMember.getName());
                long duration = 0;//how long the user stayed in the place
                MutablePair<LocationUpdate, Long> finalUpdate = potentialMember.getTimeline().get(potentialMember.getTimeline().size() - 1);
                System.out.println("Companion final update is " + finalUpdate.getLeft().getLocationId() + " at " + finalUpdate.getLeft().getTimeStamp());
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

                            Date startMax = new Date(max);
                            String maxStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(startMax);

                            Date endMin = new Date(min);
                            String minStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(endMin);

                            System.out.println("Timeline Found at " + potentialLocation.getLocationId() + " during " + maxStr + " to " + minStr);
                            if (min - max > 0) {
                                duration += (min - max);//remove negative values
                                System.out.println("duration now is " + duration);
                            }

                        }
                    }

                }
                System.out.println("Duration between " + user.getName() + " and " + potentialMember.getName() + " is " + duration);
                System.out.println("==============================================================");
                if (duration >= (1000 * 60 * 12)) {//if duration together is longer than 12 minutes, set as a possible group pairing
                    System.out.println("MutablePairing is recorded as " + user.getName() + " and " + potentialMember.getName());
                    potentialMember.setDuration(duration);
                    potentialGroupMembers.add(potentialMember);
                    System.out.println();
                }
            }

            if (potentialGroupMembers.size() > 0) {
                grouping.put(user, potentialGroupMembers); // prepare to cycle through pairings to form groups
            }
        }
        Set<ArrayList<User>> refinedGroups = new HashSet<ArrayList<User>>();
        HashMap<Integer, Long> whereWhen = new HashMap<>();  //stores the duration each group stays per location, this hashmap will be compared with the userWhereWhens to derive the smallest durations per common locations
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
            System.out.println(user.getName() + " added into graph as User");
            //compUserList.remove(user);  //remove the user from the list to prevent duplicates

            if (compUserList.size() == 0) {
                System.out.println(user.getName() + " has no companions");
            }

            for (int i = 0; i < compUserList.size(); i++) {
                User nextUser = compUserList.get(i);
                graph.addVertex(nextUser); // add vertexes for group members
                System.out.println(nextUser.getName() + " added into graph as " + user.getName() + "'s companion");
                if (!nextUser.equals(user)) {
                    graph.addEdge(user, nextUser); //draw the linkages
                    System.out.println("Linkage drawn between " + user.getName() + " and " + nextUser.getName());
                    System.out.println();
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
        //can start jsoning here
        results.addProperty("total-groups", refinedGroups.size() + "");
        JsonArray groups = new JsonArray();
        
        for (ArrayList<User> group : refinedGroups) {
            JsonObject jsonGroup = new JsonObject();
            jsonGroup.addProperty("size", group.size() + "");
            JsonArray members = new JsonArray();
            for (User user : group) {
                JsonObject member = new JsonObject();
                HashMap<Integer, Long> userWhereWhen = new HashMap<>(); //stores duration each individual stays per location
                //since each member in the group has the same locations, only differing durations
                // member email
                member.addProperty("email", user.getEmail()); //this algorithm takes the lowest duration per location and outputs
                // member mac address
                member.addProperty("mac-address", user.getMacAddress());//if you don't understand the past 3 lines you shouldn't take SE
                members.add(member);
                ArrayList<MutablePair<LocationUpdate, Long>> userTimeline = user.getTimeline();//get user timeline
                for (MutablePair<LocationUpdate, Long> update : userTimeline){ //for each update inside user's timeline
                    int locationId = update.getLeft().getLocationId(); //get location 
                    if (!userWhereWhen.containsKey(locationId)){ // if user didnt visit this location
                        userWhereWhen.put(locationId,update.getRight());//insert location and duration 
                    }
                    else{
                        long currentDuration = userWhereWhen.get(locationId);
                        userWhereWhen.put(locationId, currentDuration + update.getRight());//overwrites location and duration with new value
                    }
                }
                if (whereWhen.isEmpty()){ //if external hashmap is empty ie this is the first userWhereWhen to be passed
                    for (Map.Entry<Integer,Long> pair:userWhereWhen.entrySet()){
                        whereWhen.put(pair.getKey(), pair.getValue()); // put in all values into external hashmap
                    }
                }
                else{ //if this is not the first userWhereWhen to be passed
                    for (Map.Entry<Integer,Long> pair : whereWhen.entrySet()){
                        int theLocationId = pair.getKey();
                        if (userWhereWhen.containsKey(theLocationId)){ //if user has common location with external hashmap
                            long userDuration = userWhereWhen.get(theLocationId);
                            long theDuration = pair.getValue();
                            if (userDuration < theDuration){ // if user duration is smaller than current recorded duration
                                whereWhen.put(theLocationId,userDuration);//record smallest duration for common location
                            }
                        }
                        else {
                            whereWhen.remove(theLocationId); //this means it is not a common location, hence remove this location
                        }
                    }
                }
            }
            JsonArray jsonLocations = new JsonArray();
            long totalTimeSpent = 0;
            for (Map.Entry pair : whereWhen.entrySet()){//retrieve whereWhen hashmap for output
                // location id & duration as a group
                JsonObject location = new JsonObject();
                location.addProperty("location", ((LocationUpdate)pair.getKey()).getLocationId() + "");
                long timeSpent = (Long)(pair.getValue())/1000;
                totalTimeSpent += timeSpent;
                location.addProperty("time-spent", timeSpent + "");
                jsonLocations.add(location);
            }
            jsonGroup.addProperty("total-time-spent", totalTimeSpent);
            jsonGroup.add("members", members);
            jsonGroup.add("locations", jsonLocations);
            groups.add(jsonGroup);
        }
        results.add("groups", groups);
          output = gson.toJson(results);
        out.println(output);

    }
    
    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}