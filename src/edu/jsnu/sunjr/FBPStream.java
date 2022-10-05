package edu.jsnu.sunjr;

import edu.jsnu.sunjr.utils.FileWriterUtil;
import edu.jsnu.sunjr.utils.ValidateUtil;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author sunjr
 * @Date 2022/6/18/0018 14:14
 * @Version 1.0
 */

@Getter
@Setter
public class FBPStream {
    private int dim;
    private double lambda;
    private double radius;
    private double beta;
    private int k;
    private int alpha;
    private int watermark;
    private double minWeight;
    private int tolerantT;

    private int mcId = 0;
    private static OutlierPool outlierPool;
    private static RPTree rpTree;
    private static Set<Cluster> clusterSet;
    private static ExecutorService executor;

    public static void main(String[] args) {
        String simpleClassName = FBPStream.class.getSimpleName();
        String inputFilePath = "data/Stream4.csv";
        String outputDirPath = ".";

        int dim = 2;
        int dataUse = 110000;
        int outputTimeInterval = 5000;
        double radius = 0.02;
        double lambda = 0.998;
        double beta = 1 - lambda + 0.0001;
        int k = 15;
        int alpha = 3;
        int watermark = 3;

        FBPStream dbpStream = new FBPStream(dim, lambda, radius, beta, k, alpha, watermark, dataUse, outputTimeInterval);
        long start = System.currentTimeMillis();
        dbpStream.process(inputFilePath, outputDirPath, dataUse, outputTimeInterval);
        long end = System.currentTimeMillis();
        executor.shutdown();
        System.out.println(simpleClassName + " clustering execute over! >>> total cost time(ms): " + (end - start));
    }

    public FBPStream(int dim, double lambda, double radius, double beta, int k, int alpha, int watermark, int dataUse, int outputTimeInterval) {
        this.dim = dim;
        this.lambda = lambda;
        this.radius = radius;
        this.beta = beta;
        minWeight = beta / (1 - lambda);
        tolerantT = (int) (Math.log(1 - lambda) / Math.log(lambda) - Math.log(beta) / Math.log(lambda));
        outlierPool = new OutlierPool(tolerantT);
        this.k = k;
        this.alpha = alpha;
        this.watermark = watermark;
        executor = Executors.newFixedThreadPool(2);
        rpTree = new RPTree(k, alpha, watermark, lambda, minWeight, executor);
        clusterSet = new HashSet<>();

        ValidateUtil.validateParams_FBPStream(dim, lambda, radius, beta, minWeight, tolerantT, k, alpha, watermark, dataUse, outputTimeInterval);
    }

    public void process(String inputFilePath, String outputDirPath, int dataUse, int outputTimeInterval) {
        String separator = System.getProperty("file.separator");

        StringBuilder builder = new StringBuilder();
        builder.append(outputDirPath).append(separator).append("point2MC").append(".csv");
        String point2MCFilePath = builder.toString();

        builder.setLength(0);
        builder.append(outputDirPath).append(separator).append("MC2Cluster");
        String MC2ClusterFilePath = builder.toString();

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(inputFilePath));
            bw = new BufferedWriter(new FileWriter(point2MCFilePath));

            long start = System.currentTimeMillis();
            int tc = 0;
            String line = null;
            while ((line = br.readLine()) != null && tc < dataUse) {
                StringTokenizer stk = new StringTokenizer(line, ",");
                double[] pointCoord = new double[dim];
                for (int i = 0; i < dim; i++) {
                    pointCoord[i] = Double.parseDouble(stk.nextToken());
                }
                Point p = new Point(tc, tc , pointCoord);

                incrementalUpdate(p);

                tc++;

                bw.write(p.getId() + "," + p.getMcId() + "\n");

                if (p.getId() > 0 && p.getId() % outputTimeInterval == 0) {
                    String resFilePath = MC2ClusterFilePath + p.getId() + ".csv";
                    FileWriterUtil.MC2Cluster(resFilePath, clusterSet, radius);
                    long end = System.currentTimeMillis();
                    System.out.println("clustering 0 - " + p.getId() + " >>> cost time(ms): " + (end - start));
                    System.out.println("---------------------------------------------------------------------");
                }
            }

            String resFilePath = MC2ClusterFilePath + "final" + ".csv";
            FileWriterUtil.MC2Cluster(resFilePath, clusterSet, radius);
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void incrementalUpdate(Point p) throws ExecutionException, InterruptedException {
        boolean updated = false;
        boolean increaseTrigger = false;
        boolean decreaseTrigger = false;
        if (rpTree.size() > 0) {
            MicroCluster mc = rpTree.getNearestMC(p);
            double dist = mc.getDistTmp();
            if (dist <= radius) {
                mc.add(lambda, p, false);
                updated = true;
                increaseTrigger = true;
            }
            decreaseTrigger = rpTree.checkActive(outlierPool);
        }

        if (!updated && outlierPool.size() > 0) {
            MicroCluster mc = outlierPool.getNearestMC(p);
            if (mc != null) {
                double dist = mc.getDistTmp();
                if (dist <= radius) {
                    mc.add(lambda, p, true);
                    if (mc.getWeight() >= minWeight) {
                        mc.setActive(true);
                        outlierPool.remove(mc);
                        rpTree.add(mc);
                        increaseTrigger = true;
                    }
                    updated = true;
                }
            }
        }

        if (!updated) {
            MicroCluster mc = new MicroCluster(mcId++, p);
            outlierPool.add(mc);
        }

        if (increaseTrigger || decreaseTrigger) {
            rpTree.adjustCluster(clusterSet);
        }
    }
}


