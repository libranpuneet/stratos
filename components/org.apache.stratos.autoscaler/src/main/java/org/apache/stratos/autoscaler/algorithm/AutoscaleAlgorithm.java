/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.autoscaler.algorithm;

import org.apache.stratos.autoscaler.context.cluster.ClusterInstanceContext;
import org.apache.stratos.autoscaler.context.partition.network.ClusterLevelNetworkPartitionContext;
import org.apache.stratos.cloud.controller.stub.domain.Partition;


/**
 * This interface is should be implemented by all the algorithms that are there to select partitions of a particular
 * partition group
 */
public interface AutoscaleAlgorithm {

    /**
     * Returns whether there is available {@link Partition} to scale up considering the current count and maximum
     * @param clusterId Id of the cluster which need the availability information
     * @return availability of {@link Partition}s to scale up
     */
    public boolean scaleUpPartitionAvailable(String clusterId);

    /**
     * Returns whether there is available {@link Partition} to scale down considering the current count and minimum
     * @param clusterId Id of the cluster which need the availability information
     * @return availability of {@link Partition}s to scale down
     */
    public boolean scaleDownPartitionAvailable(String clusterId);

    /**
     * Returns a {@link Partition} to scale up from the given {@link org.apache.stratos.autoscaler.pojo.policy.deployment.partition.network.NetworkPartition} according to algorithm
     * @param clusterInstanceContext {@link org.apache.stratos.autoscaler.context.partition.network.ClusterLevelNetworkPartitionContext} which need the {@link Partition}
     * @param clusterId Id of the cluster which need the {@link Partition}
     * @return {@link Partition} to scale up
     */
    public Partition getNextScaleUpPartition(ClusterInstanceContext clusterInstanceContext, String clusterId);

    /**
     * Returns a {@link Partition} to scale down from the given {@link org.apache.stratos.autoscaler.pojo.policy.deployment.partition.network.NetworkPartition} according to algorithm
     * @param clusterInstanceContext {@link org.apache.stratos.autoscaler.context.partition.network.ClusterLevelNetworkPartitionContext} which need the {@link Partition}
     * @param clusterId Id of the cluster which need the {@link Partition}
     * @return {@link Partition} to scale down
     */
    public Partition getNextScaleDownPartition(ClusterInstanceContext clusterInstanceContext, String clusterId);
}
