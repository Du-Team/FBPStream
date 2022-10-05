package edu.jsnu.sunjr.parallelTask;

import edu.jsnu.sunjr.graph.Edge;
import edu.jsnu.sunjr.graph.Graph;
import edu.jsnu.sunjr.graph.Vertex;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author sunjr
 * @Date 2022/7/15/0015 7:59
 * @Version 1.0
 */
@Getter
@Setter
public class BorderLinkage implements Runnable {
    private List<Vertex> borderNodes;
    private Graph augmentedKnnGraph;

    public BorderLinkage(List<Vertex> borderNodes, Graph augmentedKnnGraph) {
        this.borderNodes = borderNodes;
        this.augmentedKnnGraph = augmentedKnnGraph;
    }

    @Override
    public void run() {
        for (Vertex v : borderNodes) {
            List<Edge> edgeList = augmentedKnnGraph.edgeList(v);
            if (edgeList.isEmpty()) {
                continue;
            }
            double r1 = v.getRadius();
            for (Edge e : edgeList) {
                if (e.getLength() <= r1) {
                    Vertex another = e.anotherVertex(v);
                    double r2 = another.getRadius();
                    if (e.getLength() <= r2) {
                        if (another.getDensity() > v.getDensity()) {
                            v.setRep(another);
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }
}
