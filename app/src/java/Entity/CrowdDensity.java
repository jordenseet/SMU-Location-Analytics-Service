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
public class CrowdDensity {
    private String name;
    private int numPeople;
    private int density = 0;

    /**
     *create crowd density object
     * @param name
     * @param num
     */
    public CrowdDensity(String name, int num) {
        this.name = name;
        this.numPeople = num;
        if (num >= 31) {
            density = 6;
        } else if (num >= 21) {
            density = 5;
        } else if (num >= 11) {
            density = 4;
        } else if (num >= 6) {
            density = 3;
        } else if (num >= 3) {
            density = 2;
        } else if (num >= 1) {
            density = 1;
        }
    }
    
    /**
     *get name of the location
     * @return location name
     */
    public String getName() {
        return name;
    }

    /**
     *get number of people
     * @return number of people
     */
    public int getNumPeople() {
        return numPeople;
    }
    
    /**
     *get the density
     * @return density
     */
    public int getDensity() {
        return density;
    }
}
