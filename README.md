# FBPStream
A Java implementation of "Efficient Online Stream Clustering Based on Fast Peeling of Boundary Micro-Cluster".

## Introduction
FBPStream is a fully online stream clustering algorithm. In summary, the main contributions of this paper are:

- A fully online stream clustering algorithm FBPStream is proposed, which can simultaneously guarantee the clustering efficiency and quality.
- A decay-based kernel density estimation is proposed. It can discover clusters with varying densities and identify the evolving trend of streams well.
- An efficient boundary micro-cluster peeling clustering strategy is proposed to improve the clustering quality of clusters with ambiguous boundaries.

The purposed FBPStream framework is outlined in Fig. 1. With the interplay of the six components, FBPStream can efficiently cluster data streams in real time. The descriptions of all the components are given below.
![framework.](fig/framework.jpg?v=1&type=image)
Fig. 1: fully online framework.

- __Data Stream Absorber:__ It receives each data point from the data stream and summarizes it into an existing micro-cluster (or creates a new one).
- __Outlier Pool:__ It caches inactive micro-clusters which cannot be removed directly from memory because they may be activated in the future.
- __Relationship Propagation Tree (_RP-Tree_):__ It organizes all active micro-clusters to ensure the __Relationship Propagation Rule__ is carried out.
- __Graph Manager:__ It contains an __Augmented $k$-NN Graph__ and a __Logical Mutual $k$-NN Graph__. The Augmented $k$-NN Graph maintains at most $\alpha k$ nearest neighbors incrementally for each active micro-cluster. It is used to accelerate the update of the $k$-NN graph. An abstraction of the Augmented $k$-NN Graph yields the Logical Mutual $k$-NN Graph, which is the basis of the entire algorithm.
- __Watermark-based Peeler:__ It rapidly peels off boundary micro-clusters from active micro-clusters and reveals potential core micro-clusters.
- __Parallel Clustering Executor:__ It efficiently clusters the active micro-clusters in the _RP-Tree_ based on the underlying logical structure (Logical Mutual $k$-NN Graph) and outputs the clustering results.

## Environment
- JDK 1.8
- Apache Maven 3.6.0



```
@article{
  title={Efficient Online Stream Clustering Based on Fast Peeling of Boundary Micro-Cluster},
  author={Sun Jiarui, Du Mingjing, Sun Chen, Dong Yongquan},
  journal={IEEE Transactions on Neural Networks and Learning Systems},
  volume={},
  number={},
  pages={},
  year={},
  doi={}
}
```

