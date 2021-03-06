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
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.SwiftResponse;
import com.cloud.storage.Swift;
import com.cloud.user.Account;

@Implementation(description = "List Swift.", responseObject = HostResponse.class, since="3.0.0")
public class ListSwiftsCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(ListSwiftsCmd.class.getName());
    private static final String s_name = "listswiftsresponse";
     
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the id of the swift")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }


    @Override
    public String getCommandName() {
    	return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
    
    @Override
    public void execute(){
        List<? extends Swift> result = _resourceService.listSwifts(this);
        ListResponse<SwiftResponse> response = new ListResponse<SwiftResponse>();
        List<SwiftResponse> swiftResponses = new ArrayList<SwiftResponse>();

        if (result != null) {
            SwiftResponse swiftResponse = null;
            for (Swift swift : result) {
                swiftResponse = _responseGenerator.createSwiftResponse(swift);
                swiftResponse.setResponseName(getCommandName());
                swiftResponse.setObjectName("swift");
                swiftResponses.add(swiftResponse);
            }
        }
        response.setResponses(swiftResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }
}
