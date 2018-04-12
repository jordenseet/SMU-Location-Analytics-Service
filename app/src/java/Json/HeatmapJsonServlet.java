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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "HeatmapJsonServlet", urlPatterns = {"/json/heatmap"})
public class HeatmapJsonServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *Display the output in json
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JsonObject jsonOutput = new JsonObject();
        JsonArray errors = new JsonArray();
        
        PrintWriter out = response.getWriter();

        String floor = request.getParameter("floor");
        String date = request.getParameter("date");
        String token = request.getParameter("token");
        String sharedSecret = "G7T4SHAREDSECRET";
        String output = "";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String result = JWTUtility.verify(token, sharedSecret);
            if (result == null) {
                errors.add("invalid token");
            }
        } catch (Exception e) {
            errors.add("invalid token");
        }

        int level = Integer.parseInt(floor);
        if (level < 0 && level > 5) {
            errors.add("invalid floor");
        }
        if (!dateIsValid(date)) {
            errors.add("invalid date");
        }

        if (errors.size() > 0) {
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("message", errors);
            out.println(jsonOutput);
            return;
        }

        ArrayList<CrowdDensity> cds = CrowdDensityDAO.retrieveCrowdDensitiesInWindowOnFloor(date, floor);
        JsonArray heatmap = new JsonArray();

        for (CrowdDensity cd : cds) {
            JsonObject obj = new JsonObject();
            obj.addProperty("semantic-place", cd.getName());
            obj.addProperty("num-people", cd.getNumPeople());
            obj.addProperty("crowd-density", cd.getDensity());
            heatmap.add(obj);
        }

        jsonOutput.addProperty("status", "success");
        jsonOutput.add("heatmap", heatmap);

        output = gson.toJson(jsonOutput);
        out.println(output);
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

    /**
     *check if the date is valid
     * @param value
     * @return true if it is valid, else return false
     */
    public static boolean dateIsValid(String value) {
        try {
            new SimpleDateFormat("YYYY-MM-DD HH:MM:SS").parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
