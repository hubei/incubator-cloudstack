// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.VlanIpRangeResponse;
import com.cloud.dc.Vlan;

@Implementation(description="Lists all VLAN IP ranges.", responseObject=VlanIpRangeResponse.class)
public class ListVlanIpRangesCmd extends BaseListCmd {
	public static final Logger s_logger = Logger.getLogger(ListVlanIpRangesCmd.class.getName());

    private static final String s_name = "listvlaniprangesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ACCOUNT, type=CommandType.STRING, description="the account with which the VLAN IP range is associated. Must be used with the domainId parameter.")
    private String accountName;
    
    @IdentityMapper(entityTableName="projects")
    @Parameter(name=ApiConstants.PROJECT_ID, type=CommandType.LONG, description="project who will own the VLAN")
    private Long projectId;

    @IdentityMapper(entityTableName="domain")
    @Parameter(name=ApiConstants.DOMAIN_ID, type=CommandType.LONG, description="the domain ID with which the VLAN IP range is associated.  If used with the account parameter, returns all VLAN IP ranges for that account in the specified domain.")
    private Long domainId;

    @IdentityMapper(entityTableName="vlan")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required=false, description="the ID of the VLAN IP range")
    private Long id;

    @IdentityMapper(entityTableName="host_pod_ref")
    @Parameter(name=ApiConstants.POD_ID, type=CommandType.LONG, description="the Pod ID of the VLAN IP range")
    private Long podId;

    @Parameter(name=ApiConstants.VLAN, type=CommandType.STRING, description="the ID or VID of the VLAN. Default is an \"untagged\" VLAN.")
    private String vlan;

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, description="the Zone ID of the VLAN IP range")
    private Long zoneId;
    
    @IdentityMapper(entityTableName="networks")
    @Parameter(name=ApiConstants.NETWORK_ID, type=CommandType.LONG, description="network id of the VLAN IP range")
    private Long networkId;
    
    @Parameter(name=ApiConstants.FOR_VIRTUAL_NETWORK, type=CommandType.BOOLEAN, description="true if VLAN is of Virtual type, false if Direct")
    private Boolean forVirtualNetwork;
    
    @IdentityMapper(entityTableName="physical_network")
    @Parameter(name=ApiConstants.PHYSICAL_NETWORK_ID, type=CommandType.LONG, description="physical network id of the VLAN IP range")
    private Long physicalNetworkId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getId() {
        return id;
    }

    public Long getPodId() {
        return podId;
    }

    public String getVlan() {
        return vlan;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getNetworkId() {
        return networkId;
    }
    
    public Boolean getForVirtualNetwork() {
		return forVirtualNetwork;
	}
    
    public Long getProjectId() {
        return projectId;
    }
    
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }
    
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    
	@Override
    public String getCommandName() {
        return s_name;
    }
    
    @Override
    public void execute(){
        List<? extends Vlan> vlans = _mgr.searchForVlans(this);
        ListResponse<VlanIpRangeResponse> response = new ListResponse<VlanIpRangeResponse>();
        List<VlanIpRangeResponse> vlanResponses = new ArrayList<VlanIpRangeResponse>();
        for (Vlan vlan : vlans) {  
            VlanIpRangeResponse vlanResponse = _responseGenerator.createVlanIpRangeResponse(vlan);
            vlanResponse.setObjectName("vlaniprange");
            vlanResponses.add(vlanResponse);
        }

        response.setResponses(vlanResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}
