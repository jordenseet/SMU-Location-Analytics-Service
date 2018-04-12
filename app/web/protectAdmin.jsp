<%-- 
    Document   : protectAdmin
    Created on : 5 Nov, 2017, 10:29:13 PM
    Author     : G7T4
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
            String adminName = (String)session.getAttribute("admin");
            if (adminName == null) {
                response.sendRedirect("index.jsp");
                return;
            }
        %>
    </body>
</html>
