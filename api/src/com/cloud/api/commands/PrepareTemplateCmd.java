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

import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;

@Implementation(responseObject=TemplateResponse.class, description="load template into primary storage")
public class PrepareTemplateCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(PrepareTemplateCmd.class.getName());

    private static final String s_name = "preparetemplateresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, required=true, type=CommandType.LONG, description="zone ID of the template to be prepared in primary storage(s).")
    private Long zoneId;
    
    @IdentityMapper(entityTableName="vm_template")
    @Parameter(name=ApiConstants.TEMPLATE_ID, required=true, type=CommandType.LONG, description="template ID of the template to be prepared in primary storage(s).")
    private Long templateId;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    
    public Long getZoneId() {
    	return zoneId;
    }
    
    public Long getTemplateId() {
        return templateId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
    
    @Override
    public void execute() {
        ListResponse<TemplateResponse> response = new ListResponse<TemplateResponse>();
    	
    	VirtualMachineTemplate vmTemplate = _templateService.prepareTemplate(templateId, zoneId);
    	List<TemplateResponse> templateResponses = _responseGenerator.createTemplateResponses(vmTemplate.getId(), zoneId, true);
        response.setResponses(templateResponses);
        response.setResponseName(getCommandName());              
        this.setResponseObject(response);
    }
}

