/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Json;

import DAO.AdminDAO;
import DAO.UserDAO;
import Entity.Admin;
import Entity.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "LoginJsonServlet", urlPatterns = {"/json/authenticate"})
public class LoginJsonServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *display the output in json
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Trim to remove blanks before and after email

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Create "outer" json object 
        JsonObject jsonOutput = new JsonObject();
        //Create json object to store all errors 
        JsonObject jsonErrors = new JsonObject();
        //List of errors 
        JsonArray errorMsgs = new JsonArray();

        String output = "";

        boolean success = true;

        String email = request.getParameter("email");
        String pwd = request.getParameter("password");

        if (email != null && pwd != null) {
            email = email.trim();
            //authenticatePassword method at line 62
            Object o = authenticatePassword(email, pwd);
            String sharedSecret = "G7T4SHAREDSECRET";


            String token = JWTUtility.sign(sharedSecret, email);
        
            if (o != null) {
                if (o instanceof User) {
                    User user = (User) o;
                    session.setAttribute("user", user.getName());

                    jsonOutput.addProperty("status", "success");
                    jsonOutput.addProperty("token", token);
                    output = gson.toJson(jsonOutput);
                    out.println(output);
                
                    return;
                } else {
                    Admin admin = (Admin) o;
                    session.setAttribute("admin", admin.getName());
                    jsonOutput.addProperty("status", "success");
                    jsonOutput.addProperty("token", token);
                    output = gson.toJson(jsonOutput);
                    out.println(output);
                
                    return;
                }
            } else {
                session.setAttribute("errors", "invalid username/password!");
                jsonErrors.addProperty("status", "error");
                errorMsgs.add("invalid username/password!");
                jsonErrors.add("messages", errorMsgs);
                output = gson.toJson(jsonErrors);
                out.println(output);
                return;
            }
        } else {
            session.setAttribute("errors", "missing username/password!");
            jsonErrors.addProperty("status", "error");
            errorMsgs.add("missing username/password!");
            jsonErrors.add("messages", errorMsgs);
            output = gson.toJson(jsonErrors);
            out.println(output);
            return;
        }
        

    }

    /**
     *authenticate the password
     * @param email
     * @param pwd
     * @return user if password is correct, else returns null
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
