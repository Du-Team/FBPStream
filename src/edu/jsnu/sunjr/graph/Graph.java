package edu.jsnu.sunjr.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author sunjr
 * @Date 2022/7/7/0007 14:04
 * @Version 1.0
 */
@Getter
@Setter
public class Graph {
    private Map<Vertex, List<Edge>> graph;
    private int k;

    public Graph(final int k) {
        graph = new HashMap<>();
        this.k = k;
    }

    public Graph(final Map<Vertex, List<Edge>> graph) {
        this.graph = graph;
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    public boolean containVertex(final Vertex v) {
        return graph.keySet().contains(v);
    }

    public int vertexSize() {
        return graph.size();
    }

    public int edgeSize() {
        return graph.values().parallelStream().flatMap(Collection::stream).collect(Collectors.toSet()).size();
    }

    public Set<Vertex> vertexes() {
        return graph.keySet();
    }

    public Set<Edge> edges() {
        return graph.values().parallelStream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public List<Edge> edgeList(final Vertex v) {
        return graph.get(v);
    }

    public Set<Map.Entry<Vertex, List<Edge>>> entries() {
        return graph.entrySet();
    }

    public void putEntry(final Vertex v, final List<Edge> eList) {
        graph.put(v, eList);
    }

    public void addEdge(final Vertex v, final Edge e) {
        graph.get(v).add(e);
    }

    public int insertEdge(final Vertex v, final Edge e) {
        return insertEdge(graph.get(v), e);
    }

    public void remove(final Vertex v) {
        graph.remove(v);
    }

    public int insertEdge(final List<Edge> edgeList, final Edge e) {
        int minIndex = 0;
        if (edgeList.size() > 0) {
            int maxIndex = edgeList.size() - 1;
            while (minIndex <= maxIndex) {
                int middleIndex = (minIndex + maxIndex) / 2;
                double middleEdgeDist = edgeList.get(middleIndex).getLength();
                if (e.getLength() <= middleEdgeDist) {
                    maxIndex = middleIndex - 1;
                } else {
                    minIndex = middleIndex + 1;
                }
            }
        }
        edgeList.add(minIndex, e);
        return minIndex;
    }

    public String visualGraphShape() {
        StringBuilder sb = new StringBuilder();
        if (graph == null) {
            return "visualize graph shape -> graph is null";
        }
        if (graph.isEmpty()) {
            return "visualize graph shape -> graph is null";
        }
        sb.append("visualize graph shape -> [");
        Iterator<Map.Entry<Vertex, List<Edge>>> graphIter = graph.entrySet().iterator();
        while (graphIter.hasNext()) {
            Map.Entry<Vertex, List<Edge>> entry = graphIter.next();
            Vertex vertex = entry.getKey();
            List<Edge> edgeList = entry.getValue();
            if (edgeList.size() < 20) {
                sb.append(edgeList.size());
                sb.append(",");
            }
        }
        sb.append("]");

        String str = sb.toString();
        System.out.println(str);
        return str;
    }

    public String visualGraph() {
        StringBuilder sb = new StringBuilder();
        if (graph == null) {
            return "visualize graph -> graph is null";
        }
        if (graph.isEmpty()) {
            return "visualize graph -> graph is null";
        }
        sb.append("visualize graph -> ");
        Iterator<Map.Entry<Vertex, List<Edge>>> graphIter = graph.entrySet().iterator();
        while (graphIter.hasNext()) {
            Map.Entry<Vertex, List<Edge>> entry = graphIter.next();
            Vertex vertex = entry.getKey();
            List<Edge> edgeList = entry.getValue();
            sb.append("\n");
            sb.append(vertex.toString() + ": [");
            edgeList.forEach(e -> {
                sb.append(e.anotherVertex(vertex).toString());
                sb.append(",");
            });
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
        }

        String str = sb.toString();
        System.out.println(str);
        return str;
    }
}
