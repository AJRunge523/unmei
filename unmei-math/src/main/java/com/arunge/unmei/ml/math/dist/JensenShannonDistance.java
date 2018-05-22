package com.arunge.unmei.ml.math.dist;

/**
 * 
 *<p>Computes the Jensen-Shannon distance, whih is equal to the square root of 
 *   the Jensen-Shannon Divergence and is a proper distance measure.
 *   
 *   Jensen-Shannon Divergence is a symmetrized modification to KL-Divergence, bound
 *   between 0 and 1 with the formula:
 *   
 *   JSD(P||Q) = 1/2 * (KLD(P||M) + KLD(Q||M)), where KLD is the Kullback-Liebler Divergence
 *   and M = (P + Q) / 2<p>
 *
 * @author Andrew Runge
 *
 */
public class JensenShannonDistance {

    public double eval(double[] dist1, double[] dist2) {
        if(dist1.length != dist2.length) {
            throw new IllegalArgumentException("Sizes of distributions do not match: " + dist1.length + ", " + dist2.length);
        }
        double[] mDist = new double[dist1.length];
        for(int i = 0; i < dist1.length; i++) {
            mDist[i] = (dist1[i] + dist2[i]) / 2;
        }
        return Math.sqrt(0.5 * (evalKLDivergence(dist1, mDist) + evalKLDivergence(dist2, mDist)));
    }
    
    private double evalKLDivergence(double[] dist1, double[] dist2) {
        double klDivergence = 0;
        for(int i = 0; i < dist1.length; i++) {
            if(dist2[i] != 0 && dist1[i] != 0) {
                klDivergence += dist1[i] * Math.log(dist1[i] / dist2[i]);
            }
        }
        if(klDivergence > 0) { 
            return klDivergence;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    
    public static void main(String[] args) { 
        double[] dist1 = new double[] { 0.7, 0.3, 0.0};
        double[] dist2 = new double[] { 0.0, 0.0, 1.0};
        JensenShannonDistance jsd = new JensenShannonDistance();
        System.out.println(jsd.eval(dist1, dist2));
    }
    
}
