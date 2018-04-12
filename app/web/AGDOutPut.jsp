<%-- 
    Document   : AGDOutPut.jsp
    Created on : 1 Nov, 2017, 1:05:15 AM
    Author     : G7T4
--%>

<%@include file="protect.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.ArrayList"%>
<%@page import="Entity.User"%>
<%@page import="Entity.LocationUpdate"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.lang3.tuple.MutablePair"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Automatic Group Detection Results</title>
    </head>
    <body>
        <style>
            body{
                background-image: url("http://nasher.duke.edu/wp-content/themes/nasher/images/background.jpg");
                background-repeat: no-repeat;
                background-size:cover;
                background-attachment: fixed;
                animation-name: backgroundpic;
                animation-duration: 4s;
            }
        </style>
    <center>
        <style>
            .fadeImage {
                -webkit-animation: fadein 2s; /* Safari, Chrome and Opera > 12.1 */
                -moz-animation: fadein 2s; /* Firefox < 16 */
                -ms-animation: fadein 2s; /* Internet Explorer */
                -o-animation: fadein 2s; /* Opera < 12.1 */
                animation: fadein 2s;
                display: block;
            }

            @keyframes fadein {
                from { opacity: 0; }
                to   { opacity: 1; }
            }

            /* Firefox < 16 */
            @-moz-keyframes fadein {
                from { opacity: 0; }
                to   { opacity: 1; }
            }

            /* Safari, Chrome and Opera > 12.1 */
            @-webkit-keyframes fadein {
                from { opacity: 0; }
                to   { opacity: 1; }
            }

            /* Internet Explorer */
            @-ms-keyframes fadein {
                from { opacity: 0; }
                to   { opacity: 1; }
            }

            /* Opera < 12.1 */
            @-o-keyframes fadein {
                from { opacity: 0; }
                to   { opacity: 1; }
            }
        </style>

    </center>
    <center>
        <style>
            input[type=text] {
                padding:5px; 
                border:2px solid #102b72; 
                -webkit-border-radius: 5px;
                border-radius: 5px;
            }
            input[type=password] {
                padding:5px; 
                border:2px solid #102b72; 
                -webkit-border-radius: 5px;
                border-radius: 5px;
            }
            input[type=text]:focus {
                border-color:#333;
            }

            input[type=submit] {
                padding:5px 15px; 
                background:#ccc; 
                border:2px #ccc;
                cursor:pointer;
                -webkit-border-radius: 5px;
                border-radius: 5px; 
            }
        </style>
        <h1>Automatic Group Detection Results</h1>

        <%
            String total = (String) request.getAttribute("total"); //total users in timeframe
            if (total != null) { //to avoid nullpointerexception
                out.println(total + " Users in SIS Building<br>"); //print out total value
                request.removeAttribute("error"); // remove error printout
            }

            String numGroups = (String) request.getAttribute("numGroups"); //number of groups
            if (numGroups != null) { //to avoid nullpointer exception
                out.println("Number of groups = " + numGroups + "<br>");//print out number of groups
                request.removeAttribute("NumGroups");//remove so it wont append
            }
            Set<ArrayList<User>> refinedGroups = (Set<ArrayList<User>>) request.getAttribute("refinedGroups"); //get refined groups 
            ConcurrentHashMap<Integer, Long> whereWhen = new ConcurrentHashMap<>();  //stores the duration each group stays per location, this hashmap will be compared with the userWhereWhens to derive the smallest durations per common locations
            int count = 1; //counter for group number
            for (ArrayList<User> group : refinedGroups) {
                out.println("<h1><b><br>Group " + count + " members<br></h1></b>");
                int memberCount = 1; //counter for group member number
                count++;
                for (User user : group) {
                    ConcurrentHashMap<Integer, Long> userWhereWhen = new ConcurrentHashMap<>(); //stores duration each individual stays per location
                    out.println("<b>Member " + memberCount + "<br></b>"); //since each member in the group has the same locations, only differing durations
                    out.println("Email: " + user.getEmail() + "<br>"); //this algorithm takes the lowest duration per location and outputs
                    out.println("Mac Address: " + user.getMacAddress() + "<br>");//if you don't understand the past 3 lines you shouldn't take SE
                    memberCount++;
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
                             else{
                                 whereWhen.remove(theLocationId); //this means it is not a common location, hence remove this location
                             }
                        }
                    }
                }
                for (Map.Entry pair : whereWhen.entrySet()){//retrieve whereWhen hashmap for output
                    out.println("<br>Visited " + pair.getKey() + " for " + ((Long)pair.getValue()/1000) + " seconds");//prints out value
                }
            }

        %>

    </form></br>
    </br></br>
    <button onclick="goBack()">Go Back</button>

    <script>
        function goBack() {
            window.history.back();
        }
    </script>

    <a href ="logout.jsp"><input type ="button" value ="Logout"/></a>
</body>


</html>
