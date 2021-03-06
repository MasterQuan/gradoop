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
package org.gradoop.flink.model.impl.functions.epgm;

import com.google.common.collect.Sets;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.EPGMGraphElement;
import org.gradoop.common.model.impl.id.GradoopId;

import java.util.Set;

/**
 * {@code (graphId, element),.. => (graphId, {element,..})}
 *
 * @param <EL> graph element type
 */
public class GraphElementSet<EL extends EPGMGraphElement> implements
  GroupReduceFunction<Tuple2<GradoopId, EL>, Tuple2<GradoopId, Set<EL>>> {

  @Override
  public void reduce(Iterable<Tuple2<GradoopId, EL>> iterable,
    Collector<Tuple2<GradoopId, Set<EL>>> collector) throws Exception {

    boolean first = true;
    GradoopId graphId = null;

    Set<EL> elements = Sets.newHashSet();

    for (Tuple2<GradoopId, EL> elementPair : iterable) {

      if (first) {
        graphId = elementPair.f0;
        first = false;
      }

      elements.add(elementPair.f1);
    }

    collector.collect(new Tuple2<>(graphId, elements));
  }
}
