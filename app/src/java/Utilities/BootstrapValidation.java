/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import DAO.LocationUpdateDAO;
import DAO.SemanticPlaceDAO;
import DAO.UserDAO;
import Entity.LocationUpdate;
import Entity.SemanticPlace;
import Entity.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "BootstrapValidation", urlPatterns = {"/BootstrapValidation"})
public class BootstrapValidation extends HttpServlet {

    private static ArrayList<Integer> validLocationIds = new ArrayList<Integer>();
    //Location Ids that are in location-lookup and have been validated will be stored here

    /**
     * validate users in CSV file
     *
     * @param OUTPUTDIRECTORY
     * @param isBootstrap
     * @param tempDemoFile
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public ArrayList[] validateUserCSV(String OUTPUTDIRECTORY, boolean isBootstrap, File tempDemoFile) throws IOException, SQLException {
        File demographicscsvFile = new File(OUTPUTDIRECTORY + "/" + "demographics.csv");
        //To store the data
        ArrayList<ArrayList<String>> userErrors = new ArrayList<>();
        //To store all userErrors
        ArrayList<String> userLineNumbers = new ArrayList<>();
        //To store the line numbers of userErrors
        ArrayList[] answer = new ArrayList[3];
        //Answer will be the returned object
        int counter = 1;
        //Counter is used to see the line number
        
        ArrayList<User> users = new ArrayList();
        //To store user objects that are validated
        if (demographicscsvFile.exists()) {
            CSVReader reader = new CSVReader(new FileReader(demographicscsvFile));
            UserDAO userDAO = new UserDAO();
            if (isBootstrap) {
                userDAO.deleteTable();
                //Only truncate database if it is a bootstrap
            }

            String[] nextUser; //reads next user of demographics.csv
            boolean blankField = false;
            reader.readNext(); //skip header
            while ((nextUser = reader.readNext()) != null) {
                counter++;
                ArrayList<String> errorMessages = new ArrayList<>();
                //For storing errorMessages that may be concatenated together
                blankField = false;
                for (String field : nextUser) {
                    if (field.equals("") || field.length() == 0) {
                        blankField = true;
                        //All fields are blank
                        errorMessages.add("mac-address is blank, name is blank, password is blank, email is blank, gender is blank");
                    }
                }
                if (blankField != true) {

                    String mac = nextUser[0].trim();
                    String name = nextUser[1].trim();
                    String password = nextUser[2].trim();
                    String email = nextUser[3].trim();
                    String gender = nextUser[4].trim();
                    char sex = gender.charAt(0);//trim method doesnt work while method chaining?

                    boolean macTest = (!mac.isEmpty());
                    //Check for blank field
                    boolean nameTest = (!name.isEmpty());
                    //Check for blank field
                    boolean passwordTest = (!password.isEmpty());
                    //Check for blank field
                    boolean emailTest = (!email.isEmpty());
                    //Check for blank field
                    boolean genderTest = (!gender.isEmpty());
                    //Check for blank field

                    User user = new User(mac, name, password, email, sex);
                    //creation of user object as validation methods are in user class

                    if (!macTest || !nameTest || !passwordTest || !emailTest || !genderTest) {
                        //if blankfields exist
                        System.out.println("line 109");
                        int blankCounter = 0;
                        if (!macTest) {
                            errorMessages.add("mac-address is blank");
                            blankCounter++;
                        }
                        if (!nameTest) {
                            if (blankCounter == 0) {
                                errorMessages.add("name is blank");
                                blankCounter++;
                            } else {
                                errorMessages.add(", name is blank");
                                //if there is already an error message to concatenate to
                                blankCounter++;
                            }

                            if (!passwordTest) {
                                if (blankCounter == 0) {
                                    errorMessages.add("password is blank");
                                    blankCounter++;
                                } else {
                                    errorMessages.add(", password is blank");
                                    //if there is already an error message to concatenate to
                                    blankCounter++;
                                }
                            }

                            if (!emailTest) {
                                if (blankCounter == 0) {
                                    errorMessages.add("email is blank");
                                    blankCounter++;
                                } else {
                                    errorMessages.add(", email is blank");
                                    //if there is already an error message to concatenate to
                                    blankCounter++;
                                }
                            }
                            if (!genderTest) {
                                if (blankCounter == 0) {
                                    errorMessages.add("gender is blank");
                                    blankCounter++;
                                } else {
                                    errorMessages.add(", gender is blank");
                                    //if there is already an error message to concatenate to
                                    blankCounter++;
                                }
                            }

                        }

                    } else if (macTest && nameTest && passwordTest && emailTest && genderTest) {
                        //only do these checks if there are no blank fields

                        if (!user.validateMacAddress()) {
                            errorMessages.add("invalid mac address");
                        }
                        if (!user.validatePassword()) {
                            errorMessages.add("invalid password");
                        }
                        if (!user.validateEmail()) {
                            errorMessages.add("invalid email");
                        }
                        if (!user.validateGender()) {
                            errorMessages.add("invalid gender");
                        }
                    }
                    if (errorMessages.size() > 0) {
                        //store errormessages and line number of error messages
                        userLineNumbers.add("" + counter);
                        userErrors.add(errorMessages);
                    } else {
                        //if there are no errors, add the user to users arraylist
                        users.add(user);
                    }
                }
            }
            reader.close();

            Iterator iter = users.iterator();
            BufferedWriter bw = null;

            try {

                //Specify the file name and path here
                /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
                if (!tempDemoFile.exists()) {
                    tempDemoFile.createNewFile();
                }

                FileWriter fw = new FileWriter(tempDemoFile);
                bw = new BufferedWriter(fw);

                while (iter.hasNext()) {
                    User user = (User) iter.next();
                    //get mac-address
                    String macAddress = user.getMacAddress();
                    //get name
                    String name = user.getName();
                    //get password
                    String password = user.getPassword();
                    //get email 
                    String email = user.getEmail();
                    //get gender
                    char gender = user.getGender();
                    //change gender to String
                    String toWrite = macAddress + "," + name + "," + password + "," + email + "," + gender;

                    bw.write(toWrite);
                    //Write the correct users to a new file which will be inserted to database
                    bw.newLine();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Exception ex) {
                    System.out.println("Error in closing the BufferedWriter" + ex);
                }
            }
            userDAO = new UserDAO();

            userDAO.insert(tempDemoFile.getAbsolutePath());
            //Inserts into database

            demographicscsvFile.delete();
            //To prevent this file from being called during future bootstraps/additional
        }

        answer[0] = userErrors;
        answer[1] = userLineNumbers;
        int number = users.size();

        ArrayList<Integer> loaded = new ArrayList<>();
        loaded.add(number);
        answer[2] = loaded;
        //Returning all the correct information to bootstrap servlet
        return answer;
    }

    /**
     * validate semantic places in CSV file
     *
     * @param OUTPUTDIRECTORY
     * @param tempSemanticFile
     * @return ArrayList[] of semantic place results
     * @throws IOException
     * @throws SQLException
     */
    public ArrayList[] validateSemanticPlaceCSV(String OUTPUTDIRECTORY, File tempSemanticFile) throws IOException, SQLException {

        File semanticcsvFile = new File(OUTPUTDIRECTORY + "/" + "location-lookup.csv");
        ArrayList<ArrayList<String>> semanticErrors = new ArrayList<>();
        //All error messages for location-lookup.csv will be stored here
        ArrayList<String> semanticLineNumbers = new ArrayList<>();
        //All error message line numbers will be stored here
        ArrayList[] answer = new ArrayList[3];
        //answer is the object to be returned
        int counter = 1;
        //counter to record line numbers
        int max = Integer.MAX_VALUE;
        int maxDividedBy10 = max/10;
        //This will be used to check whether locationIds exceed the length of a positive Integer value
        
        ArrayList<SemanticPlace> places = new ArrayList();
        //To store all semantic place objects that are validated

        if (semanticcsvFile.exists()) {
            CSVReader reader = new CSVReader(new FileReader(semanticcsvFile));
            SemanticPlaceDAO semanticPlaceDAO = new SemanticPlaceDAO();
            semanticPlaceDAO.deleteTable();
            //Truncate all existing data in database
            boolean blankField = false;
            //for checking blankfields later
            String[] nextSemanticPlace;
            reader.readNext(); //skip header
            while ((nextSemanticPlace = reader.readNext()) != null) {
                counter++;
                ArrayList<String> errorMessages = new ArrayList<>();
                blankField = false;
                for (String field : nextSemanticPlace) {
                    if (field.equals("") || field.length() == 0) {
                        blankField = true;
                        errorMessages.add("location-id is blank, semantic-place is blank");
                        //if whole row is blank
                    }
                }
                if (!blankField) {
                    int locIdCheck = 0;
                    String locationIdStr = nextSemanticPlace[0].trim();
                    if (locationIdStr.indexOf(',') != -1) {
                        locIdCheck = 1;
                        locationIdStr = locationIdStr.replaceAll(",", "");
                        //This prevents an exception from being thrown, error message will be recorded later
                        //Necessary as validation methods are in semanticPlace object class
                    }
                    //To ensure that the locationId doesn't exceed an integer value
                    boolean testForIntegerSize1 = locationIdStr.length() <= 10;
                    boolean testForIntegerSize2 = false;
                    boolean toRecordWronglocationId = false;
                    //toRecordWrongLocationId will record if the locationId is wrong because our validation is in semanticPlace Object
                    if (locationIdStr.length() == 10){
                        int testLocationId = Integer.parseInt(locationIdStr.substring(0, locationIdStr.length()-1));
                        //substring to length -1 as if the value exceeds max int, it cannot be stored in an int object
                        if (maxDividedBy10 >= testLocationId){
                            //Object is of length 10 but still smaller than the max int
                            testForIntegerSize2 = true;
                        }
                    }
                    if (!testForIntegerSize1 || !testForIntegerSize2){
                        locationIdStr = locationIdStr.substring(0,9);
                        toRecordWronglocationId = true;
                    }
                    
                    int locationId = Integer.parseInt(locationIdStr);
                    //location id is stored as an int in the semantic place obj
                    String sp = nextSemanticPlace[1].trim();

                    SemanticPlace semanticPlace = new SemanticPlace(locationId, sp);
                    boolean locationIdTest = (!locationIdStr.isEmpty());
                    boolean semanticPlaceTest = (!sp.isEmpty());

                    if (!locationIdTest || !semanticPlaceTest) {
                        //if either of them are blank
                        int blankCounter = 0;
                        if (!locationIdTest) {
                            errorMessages.add("location-id is blank");
                            blankCounter++;
                        }
                        if (!semanticPlaceTest) {
                            if (blankCounter == 0) {
                                errorMessages.add("semantic-place is blank");
                            } else {
                                errorMessages.add(", semantic-place is blank");
                                //to concatenate the string if location-id is blank as well
                            }
                        }
                    } else if (locationIdTest || semanticPlaceTest) {

                        if (!semanticPlace.validLocationId(locationId) || locIdCheck > 0 || toRecordWronglocationId) {
                            errorMessages.add("invalid location id");
                        }
                        if (!semanticPlace.validName(sp)) {
                            errorMessages.add("invalid semantic place");
                        }
                        
                    }
                    if (errorMessages.size() > 0) {
                        // if there are error messages, record the line number and error messages
                        semanticLineNumbers.add("" + counter);
                        semanticErrors.add(errorMessages);
                    } else {
                        //adds the validated locationId to the list of validated location ids
                        validLocationIds.add(locationId);
                        //Adds the semantic place to the list of validated semantic places
                        places.add(semanticPlace);
                    }
                    
                }
            }
            reader.close();

            Collections.sort(validLocationIds);

            BufferedWriter bw = null;
            try {

                if (!tempSemanticFile.exists()) {
                    tempSemanticFile.createNewFile();
                }

                FileWriter fw = new FileWriter(tempSemanticFile);
                bw = new BufferedWriter(fw);

                for (int i = 0; i < places.size(); i++) {
                    String toWrite = "";
                    String locatId = places.get(i).getLocationId() + "";
                    String name = places.get(i).getName();

                    toWrite += locatId + ",";
                    toWrite += name + ",";
                    bw.write(toWrite);
                    bw.newLine();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Exception ex) {
                    System.out.println("Error in closing the BufferedWriter" + ex);
                }
            }

            semanticPlaceDAO.insert(tempSemanticFile.getAbsolutePath());

            semanticcsvFile.delete();
        }

        answer[0] = semanticErrors;
        answer[1] = semanticLineNumbers;
        int number = places.size();

        ArrayList<Integer> loaded = new ArrayList<>();
        loaded.add(number);
        answer[2] = loaded;
        return answer;
        //returning all the results
    }

