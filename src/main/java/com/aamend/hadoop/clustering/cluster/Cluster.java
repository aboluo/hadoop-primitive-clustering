package com.aamend.hadoop.clustering.cluster;

import com.aamend.hadoop.clustering.distance.DistanceMeasure;

import java.util.List;

/**
 * Author: antoine.amend@gmail.com
 * Date: 21/03/14
 */
public interface Cluster {

    // default directory for all clustered points
    String CLUSTERED_POINTS_DIR = "clustered-points";

    // default directory for output of clusters per iteration
    String CLUSTERS_TMP_DIR = "clusters-tmp-";

    // final directory for output of clusters per iteration
    String CLUSTERS_FINAL_DIR = "clusters-final";

    double pdf(int[] x, DistanceMeasure measure);

    void observe(long number);

    void computeCenter(List<int[]> centers, DistanceMeasure measure);

    int getId();

    int[] getCenter();

    long getNum();

    String asFormattedString();

}