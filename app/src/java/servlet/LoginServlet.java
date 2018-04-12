/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DAO.AdminDAO;
import DAO.UserDAO;
import Entity.Admin;
import Entity.User;
import is203.JWTUtility;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Trim to remove blanks before and after email
        String email = request.getParameter("email").trim();
        String pwd = request.getParameter("password");
        //authenticatePassword method at line 62
        Object o = authenticatePassword(email, pwd);
        HttpSession session = request.getSession();
        String sharedSecret = "G5T3SHAREDSECRET";
        String token = JWTUtility.sign(sharedSecret, email);
        if (o != null) {
            if (o instanceof User) {
                User user = (User) o;
                session.setAttribute("user", user.getName());
                response.sendRedirect("home.jsp");
                 session.setAttribute("token", token);
            } else {
                Admin admin = (Admin) o;
                session.setAttribute("admin", admin.getName());
                response.sendRedirect("adminHome.jsp");
                 session.setAttribute("token", token);
            }
        } else {
            session.setAttribute("errors", "Invalid username/password!");
            response.sendRedirect("index.jsp");
        }
    }

    /**
     *authenticate password
     * @param email
     * @param pwd
     * @return user if password is correct, else return null
     */
    public static Object authenticatePassword(String email, String pwd) {
        AdminDAO adminDAO = new AdminDAO();
        Admin admin = adminDAO.getAdmin(email);
        if (admin != null && admin.getPassword().equals(pwd)) {
            return admin;
        } else {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserWithEmail(email);
            if (user != null && user.getPassword().equals(pwd)) {
                return user;
            }
        }
        return null;
    }// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