    /**
     * this method does for bootstrap file, if it is true, then it will return
     * all the valid location ids
     *
     * @param isBootstrap
     * @return valid location ids
     */
    public ArrayList<Integer> returnValidLocationids(boolean isBootstrap) {
        return validLocationIds;
    }

    //Boolean isBootstrap checks whether the method is used for bootstrap or additional File Uploader
    /**
     * validates location csv file
     *
     * @param OUTPUTDIRECTORY
     * @param isBootstrap
     * @param tempLocationFile
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public ArrayList[] validateLocationCSV(String OUTPUTDIRECTORY, boolean isBootstrap, File tempLocationFile) throws IOException, SQLException {
        File locationcsvFile = new File(OUTPUTDIRECTORY + "/" + "location.csv");
        ArrayList<ArrayList<String>> locationErrors = new ArrayList<>();
        ArrayList<String> locationLineNumbers = new ArrayList<>();
        ArrayList<LocationUpdate> places = new ArrayList();
        ArrayList[] answer = new ArrayList[3];
        int counter = 1;
        
        int max = Integer.MAX_VALUE;
        int maxDividedBy10 = max/10;
        
        HashMap<String, Integer> locations = new HashMap();
        if (locationcsvFile.exists()) {
            CSVReader reader = new CSVReader(new FileReader(locationcsvFile));
            LocationUpdateDAO locationUpdateDAO = new LocationUpdateDAO();
            boolean blankField = false;
            if (isBootstrap) {
                locationUpdateDAO.deleteTable();
            } else {
                validLocationIds = SemanticPlaceDAO.retrieveAllValidatedLocationIds();
                //As validLocationIds arraylist won't be available otherwise if no location-lookup.csv was uploaded
            }

            HashMap<String, Integer> dbLocations = LocationUpdateDAO.retrieveAllValidatedLocationsDetails();
            //Retrieves all existing locations in database. Key is timestamp+macaddress, value is location id

            String[] nextLocationUpdate; //reads next location of location.csv
            reader.readNext(); //skip header
            while ((nextLocationUpdate = reader.readNext()) != null) {
                blankField = false;
                ArrayList<String> errorMessages = new ArrayList<>();
                //For storing error messages
                counter++;
                //for recording the line number in the file
                for (String field : nextLocationUpdate) {
                    if (field.equals("") || field.length() == 0) {
                        //All fields are blank
                        blankField = true;
                        errorMessages.add("timestamp is blank, mac-address is blank, location-id is blank");
                    }
                }
                if (!blankField) {
                    String timeStamp = nextLocationUpdate[0].trim();
                    String macAddress = nextLocationUpdate[1].trim();
                    int locIdCheck = 0;
                    String locationIdStr = nextLocationUpdate[2].trim();
                    if (locationIdStr.indexOf(',') != -1) {
                        locIdCheck = 1;
                        locationIdStr = locationIdStr.replaceAll(",", "");
                        //necessary as we need to form a location object to call our validation methods, error message will be handled later
                    }
                    
                    boolean testForIntegerSize1 = locationIdStr.length() <= 10;
                    boolean testForIntegerSize2 = false;
                    boolean toRecordWronglocationId = false;
                    //toRecordWrongLocationId will record if the locationId is wrong because our validation is in semanticPlace Object
                    if (locationIdStr.length() == 10){
                        int testLocationId = Integer.parseInt(locationIdStr.substring(0, locationIdStr.length()-1));
                        //substring to length -1 as if the value exceeds max int, it cannot be stored in an int object
                        if (maxDividedBy10 >= testLocationId){
                            //Object is of length 10 but still smaller than the max int
                            testForIntegerSize2 = true;
                        }
                    }
                    if (!testForIntegerSize1 || !testForIntegerSize2){
                        locationIdStr = locationIdStr.substring(0,9);
                        toRecordWronglocationId = true;
                    }
                    
                    int locationId = Integer.parseInt(locationIdStr);
                    String key = timeStamp + macAddress;
                    //For comparing hashmaps later to look for duplicate locationIds

                    LocationUpdate location = new LocationUpdate(timeStamp, macAddress, locationId);
                    boolean timeStampTest = (!timeStamp.isEmpty());
                    boolean macAddressTest = (!macAddress.isEmpty());
                    boolean locationIdTest = (!locationIdStr.isEmpty());
                    boolean duplicateRow = false;

                    if (!timeStampTest || !macAddressTest || !locationIdTest) {
                        //checking and recording blank field errors
                        int blankCounter = 0;
                        if (!timeStampTest) {
                            errorMessages.add("time-stamp is blank");
                            blankCounter++;
                        }
                        if (!macAddressTest) {
                            if (blankCounter == 0) {
                                errorMessages.add("mac-address is blank");
                            } else {
                                errorMessages.add(", mac-address is blank");
                            }
                        }
                        if (!locationIdTest) {
                            if (blankCounter == 0) {
                                errorMessages.add("location-id is blank");
                            } else {
                                errorMessages.add(", location-id is blank");
                            }
                        }
                    } else if (timeStampTest || macAddressTest || locationIdTest) {
                        
                        boolean locationIsValid = location.validateLocation(validLocationIds);
                        //checks for a valid location
                        boolean macAddressIsValid = location.validateMacAddress();
                        //checks for valid mac Address
                        boolean timeStampIsValid = location.validateTimeStamp();
                        

                        if (!timeStampIsValid) {
                            errorMessages.add("invalid timestamp");

                        }
                        if (!macAddressIsValid) {
                            errorMessages.add("invalid mac address");

                        }
                        if (!locationIsValid || locIdCheck > 0 || toRecordWronglocationId) {
                            errorMessages.add("invalid location");

                        }

                        
                        if (locations.containsKey(key) && (timeStampIsValid) && (macAddressIsValid) && (locationIsValid)) {
                                duplicateRow = true;
                                //removing duplicate locationIds from within the csv file
                                int locationId2 = locations.get(key);
                                //locationId2 is the one already existing in the locations hashmap
                                    errorMessages.add("duplicate row");
                                    System.out.println("Error message: " + errorMessages + "Line Number: " + counter);
                                    System.out.println(locationId);
                                if (locationId2 != locationId){
                                    //replace the existing location Id with the latest row
                                    String timeStampForHash = location.getTimeStamp();
                                    String macAddressForHash = location.getMacAddress();
                                    int locationIdForHash = location.getLocationId();
                                    //Creation of hashMap for duplicate checking
                                    String keyForHash = timeStampForHash+macAddressForHash;
                                    locations.put(keyForHash,locationIdForHash);
                                }
                                
                        }
                        
                        if (!isBootstrap && (timeStampIsValid) && (macAddressIsValid) && (locationIsValid)) {
                            if (dbLocations.containsKey(key)) {
                                //comparing locationIds in the database to the new ones
                                errorMessages.add("duplicate row");
                                //No need to replace the one in database as for additional, the ones in additional file are discared
                            }

                        }
                    }
                    if (errorMessages.size() > 0) {
                        locationErrors.add(errorMessages);
                        //adding error messages 
                        if (!duplicateRow){
                        locationLineNumbers.add("" + counter);
                        //adding line numbers for error messages
                        }else{
                            //Because we use batch insert, the amount recorded is not automatically reduced
                            int counterWithDuplicates = counter-1;
                            locationLineNumbers.add("" + counterWithDuplicates);
                        }
                        
                    } else {
                        String timeStampForHash = location.getTimeStamp();
                        String macAddressForHash = location.getMacAddress();
                        int locationIdForHash = location.getLocationId();
                        //Creation of hashMap for duplicate checking
                        String keyForHash = timeStampForHash+macAddressForHash;
                        locations.put(keyForHash,locationIdForHash);
                        places.add(location);
                    }
                }
            }

            BufferedWriter bw = null;
            try {

                //Specify the file name and path here
                /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
                if (!tempLocationFile.exists()) {
                    tempLocationFile.createNewFile();
                }

                FileWriter fw = new FileWriter(tempLocationFile);
                bw = new BufferedWriter(fw);

                //while (iter.hasNext()) {
                for (int i = 0; i < places.size(); i++){  
                String toWrite = "";
                LocationUpdate location = places.get(i);
                    //retrieves all locations one by one
                    //retrives the components of the location object
                    String mac = location.getMacAddress();
                    String timestmp = location.getTimeStamp();
                    int locationId = location.getLocationId();

                    toWrite += timestmp + ",";
                    toWrite += mac + ",";
                    toWrite += locationId;

                    bw.write(toWrite);
                    //Writes the line into the temp csv
                    bw.newLine();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Exception ex) {
                    System.out.println("Error in closing the BufferedWriter" + ex);
                }
            }

            locationUpdateDAO.insert(tempLocationFile.getAbsolutePath());
            //uploads to database
            locationcsvFile.delete();
            //deletes this file to prevent it from being called in future bootstraps
        }

        answer[0] = locationErrors;
        answer[1] = locationLineNumbers;
        int number = locations.size();

        ArrayList<Integer> loaded = new ArrayList<>();
        loaded.add(number);
        answer[2] = loaded;
        return answer;
    }

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
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet BootstrapValidation</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet BootstrapValidation at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
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
