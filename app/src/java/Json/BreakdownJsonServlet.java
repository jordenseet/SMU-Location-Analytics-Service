/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DAO.UserDAO;
import Entity.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "BreakdownJsonServlet", urlPatterns = {"/json/basic-loc-report"})
public class BreakdownJsonServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     *display output in json
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        JsonObject jsonOutput = new JsonObject();
        JsonArray errors = new JsonArray();
        JsonObject obj = new JsonObject();
        JsonArray topKBreakdowns = new JsonArray();
        PrintWriter out = response.getWriter();
        try {
            String datetime = request.getParameter("datetime");
            String field = request.getParameter("field");
            String field2 = request.getParameter("field2");
            String field3 = request.getParameter("field3");
            ArrayList<User> users = UserDAO.retrieveUsersInWindow(datetime);

            String token = request.getParameter("token");
            String sharedSecret = "G7T4SHAREDSECRET";

            //check for valid token
            try {
                String result = JWTUtility.verify(token, sharedSecret);
                if (result == null) {
                    errors.add("Invalid token");
                }
            } catch (Exception e) {
                errors.add(e.getMessage());
                errors.add("Invalid token");
                out.println(jsonOutput);

            }

            if (users == null) {
                errors.add("Zero records were found");
                out.println(jsonOutput);
                return;
            } else if (field.equals("- Select -") & field2.equals("- Select -") & field3.equals("- Select -")) {
                errors.add("Please Select at least one category");
                out.println(jsonOutput);
                return;
            } else if (!field.equals("- Select -") & !field2.equals("- Select -") & !field3.equals("- Select -") & field.equals(field2) & field2.equals(field3)) {
                errors.add("Please Select different categories");
                out.println(jsonOutput);
                return;
            } else if ((field.equals(field2) & field3.equals("- Select -")) || (field2.equals(field3) & field.equals("- Select -")) || (field3.equals(field) & field2.equals("- Select -"))) {
                errors.add("Please Select different categories");
                out.println(jsonOutput);
                return;
            }
            //user first choose year
            if (field.equals("Year") || (field.equals("- Select -") & field2.equals("Year")) || (field.equals("- Select -") & field2.equals("- Select -") & field3.equals("Year"))) {
                boolean countGender = false;
                boolean countSchool = false;
                if (field2.equals("Gender") || field3.equals("Gender")) {
                    countGender = true;
                }
                if (field2.equals("School") || field3.equals("School")) {
                    countSchool = true;
                }
                yearBreakdown(request, response, users, countGender, countSchool);
                int total = users.size();
                obj.addProperty("total", total);
                obj.addProperty("field", field);
                obj.addProperty("field2", field2);
                obj.addProperty("field3", field3);
            }
            //user first choose gender
            if (field.equals("Gender") || (field.equals("- Select -") & field2.equals("Gender")) || (field.equals("- Select -") & field2.equals("- Select -") & field3.equals("Gender"))) {
                boolean countYear = false;
                boolean countSchool = false;
                if (field2.equals("Year") || field3.equals("Year")) {
                    countYear = true;
                }
                if (field2.equals("School") || field3.equals("School")) {
                    countSchool = true;
                }
                genderBreakdown(request, response, users, countYear, countSchool);
                int total = users.size();
                obj.addProperty("total", total);
                obj.addProperty("field", field);
                obj.addProperty("field2", field2);
                obj.addProperty("field3", field3);
            }
            //user first choose school
            if (field.equals("School") || (field.equals("- Select -") & field2.equals("School")) || (field.equals("- Select -") & field2.equals("- Select -") & field3.equals("School"))) {
                boolean countYear = false;
                boolean countGender = false;
                if (field2.equals("Year") || field3.equals("Year")) {
                    countYear = true;
                }
                if (field2.equals("Gender") || field3.equals("Gender")) {
                    countGender = true;
                }
                schoolBreakdown(request, response, users, countYear, countGender);
                int total = users.size();
                obj.addProperty("total", total);
                obj.addProperty("field", field);
                obj.addProperty("field2", field2);
                obj.addProperty("field3", field3);
            }
        } catch (IndexOutOfBoundsException e) {

            errors.add("Invalid Breakdown input detected, try again");
            out.println(jsonOutput);
            return;
        }
                    topKBreakdowns.add(obj);


    }

    /**
     *breakdown by year
     * @param request
     * @param response
     * @param users
     * @param countGender
     * @param countSchool
     */
    protected void yearBreakdown(HttpServletRequest request, HttpServletResponse response, ArrayList<User> users, boolean countGender, boolean countSchool) {
        HashMap<String, Integer> yearMap = new HashMap<>();
        for (int i = 2013; i <= 2017; i++) {
            yearMap.put("" + i, 0);
        }
        for (User u : users) {
            String year = u.getYear();
            int count = yearMap.get(year) + 1;
            yearMap.put(year, count);
        }
        Iterator iterKeys = yearMap.keySet().iterator();
        while (iterKeys.hasNext()) {
            String yr = (String) iterKeys.next();
            int count = yearMap.get(yr);
            request.setAttribute(yr, count);
        }

        ArrayList<User> user2013 = new ArrayList<>();
        ArrayList<User> user2014 = new ArrayList<>();
        ArrayList<User> user2015 = new ArrayList<>();
        ArrayList<User> user2016 = new ArrayList<>();
        ArrayList<User> user2017 = new ArrayList<>();
        for (User u : users) {
            String year = u.getYear();
            if (year.equals("2013")) {
                user2013.add(u);
            }
            if (year.equals("2014")) {
                user2014.add(u);
            }
            if (year.equals("2015")) {
                user2015.add(u);
            }
            if (year.equals("2016")) {
                user2016.add(u);
            }
            if (year.equals("2017")) {
                user2017.add(u);
            }
        }
        //if user request for gender 
        if (countGender == true) {
            int[] genderArr1 = new int[2];
            genderArr1[0] = 0;
            genderArr1[1] = 0;
            for (User u : user2013) {
                char sex = u.getGender();
                if (sex == 'f' || sex == 'F') {
                    genderArr1[0]++;
                }
                if (sex == 'm' || sex == 'M') {
                    genderArr1[1]++;
                }
            }
            request.setAttribute("2013F", genderArr1[0]);
            request.setAttribute("2013M", genderArr1[1]);

            int[] genderArr2 = new int[2];
            genderArr2[0] = 0;
            genderArr2[1] = 0;
            for (User u : user2014) {
                char sex = u.getGender();
                if (sex == 'f' || sex == 'F') {
                    genderArr2[0]++;
                }
                if (sex == 'm' || sex == 'M') {
                    genderArr2[1]++;
                }
            }
            request.setAttribute("2014F", genderArr2[0]);
            request.setAttribute("2014M", genderArr2[1]);

            int[] genderArr3 = new int[2];
            genderArr3[0] = 0;
            genderArr3[1] = 0;
            for (User u : user2015) {
                char sex = u.getGender();
                if (sex == 'f' || sex == 'F') {
                    genderArr3[0]++;
                }
                if (sex == 'm' || sex == 'M') {
                    genderArr3[1]++;
                }
            }
            request.setAttribute("2015F", genderArr3[0]);
            request.setAttribute("2015M", genderArr3[1]);

            int[] genderArr4 = new int[2];
            genderArr4[0] = 0;
            genderArr4[1] = 0;
            for (User u : user2016) {
                char sex = u.getGender();
                if (sex == 'f' || sex == 'F') {
                    genderArr4[0]++;
                }
                if (sex == 'm' || sex == 'M') {
                    genderArr4[1]++;
                }
            }
            request.setAttribute("2016F", genderArr4[0]);
            request.setAttribute("2016M", genderArr4[1]);

            int[] genderArr5 = new int[2];
            genderArr5[0] = 0;
            genderArr5[1] = 0;
            for (User u : user2017) {
                char sex = u.getGender();
                if (sex == 'f' || sex == 'F') {
                    genderArr5[0]++;
                }
                if (sex == 'm' || sex == 'M') {
                    genderArr5[1]++;
                }
            }
            request.setAttribute("2017F", genderArr5[0]);
            request.setAttribute("2017M", genderArr5[1]);
        }

        //if user request for school
        HashMap<String, Integer> schoolMap = new HashMap<>();
        for (int i = 2013; i <= 2017; i++) {
            schoolMap.put(i + "business", 0);
            schoolMap.put(i + "sis", 0);
            schoolMap.put(i + "socsc", 0);
            schoolMap.put(i + "economics", 0);
            schoolMap.put(i + "accountancy", 0);
            schoolMap.put(i + "law", 0);
        }

        if (countSchool == true) {
            for (User u : user2013) {
                String school = u.getSchool();
                String yrSchool = "2013" + school;
                int count = schoolMap.get(yrSchool) + 1;
                schoolMap.put(yrSchool, count);
            }

            for (User u : user2014) {
                String school = u.getSchool();
                String yrSchool = "2014" + school;
                int count = schoolMap.get(yrSchool) + 1;
                schoolMap.put(yrSchool, count);
            }

            for (User u : user2015) {
                String school = u.getSchool();
                String yrSchool = "2015" + school;
                int count = schoolMap.get(yrSchool) + 1;
                schoolMap.put(yrSchool, count);
            }

            for (User u : user2016) {
                String school = u.getSchool();
                String yrSchool = "2016" + school;
                int count = schoolMap.get(yrSchool) + 1;
                schoolMap.put(yrSchool, count);
            }

            for (User u : user2017) {
                String school = u.getSchool();
                String yrSchool = "2017" + school;
                int count = schoolMap.get(yrSchool) + 1;
                schoolMap.put(yrSchool, count);
            }

            Iterator iterKeys2 = schoolMap.keySet().iterator();
            while (iterKeys2.hasNext()) {
                String yrSchool = (String) iterKeys2.next();
                int count = schoolMap.get(yrSchool);
                request.setAttribute(yrSchool, count);
            }
        }
    }

    /**
     *break down by gender
     * @param request
     * @param response
     * @param users
     * @param countYear
     * @param countSchool
     */
    protected void genderBreakdown(HttpServletRequest request, HttpServletResponse response, ArrayList<User> users, boolean countYear, boolean countSchool) {
        int[] genderArr = new int[2];
        genderArr[0] = 0;
        genderArr[1] = 0;
        ArrayList<User> userFemale = new ArrayList<>();
        ArrayList<User> userMale = new ArrayList<>();
        for (User u : users) {
            char sex = u.getGender();
            if (sex == 'f' || sex == 'F') {
                genderArr[0]++;
                userFemale.add(u);
            }
            if (sex == 'm' || sex == 'M') {
                genderArr[1]++;
                userMale.add(u);
            }
        }
        request.setAttribute("numFemales", genderArr[0]);
        request.setAttribute("numMales", genderArr[1]);

        if (countYear == true) {
            HashMap<String, Integer> yearMap = new HashMap<>();
            for (int i = 2013; i <= 2017; i++) {
                yearMap.put("Female" + i, 0);
                yearMap.put("Male" + i, 0);
            }
            for (User u : userFemale) {
                String GenderYear = "Female" + u.getYear();
                int count = yearMap.get(GenderYear) + 1;
                yearMap.put(GenderYear, count);
            }
            for (User u : userMale) {
                String GenderYear = "Male" + u.getYear();
                int count = yearMap.get(GenderYear) + 1;
                yearMap.put(GenderYear, count);
            }
            Iterator iterKeys = yearMap.keySet().iterator();
            while (iterKeys.hasNext()) {
                String yr = (String) iterKeys.next();
                int count = yearMap.get(yr);
                request.setAttribute(yr, count);
            }
        }

        if (countSchool == true) {
            HashMap<String, Integer> schoolMap = new HashMap<>();
            ArrayList<String> schools = new ArrayList<>();
            schools.add("business");
            schools.add("sis");
            schools.add("socsc");
            schools.add("economics");
            schools.add("accountancy");
            schools.add("law");
            for (int i = 0; i <= 5; i++) {
                schoolMap.put("Female" + schools.get(i), 0);
                schoolMap.put("Male" + schools.get(i), 0);
            }
            for (User u : userFemale) {
                String GenderSchool = "Female" + u.getSchool();
                int count = schoolMap.get(GenderSchool) + 1;
                schoolMap.put(GenderSchool, count);
            }
            for (User u : userMale) {
                String GenderSchool = "Male" + u.getSchool();
                int count = schoolMap.get(GenderSchool) + 1;
                schoolMap.put(GenderSchool, count);
            }
            Iterator iterKeys = schoolMap.keySet().iterator();
            while (iterKeys.hasNext()) {
                String yr = (String) iterKeys.next();
                int count = schoolMap.get(yr);
                request.setAttribute(yr, count);
            }
        }
    }

    /**
     *break down by school
     * @param request
     * @param response
     * @param users
     * @param countYear
     * @param countGender
     */
    protected void schoolBreakdown(HttpServletRequest request, HttpServletResponse response, ArrayList<User> users, boolean countYear, boolean countGender) {
        HashMap<String, Integer> schoolMap = new HashMap<>();
        for (User u : users) {
            String school = u.getSchool();
            if (!schoolMap.containsKey(school)) {
                schoolMap.put(school, 1);
            } else {
                int count = schoolMap.get(school) + 1;
                schoolMap.put(school, count);
            }
        }
        Iterator iterKeys = schoolMap.keySet().iterator();
        while (iterKeys.hasNext()) {
            String school = (String) iterKeys.next();
            int count = schoolMap.get(school);
            request.setAttribute(school, count);
        }
        ArrayList<String> schools = new ArrayList<>();
        schools.add("business");
        schools.add("sis");
        schools.add("socsc");
        schools.add("economics");
        schools.add("accountancy");
        schools.add("law");
        if (countYear == true) {
            HashMap<String, Integer> yearMap = new HashMap<>();
            for (int i = 2013; i <= 2017; i++) {
                for (int j = 0; j <= 5; j++) {
                    yearMap.put(schools.get(j) + i, 0);
                }
            }

            for (User u : users) {
                String schoolYr = u.getSchool() + u.getYear();
                int count = yearMap.get(schoolYr) + 1;
                yearMap.put(schoolYr, count);
            }

            Iterator iterKeys2 = yearMap.keySet().iterator();
            while (iterKeys2.hasNext()) {
                String school = (String) iterKeys2.next();
                int count = yearMap.get(school);
                request.setAttribute(school, count);
            }
        }

        if (countGender == true) {
            HashMap<String, Integer> genderMap = new HashMap<>();
            for (int i = 0; i <= 5; i++) {
                genderMap.put(schools.get(i) + "Female", 0);
                genderMap.put(schools.get(i) + "Male", 0);
            }
            for (User u : users) {
                char gender = u.getGender();
                if (gender == 'f' || gender == 'F') {
                    String schoolGender = u.getSchool() + "Female";
                    int count = genderMap.get(schoolGender) + 1;
                    genderMap.put(schoolGender, count);
                } else {
                    String schoolGender = u.getSchool() + "Male";
                    int count = genderMap.get(schoolGender) + 1;
                    genderMap.put(schoolGender, count);
                }
            }

            Iterator iterKeys2 = genderMap.keySet().iterator();
            while (iterKeys2.hasNext()) {
                String school = (String) iterKeys2.next();
                int count = genderMap.get(school);
                request.setAttribute(school, count);
            }
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
