package edu.jsnu.sunjr;

import edu.jsnu.sunjr.graph.Edge;
import edu.jsnu.sunjr.graph.Graph;
import edu.jsnu.sunjr.graph.Vertex;
import edu.jsnu.sunjr.parallelTask.BorderLinkage;
import edu.jsnu.sunjr.parallelTask.CoreClustering;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.*;

/**
 * @Author sunjr
 * @Date 2022/7/9/0009 22:23
 * @Version 1.0
 */

@Getter
@Setter
public class RPTree {
    private int k;
    private int alpha;
    private Graph augmentedKnnGraph;
    private List<Vertex> nodes;
    private int watermark;
    private double lambda;
    private double minWeight;
    private int tu;
    private ExecutorService executor;

    public RPTree(final int k, final int alpha, final int watermark, final double lambda, final double minWeight, final ExecutorService executor) {
        this.k = k;
        this.alpha = alpha;
        augmentedKnnGraph = new Graph(k);
        nodes = new ArrayList<>();
        this.watermark = watermark;
        this.lambda = lambda;
        this.minWeight = minWeight;
        tu = 0;
        this.executor = executor;
    }

    public int size() {
        return nodes.size();
    }

    public void add(MicroCluster mc) {

        Vertex v = new Vertex(mc);

        addVertex2KNNGraph(v);

        nodes.add(v);
    }

    public boolean checkActive(final OutlierPool outlierPool) {
        boolean decreaseTrigger = false;
        Iterator<Vertex> vIter = nodes.iterator();
        while (vIter.hasNext()) {
            Vertex v = vIter.next();
            MicroCluster mc = v.getMc();
            if (mc.getWeight() < minWeight) {
                decreaseTrigger = true;
                vIter.remove();
                Cluster cluster = v.getLabel();
                if (cluster != null) {
                    cluster.remove(mc);
                    v.setLabel(null);
                }

                mc.setActive(false);
                outlierPool.add(mc);

                deleteVertexFromKNNGraph(v);
            }
        }

        return decreaseTrigger;
    }

    private void addVertex2KNNGraph(Vertex v) {
        List<Edge> newEdgeList = new ArrayList<>();
        if (augmentedKnnGraph.isEmpty()) {
            augmentedKnnGraph.putEntry(v, newEdgeList);
            return;
        }

        List<Vertex> denChanges = new ArrayList<>();
        for (Vertex vertex : nodes) {
            List<Edge> edgeList = augmentedKnnGraph.edgeList(vertex);
            Edge newEdge = null;
            if (edgeList.size() < alpha * k) {
                newEdge = new Edge(vertex, v);
                int loc1 = augmentedKnnGraph.insertEdge(edgeList, newEdge);
                if (edgeList.size() <= k) {
                    vertex.setRadius(edgeList.get(edgeList.size() - 1).getLength());
                    denChanges.add(vertex);
                } else {
                    if (loc1 < k) {
                        vertex.setRadius(edgeList.get(k - 1).getLength());
                        denChanges.add(vertex);
                    }
                }
            } else {
                double diffDist = vertex.getMc().getDistTmp() - v.getMc().getDistTmp();
                double distTo2KthNN1 = edgeList.get(alpha * k - 1).getLength();
                if (diffDist < distTo2KthNN1) {
                    newEdge = new Edge(vertex, v);
                    if (newEdge.getLength() < distTo2KthNN1) {
                        int loc1 = augmentedKnnGraph.insertEdge(edgeList, newEdge);
                        edgeList.remove(alpha * k);
                        if (loc1 < k) {
                            vertex.setRadius(edgeList.get(k - 1).getLength());
                            denChanges.add(vertex);
                        }
                    }
                }
            }

            if (newEdgeList.size() < alpha * k) {
                if (newEdge == null) {
                    newEdge = new Edge(vertex, v);
                }
                int loc2 = augmentedKnnGraph.insertEdge(newEdgeList, newEdge);
                if (newEdgeList.size() < k) {
                    v.setRadius(newEdgeList.get(newEdgeList.size() - 1).getLength());
                } else {
                    if (loc2 < k) {
                        v.setRadius(newEdgeList.get(k - 1).getLength());
                    }
                }
            } else {
                double diffDist = vertex.getMc().getDistTmp() - v.getMc().getDistTmp();
                double distTo2KthNN2 = newEdgeList.get(alpha * k - 1).getLength();
                if (newEdge == null) {
                    if (diffDist < distTo2KthNN2) {
                        newEdge = new Edge(vertex, v);
                        if (newEdge.getLength() < distTo2KthNN2) {
                            int loc2 = augmentedKnnGraph.insertEdge(newEdgeList, newEdge);
                            newEdgeList.remove(alpha * k);
                            if (loc2 < k) {
                                v.setRadius(newEdgeList.get(k - 1).getLength());
                            }
                        }
                    }
                } else {
                    if (newEdge.getLength() < distTo2KthNN2) {
                        int loc2 = augmentedKnnGraph.insertEdge(newEdgeList, newEdge);
                        newEdgeList.remove(alpha * k);
                        if (loc2 < k) {
                            v.setRadius(newEdgeList.get(k - 1).getLength());
                        }
                    }
                }
            }
        }
        augmentedKnnGraph.putEntry(v, newEdgeList);

        denChanges.add(v);
        denChanges.forEach(this::adjustDensity);
    }

