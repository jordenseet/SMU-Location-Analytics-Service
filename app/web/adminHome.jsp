<%-- 
   Document   : adminHome
   Created on : 10 Oct, 2017, 6:41:18 PM
   Author     : G7T4
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="com.google.gson.JsonElement"%>
<%@page import="com.google.gson.JsonParser"%>
<%@page import="com.google.gson.JsonObject"%>
<%@include file="protectAdmin.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.HashMap" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Admin Home</title>
    </head>
    <body>
        <table>
            <tr>
                <td width="500">
                    <h1>Bootstrap file goes here</h1>

                    <form action = "BootStrapServlet" method = "post"
                          enctype = "multipart/form-data">
                        <p><input type = "file" name = "file" size = "50" /></p>    
                        <input type="text" name="token" value="<% out.print(request.getSession().getAttribute("token").toString()); %>"
                               <p><input type = "submit" value = "Upload" /></p>
                    </form>
                </td>
                <td>
                    <h1>Additional file goes here</h1>
                    <form action = "additionalUploadServlet" method = "post"
                          enctype = "multipart/form-data">
                        <p><input type = "file" name = "file" size = "50" /></p>
                        <input type="text" name="token" value="<% out.print(request.getSession().getAttribute("token").toString()); %>"
                               <p><input type = "submit" value = "Upload" /></p>
                    </form>
                </td>
            </tr>
            <tr>
                <td width="500"> 
                    <a href ="logout.jsp"><input type ="button" value ="Logout"/></a>
                </td>
            </tr>    
        </table>
        <br />
        <%
            if (request.getAttribute("errorMsg") != null) {
                out.println(request.getAttribute("errorMsg").toString());
            }
            String status = "";
            HashMap<String, Integer> recordsLoaded = new HashMap<>();
            if (request.getAttribute("status") != null) {
                status = (String) request.getAttribute("status");
                //status is success or error
            }
            if (request.getAttribute("recordsLoaded") != null) {
                recordsLoaded = (HashMap<String, Integer>) request.getAttribute("recordsLoaded");
                //Number of data that has been loaded
            }
            if (status.length() > 0) {
                out.println("<table width = \"40%\" border = \"1\">");
                //Print out status
                out.println("<tr bgcolor=\"#F8F8F8\">");
                out.println("<td>Status</td>");
                out.println("<td>");
                out.println(status);
                out.println("</td>");
                out.println("</tr>");
                //Print out headings
                out.println("<tr bgcolor=\"#F8F8F8\">");
                out.println("<td>File</td>");
                out.println("<td># of Records Loaded</td>");
                out.println("</tr>");
                //print out the file names and number that was loaded
                //For location.csv
                out.println("<tr bgcolor=\"#F8F8F8\">");
                out.println("<td>location.csv</td>");
                out.println("<td>");
                out.println(recordsLoaded.get("location.csv"));
                out.println("</td>");
                out.println("</tr>");
                //For location-lookup.csv
                out.println("<tr bgcolor=\"#F8F8F8\">");
                out.println("<td>location-lookup.csv</td>");
                out.println("<td>");
                out.println(recordsLoaded.get("location-lookup.csv"));
                out.println("</td>");
                out.println("</tr>");
                //For demographics.csv
               out.println("<tr bgcolor=\"#F8F8F8\">");
                out.println("<td>demographics.csv</td>");
                out.println("<td>");
                out.println(recordsLoaded.get("demographics.csv"));
                out.println("</td>");
                out.println("</tr>");

                if (request.getAttribute("userErrors") != null) {
                    ArrayList<ArrayList<String>> userErrors = (ArrayList<ArrayList<String>>) request.getAttribute("userErrors");
                    //retrieves all user errors
                    ArrayList<String> userLineNumbers = (ArrayList<String>) request.getAttribute("userLineNumbers");
                    //retrieves all user line numbers
                    if (userLineNumbers.size() > 0) {
                        out.println("<tr bgcolor=\"#CCFFFF\" >");
                        out.println("<td>file</td>");
                        out.println("<td>demographics.csv</td>");
                        out.println("</tr>");
                    }
                    for (int i = 0; i < userLineNumbers.size(); i++) {
                        String userLine = userLineNumbers.get(i);
                        ArrayList<String> errorMessage = userErrors.get(i);
                        String errorMessageToPrint = "";
                        int userErrorCounter = 0;

                        out.println("<tr bgcolor=\"#CCFFFF\" >");
                        out.println("<td>line</td>");
                        out.println("<td>");
                        out.println(userLine);
                        out.println("</td>");
                        out.println("</tr>");
                        for (String errormsg : errorMessage) {
                            //To retrieve the error messages from the arraylist string of all error messages
                            
                            out.println("<tr bgcolor=\"#CCFFFF\" >");
                            out.println("<td>message</td>");
                            out.println("<td>");
                            out.println(errormsg);
                            //prints out error message
                            out.println("</td>");
                            out.println("</tr>");
                        }
                    }
                }
                if (request.getAttribute("semanticErrors") != null) {
                    ArrayList<ArrayList<String>> semanticErrors = (ArrayList<ArrayList<String>>) request.getAttribute("semanticErrors");
                    //retrieve all location-lookup error messages
                    ArrayList<String> semanticLineNumbers = (ArrayList<String>) request.getAttribute("semanticLineNumbers");
                    //retrieve all error line numbers
                    if (semanticLineNumbers.size() > 0) {
                        out.println("<tr bgcolor=\"#F0F8FF\" >");
                        out.println("<td>file</td>");
                        out.println("<td>location-lookup.csv</td>");
                        out.println("</tr>");
                    }
                    for (int i = 0; i < semanticLineNumbers.size(); i++) {
                        String semanticLine = semanticLineNumbers.get(i);
                        ArrayList<String> errorMessage = semanticErrors.get(i);
                        String errorMessageToPrint = "";
                        int semanticErrorCounter = 0;

                        out.println("<tr bgcolor=\"#F0F8FF\" >");
                        out.println("<td>line</td>");
                        out.println("<td>");
                        out.println(semanticLine);
                        out.println("</td>");
                        out.println("</tr>");
                        for (String errormsg : errorMessage) {
                            
                            out.println("<tr bgcolor=\"#F0F8FF\" >");
                            out.println("<td>message</td>");
                            out.println("<td>");
                            out.println(errormsg);
                            //prints out error message
                            out.println("</td>");
                            out.println("</tr>");
                        }
                    }

                }

                if (request.getAttribute("locationErrors") != null) {
                    ArrayList<ArrayList<String>> locationErrors = (ArrayList<ArrayList<String>>) request.getAttribute("locationErrors");
                    //retrieves all location error messages
                    ArrayList<String> locationLineNumbers = (ArrayList<String>) request.getAttribute("locationLineNumbers");
                    //retrieves the line number of all error messages
                    if (locationLineNumbers.size()>0){
                    out.println("<tr bgcolor=\"#FFEBCD\" >");
                    out.println("<td>file</td>");
                    out.println("<td>location.csv</td>");
                    out.println("</tr>");
                    }
                    for (int i = 0; i < locationLineNumbers.size(); i++) {
                        String locationLine = locationLineNumbers.get(i);
                        ArrayList<String> errorMessage = locationErrors.get(i);
                        String errorMessageToPrint = "";
                        int locationErrorCounter = 0;

                        out.println("<tr bgcolor=\"#FFEBCD\" >");
                        out.println("<td>line</td>");
                        out.println("<td>");
                        out.println(locationLine);
                        out.println("</td>");
                        out.println("</tr>");
                        for (String errormsg : errorMessage) {
                          
                            out.println("<tr bgcolor=\"#FFEBCD\" >");
                            out.println("<td>message</td>");
                            out.println("<td>");
                            out.println(errormsg);
                            //prints out error message
                            out.println("</td>");
                            out.println("</tr>");
                        }
                    }

                }

                out.println("</table>");
            }

        %>
    </body>
</html>