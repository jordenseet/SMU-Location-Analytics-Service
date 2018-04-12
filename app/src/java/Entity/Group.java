/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author G7T4
 */
public class Group {

    private Date startTime;
    private Date endTime;
    private int locationId;
    private ArrayList<String> macAddressList;

    /**
     *create group object
     * @param startTime
     * @param endTime
     * @param locationId
     * @param macAddressList
     */
    public Group(Date startTime,Date endTime, int locationId, ArrayList<String> macAddressList) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationId = locationId;
        this.macAddressList = macAddressList;
    }

    /**
     * get the start time of in that group
     * @return start time
     */
    public Date getStartTime() {
        return startTime;
    }
    
    /**
     *get the end time in that group
     * @return end time
     */
    public Date getEndtTime() {
        return endTime;
    }

    /**
     *set the start time
     * @param startTime
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    /**
     *set the end time
     * @param endTime
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     *get location ID
     * @return locationID
     */
    public int getLocationId() {
        return locationId;
    }

    /**
     *set location ID
     * @param locationId
     */
    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getMembers() {
        return macAddressList;
    }

    /**
     *
     * @param macAddressList
     */
    public void setMembers(ArrayList<String> macAddressList) {
        this.macAddressList = macAddressList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if (this.locationId != other.locationId) {
            return false;
        }
        if (!Objects.equals(this.startTime, other.startTime)) {
            return false;
        }
        if (!Objects.equals(this.endTime, other.endTime)) {
            return false;
        }
        if (!Objects.equals(this.macAddressList, other.macAddressList)) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return
     */
    public int groupSize() {
        return macAddressList.size();
    }

}
