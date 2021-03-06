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
package com.cloud.storage.allocator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;

//
// TODO
// Rush to make LocalStoragePoolAllocator use static allocation status, we should revisit the overall
// allocation process to make it more reliable in next release. The code put in here is pretty ugly
//
@Local(value = StoragePoolAllocator.class)
public class LocalStoragePoolAllocator extends FirstFitStoragePoolAllocator {
    private static final Logger s_logger = Logger.getLogger(LocalStoragePoolAllocator.class);

    @Inject
    StoragePoolHostDao _poolHostDao;
    @Inject
    VMInstanceDao _vmInstanceDao;
    @Inject
    UserVmDao _vmDao;
    @Inject
    ServiceOfferingDao _offeringDao;
    @Inject
    CapacityDao _capacityDao;
    @Inject
    ConfigurationDao _configDao;

    protected GenericSearchBuilder<VMInstanceVO, Long> VmsOnPoolSearch;

    private int _secondsToSkipStoppedVMs = 86400;

    @Override
    public boolean allocatorIsCorrectType(DiskProfile dskCh) {
        return localStorageAllocationNeeded(dskCh);
    }

    @Override
    public List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, ExcludeList avoid, int returnUpTo) {

        List<StoragePool> suitablePools = new ArrayList<StoragePool>();

        // Check that the allocator type is correct
        if (!allocatorIsCorrectType(dskCh)) {
            return suitablePools;
        }

        ExcludeList myAvoids = new ExcludeList(avoid.getDataCentersToAvoid(), avoid.getPodsToAvoid(), avoid.getClustersToAvoid(), avoid.getHostsToAvoid(), avoid.getPoolsToAvoid());

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("LocalStoragePoolAllocator trying to find storage pool to fit the vm");
        }

        List<StoragePool> availablePool;
        while (!(availablePool = super.allocateToPool(dskCh, vmProfile, plan, myAvoids, 1)).isEmpty()) {
            StoragePool pool = availablePool.get(0);
            myAvoids.addPool(pool.getId());
            List<StoragePoolHostVO> hostsInSPool = _poolHostDao.listByPoolId(pool.getId());
            assert (hostsInSPool.size() == 1) : "Local storage pool should be one host per pool";

            s_logger.debug("Found suitable local storage pool " + pool.getId() + ", adding to list");
            suitablePools.add(pool);

            if (suitablePools.size() == returnUpTo) {
                break;
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("LocalStoragePoolAllocator returning " + suitablePools.size() + " suitable storage pools");
        }

        if (suitablePools.isEmpty()) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to find storage pool to fit the vm");
            }
        }
        return suitablePools;
    }

    // we don't need to check host capacity now, since hostAllocators will do that anyway
    private boolean hostHasCpuMemoryCapacity(long hostId, List<Long> vmOnHost, VMInstanceVO vm) {

        ServiceOffering so = _offeringDao.findById(vm.getServiceOfferingId());

        long usedMemory = calcHostAllocatedCpuMemoryCapacity(vmOnHost, CapacityVO.CAPACITY_TYPE_MEMORY);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Calculated static-allocated memory for VMs on host " + hostId + ": " + usedMemory + " bytes, requesting memory: " + (so != null ? so.getRamSize() * 1024L * 1024L : "")
                    + " bytes");
        }

        SearchCriteria<CapacityVO> sc = _capacityDao.createSearchCriteria();
        sc.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, hostId);
        sc.addAnd("capacityType", SearchCriteria.Op.EQ, CapacityVO.CAPACITY_TYPE_MEMORY);
        List<CapacityVO> capacities = _capacityDao.search(sc, null);
        if (capacities.size() > 0) {
            if (capacities.get(0).getTotalCapacity() < usedMemory + (so != null ? so.getRamSize() * 1024L * 1024L : 0)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host " + hostId + " runs out of memory capacity");
                }
                return false;
            }
        } else {
            s_logger.warn("Host " + hostId + " has not reported memory capacity yet");
            return false;
        }

        long usedCpu = calcHostAllocatedCpuMemoryCapacity(vmOnHost, CapacityVO.CAPACITY_TYPE_CPU);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Calculated static-allocated CPU for VMs on host " + hostId + ": " + usedCpu + " GHz, requesting cpu: " + (so != null ? so.getCpu() * so.getSpeed() : "") + " GHz");
        }

        sc = _capacityDao.createSearchCriteria();
        sc.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, hostId);
        sc.addAnd("capacityType", SearchCriteria.Op.EQ, CapacityVO.CAPACITY_TYPE_CPU);
        capacities = _capacityDao.search(sc, null);
        if (capacities.size() > 0) {
            if (capacities.get(0).getTotalCapacity() < usedCpu + (so != null ? so.getCpu() * so.getSpeed() : 0)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host " + hostId + " runs out of CPU capacity");
                }
                return false;
            }
        } else {
            s_logger.warn("Host " + hostId + " has not reported CPU capacity yet");
            return false;
        }

        return true;
    }

    private boolean skipCalculation(VMInstanceVO vm) {
        if (vm == null) {
            return true;
        }

        if (vm.getState() == State.Expunging) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Skip counting capacity for Expunging VM : " + vm.getInstanceName());
            }
            return true;
        }

        if (vm.getState() == State.Destroyed && vm.getType() != VirtualMachine.Type.User) {
            return true;
        }

        if (vm.getState() == State.Stopped || vm.getState() == State.Destroyed) {
            // for stopped/Destroyed VMs, we will skip counting it if it hasn't been used for a while

            long millisecondsSinceLastUpdate = DateUtil.currentGMTTime().getTime() - vm.getUpdateTime().getTime();
            if (millisecondsSinceLastUpdate > _secondsToSkipStoppedVMs * 1000L) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Skip counting vm " + vm.getInstanceName() + " in capacity allocation as it has been stopped for " + millisecondsSinceLastUpdate / 60000 + " minutes");
                }
                return true;
            }
        }
        return false;
    }

    private long calcHostAllocatedCpuMemoryCapacity(List<Long> vmOnHost, short capacityType) {
        assert (capacityType == CapacityVO.CAPACITY_TYPE_MEMORY || capacityType == CapacityVO.CAPACITY_TYPE_CPU) : "Invalid capacity type passed in calcHostAllocatedCpuCapacity()";

        long usedCapacity = 0;
        for (Long vmId : vmOnHost) {
            VMInstanceVO vm = _vmInstanceDao.findById(vmId);
            if (skipCalculation(vm)) {
                continue;
            }

            ServiceOffering so = _offeringDao.findById(vm.getServiceOfferingId());
            if (vm.getType() == VirtualMachine.Type.User) {
                UserVmVO userVm = _vmDao.findById(vm.getId());
                if (userVm == null) {
                    continue;
                }
            } 

            if (capacityType == CapacityVO.CAPACITY_TYPE_MEMORY) {
                usedCapacity += so.getRamSize() * 1024L * 1024L;
            } else if (capacityType == CapacityVO.CAPACITY_TYPE_CPU) {
                usedCapacity += so.getCpu() * so.getSpeed();
            }
        }

        return usedCapacity;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        _storageOverprovisioningFactor = new BigDecimal(1);
        _extraBytesPerVolume = NumbersUtil.parseLong((String) params.get("extra.bytes.per.volume"), 50 * 1024L * 1024L);

        Map<String, String> configs = _configDao.getConfiguration("management-server", params);
        String value = configs.get("vm.resource.release.interval");
        _secondsToSkipStoppedVMs = NumbersUtil.parseInt(value, 86400);

        VmsOnPoolSearch = _vmInstanceDao.createSearchBuilder(Long.class);
        VmsOnPoolSearch.select(null, Func.DISTINCT, VmsOnPoolSearch.entity().getId());
        VmsOnPoolSearch.and("removed", VmsOnPoolSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        VmsOnPoolSearch.and("state", VmsOnPoolSearch.entity().getState(), SearchCriteria.Op.NIN);

        SearchBuilder<VolumeVO> sbVolume = _volumeDao.createSearchBuilder();
        sbVolume.and("poolId", sbVolume.entity().getPoolId(), SearchCriteria.Op.EQ);

        VmsOnPoolSearch.join("volumeJoin", sbVolume, VmsOnPoolSearch.entity().getId(), sbVolume.entity().getInstanceId(), JoinBuilder.JoinType.INNER);

        sbVolume.done();
        VmsOnPoolSearch.done();

        return true;
    }

    public LocalStoragePoolAllocator() {
    }
}
