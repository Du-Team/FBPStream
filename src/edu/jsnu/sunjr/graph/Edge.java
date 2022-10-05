package edu.jsnu.sunjr.graph;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author sunjr
 * @Date 2022/7/7/0007 13:48
 * @Version 1.0
 */
@Getter
@Setter
public class Edge implements Comparable<Edge>{
    private Vertex v1;
    private Vertex v2;
    private double length;
    private boolean visited;

    public Edge(final Vertex v1, final Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
        length = v1.getDistTo(v2);
        visited = false;
    }

    public Vertex anotherVertex(final Vertex v) {
        return v.equals(v1) ? v2 : v1;
    }

    public boolean isPeeled() {
        return v1.isPeeled() || v2.isPeeled();
    }

    @Override
    public int compareTo(final Edge o) {
        return Double.compare(length, o.getLength());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof Edge) {
            Edge edge = (Edge) obj;
            if (v1.equals(edge.v1) && v2.equals(edge.v2)) {
                return true;
            }
            if (v1.equals(edge.v2) && v2.equals(edge.v1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return v1.hashCode() + v2.hashCode();
    }
}
