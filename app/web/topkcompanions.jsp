<%-- 
    Document   : topkcompanions
    Created on : 28 Oct, 2017, 2:23:22 PM
    Author     : G7T4
--%>

<%@include file="protect.jsp" %>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="Entity.User"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top K Companions</title>
    </head>
    <body>
        <%  String none = (String) (request.getAttribute("error"));
            if (none == null) {
                int k = (Integer) (request.getAttribute("k"));
                out.println("<h1>Top " + k + " Companions</h1>");
                TreeMap<Integer, HashMap<ArrayList<User>, Long>> rankMap = (TreeMap<Integer, HashMap<ArrayList<User>, Long>>) (request.getAttribute("rankMap"));
                int count = 1;
                for (int i = 0; i < k; i++) {
                    
                    if (rankMap.get(count) != null) {
                        out.println("<h2><b>Rank " + count + ": </h2></b>");
                        HashMap<ArrayList<User>, Long> thisRank = rankMap.get(count);
                        for (Map.Entry<ArrayList<User>, Long> pair : thisRank.entrySet()) {
                            ArrayList<User> group = pair.getKey();
                            long duration = pair.getValue();
                            if (duration <= 0){
                                out.println("No companions found");
                            }
                            else{
                                for (User user : group) {
                                    out.println("Email is : " + user.getEmail() + "<br>");
                                    out.println("Mac Address is " + user.getMacAddress() + "<br>");
                                }
                                out.println("Time spent together is " + duration / 1000 + " seconds<br>");
                            }
                            
                        }
                    }
                    count++;
                }
            } else {
                out.println(none);
            }
        %>
        <a href ="basicLocationReport.jsp"><input type ="button" value ="Back"/></a>
    </body>
</html>
