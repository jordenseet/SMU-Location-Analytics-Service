/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import DAO.UserDAO;
import Entity.User;
import Utilities.DAOUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "BreakdownServlet", urlPatterns = {"/BreakdownServlet"})
public class BreakdownServlet extends HttpServlet {

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
        String datetime = request.getParameter("date");
        String order = "";
        String field1 = request.getParameter("field1");

        String field2 = request.getParameter("field2");

        String field3 = request.getParameter("field3");
        
        if (field2.equals("- Select -") && !field3.equals("- Select -")){
            request.setAttribute("error", "invalid order");
        }
        
        else{
            if (!field1.equals("- Select -") && !field2.equals("- Select -") && !field3.equals("- Select -")){
                order= (field1 + ", " + field2 + ", " + field3);
            }
            else if (!field1.equals("- Select -") && !field2.equals("- Select -") && field3.equals("- Select -")){
                order = (field1 + ", " + field2);
            }
            else{
                order = field1;
            }
        }
        
        request.setAttribute("order",order);
        if (datetime != null) {
            datetime = DAOUtility.formatDatetime(datetime);
            ArrayList<User> users = UserDAO.retrieveUsersInWindow(datetime);
            TreeMap<String, TreeMap<String, ArrayList<User>>> breakdown = new TreeMap<>();
            TreeMap<String, ArrayList<User>> userMap = new TreeMap<>();
            TreeMap<String, ArrayList<User>> map1 = null;
            TreeMap<String, ArrayList<User>> map2 = null;
            TreeMap<String, ArrayList<User>> map3 = null;
            userMap.put("first", users);
            request.setAttribute("field1", field1);
            request.setAttribute("field2", field2);
            request.setAttribute("field3", field3);
            request.setAttribute("total", users.size());
            switch (field1) {
                case "Year":
                    map1 = breakdownByYear(userMap);
                    breakdown.put(field1, map1);
                    switch (field2) {
                        case "Gender":
                            map2 = breakdownByGender(map1);
                            breakdown.put(field2, map2);
                            switch (field3) {
                                case "School":
                                    map3 = breakdownBySchool(map2);
                                    breakdown.put(field3, map3);
                                    break;
                            }
                            break;
                        case "School":
                            map2 = breakdownBySchool(map1);
                            breakdown.put(field2, map2);
                            switch (field3) {
                                case "Gender":
                                    map3 = breakdownByGender(map2);
                                    breakdown.put(field3, map3);
                                    break;
                            }
                            break;
                    }
                    break;
                case "Gender":
                    map1 = breakdownByGender(userMap);
                    breakdown.put(field1, map1);
                    switch (field2) {
                        case "Year":
                            map2 = breakdownByYear(map1);
                            breakdown.put(field2, map2);
                            switch (field3) {
                                case "School":
                                    map3 = breakdownBySchool(map2);
                                    breakdown.put(field3, map3);
                                    break;
                            }
                            break;
                        case "School":
                            map2 = breakdownBySchool(map1);
                            breakdown.put(field2, map2);
                            switch (field3) {
                                case "Year":
                                    map3 = breakdownByYear(map2);
                                    breakdown.put(field3, map3);
                                    break;
                            }
                            break;
                    }
                    break;
                case "School":
                    map1 = breakdownBySchool(userMap);
                    breakdown.put(field1, map1);
                    switch (field2) {
                        case "Year":
                            map2 = breakdownByYear(map1);
                            breakdown.put(field2, map2);
                            switch (field3) {
                                case "Gender":
                                    map3 = breakdownByGender(map2);
                                    breakdown.put(field3, map3);
                                    break;
                            }
                            break;
                        case "Gender":
                            map2 = breakdownByGender(map1);
                            breakdown.put(field2, map2);
                            switch (field3) {
                                case "Year":
                                    map3 = breakdownByYear(map2);
                                    breakdown.put(field3, map3);
                                    break;
                            }
                            break;
                    }
                    break;
                default: request.setAttribute("error", "invalid order");
            }
            request.setAttribute("breakdown", breakdown);
        } else {
            request.setAttribute("error", "Invalid datetime");
        }
        request.getRequestDispatcher("/breakdown.jsp").forward(request, response);
    }
    
    /**
     *
     * @param map
     * @return
     */
    protected TreeMap<String, ArrayList<User>> breakdownByYear(TreeMap<String, ArrayList<User>> map) {
        // if first - "2013": users, "2014": users, etc.
        // if second - "gender/sch 2013": users, "gender/sch 2014": users, etc
        // if third - "gender/sch sch/gender 2013": users, "gender/sch sch/gender 2014": users, etc
        TreeMap<String, ArrayList<User>> yearMap = new TreeMap<>();
        boolean firstTime = false;
        Iterator<String> iterKeys = map.keySet().iterator();
        while (iterKeys.hasNext()) {
            String key = iterKeys.next();
            ArrayList<User> users = map.get(key);
            if (users.isEmpty()){
                for (String emptyYear: getKeyList("Year")){
                    String emptyNewKey = key + emptyYear;
                    yearMap.put(emptyNewKey, new ArrayList<>());
                }
            }
            for (User u : users) {
                String year = u.getYear();
                ArrayList<User> yearList = new ArrayList<>();
                String newKey = year;
                if (key.equals("first")) {
                    firstTime = true;
                } else {
                    newKey = key + " " + year;
                }
                if (yearMap.containsKey(newKey)) {
                    yearList = yearMap.get(newKey);
                }
                yearList.add(u);
                yearMap.put(newKey, yearList);
            }
        }
        ArrayList<String> keyList = getKeyList("Year");
        Set<String> keySet = yearMap.keySet();
        for (String k : keyList) {
            boolean hasK = false;
            for (String s : keySet) {
                if (s.contains(k)) {
                    hasK = true;
                }
            }
            if (!hasK) {
                if (!firstTime) {
                    String s = yearMap.keySet().iterator().next();
                    k = s.substring(0, s.length()-5) + k;
                }
                yearMap.put(k, new ArrayList<>());
            }
        }
        return yearMap;
    }
    
    /**
     *
     * @param map
     * @return
     */
    protected TreeMap<String, ArrayList<User>> breakdownByGender(TreeMap<String, ArrayList<User>> map) {
        // if first - "F": users, "M": users, etc.
        // if second - "yr/sch F": users, "yr/sch M": users, etc
        // if third - "yr/sch sch/yr F": users, "yr/sch sch/yr M": users, etc
        TreeMap<String, ArrayList<User>> genderMap = new TreeMap<>();
        boolean firstTime = false;
        Iterator<String> iterKeys = map.keySet().iterator();
        while (iterKeys.hasNext()) {
            String key = iterKeys.next();
            ArrayList<User> users = map.get(key);
            if (users.isEmpty()){
                for (String emptyGender: getKeyList("Gender")){
                    String emptyNewKey = key + emptyGender;
                    genderMap.put(emptyNewKey, new ArrayList<>());
                }
            }
            for (User u : users) {
                String gender = u.getGender() + "";
                ArrayList<User> genderList = new ArrayList<>();
                String newKey = gender;
                if (key.equals("first")) {
                    firstTime = true;
                } else {
                    newKey = key + " " + gender;
                }
                if (genderMap.containsKey(newKey)) {
                    genderList = genderMap.get(newKey);
                }
                genderList.add(u);
                genderMap.put(newKey, genderList);
            }
        }
        ArrayList<String> keyList = getKeyList("Gender");
        Set<String> keySet = genderMap.keySet();
        for (String k : keyList) {
            boolean hasK = false;
            for (String s : keySet) {
                if (s.contains(k)) {
                    hasK = true;
                }
            }
            if (!hasK) {
                if (!firstTime) {
                    String s = genderMap.keySet().iterator().next();
                    k = s.substring(0, s.length()-2) + k;
                }
                genderMap.put(k, new ArrayList<>());
            }
        }
        return genderMap;
    }
    
    /**
     *
     * @param map
     * @return
     */
    protected TreeMap<String, ArrayList<User>> breakdownBySchool(TreeMap<String, ArrayList<User>> map) {
        // if first - "biz": users, "acc": users, etc.
        // if second - "yr/sex biz": users, "yr/sex acc": users, etc
        // if third - "yr/sex sex/yr biz": users, "yr/sex sex/yr acc": users, etc
        TreeMap<String, ArrayList<User>> schoolMap = new TreeMap<>();
        boolean firstTime = false;
        Iterator<String> iterKeys = map.keySet().iterator();
        while (iterKeys.hasNext()) {
            String key = iterKeys.next();
            System.out.println("Year is " + key);
            ArrayList<User> users = map.get(key);
            if (users.isEmpty()){
                for (String emptySchool: getKeyList("School")){
                    String emptyNewKey = key + emptySchool;
                    schoolMap.put(emptyNewKey, new ArrayList<>());
                }
            }
            for (User u : users) {
                String school = u.getSchool();
                ArrayList<User> schoolList = new ArrayList<>();
                String newKey = school;
                if (key.equals("first")) {
                    firstTime = true;
                } else {
                    newKey = key + " " + school;
                }
                if (schoolMap.containsKey(newKey)) {
                    schoolList = schoolMap.get(newKey);
                }
                schoolList.add(u);
                schoolMap.put(newKey, schoolList);
            }
        }
        ArrayList<String> keyList = getKeyList("School");
        Set<String> keySet = schoolMap.keySet();
        for (String k : keyList) {
            boolean hasK = false;
            for (String s : keySet) {
                if (s.contains(k)) {
                    hasK = true;
                }
            }
            if (!hasK) {
                if (!firstTime) {
                    String s = schoolMap.keySet().iterator().next();
                    k = s.substring(0, 7) + k;
                }
                schoolMap.put(k, new ArrayList<>());
            }
        }
        return schoolMap;
    }

    /**
     *
     * @param field
     * @return
     */
    protected ArrayList<String> getKeyList(String field) {
        ArrayList<String> keyList = new ArrayList<>();
        switch (field) {
            case "Year":
                for (int i = 2013; i <= 2017; i++) {
                    keyList.add("" + i);
                }
                break;
            case "Gender":
                keyList.add("F");
                keyList.add("M");
                break;
            case "School":
                // business, accountancy, sis, economics, law, or socsc
                keyList.add("business");
                keyList.add("accountancy");
                keyList.add("sis");
                keyList.add("economics");
                keyList.add("law");
                keyList.add("socsc");
                break;
        }
        return keyList;
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
