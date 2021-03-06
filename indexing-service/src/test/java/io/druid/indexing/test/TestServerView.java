/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.indexing.test;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.metamx.common.Pair;
import io.druid.client.FilteredServerView;
import io.druid.client.ServerView;
import io.druid.server.coordination.DruidServerMetadata;
import io.druid.timeline.DataSegment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class TestServerView implements FilteredServerView, ServerView.SegmentCallback
{
  final ConcurrentMap<ServerView.SegmentCallback, Pair<Predicate<DataSegment>, Executor>> callbacks = Maps.newConcurrentMap();

  @Override
  public void registerSegmentCallback(
      final Executor exec,
      final ServerView.SegmentCallback callback,
      final Predicate<DataSegment> filter
  )
  {
    callbacks.put(callback, Pair.of(filter, exec));
  }

  @Override
  public ServerView.CallbackAction segmentAdded(
      final DruidServerMetadata server,
      final DataSegment segment
  )
  {
    for (final Map.Entry<ServerView.SegmentCallback, Pair<Predicate<DataSegment>, Executor>> entry : callbacks.entrySet()) {
      if (entry.getValue().lhs.apply(segment)) {
        entry.getValue().rhs.execute(
            new Runnable()
            {
              @Override
              public void run()
              {
                entry.getKey().segmentAdded(server, segment);
              }
            }
        );
      }
    }

    return ServerView.CallbackAction.CONTINUE;
  }

  @Override
  public ServerView.CallbackAction segmentRemoved(
      final DruidServerMetadata server,
      final DataSegment segment
  )
  {
    for (final Map.Entry<ServerView.SegmentCallback, Pair<Predicate<DataSegment>, Executor>> entry : callbacks.entrySet()) {
      if (entry.getValue().lhs.apply(segment)) {
        entry.getValue().rhs.execute(
            new Runnable()
            {
              @Override
              public void run()
              {
                entry.getKey().segmentRemoved(server, segment);
              }
            }
        );
      }
    }

    return ServerView.CallbackAction.CONTINUE;
  }

  @Override
  public ServerView.CallbackAction segmentViewInitialized()
  {
    for (final Map.Entry<ServerView.SegmentCallback, Pair<Predicate<DataSegment>, Executor>> entry : callbacks.entrySet()) {
      entry.getValue().rhs.execute(
          new Runnable()
          {
            @Override
            public void run()
            {
              entry.getKey().segmentViewInitialized();
            }
          }
      );
    }

    return ServerView.CallbackAction.CONTINUE;
  }
}