    private void deleteVertexFromKNNGraph(Vertex v) {
        augmentedKnnGraph.remove(v);
        List<Vertex> denChanges = new ArrayList<>();
        for (Vertex vertex : nodes) {
            List<Edge> edgeList = augmentedKnnGraph.edgeList(vertex);
            Optional<Edge> any = edgeList
                    .stream()
                    .filter(e -> e.anotherVertex(vertex).equals(v))
                    .findAny();
            if (any.isPresent()) {
                Edge delEdge = any.get();
                edgeList.remove(delEdge);
                if (delEdge.getLength() <= vertex.getRadius()) {
                    denChanges.add(vertex);
                    int size = edgeList.size();
                    if (size == 0) {
                        vertex.setRadius(0);
                    } else {
                        if (size <= k) {
                            vertex.setRadius(edgeList.get(size - 1).getLength());
                        } else {
                            vertex.setRadius(edgeList.get(k - 1).getLength());
                        }
                    }

                }
            }
        }

        denChanges.forEach(this::adjustDensity);
    }

    private void adjustDensity(Vertex v) {
        double density = 0;
        double r1 = v.getRadius();
        List<Edge> edgeList = augmentedKnnGraph.edgeList(v);
        for (Edge e : edgeList) {
            if (e.getLength() <= r1) {
                double r2 = e.anotherVertex(v).getRadius();
                if (e.getLength() <= r2) {
                    density += gaussianKernelFunc(e.getLength(), r2);
                }
            } else {
                break;
            }
        }

        double weight = v.getMc().getWeight();
        v.setDensity(density * weight);
    }

    private double gaussianKernelFunc(double dist, double sigma) {
        return Math.exp(-Math.pow(dist, 2) / Math.pow(sigma, 2));
    }

