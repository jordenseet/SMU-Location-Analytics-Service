package Entity;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author G7T4
 */
public class Admin { //assume there is only 1 admin as of now 

    private String name;
    private String password;
/**
    * create admin object
    * @param name
     * @param password
    */
    public Admin(String name, String password){
        this.name = name;
        this.password = password;
    }

    /**
     *get admin name
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
    *  get admin password
    *  @return password
    */
    public String getPassword() {
        return password;
    }
}

