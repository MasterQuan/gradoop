/*
 * Copyright © 2014 - 2020 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradoop.flink.model.impl.layouts.gve.indexed;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.EPGMEdge;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.common.model.impl.pojo.EPGMGraphHead;
import org.gradoop.flink.model.impl.layouts.gve.GVELayout;
import org.gradoop.flink.model.impl.layouts.gve.GVELayoutTest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class IndexedGVELayoutTest extends GVELayoutTest {

  @Override
  protected GVELayout from(Collection<EPGMGraphHead> graphHeads, Collection<EPGMVertex> vertices,
    Collection<EPGMEdge> edges) {
    Map<String, DataSet<EPGMGraphHead>> indexedGraphHeads = graphHeads.stream()
      .collect(Collectors.groupingBy(EPGMGraphHead::getLabel)).entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> getExecutionEnvironment().fromCollection(e.getValue())));

    Map<String, DataSet<EPGMVertex>> indexedVertices = vertices.stream()
      .collect(Collectors.groupingBy(EPGMVertex::getLabel)).entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> getExecutionEnvironment().fromCollection(e.getValue())));

    Map<String, DataSet<EPGMEdge>> indexedEdges = edges.stream()
      .collect(Collectors.groupingBy(EPGMEdge::getLabel)).entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> getExecutionEnvironment().fromCollection(e.getValue())));

    return new IndexedGVELayout(indexedGraphHeads, indexedVertices, indexedEdges);
  }

  @Override
  public void isIndexedGVELayout() throws Exception {
    assertTrue(from(asList(g0, g1), asList(v0, v1, v2), asList(e0, e1)).isIndexedGVELayout());
  }
}
