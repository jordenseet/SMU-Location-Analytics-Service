<%-- 
    Document   : displayHeatmap
    Created on : 22 Oct, 2017, 12:44:59 PM
    Author     : G7T4
--%>

<%@include file="protect.jsp" %>
<%@page import="Entity.CrowdDensity"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Heatmap</title>
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
            out.println("<h1>SIS Building Heatmap for " + request.getAttribute("floor") + "</h1>");
            ArrayList<CrowdDensity> cds = (ArrayList<CrowdDensity>)(request.getAttribute("crowddensities"));
            out.println("<table><tr><th>Semantic Place</th><th>Crowd Density</th></tr>");
            for (CrowdDensity cd : cds) {
                out.println("<tr><td>" + cd.getName() + "</td><td style = \"text-align: center;\">" + cd.getDensity() + "</td></tr>");
            }
            out.println("</table>");
        %>
        <h2>Legend</h2>
        <table style = "text-align: center;">
            <tr>
                <th>Crowd Density</th>
                <th>Number of People</th>
            </tr>
            <tr>
                <td>0</td>
                <td>0</td>
            </tr>
            <tr>
                <td>1</td>
                <td>1 to 2</td>
            </tr>
            <tr>
                <td>2</td>
                <td>3 to 5</td>
            </tr>
            <tr>
                <td>3</td>
                <td>6 to 10</td>
            </tr>
            <tr>
                <td>4</td>
                <td>11 to 20</td>
            </tr>
            <tr>
                <td>5</td>
                <td>21 to 30</td>
            </tr>
            <tr>
                <td>6</td>
                <td>31 and more</td>
            </tr>
        </table>
        
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
