<%-- 
    Document   : basicLocationReport
    Created on : 22 Oct, 2017, 11:58:24 AM
    Author     : G7T4
--%>

<%@page import="DAO.SemanticPlaceDAO"%>
<%@page import="java.util.ArrayList"%>
<%@include file="protect.jsp" %>
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
        <h1>Basic Location Report</h1>
         <%
            String error = (String)request.getAttribute("error");
            if (error != null) {
                out.println(error);
                request.removeAttribute("error");
            }
        %>
        <h2>Breakdown by year, gender, or school</h2>
        <form action="BreakdownServlet" method="POST">
            Datetime
            <input type="datetime-local" name="date" step="1"></br></br>
            Category
            <%
                ArrayList<String> fields = new ArrayList<>();
                fields.add("- Select -");
                fields.add("Year");
                fields.add("Gender");
                fields.add("School");
                for (int i = 1; i <= 3; i++) {
                    out.println("<select name=\"field" + i + "\">");
                    for (String f : fields) {
                        out.println("<option value=\"" + f + "\">" + f + "</option>");
                    }
                    out.println("</select>");
                }
            %>
            </br></br>
            <input type="submit" value="Submit">
           
        </form></br>
        <h2>Top-k popular places</h2>
        <form action="TopKPopServlet" method="POST">
            Datetime
            <input type="datetime-local" name="date" step="1"></br></br>
            Number of places
            <select name="k">
                <%
                    for (int i = 1; i <= 10; i++) {
                        if (i != 3) {
                            out.println("<option value=" + i + ">" + i + "</option>");
                        } else {
                            out.println("<option selected=\"selected\">3</option>");
                        }
                    }
                %>
            </select></br></br>
            <input type="submit" value="Submit">
        </form></br>
        <h2>Top-k companions</h2>
        <form action="TopKCompServlet" method="POST">
            Datetime
            <input type="datetime-local" name="date"step="1"></br></br>
            MAC Address
            <input type="text" name="mac-address"></br></br>
            Number of companions
            <select name="k">
            <%
                for (int i = 1; i <= 10; i++) {
                    if (i != 3) {
                        out.println("<option value=" + i + ">" + i + "</option>");
                    } else {
                        out.println("<option selected=\"selected\">3</option>");
                    }
                }
            %>
            </select></br></br>
            <input type="submit" name="Submit">
        </form></br>
        <h2>Top-k next places</h2>
        <form action="TopKNextServlet" method = "POST">
            Number of places
            <select name="k">
            <%
                for (int i = 1; i <= 10; i++) {
                    if (i != 3) {
                        out.println("<option value=" + i + ">" + i + "</option>");
                    } else {
                        out.println("<option selected=\"selected\">3</option>");
                    }
                }
            %>
            </select></br></br>
            Place
            <select name="origin">
            <%
                ArrayList<String> semanticPlaces = SemanticPlaceDAO.retrieveAllSemanticPlaces();
                for (String sp : semanticPlaces) {
                    out.println("<option value=\"" + sp + "\">" + sp + "</option>");
                }
            %>
            </select></br></br>
            Datetime
            <input type="datetime-local" name="date" step="1"></br></br>
            <input type="submit" name="Submit">
        </form>
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
