package edu.jsnu.sunjr.utils;

import edu.jsnu.sunjr.Cluster;
import edu.jsnu.sunjr.MicroCluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * @Author sunjr
 * @Date 2022/4/21/0021 10:40
 * @Version 1.0
 */
public class FileWriterUtil {

    public static void MC2Cluster(
            String resFilePath, Set<Cluster> clusterSet, double radius) throws IOException {
        System.out.println("generate clustering res >>> " + resFilePath);
        BufferedWriter bw = new BufferedWriter(new FileWriter(resFilePath));
        for (Cluster cluster : clusterSet) {
            for (MicroCluster mc : cluster.getSet()) {
                bw.write(mc.getId() + "," + cluster.getId() + "\n");
            }
        }
        bw.close();
    }
}