    public void adjustCluster(Set<Cluster> clusterSet) throws ExecutionException, InterruptedException {
        if (nodes.isEmpty()) {
            System.out.println("RP-Tree has no active micro-clusters!");
            return;
        }

        if (clusterSet.isEmpty()) {
            Cluster cluster = new Cluster();
            clusterSet.add(cluster);
            for (Vertex v : nodes) {
                v.setLabel(cluster);
                cluster.add(v.getMc());
            }
            return;
        }

        List<Vertex> coreNodes = new ArrayList<>();
        List<Vertex> borderNodes = new ArrayList<>();
        if (watermark == 0) {
            coreNodes = nodes;
            for (Vertex node : coreNodes) {
                node.setCheckLabel(false);
                Cluster label = node.getLabel();
                if (label != null) {
                    label.remove(node.getMc());
                    node.setLabel(null);
                }
            }
        } else {
            double sum = nodes.stream().mapToDouble(Vertex::getDensity).sum();
            double avg;
            for (int i = 0; i < watermark; i++) {
                if (i == 0) {
                    avg = sum / nodes.size();
                    sum = 0.0;
                    for (Vertex node : nodes) {
                        double density = node.getDensity();
                        node.setRep(null);
                        node.setCheckLabel(false);
                        Cluster label = node.getLabel();
                        if (label != null) {
                            label.remove(node.getMc());
                            node.setLabel(null);
                        }
                        if (density >= avg) {
                            node.setPeeled(false);
                            coreNodes.add(node);
                            sum += density;
                        } else {
                            node.setPeeled(true);
                            borderNodes.add(node);
                        }
                    }
                } else {
                    avg = sum / coreNodes.size();
                    sum = 0.0;
                    Iterator<Vertex> nodeIter = coreNodes.iterator();
                    while (nodeIter.hasNext()) {
                        Vertex node = nodeIter.next();
                        double density = node.getDensity();
                        if (density >= avg) {
                            node.setPeeled(false);
                            sum += density;
                        } else {
                            node.setPeeled(true);
                            borderNodes.add(node);
                            nodeIter.remove();
                        }
                    }
                }
            }
        }

        BorderLinkage borderLinkage = new BorderLinkage(borderNodes, augmentedKnnGraph);
        CoreClustering coreClustering = new CoreClustering(coreNodes, augmentedKnnGraph, clusterSet);
        if (nodes.size() < 500) {
            if (watermark > 0) {
                borderLinkage.run();
            }
            coreClustering.run();
        } else {
            if (watermark > 0) {
                CompletableFuture<Void> cf1 = CompletableFuture.runAsync(borderLinkage, executor);
                CompletableFuture<Void> cf2 = CompletableFuture.runAsync(coreClustering, executor);
                CompletableFuture<Void> cfAll = CompletableFuture.allOf(cf1, cf2);
                cfAll.get();
            } else {
                coreClustering.run();
            }
        }

        if (watermark > 0 && borderNodes.size() > 0) {
            for (Vertex v : borderNodes) {
                if (v.isCheckLabel()) {
                    continue;
                }
                adjustBorderAssign(v, clusterSet);
            }
        }

        clusterSet.removeIf(Cluster::isEmpty);
    }

    private void adjustBorderAssign(Vertex v, Set<Cluster> clusterSet) {
        if (v.getRep() == null) {
            if (v.getLabel() == null) {
                Cluster cluster = new Cluster();
                clusterSet.add(cluster);
                v.setLabel(cluster);
                cluster.add(v.getMc());
            }
            v.setCheckLabel(true);
            return;
        }

        adjustBorderAssign(v.getRep(), clusterSet);
        Cluster repLabel = v.getRep().getLabel();
        Cluster label = v.getLabel();
        if (label != repLabel) {
            if (label != null) {
                label.remove(v.getMc());
            }
            v.setLabel(repLabel);
            repLabel.add(v.getMc());
        }
        v.setCheckLabel(true);
    }

    public MicroCluster getNearestMC(final Point p) {
        MicroCluster result = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (Vertex node : nodes) {
            MicroCluster mc = node.getMc();
            double dist = mc.getDistTo(p);
            mc.setDistTmp(dist);
            if (dist < minDist) {
                minDist = dist;
                result = mc;
            }

            periodicDecay(mc, p.getArrivalTime());
        }
        tu = p.getArrivalTime();
        return result;
    }

    private void periodicDecay(MicroCluster mc, final int tc) {
        double decayedWeight = mc.getWeight() * getDecayCoeff(tc);
        mc.setWeight(decayedWeight);
    }

    public double getDecayCoeff(final int tc) {
        return Math.pow(lambda, tc - tu);
    }
}
