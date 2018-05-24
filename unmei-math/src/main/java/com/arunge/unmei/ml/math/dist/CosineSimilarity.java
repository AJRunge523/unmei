package com.arunge.unmei.ml.math.dist;

import java.util.Map;

/**
 * 
 *<p>Computes the cosine similarity between two vectors. The cosine similarity metric is defined as
 *   A * B / (||A|| * ||B||). <p>
 *
 * @author Andrew Runge
 *
 */
public class CosineSimilarity {

    public double eval(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        if(v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        double firstNorm = 0.0;
        double secondNorm = 0.0;
        double dot = 0.0;
        for(Integer key : v1.keySet()) {
            if(v2.containsKey(key)) { 
                dot += (v1.get(key) * v2.get(key));
            }
            firstNorm += (v1.get(key) * v1.get(key));
        }
        for(Integer key : v2.keySet()) {
            secondNorm += (v2.get(key) * v2.get(key));
        }
        if(firstNorm == 0 || secondNorm == 0) { 
            return 0.0;
        }
        firstNorm = Math.sqrt(firstNorm);
        secondNorm = Math.sqrt(secondNorm);
        double cosine = dot / (firstNorm * secondNorm);
        return cosine;
    }
    
    public double eval(double[] v1, double[] v2) {
        if(v1 == null || v1.length == 0 || v2 == null || v2.length == 0 || v1.length != v2.length) {
            return 0.0;
        }
        double firstNorm = 0.0;
        double secondNorm = 0.0;
        double dot = 0.0;
        for(int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            firstNorm += v1[i] * v1[i];
            secondNorm += v2[i] * v2[i];
        }
        if(firstNorm == 0 || secondNorm == 0) { 
            return 0.0;
        }
        firstNorm = Math.sqrt(firstNorm);
        secondNorm = Math.sqrt(secondNorm);
        double cosine = dot / (firstNorm * secondNorm);
        return cosine;
    }
    
    
}
