/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.text.DecimalFormat;

/**
 *
 * @author G7T4
 */
public class Utility {

    /**
     *take the percentage from count and total and it will round up the percentage
     * @param count
     * @param total
     * @return the percentage that has been rounded up 
     */
    public static int percentHalfRoundUp(int count, int total) {
        if (count == 0) {
            return count;
        }
        double d = 100.0*count/total;
        DecimalFormat df = new DecimalFormat("0.0");
        String oneDP = df.format(d);
        int dot = oneDP.indexOf('.');
        int rounded = Integer.parseInt(oneDP.substring(0, dot));
        if (dot != -1 && Character.getNumericValue(oneDP.charAt(dot+1)) >= 5) {
            rounded += 1;
        }
        return rounded;
    }
}
