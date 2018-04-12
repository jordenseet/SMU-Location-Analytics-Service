/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DAO.CrowdDensityDAO;
import Entity.CrowdDensity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "TopKPopServlet", urlPatterns = {"/TopKPopServlet"})
public class TopKPopServlet extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        String datetime = request.getParameter("date");
        if (datetime != null) {
            int k = Integer.parseInt(request.getParameter("k"));
            ArrayList<CrowdDensity> cds = CrowdDensityDAO.retrieveCrowdDensitiesInWindow(datetime);
            TreeMap<Integer, ArrayList<String>> map = new TreeMap<>();
            int i = 0;
            while (i < cds.size() ) {
                CrowdDensity cd = cds.get(i);
                String name = cd.getName();
                int num = cd.getNumPeople();
                ArrayList<String> places = new ArrayList<>();
                if (map.containsKey(num)) {
                    places = map.get(num);
                }
                places.add(name);
                map.put(num, places);
                i++;
            }
            request.setAttribute("k", k);
            request.setAttribute("rankMap", map);
            request.getRequestDispatcher("/topkpopular.jsp").forward(request, response);
        } else {
            request.setAttribute("error","Invalid Top-k Popular Places input detected, try again");
            request.getRequestDispatcher("/basicLocationReport.jsp").forward(request, response);
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

