<%-- 
    Document   : logout
    Created on : 14 Nov, 2017, 5:01:36 PM
    Author     : G7T4
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Logout</title>
    </head>
    <body>
    <%
        session.invalidate();
        response.sendRedirect("index.jsp");
    %>
    </body>
</html>
