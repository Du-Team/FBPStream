package edu.jsnu.sunjr.parallelTask;

import edu.jsnu.sunjr.Cluster;
import edu.jsnu.sunjr.graph.Edge;
import edu.jsnu.sunjr.graph.Graph;
import edu.jsnu.sunjr.graph.Vertex;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @Author sunjr
 * @Date 2022/7/15/0015 8:00
 * @Version 1.0
 */
@Getter
@Setter
public class CoreClustering implements Runnable {
    private List<Vertex> coreNodes;
    private Graph augmentedKnnGraph;
    private Set<Cluster> clusterSet;

    public CoreClustering(List<Vertex> coreNodes, Graph augmentedKnnGraph, Set<Cluster> clusterSet) {
        this.coreNodes = coreNodes;
        this.augmentedKnnGraph = augmentedKnnGraph;
        this.clusterSet = clusterSet;
    }

    @Override
    public void run() {
        if (coreNodes.size() == 1 && coreNodes.get(0).getLabel() == null) {
            Cluster newCluster = new Cluster();
            clusterSet.add(newCluster);
            coreNodes.get(0).setLabel(newCluster);
            newCluster.add(coreNodes.get(0).getMc());
            return;
        }

        for (Vertex v : coreNodes) {
            if (v.isCheckLabel()) {
                continue;
            }
            v.setCheckLabel(true);
            if (v.getLabel() == null) {
                Cluster newCluster = new Cluster();
                clusterSet.add(newCluster);
                v.setLabel(newCluster);
                newCluster.add(v.getMc());
            }
            Queue<Vertex> queue = new LinkedList<>();
            queue.add(v);
            while (!queue.isEmpty()) {
                Vertex seed = queue.poll();
                double r1 = seed.getRadius();
                Cluster cluster = seed.getLabel();
                List<Edge> edgeList = augmentedKnnGraph.edgeList(seed);
                if (edgeList.isEmpty()) {
                    continue;
                }
                for (Edge e : edgeList) {
                    if (e.getLength() <= r1) {
                        Vertex another = e.anotherVertex(seed);
                        double r2 = another.getRadius();
                        if (e.getLength() <= r2 && !another.isPeeled()) {
                            if (!another.isCheckLabel()) {
                                another.setCheckLabel(true);
                                queue.add(another);
                                another.setLabel(cluster);
                                cluster.add(another.getMc());
                            }
                        }
                    }
                }
            }
        }
    }
}
