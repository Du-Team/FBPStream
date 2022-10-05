package edu.jsnu.sunjr;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Point implements Comparable<Point>{
    protected int id;
    protected int dim;
    protected double[] coord;
    protected boolean visited = false;
    private int mcId = -1;
    private int arrivalTime;

    public Point(final int id, final int arrivalTime, final double[] coord) {
        this.id = id;
        dim = coord.length;
        this.coord = coord;
        this.arrivalTime = arrivalTime;
    }

    @Override
    public int compareTo(final Point p) {
        double[] coord1 = coord;
        double[] coord2 = p.coord;
        if (coord1.length < coord2.length) {
            return -1;
        } else if (coord1.length > coord2.length) {
            return 1;
        } else {
            for (int i = 0; i < coord1.length; i++) {
                if (coord1[i] < coord2[i]) {
                    return -1;
                } else if (coord1[i] > coord2[i]) {
                    return 1;
                }
            }
            return 0;
        }
    }
}
