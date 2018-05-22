package com.arunge.unmei.ml.math.dist;

import java.util.Map;

/**
 * 
 *<p>Hellinger distance is a proper distance metric, defined as 1/SQRT(2) * SQRT(((SQRT(A) - SQRT(B)) ^ 2)).
 *   Note that this actually calculates the squared Hellinger distance. <p>
 *
 * @author Andrew Runge
 *
 */
public class HellingerDistance {

    public double eval(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        if(v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for(Integer key : v1.keySet()) {
            if(v2.containsKey(key)) { 
                sum += Math.pow(Math.sqrt(v1.get(key)) - Math.sqrt(v2.get(key)), 2);
            } else {
                sum += v1.get(key);
            }
        }
        for(Integer key : v2.keySet()) {
            if(v1.containsKey(key)) {
                continue;
            } else {
                sum += v2.get(key);
            }
        }
        return 0.5 * sum;
    }
    
    public double eval(double[] v1, double[] v2) {
        if(v1 == null || v1.length == 0 || v2 == null || v2.length == 0 || v1.length != v2.length) {
            return 0.0;
        }
        double sum = 0.0;
        for(int i = 0; i < v1.length; i++) {
            sum += Math.pow(Math.sqrt(v1[i]) - Math.sqrt(v2[i]), 2);
        }
        return 0.5 * sum;
    }
    
}

