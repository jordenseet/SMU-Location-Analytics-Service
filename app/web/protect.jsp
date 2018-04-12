<%-- 
    Document   : protect
    Created on : 22 Oct, 2017, 6:59:48 PM
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
            String userName = (String)session.getAttribute("user");
            String adminName = (String)session.getAttribute("admin");
            if (userName == null && adminName == null) {
                response.sendRedirect("index.jsp");
                return;
            }
        %>
    </body>
</html>