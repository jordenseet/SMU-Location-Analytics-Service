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
import java.util.Iterator;
import java.util.TreeMap;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import is203.JWTUtility;

/**
 *
 * @author G7T4
 */
@WebServlet(name = "BootStrapServlet", urlPatterns = {"/BootStrapServlet", "/additionalUploadServlet"})
@MultipartConfig
public class BootStrapServlet extends HttpServlet {

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
                    return;
                }

                String uploadedFilePath = uploadFile(bootstrapFile, DIRECTORY);

                if (uploadedFilePath != null) {

                    if (unzip(uploadedFilePath, OUTPUTDIRECTORY) == true) {

                        boolean isBootstrap = request.getRequestURI().contains("/BootStrapServlet");
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
                            File tempSemanticFile = new File(getServletContext().getRealPath("/bootstrapFiles") + "/tempSemantic.csv");
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

                        recordsLoaded.put("demographics.csv", demoSize);
                        //hashmap stores file number and number of successful uploads
                        recordsLoaded.put("location.csv", locSize);
                        recordsLoaded.put("location-lookup.csv", semanticSize);

                        RequestDispatcher rd = request.getRequestDispatcher("adminHome.jsp");
 
                        request.setAttribute("status", bootstrapResult);
                        //bootstrap result is success or error
                        request.setAttribute("recordsLoaded", recordsLoaded);
                        //The number of data loaded for each file type
                        request.setAttribute("userErrors", userErrors);
                        //All user error messages
                        request.setAttribute("userLineNumbers", userLineNumbers);
                        //The line numbers for all user error messages
                        if (isBootstrap){
                            request.setAttribute("semanticErrors", semanticErrors);
                            //All location-lookup error messages
                            request.setAttribute("semanticLineNumbers", semanticLineNumbers);
                            //The line number for all location-lookup errors
                        }
                        request.setAttribute("locationErrors", locationErrors);
                        //All location error messages
                        request.setAttribute("locationLineNumbers", locationLineNumbers);
                        //All location error line numbers
                        rd.forward(request, response);
                    }
                }
            } else {
                //No file was uploaded
                RequestDispatcher rd = request.getRequestDispatcher("adminHome.jsp");
                request.setAttribute("errorMsg", "Uploaded document is not a valid file");
                rd.forward(request, response);
                return;
            }
        } catch (Exception e) {
            //Catches any exceptions that get thrown
            RequestDispatcher rd = request.getRequestDispatcher("adminHome.jsp");
            request.setAttribute("errorMsg", "Error: " + e.getMessage());
            e.printStackTrace();
            rd.forward(request, response);
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
