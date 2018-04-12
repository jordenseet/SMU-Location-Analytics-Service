<%-- 
    Document   : topknext
    Created on : 1 Nov, 2017, 10:33:32 PM
    Author     : G7T4
--%>

<%@include file="protect.jsp" %>
<%@page import="java.util.Iterator"%>
<%@page import="Utilities.Utility"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top K Next Places</title>
    </head>
    <body>
    <%
        int k = (Integer)(request.getAttribute("k"));
        String semanticPlace = (String)(request.getAttribute("origin"));
        String errorMsg = (String)(request.getAttribute("error"));
        // Output the top k ranks (along with the name of the semantic place(s) 
        // for each rank) with the number of users in each rank (ties are
        // handled in the same way as above) as well as the % of people who
        // went to this place out of the total number of users in the semantic
        // place being queried.
        if (errorMsg == null) {
            out.println("<h1>Top " + k + "Places People Visited After " + semanticPlace + "</h1>");
            out.println("<table><tr><th>Rank</th><th>Semantic Place(s)</th><th>Number of users</th><th>Percentage</th></tr>");
            int totalPplQueried = (Integer)(request.getAttribute("totalPplQueried"));
            int numPplWhoLeft = (Integer)(request.getAttribute("numPplWhoLeft"));
            TreeMap<Integer, ArrayList<String>> rankMap = (TreeMap<Integer, ArrayList<String>>)(request.getAttribute("rankMap"));
            Iterator<Integer> iterKeys = rankMap.descendingKeySet().iterator();
            int rank = 1;
            while (iterKeys.hasNext()) {
                int numPpl = iterKeys.next();
                ArrayList<String> nextPlaces = new ArrayList<>();
                int numPlaces = nextPlaces.size();
                out.print("<tr><td>" + rank + "</td><td>");
                for (int i = 0; i < numPlaces; i++) {
                    out.print(nextPlaces.get(i));
                    if (i < numPlaces - 1) {
                        out.print("\n");
                    }
                }
                out.println("</td><td>" + numPpl + "</td><td>" + Utility.percentHalfRoundUp(numPpl, totalPplQueried) + "</td></tr>");
            }
            out.println("</table>");
            // Also output the total number of users in the semantic place being
            // queried as well as the number of users who visited another place
            // (exclude those left the place but have not visited another place)
            // in the query window.
            out.println("Total number of users at " + semanticPlace + ": " + totalPplQueried);
            out.println("<br>Number of users who visited another place: " + (totalPplQueried - numPplWhoLeft));
        } else {
            out.println(errorMsg);
        }
    %>
    <a href ="basicLocationReport.jsp"><input type ="button" value ="Back"/></a>
    </body>
</html>
