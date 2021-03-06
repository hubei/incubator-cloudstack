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
package com.cloud.vm;

import java.util.List;
import java.util.Map;

import com.cloud.agent.api.to.VolumeTO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.ServiceOffering;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.user.Account;

/**
 * VirtualMachineProfile describes one virtual machine. This object
 * on what the virtual machine profile should look like before it is
 * actually started on the hypervisor.
 * 
 * @param <T>
 *            a VirtualMachine
 */
public interface VirtualMachineProfile<T extends VirtualMachine> {

    public static class Param {

        public static final Param VmPassword = new Param("VmPassword");
        public static final Param ControlNic = new Param("ControlNic");
        public static final Param ReProgramNetwork = new Param("RestartNetwork");
        public static final Param PxeSeverType = new Param("PxeSeverType");
        public static final Param HaTag = new Param("HaTag");


        private String name;

        public Param(String name) {
            synchronized (Param.class) {
                this.name = name;
            }
        }

        public String getName() {
            return name;
        }
    }

    String getHostName();

    String getInstanceName();

    Account getOwner();

    /**
     * @return the virtual machine that backs up this profile.
     */
    T getVirtualMachine();

    /**
     * @return service offering for this virtual machine.
     */
    ServiceOffering getServiceOffering();

    /**
     * @return parameter specific for this type of virtual machine.
     */
    Object getParameter(Param name);

    /**
     * @return the hypervisor type needed for this virtual machine.
     */
    HypervisorType getHypervisorType();

    /**
     * @return template the virtual machine is based on.
     */
    VirtualMachineTemplate getTemplate();

    /**
     * @return the template id
     */
    long getTemplateId();

    /**
     * @return the service offering id
     */
    long getServiceOfferingId();

    /**
     * @return virtual machine id.
     */
    long getId();

    List<NicProfile> getNics();

    List<VolumeTO> getDisks();

    void addNic(int index, NicProfile nic);

    void addDisk(int index, VolumeTO disk);

    StringBuilder getBootArgsBuilder();

    void addBootArgs(String... args);

    String getBootArgs();

    void addNic(NicProfile nic);

    void addDisk(VolumeTO disk);

    VirtualMachine.Type getType();

    void setParameter(Param name, Object value);

    void setBootLoaderType(BootloaderType bootLoader);

    BootloaderType getBootLoaderType();

    Map<Param, Object> getParameters();
}
