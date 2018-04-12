<%-- 
    Document   : topkpopular
    Created on : 16 Oct, 2017, 7:35:50 PM
    Author     : G7T4
--%>

<%@include file="protect.jsp" %>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Location Report</title>
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
        <%
            TreeMap<Integer,ArrayList<String>> map = (TreeMap<Integer,ArrayList<String>>)request.getAttribute("rankMap");
            int k = (Integer)(request.getAttribute("k"));
            out.println("<h1>Top " + k + " Popular Places</h1>");
            out.println("<table><tr><th>Rank</th><th>Semantic Place(s)</th><th>Number of people</th></tr>");
            Iterator<Integer> iterKeys = map.descendingKeySet().iterator();
            int rank = 0;
            while (iterKeys.hasNext() && rank <k) {
                int numPeople = iterKeys.next();
                ArrayList<String> places = map.get(numPeople);
                Collections.sort(places);
                out.println("<tr><td>" + ++rank + "</td><td>");
                for (int i = 0; i < places.size(); i++) {
                    String s = places.get(i);
                    out.print(s + "<br>");
                }
                out.println("</td><td style = \"text-align: center;\">" + numPeople + "</td></tr>");
            }
            out.println("</table>");
        %>
       <a href ="basicLocationReport.jsp"><input type ="button" value ="Back"/></a>
    </body>
</html>

