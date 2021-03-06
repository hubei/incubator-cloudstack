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

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(description="Deletes an ISO file.", responseObject=SuccessResponse.class)
public class DeleteIsoCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteIsoCmd.class.getName());
    private static final String s_name = "deleteisosresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="vm_template")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required=true, description="the ID of the ISO file")
    private Long id;

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, description="the ID of the zone of the ISO file. If not specified, the ISO will be deleted from all the zones")
    private Long zoneId;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }
    
    public static String getStaticName() {
        return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        VirtualMachineTemplate iso = _entityMgr.findById(VirtualMachineTemplate.class, getId());
        if (iso != null) {
            return iso.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ISO_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting iso " + getId();
    }
    
    public AsyncJob.Type getInstanceType() {
    	return AsyncJob.Type.Iso;
    }
    
    public Long getInstanceId() {
    	return getId();
    }
    
    @Override
    public void execute(){
    	UserContext.current().setEventDetails("ISO Id: "+getId());
        boolean result = _templateService.deleteIso(this);
        if (result) {
            SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete iso");
        }
    }
}
