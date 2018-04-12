<%-- 
    Document   : heatmap
    Created on : 22 Oct, 2017, 12:02:58 PM
    Author     : G7T4
--%>
 
<%@include file="protect.jsp" %>
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
        <h1>Heatmap</h1>
        <form action="HeatmapServlet" method="POST">
            Select a floor
            <select name="floor">
                <%                
                    out.println("<option value=\"B1\">B1</option>");
                    for (int i = 1; i <= 5; i++) {
                        out.println("<option value=\"L" + i + "\">L" + i + "</option>");
                    }
                %>
            </select></br></br>
            Datetime
            <input type="datetime-local" name="date" step="1"></br></br>
            <input type="submit" value="View heatmap">
        </form>
        <br /><br />
        <button onclick="goBack()">Go Back</button>

        <script>
            function goBack() {
                window.history.back();
            }
        </script>
       <a href ="logout.jsp"><input type ="button" value ="Logout"/></a>
    </body>
</html>
