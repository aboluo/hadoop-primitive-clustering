package com.aamend.hadoop.mahout.sequence.mapreduce;

import com.aamend.hadoop.mahout.sequence.cluster.SequenceAbstractCluster;
import com.aamend.hadoop.mahout.sequence.cluster.SequenceCanopy;
import com.aamend.hadoop.mahout.sequence.cluster.SequenceCanopyConfigKeys;
import com.aamend.hadoop.mahout.sequence.distance.SequenceDistanceMeasure;
import com.aamend.hadoop.mahout.sequence.distance.SequenceLevenshteinDistanceMeasure;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ArrayPrimitiveWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class SequenceCanopyCreateMapper extends
        Mapper<Text, ArrayPrimitiveWritable, Text,
                ArrayPrimitiveWritable> {

    private float t1;
    private float t2;
    private int nextCanopyId;
    private SequenceDistanceMeasure measure;
    private Collection<SequenceCanopy> canopies = Lists.newArrayList();

    private static final Text KEY = new Text();
    private static final String COUNTER = "data";
    private static final String COUNTER_CANOPY = "canopies";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SequenceCanopyCreateMapper.class);

    @Override
    protected void setup(Context context)
            throws IOException, InterruptedException {

        // Retrieve params fom configuration
        Configuration conf = context.getConfiguration();
        t1 = conf.getFloat(SequenceCanopyConfigKeys.T1_KEY, 1.0f);
        t2 = conf.getFloat(SequenceCanopyConfigKeys.T2_KEY, 0.8f);

        LOGGER.info("Configuring distance with T1, T2 = {}, {}", t1, t2);

        // Configure distance measure
        measure = SequenceCanopyConfigKeys
                .configureSequenceDistanceMeasure(context.getConfiguration());
    }

    @Override
    protected void map(Text key,
                       ArrayPrimitiveWritable value, Context context)
            throws IOException, InterruptedException {

        // Add this point to canopies
        int[] point = (int[]) value.get();
        boolean newCanopy = addPointToCanopies(point, context);
        if (newCanopy) {
            context.getCounter(COUNTER, COUNTER_CANOPY).increment(1L);
        }
    }

    public boolean addPointToCanopies(int[] point,
                                      Context context)
            throws IOException, InterruptedException {

        boolean stronglyBound = false;
        for (SequenceCanopy sequenceCanopy : canopies) {
            double dist = measure.distance(sequenceCanopy.getCenter(), point);
            if (dist < t1) {
                sequenceCanopy.observe(point);
                KEY.set(Arrays.toString(point));
                context.write(KEY,
                        new ArrayPrimitiveWritable(sequenceCanopy.getCenter()));
            }
            stronglyBound = stronglyBound || dist < t2;
        }
        if (!stronglyBound) {
            nextCanopyId++;
            canopies.add(new SequenceCanopy(point, nextCanopyId, measure));
            KEY.set(Arrays.toString(point));
            context.write(KEY, new ArrayPrimitiveWritable(point));
        }

        return !stronglyBound;
    }
}
