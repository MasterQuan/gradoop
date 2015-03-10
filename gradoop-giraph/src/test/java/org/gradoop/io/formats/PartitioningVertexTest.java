package org.gradoop.io.formats;

import com.google.common.collect.Maps;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.utils.InternalVertexRunner;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.gradoop.io.PartitioningVertex;
import org.junit.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Test Class for PartitioningVertex
 */
public class PartitioningVertexTest {
  private static final Pattern LINE_TOKEN_SEPARATOR = Pattern.compile(" ");

  @Test
  public void testSmallConnectedGraph() throws Exception {
    String[] graph = getSmallConnectedGraph();
    validateSmallConnectedGraphResult(computeResults(graph));
  }

  /**
   * @return a small graph with two connected partitions
   */
  protected String[] getSmallConnectedGraph() {
    return new String[]{"0 0 7 0 1",
                        "1 0 6 0 0",
                        "2 0 5 0 3",
                        "3 0 4 0 2",
                        "4 0 3 0 5",
                        "5 0 2 0 4",
                        "6 0 1 0 7",
                        "7 0 0 0 6"};
  }

  private void validateSmallConnectedGraphResult(
    Map<Integer, Integer> vertexIDwithValue) {
    assertEquals(0, vertexIDwithValue.get(0).intValue());
    assertEquals(0, vertexIDwithValue.get(1).intValue());
    assertEquals(0, vertexIDwithValue.get(2).intValue());
    assertEquals(0, vertexIDwithValue.get(3).intValue());
    assertEquals(0, vertexIDwithValue.get(4).intValue());
    assertEquals(0, vertexIDwithValue.get(5).intValue());
    assertEquals(0, vertexIDwithValue.get(6).intValue());
    assertEquals(0, vertexIDwithValue.get(7).intValue());
  }

  private Map<Integer, Integer> computeResults(String[] graph) throws
    Exception {
    GiraphConfiguration conf = getConfiguration();
    Iterable<String> results = InternalVertexRunner.run(conf, graph);
    return parseResults(results);
  }

  private GiraphConfiguration getConfiguration() {
    GiraphConfiguration conf = new GiraphConfiguration();
    conf.setComputationClass(GetLastValueComputation.class);
    conf.setVertexInputFormatClass(AdaptiveRepartitioningInputFormat.class);
    conf.setVertexOutputFormatClass(AdaptiveRepartitioningOutputFormat.class);
    return conf;
  }

  private Map<Integer, Integer> parseResults(Iterable<String> results) {
    Map<Integer, Integer> parsedResults = Maps.newHashMap();
    String[] lineTokens;
    int value;
    int vertexID;
    for (String line : results) {
      lineTokens = LINE_TOKEN_SEPARATOR.split(line);
      vertexID = Integer.parseInt(lineTokens[0]);
      value = Integer.parseInt(lineTokens[1]);
      parsedResults.put(vertexID, value);
    }
    return parsedResults;
  }

  /**
   * Example Computation that get the LastValue and save it as CurrentValue
   */
  public static class GetLastValueComputation extends
    BasicComputation<IntWritable, PartitioningVertex, NullWritable,
      IntWritable> {
    @Override
    public void compute(
      Vertex<IntWritable, PartitioningVertex, NullWritable> vertex,
      Iterable<IntWritable> messages) {
      if (getSuperstep() == 0) {
        sendMessageToAllEdges(vertex, vertex.getValue().getDesiredPartition());
        vertex.voteToHalt();
      } else {
        int currentValue = vertex.getValue().getCurrentPartition().get();
        for (IntWritable lastValue : messages) {
          if (currentValue < lastValue.get()) {
            vertex.getValue().setCurrentPartition(lastValue);
            sendMessageToAllEdges(vertex,
              vertex.getValue().getCurrentPartition());
            vertex.voteToHalt();
          }
        }
      }
      vertex.voteToHalt();
    }
  }
}
