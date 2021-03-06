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

from apisession import ApiSession
from vm import VMCreator

#http://localhost:8080/client/api?_=1303171711292&command=deployVirtualMachine&zoneId=1&hypervisor=KVM&templateId=4&serviceOfferingId=7&response=json&sessionkey=%2Bh3Gh4BffWpQdk4nXmcC88uEk9k%3D

def create_vm():
    vmcreator = VMCreator(api, {'zoneId':1, 'hypervisor':'KVM', 'templateId':4, 
                                'serviceOfferingId':7,
                                'userdata':'dGhpcyBpcyBhIHRlc3QK'})
    vmid = vmcreator.create()
    vmcreator.poll(10, 3)

if __name__ == "__main__":
    api = ApiSession('http://localhost:8080/client/api', 'admin', 'password')
    api.login()

    create_vm()


