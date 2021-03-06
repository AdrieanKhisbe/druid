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

package io.druid.query;

import com.metamx.common.guava.Sequence;

import java.util.Map;

/**
 * If there's a subquery, run it instead of the outer query
 */
public class SubqueryQueryRunner<T> implements QueryRunner<T>
{
  private final QueryRunner<T> baseRunner;

  public SubqueryQueryRunner(QueryRunner<T> baseRunner)
  {
    this.baseRunner = baseRunner;
  }

  @Override
  public Sequence<T> run(final Query<T> query, Map<String, Object> responseContext)
  {
    DataSource dataSource = query.getDataSource();
    if (dataSource instanceof QueryDataSource) {
      return run((Query<T>) ((QueryDataSource) dataSource).getQuery(), responseContext);
    } else {
      return baseRunner.run(query, responseContext);
    }
  }
}
