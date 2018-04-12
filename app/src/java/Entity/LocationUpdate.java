package Entity;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 *
 * @author G7T4
 */
public class LocationUpdate {

    private String timeStamp;
    private String macAddress;
    private int locationId;

    //private String semanticPlace
    //Because you won't know the Semantic Place until you look it up

    /**
     *create location update object
     * @param timeStamp
     * @param macAddress
     * @param locationId
     */
    public LocationUpdate(String timeStamp, String macAddress, int locationId) {
        this.timeStamp = timeStamp;
        this.macAddress = macAddress;
        this.locationId = locationId;
    }

    /**
     *get timestamp
     * @return timestamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     *get date in the format 
     * @return date
     */
    public Date getDate() {
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(timeStamp);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     *get mac address
     * @return mac address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *get location id
     * @return location id
     */
    public int getLocationId() {
        return locationId;
    }

    /*public String getSemanticPlace() {
		return semanticPlace;
	}*/

    /**
     *validate time stamp
     * @return true if it is valid, else, return false
     */

    public boolean validateTimeStamp() {
        if (timeStamp.length() !=19){
            return false;
        }
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setLenient(false);
        try {
            format.parse(timeStamp);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    //if the value is not a SHA-1 hash value(a hexadecimal number, 40 digits long)

    /**
     *validate mac address
     * @return true if it is valid, else, return false
     */
    public boolean validateMacAddress() {
        if (macAddress.length() != 40) {
            return false;
        }
        if (!macAddress.matches("[a-fA-F0-9]+")) {
            return false;
        }
        return true;
    }
    //Return true if no duplicates

    /**
     *remove duplicates
     * @param existingLocations
     * @return true if it is removed, else, return false
     */
    public boolean removeDuplicates(HashMap<String, ArrayList<Integer>> existingLocations) {
        if (!existingLocations.containsKey(macAddress)) {
            return true;
        }
        ArrayList<Integer> locationList = existingLocations.get(macAddress);
        int duplicateCounter = 0;
        for (int existingLocation : locationList) {
            if (locationId == existingLocation) {
                duplicateCounter++;
            }
        }
        if (duplicateCounter > 0) {
            return false;
        }
        return true;
    }

    /**
     *validate location
     * @param validLocations
     * @return true if it is valid, else, return false
     */
    public boolean validateLocation(ArrayList<Integer> validLocations) {
        int counter = 0;
        for (Integer location : validLocations) {
            if (location == locationId) {
                counter++;
            }
        }
        return counter != 0;
    }

      /**
     *override the existing equals method
     * @param obj
     * @return true if it is valid, else, return false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocationUpdate) {
            LocationUpdate toCompare = (LocationUpdate) obj;

            String testIdentifier = getTimeStamp().concat(getMacAddress()).replace(".0", "");
            String toTestIdentifier = toCompare.getTimeStamp().concat(toCompare.getMacAddress()).replace(".0", "");
            
            if (toTestIdentifier.equals(testIdentifier)) {
                return true;
            }
        }
        return false;
    }

}
