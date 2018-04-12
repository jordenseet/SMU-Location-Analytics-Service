/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.RequestDispatcher;
import Utilities.BootstrapValidation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTUtility;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "BootstrapJsonServlet", urlPatterns = {"/json/bootstrap"})
@MultipartConfig
public class BootstrapJsonServlet extends HttpServlet {

    private BootstrapValidation bootstrapValidation = new BootstrapValidation();

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
        //insert process request pseudocode here
        PrintWriter out = response.getWriter();
        //Setting directory for new files to be written
        final String DIRECTORY = "bootstrapFiles";
        final String OUTPUTDIRECTORY = getServletContext().getRealPath("/bootstrapFiles");
        Part bootstrapFile = null;
        String token = request.getParameter("token");
        String sharedSecret = "G7T4SHAREDSECRET";
        String bootstrapResult = "";
        HashMap<String, Integer> recordsLoaded = new HashMap();
        JsonArray records = new JsonArray();
        ArrayList[] semanticPlaceCSVResult;
        ArrayList<ArrayList<String>> semanticErrors = new ArrayList<>();
        ArrayList<String> semanticLineNumbers = new ArrayList<>();
        ArrayList<Integer> semanticLoaded = new ArrayList<>();
        int semanticSize = 0;
        JsonArray firstArray = new JsonArray();
        JsonArray secondArray = new JsonArray();
        JsonObject firstObject = new JsonObject();
        String output = "";
        JsonArray errors = new JsonArray();
        JsonObject jsonOutput = new JsonObject();
          Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String result = JWTUtility.verify(token, sharedSecret);
            if (result == null) {
                errors.add("invalid token");
            }
        } catch (Exception e) {
            errors.add(e.getMessage());
            errors.add("invalid token");
        }

        try {
            bootstrapFile = request.getPart("file");

            if (bootstrapFile != null) {
                //ensures a file is being uploaded
                if (!getFileName(bootstrapFile).toLowerCase().contains(".zip")) {
                    //File is not a zipped file
                    RequestDispatcher rd = request.getRequestDispatcher("adminHome.jsp");
                    request.setAttribute("errorMsg", "Uploaded document is not a valid file");
                    //sends error message to adminHome.jsp
                    rd.forward(request, response);

                }

                String uploadedFilePath = uploadFile(bootstrapFile, DIRECTORY);

                if (uploadedFilePath != null) {

                    if (unzip(uploadedFilePath, OUTPUTDIRECTORY) == true) {

                        boolean isBootstrap = true;
                        // To identify whether this is a bootstrap or additional file upload
                        File tempDemoFile = new File(getServletContext().getRealPath("/bootstrapFiles") + "/tempDemographics.csv");
                        ArrayList[] userResults = bootstrapValidation.validateUserCSV(OUTPUTDIRECTORY, isBootstrap, tempDemoFile);
                        //userResults is an ArrayList array of size 3 
                        ArrayList<ArrayList<String>> userErrors = userResults[0];

                        //The error messages 
                        ArrayList<String> userLineNumbers = userResults[1];
                        //The line numbers of errors
                        ArrayList<Integer> userloaded = userResults[2];
                        //The number of users successfully uploaded
                        int demoSize = userloaded.get(0);

                        if (isBootstrap) {
                            //If statement as there won't be location-lookup.csv in additional file upload
                            File tempSemanticFile = new File(getServletContext().getRealPath("/"
                                    + "bootstrapFiles") + "/tempSemantic.csv");
                            semanticPlaceCSVResult = bootstrapValidation.validateSemanticPlaceCSV(OUTPUTDIRECTORY, tempSemanticFile);
                            //semanticPlaceCSVResult is an ArrayList array of size 3
                            semanticErrors = semanticPlaceCSVResult[0];
                            //The error messages 
                            semanticLineNumbers = semanticPlaceCSVResult[1];
                            //The line numbers of errors
                            semanticLoaded = semanticPlaceCSVResult[2];
                            //The number of users successfully uploaded
                            semanticSize = semanticLoaded.get(0);
                        }

                        File tempLocationFile = new File(getServletContext().getRealPath("/bootstrapFiles") + "/tempLocation.csv");
                        ArrayList[] locationUpdateCSVResult = bootstrapValidation.validateLocationCSV(OUTPUTDIRECTORY, isBootstrap, tempLocationFile);
                        //semanticPlaceCSVResult is an ArrayList array of size 3
                        ArrayList<ArrayList<String>> locationErrors = locationUpdateCSVResult[0];
                        //The error messages 
                        ArrayList<String> locationLineNumbers = locationUpdateCSVResult[1];
                        //The line numbers of errors
                        ArrayList<Integer> locationloaded = locationUpdateCSVResult[2];
                        //The number of users successfully uploaded
                        int locSize = locationloaded.get(0);

                        boolean hasError = false;
                        //hasError will be used to check for the size of error messages
                        boolean sizeTest = false;
                        //Checks that there are successful uploads

                        if (isBootstrap) {
                            //Because bootstrap has semantic
                            hasError = (userLineNumbers.size() > 0 || semanticLineNumbers.size() > 0 || locationLineNumbers.size() > 0);
                            sizeTest = demoSize > 0 || locSize > 0 || semanticSize > 0;
                            //To prevent status from being success with no data loaded
                        } else {
                            hasError = (userLineNumbers.size() > 0 || locationLineNumbers.size() > 0);
                            sizeTest = demoSize > 0 || locSize > 0;
                        }

                        //result.addProperty("status", (hasError ? "error" : "success"));
                        if (!hasError && sizeTest) {
                            bootstrapResult = "success";
                        } else {
                            bootstrapResult = "error";
                        }
                        ArrayList<ArrayList<String>> forJSON = new ArrayList<>();
                        String fileName;
                        String errorMessage;
                        for (int i = 0; i < userLineNumbers.size(); i++) {
                            ArrayList<String> insertForJSON = new ArrayList<>();
                            String lineNumber = userLineNumbers.get(i);

                            fileName = "demographics.csv";
                            errorMessage = "";
                            int errorCounter = 0;
                            ArrayList<String> errorWords = userErrors.get(i);
                            for (String word : errorWords) {
                                if (errorCounter == 0) {
                                    errorMessage += word;
                                    errorCounter++;
                                } else {
                                    errorMessage += ", ";
                                    errorMessage += word;
                                }
                               
                            }
                            insertForJSON.add(fileName);
                            insertForJSON.add(lineNumber);
                     
                            insertForJSON.add(errorMessage);
                            forJSON.add(insertForJSON);
                        }

                        for (int i = 0; i < locationLineNumbers.size(); i++) {
                            ArrayList<String> insertForJSON = new ArrayList<>();
                            String lineNumber = locationLineNumbers.get(i);

                            fileName = "location.csv";
                            errorMessage = "";
                            int errorCounter = 0;
                            ArrayList<String> errorWords = locationErrors.get(i);
                            for (String word : errorWords) {
                                if (errorCounter == 0) {
                                    errorMessage += word;
                                    errorCounter++;
                                } else {
                                    errorMessage += ", ";
                                    errorMessage += word;
                                }

                            }
                            insertForJSON.add(fileName);
                            insertForJSON.add(lineNumber);
                            insertForJSON.add(errorMessage);
                            forJSON.add(insertForJSON);
                        }
                        for (int i = 0; i < semanticLineNumbers.size(); i++) {
                            ArrayList<String> insertForJSON = new ArrayList<>();
                            String lineNumber = semanticLineNumbers.get(i);

                            fileName = "location-lookup.csv";
                            errorMessage = "";
                            int errorCounter = 0;
                            ArrayList<String> errorWords = locationErrors.get(i);
                            for (String word : errorWords) {
                                if (errorCounter == 0) {
                                    errorMessage += word;
                                    errorCounter++;
                                } else {
                                    errorMessage += ", ";
                                    errorMessage += word;
                                }

                            }
                            insertForJSON.add(fileName);
                            insertForJSON.add(lineNumber);
                            insertForJSON.add(errorMessage);
                            forJSON.add(insertForJSON);
                        }

                        firstObject.addProperty("demographics.csv", demoSize);
                        //hashmap stores file number and number of successful uploads
                        firstObject.addProperty("location.csv", locSize);
                        firstObject.addProperty("location-lookup.csv", semanticSize);

                        jsonOutput.addProperty("status", bootstrapResult);
                        //bootstrap result is success or error
                        firstArray.add(firstObject);
                        jsonOutput.add("num-recorded-loaded", firstArray);
                        // out.println("status:" + bootstrapResult );
                        // out.println("num-recorded-loaded");

                        for (int i = 0; i < forJSON.size(); i++) {
                            JsonObject secondObject = new JsonObject();
                            ArrayList<String> listOfErrors = forJSON.get(i);
                            String file = listOfErrors.get(0);
                            String line = listOfErrors.get(1);
                            String message = listOfErrors.get(2);

                            //out.println("file:" + file + " line:" + line + "message:" + message);
                            secondObject.addProperty("file", file);
                            secondObject.addProperty("line", line);
                            secondObject.addProperty("message", message);
                            secondArray.add(secondObject);
                        }
                        //secondObject.addProperty("file", listOfErrors.get(0));
                        //secondObject.addProperty("line", listOfErrors.get(1));
                        // secondObject.addProperty("message", listOfErrors.get(2));
                           
                        // secondArray.add(secondObject);
                        
                        if (bootstrapResult.equals("error")) {
                         
                            jsonOutput.add(bootstrapResult, secondArray);
                            
                          
                        }

                     output = gson.toJson(jsonOutput);
                             out.println(output);
                        //The line numbers for all user error messages

                        //All location error line numbers
                    }

                }
            } else {
                //No file was uploaded
                errors.add("Uploaded document is not a valid file");
            }
        } catch (Exception e) {
            //Catches any exceptions that get thrown
            errors.add("Uploaded document is not a valid file");
        }
    }

    /**
     * this method gets the file name
     *
     * @param part
     * @return the name of file name if the file name starts with "filename",
     * else return null
     */
    protected String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);
            }
        }
        return null;
    }

    /**
     * it uploads the file
     *
     * @param filePart
     * @param UPLOAD_DIRECTORY
     * @return the file path
     * @throws IOException
     */
    protected String uploadFile(Part filePart, String UPLOAD_DIRECTORY) throws IOException {
        // constructs the directory path to store upload file
        // this path is relative to application's directoryString
        String uploadPath = getServletContext().getRealPath("") + "/" + UPLOAD_DIRECTORY;
        //String uploadPath = System.getenv("OPENSHIFT_DATA_DIR") + "/" + UPLOAD_DIRECTORY;
        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        //upload file to the directory specified
        String filename = getFileName(filePart);
        String filePath = uploadPath + "/" + filename;
        filePart.write(filePath);
        System.out.println("file uploaded to : " + filePath);
        return filePath;
    }

    /**
     * it unzip the file
     *
     * @param zipFile
     * @param destination throws FileNotFoundException, IOException
     * @return true if it has unzipped, else return false
     */
    protected boolean unzip(String zipFile, String destination) {
        File outputPlace = new File(destination);

        if (outputPlace.exists()) {
            outputPlace.delete();
        }
        outputPlace.mkdir();
        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = null;
            int len;
            byte[] buffer = new byte[1024];

            while ((entry = zip.getNextEntry()) != null) {

                if (!entry.isDirectory()) {
                    if (entry.getName().contains("/") == false) {
                        File file = new File(destination + "/" + entry.getName());
                        FileOutputStream fos = new FileOutputStream(file);
                        while ((len = zip.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        System.out.println("file written to : " + destination + "/" + entry.getName());
                        fos.close();
                    }
                }

            }
            zip.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("Found not file, Unable to unzip file");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("Invalid file format, Unable to unzip file");
            return false;
        }
    }

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
