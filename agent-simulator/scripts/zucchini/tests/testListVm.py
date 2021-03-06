#!/usr/bin/env python
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
    import unittest2 as unittest
except ImportError:
    import unittest

import timeit
import random
from cloudstackAPI import *
from cloudstackTestCase import *


class ListVmTests(cloudstackTestCase):
    '''
    List Virtual Machine tests
    '''

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def listAllVm(self):
        numVms = 0
        api = self.testClient.getApiClient()
        listVmCmd = listVirtualMachines.listVirtualMachinesCmd()
        listVmCmd.account = 'admin'
        listVmCmd.zoneid = 1
        listVmCmd.domainid = 1
        listVmResponse = api.listVirtualMachines(listVmCmd)
        if listVmResponse is not None:
            numVms = len(listVmResponse)

    @unittest.skip("skipping")
    def test_timeListVm(self):
        t = timeit.Timer(self.listAllVm)
        l = t.repeat(50, 50)
