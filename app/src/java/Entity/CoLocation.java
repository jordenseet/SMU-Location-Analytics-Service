/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entity;

/**
 *
 * @author G7T4
 */
public class CoLocation {
    private String userMac;
    private String companionMac;
    private int locationId;
    private long duration;

    /**
     *
     * @param userMac
     * @param companionMac
     * @param locationId
     * @param duration
     */
    public CoLocation(String userMac, String companionMac, int locationId, long duration) {
        this.userMac = userMac;
        this.companionMac = companionMac;
        this.locationId = locationId;
        this.duration = duration;
    }

    /**
     *get user's mac address
     * @return user mac address
     */
    public String getUserMac() {
        return userMac;
    }

    /**
     * get companion mac address
     * @return companion mac address
     */
    public String getCompanionMac() {
        return companionMac;
    }

    /**
     *get location ID
     * @return location ID
     */
    public int getLocationId() {
        return locationId;
    }

    /**
     *get duration in that location
     * @return duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     *set duration
     * @param duration
     * set duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }
}
