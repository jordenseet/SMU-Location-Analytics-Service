/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Json;

import DAO.CrowdDensityDAO;
import Entity.CrowdDensity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "TopKPopJsonServlet", urlPatterns = {"/json/top-k-popular-places"})
public class TopKPopJsonServlet extends HttpServlet {

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JsonObject jsonOutput = new JsonObject();
        JsonArray errors = new JsonArray();
        PrintWriter out = response.getWriter();
        int numK = Integer.parseInt(request.getParameter("k"));
        String date = request.getParameter("date");
        String token = request.getParameter("token");
        String sharedSecret = "G7T4SHAREDSECRET";
        String output = "";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String result = JWTUtility.verify(token, sharedSecret);
            if (result == null) {
                errors.add("Invalid token");
            }
        } catch (Exception e) {
            errors.add(e.getMessage());
            errors.add("Invalid token");
            output = gson.toJson(jsonOutput);
            out.println(output);
            return;
        }
        if (!dateIsValid(date)) {
            errors.add("Invalid date");
            output = gson.toJson(jsonOutput);
            out.println(output);
            return;
        }
        ArrayList<CrowdDensity> cds = CrowdDensityDAO.retrieveCrowdDensitiesInWindow(date);
        JsonArray map = new JsonArray();
        JsonObject obj = new JsonObject();
        int i = 0;
        while (map.size() < numK && i < cds.size()) {
            CrowdDensity cd = cds.get(i);

            String name = cd.getName();
            int number = cd.getNumPeople();

            obj.addProperty("rank", numK);
            obj.addProperty("semantic-place", cd.getNumPeople());
            obj.addProperty("count", cd.getDensity());
            map.add(obj);

            ArrayList<String> places = new ArrayList<>();

            places.add(name);

            i++;
        }

        jsonOutput.addProperty("status", "success");
        jsonOutput.add("results", map);
        output = gson.toJson(jsonOutput);
        out.println(output);

    }

    /**
     * check if the date is valid
     *
     * @param value
     * @return true if the date is valid, else return false
     */
    public static boolean dateIsValid(String value) {
        try {
            new SimpleDateFormat("YYYY-MM-DD HH:MM:SS").parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
