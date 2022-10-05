package edu.jsnu.sunjr;

import edu.jsnu.sunjr.utils.distance.DistanceMeasure;
import edu.jsnu.sunjr.utils.distance.impl.EuclideanDistance;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MicroCluster {
    private int id;
    private int dim;
    private double[] center;
    private DistanceMeasure distanceMeasure;
    private boolean visited = false;
    private double weight;
    private boolean active;
    private int tu;
    private double distTmp;


    public MicroCluster(final int id, final Point p) {
        this.id = id;
        dim = p.dim;
        center = new double[dim];
        System.arraycopy(p.coord, 0, center, 0, dim);
        this.distanceMeasure = new EuclideanDistance();
        p.setMcId(id);
        weight = 1;
        active = false;
        tu = p.getArrivalTime();
        distTmp = 0;
    }

    public void add(final double lambda, final Point p, final boolean decay) {
        p.setMcId(id);
        if (decay) {
            double decayedWeight = getDecayedWeight(lambda, p.getArrivalTime());
            weight = decayedWeight + 1;
        } else {
            weight++;
        }

        tu = p.getArrivalTime();
    }

    public double getDecayedWeight(final double lambda, final int tc) {
        return weight * getDecayCoeff(lambda, tc);
    }

    public double getDecayCoeff(final double lambda, final int tc) {
        return Math.pow(lambda, tc - tu);
    }

    public double getDistTo(final Point p) {
        return distanceMeasure.compute(center, p.coord);
    }

    public double getDistTo(final MicroCluster mc) {
        return distanceMeasure.compute(center, mc.center);
    }
}
