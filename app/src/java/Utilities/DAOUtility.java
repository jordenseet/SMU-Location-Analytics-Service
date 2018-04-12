/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

/**
 *
 * @author G7T4
 */
public class DAOUtility {

    /**
     *to format the date time
     * @param datetime
     * @return
     */
    public static String formatDatetime(String datetime) {
        // datetime from HTML is in the form YYYY-MM-DDTHH:MM:SS (if SS not 00)
        // we need YYYY-MM-DD HH:MI:SS for SQL
        if (datetime.length() < 19) {
            datetime += ":00";
        }
        return datetime.replace('T', ' ');
    }
}

