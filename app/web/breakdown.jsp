<%-- 
    Document   : breakdown
    Created on : 16 Oct, 2017, 1:52:34 AM
    Author     : G7T4
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeMap"%>
<%@page import="Entity.User"%>
<%@include file="protect.jsp" %>
<%@page import="java.util.ArrayList"%>
<%@page import="Utilities.Utility"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Location Breakdown Report</title>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M" crossorigin="anonymous">
    </head>
    <body>
        <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js" integrity="sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4" crossorigin="anonymous"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/js/bootstrap.min.js" integrity="sha384-h0AbiXch4ZDo7tp9hKZ4TsHbi047NrKGLO3SEJAg45jXxnGIfYzk4Si90RDIqNm1" crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.0/Chart.bundle.min.js"></script>
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
            String errorMsg = (String) request.getAttribute("error");
            if (errorMsg == null) {
                String field1 = (String) request.getAttribute("field1");
                String field2 = (String) request.getAttribute("field2");
                String field3 = (String) request.getAttribute("field3");
                if ((field1.equals(field2) && !field1.equals("- Select -"))|| (field2.equals(field3) && !field2.equals("- Select -"))|| (field3.equals(field1) && !field3.equals("- Select -")) ){
                    out.println("<h1>Please select different values for each field</h1><br>");
                }
                else{
                    int total = (Integer)(request.getAttribute("total"));
                    TreeMap<String, TreeMap<String, ArrayList<User>>> breakdown = (TreeMap<String, TreeMap<String, ArrayList<User>>>) request.getAttribute("breakdown");
                    if (!breakdown.isEmpty() && breakdown.containsKey(field1)) {
                        out.println("<h2><b>Breakdown by " + field1 + "</h2></b>");
                        TreeMap<String, ArrayList<User>> map1 = breakdown.get(field1);
                        Iterator<String> iterKeys1 = map1.keySet().iterator();
                        while (iterKeys1.hasNext()) {
                            String key1 = iterKeys1.next();
                            int count1 = map1.get(key1).size();
                            out.println(key1 + ": " + count1 + " (" + Utility.percentHalfRoundUp(count1, total) + "%)<br>");
                        }
                        if (breakdown.containsKey(field2)) {
                            out.println("<h2><br><b>Breakdown by " + field2+"</h2></b>");
                            TreeMap<String, ArrayList<User>> map2 = breakdown.get(field2);
                            Iterator<String> iterKeys2 = map2.keySet().iterator();
                            while (iterKeys2.hasNext()) {
                                String key2 = iterKeys2.next();
                                System.out.println(key2);
                                //int spaceIndex = key2.indexOf(' ');
                                //String key2a = key2.substring(0, spaceIndex);
                                //String key2b = key2.substring(spaceIndex+1,key2.length());
                                //int count1 = map1.get(key2a).size();
                                int count2 = map2.get(key2).size();
                                out.println(key2 + ": " + count2 + " (" + Utility.percentHalfRoundUp(count2, total) + "%)<br>");
                            }
                            if (breakdown.containsKey(field3)) {
                                out.println("<h2><br><b>Breakdown by " + field3 +"</b></h2>");
                                TreeMap<String, ArrayList<User>> map3 = breakdown.get(field3);
                                Iterator<String> iterKeys3 = map3.keySet().iterator();
                                while (iterKeys3.hasNext()) {
                                    String key3 = iterKeys3.next();
                                    //int spaceIndex2 = key3.lastIndexOf(' ');
                                    //String key3a = key3.substring(0, spaceIndex2);
                                    //String key3b = key3.substring(spaceIndex2+1,key3.length());
                                    //int count2 = map2.get(key3a).size();
                                    int count3 = map3.get(key3).size();
                                    out.println(key3 + ": " + count3 + " (" + Utility.percentHalfRoundUp(count3, total) + "%)<br>");
                                }
                            }
                        }
                    }
                }

            } else {
                out.println("<br><h1>"+errorMsg + "</h1>");
            }
            
            // ArrayList<Integer> data = new ArrayList<>();
        %>
       <a href ="basicLocationReport.jsp"><input type ="button" value ="Back"/></a>
    </body>
</html>
