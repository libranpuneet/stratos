/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.messaging.event.topology;

import org.apache.stratos.messaging.domain.topology.ClusterDataHolder;

import java.util.Set;

/**
 * This event will be sent to Topology upon termination of application
 */
public class ApplicationTerminatedEvent extends TopologyEvent {
    private final String appId;
    private final Set<ClusterDataHolder> clusterData;
    private String tenantDomain;
    private int tenantId;

    public ApplicationTerminatedEvent(String appId, Set<ClusterDataHolder> clusterData,
                                      int tenantId, String tenantDomain) {
        this.appId = appId;
        this.clusterData = clusterData;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
    }

    public String getAppId() {
        return appId;
    }

    public Set<ClusterDataHolder> getClusterData() {
        return clusterData;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }
}