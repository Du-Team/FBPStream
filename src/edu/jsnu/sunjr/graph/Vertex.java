package edu.jsnu.sunjr.graph;

import edu.jsnu.sunjr.Cluster;
import edu.jsnu.sunjr.MicroCluster;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author sunjr
 * @Date 2022/7/7/0007 13:38
 * @Version 1.0
 */
@Getter
@Setter
public class Vertex {
    private MicroCluster mc;
    private boolean visited;
    private boolean peeled;
    private double radius;
    private double density;
    private Vertex rep;
    private Cluster label;
    private boolean checkLabel;

    public Vertex(MicroCluster mc) {
        this.mc = mc;
        visited = false;
        peeled = false;
        radius = 0;
        density = 0;
        rep = null;
        label = null;
        checkLabel = false;
    }

    public double getDistTo(Vertex v) {
        return mc.getDistTo(v.mc);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof Vertex) {
            Vertex v = (Vertex) obj;
            return mc.equals(v.mc);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mc.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(mc.getId());
    }
}
