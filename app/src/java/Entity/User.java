package Entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 *
 * @author G7T4
 */
public class User {

    private String macAddress;
    private String name;
    private String password;
    private String email;
    private char gender;
    private ArrayList<MutablePair<LocationUpdate, Long>> timeline = new ArrayList();
    private long duration;

    /**
     *get duration
     * @return duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     *set duration
     * @param duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     *create user object
     * @param macAddress
     * @param name
     * @param password
     * @param email
     * @param gender
     */
    public User(String macAddress, String name, String password, String email, char gender) {
        this.macAddress = macAddress;
        this.name = name;
        this.password = password;
        this.email = email;
        this.gender = gender;
    }

    /**
     *create user object using mac address
     * @param macAddress
     */
    public User(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     *get mac address
     * @return mac address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *get user name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *get user's password
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     *get user's email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     *get user's gender
     * @return user's gender
     */
    public char getGender() {
        return gender;
    }

    /**
     *get user's year
     * @return user's year
     */
    public String getYear() {
        int atIndex = email.indexOf('@');
        int yearDot = email.lastIndexOf('.', atIndex);
        return email.substring(yearDot + 1, atIndex);
    }

    /**
     *get user's school
     * @return school
     */
    public String getSchool() {
        return email.substring(email.indexOf('@') + 1, email.length() - 11);
    }
    /**
     *combine mac address, name, password, email and gender in a string
     * @return the results
     */
    public String toString() {
        return macAddress + ", " + name + ", " + password + ", " + email + ", " + gender;
    }

    // Invalid if value is not a SHA-1 hash value(a hexidecimal number, 40 digits long)

    /**
     *validate mac address
     * @return true if it is valid, else return false
     */
    public boolean validateMacAddress() {
        if (macAddress.length() != 40) {
            return false;
        }
        if (!macAddress.matches("[a-fA-F0-9]+")) { // '+' in regex means it occurs one or more times
            return false;
        }
        return true;
    }

    // Invalid if the length of the password is less than 8 characters or if it
    // includes whitespaces.

    /**
     *validate password
     * @return true if the password is valid, else return false
     */
    public boolean validatePassword() {
        if (password.length() < 8) {
            return false;
        }

        int i = password.indexOf(' ');
        if (i == -1) {
            return true;
        }
        return false;
    }

    // Valid email should be of the format xxx.<year>@<school>.smu.edu.sg where
    // school is either business, accountancy, sis, economics, law, or socsc,
    // and year is between 2013 (inclusive) to 2017 (inclusive). xxx should
    // contain only letters (a-z or A-Z), numbers or dot.

    /**
     *valid email address
     * @return true if the email is valid, else return false
     */
    public boolean validateEmail() {
        int emailLength = email.length();

        // Invalid if email doesn't end with ".smu.edu.sg"
        if (!email.substring(emailLength - 11, emailLength).equals(".smu.edu.sg")) {
            return false;
        }

        // Invalid if less/more than one '@' or cannot find '.' just before <year>.
        int atIndex = email.indexOf('@');
        if (atIndex != email.lastIndexOf('@') || atIndex == -1) {
            return false;
        }
        int yearDot = email.lastIndexOf('.', atIndex); // Index of '.' just before <year>
        if (yearDot == -1) {
            return false;
        }

        // Invalid if <xxx> contains characters besides a-z, A-Z, numbers or dot.
        String name = email.substring(0, yearDot);
        if (!name.matches("[A-Za-z0-9.]+")) {
            return false;
        }

        // Invalid if year not integer between 2013 (inclusive) and 2017 (inclusive).
        String yearStr = email.substring(yearDot + 1, atIndex);
        if (!yearStr.matches("[0-9]+")) { // '+' in regex means it occurs one or more times
            return false;
        }
        int year = Integer.parseInt(yearStr);
        if (year < 2013 || year > 2017) {
            return false;
        }

        // Valid if school is either business, accountancy, sis, economics, law, or socsc
        String school = email.substring(atIndex + 1, emailLength - 11);
        return school.equals("business") || school.equals("accountancy")
                || school.equals("sis") || school.equals("economics")
                || school.equals("law") || school.equals("socsc");
    }
    // Gender should be either "M" or "F" (case-insensitive)

    /**
     *validate the gender
     * @return true if the gender is valid, else return false
     */
    public boolean validateGender() {
        String validGenders = "MmFf";
        return validGenders.indexOf(gender) != -1;
    }

    /**
     *get the timeline of the users 
     * @return timeline with arraylist of locationUpdate and duration
     */
    public ArrayList<MutablePair<LocationUpdate, Long>> getTimeline() {
        return this.timeline;
    }

    /**
     *set list of user's timeline
     * @param timeline
     * 
     */
    public void setTimeline(List<LocationUpdate> timeline) {
        boolean isLast = false;
        for (int i = 0; i <= timeline.size() - 1; i++) {
            LocationUpdate currentLocation = timeline.get(i);
            LocationUpdate nextLocation = null;
            if (i+1 <= timeline.size() -1){
                nextLocation = timeline.get(i+1);
            }
            else{
                nextLocation = timeline.get(i);
                isLast = true;
                
            }
            
            try {
                long startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentLocation.getTimeStamp().replace(".0", "")).getTime();
                long endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nextLocation.getTimeStamp().replace(".0", "")).getTime();
                
                if (isLast) {
                    endTime = startTime + (1000 * 60 * 5); // Assume 5 min stay at last location
                }
                
                long stayDuration = endTime - startTime;
                
                if (stayDuration > (1000 * 60 * 5)) {
                    stayDuration = (1000 * 60 * 5); //// Assume 5 min stay at location
                }
                
                MutablePair<LocationUpdate, Long> data = new MutablePair(currentLocation, stayDuration);
                this.timeline.add(data);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
