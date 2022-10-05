package edu.jsnu.sunjr.utils.distance;

import java.io.Serializable;

public interface DistanceMeasure extends Serializable {

    /**
     * Compute the distance between two n-dimensional vectors.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the distance between the two vectors
     */
    double compute(double[] a, double[] b);
}
