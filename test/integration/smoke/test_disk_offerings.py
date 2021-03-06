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
""" BVT tests for Disk offerings"""

#Import Local Modules
import marvin
from marvin.cloudstackTestCase import *
from marvin.cloudstackAPI import *
from integration.lib.utils import *
from integration.lib.base import *
from integration.lib.common import *

class Services:
    """Test Disk offerings Services
    """

    def __init__(self):
        self.services = {
                         "off": {
                                        "name": "Disk offering",
                                        "displaytext": "Disk offering",
                                        "disksize": 1   # in GB
                                },
                         }

class TestCreateDiskOffering(cloudstackTestCase):

    def setUp(self):
        self.services = Services().services
        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []
        return

    def tearDown(self):
        try:
            self.dbclient.close()
            #Clean up, terminate the created templates
            cleanup_resources(self.apiclient, self.cleanup)

        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def test_01_create_disk_offering(self):
        """Test to create disk offering"""

        # Validate the following:
        # 1. createDiskOfferings should return valid info for new offering
        # 2. The Cloud Database contains the valid information

        disk_offering = DiskOffering.create(
                                        self.apiclient,
                                        self.services["off"]
                                        )
        self.cleanup.append(disk_offering)

        self.debug("Created Disk offering with ID: %s" % disk_offering.id)

        list_disk_response = list_disk_offering(
                                                self.apiclient,
                                                id=disk_offering.id
                                                )
        self.assertEqual(
                            isinstance(list_disk_response, list),
                            True,
                            "Check list response returns a valid list"
                        )
        self.assertNotEqual(
                            len(list_disk_response),
                            0,
                            "Check Disk offering is created"
                        )
        disk_response = list_disk_response[0]

        self.assertEqual(
                            disk_response.displaytext,
                            self.services["off"]["displaytext"],
                            "Check server id in createServiceOffering"
                        )
        self.assertEqual(
                            disk_response.name,
                            self.services["off"]["name"],
                            "Check name in createServiceOffering"
                        )
        return


class TestDiskOfferings(cloudstackTestCase):

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.dbclient = self.testClient.getDbConnection()
        self.cleanup = []

    def tearDown(self):

        try:
            self.dbclient.close()
            #Clean up, terminate the created templates
            cleanup_resources(self.apiclient, self.cleanup)

        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @classmethod
    def setUpClass(cls):
        cls.services = Services().services
        cls.api_client = super(TestDiskOfferings, cls).getClsTestClient().getApiClient()
        cls.disk_offering_1 = DiskOffering.create(
                                                  cls.api_client,
                                                  cls.services["off"]
                                                  )
        cls.disk_offering_2 = DiskOffering.create(
                                                  cls.api_client,
                                                  cls.services["off"]
                                                  )
        cls._cleanup = [cls.disk_offering_1]
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cls.api_client = super(TestDiskOfferings, cls).getClsTestClient().getApiClient()
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def test_02_edit_disk_offering(self):
        """Test to update existing disk offering"""

        # Validate the following:
        # 1. updateDiskOffering should return
        #    a valid information for newly created offering

        #Generate new name & displaytext from random data
        random_displaytext = random_gen()
        random_name = random_gen()

        self.debug("Updating Disk offering with ID: %s" % 
                                    self.disk_offering_1.id)
        
        cmd = updateDiskOffering.updateDiskOfferingCmd()
        cmd.id = self.disk_offering_1.id
        cmd.displaytext = random_displaytext
        cmd.name = random_name

        self.apiclient.updateDiskOffering(cmd)

        list_disk_response = list_disk_offering(
                                                self.apiclient,
                                                id=self.disk_offering_1.id
                                                )
        self.assertEqual(
                            isinstance(list_disk_response, list),
                            True,
                            "Check list response returns a valid list"
                        )
        self.assertNotEqual(
                            len(list_disk_response),
                            0,
                            "Check disk offering is updated"
                        )

        disk_response = list_disk_response[0]

        self.assertEqual(
                        disk_response.displaytext,
                        random_displaytext,
                        "Check service displaytext in updateServiceOffering"
                        )
        self.assertEqual(
                        disk_response.name,
                        random_name,
                        "Check service name in updateServiceOffering"
                        )
        return

    def test_03_delete_disk_offering(self):
        """Test to delete disk offering"""

        # Validate the following:
        # 1. deleteDiskOffering should return
        #    a valid information for newly created offering

        self.disk_offering_2.delete(self.apiclient)

        self.debug("Deleted Disk offering with ID: %s" % 
                                    self.disk_offering_2.id)
        list_disk_response = list_disk_offering(
                                                self.apiclient,
                                                id=self.disk_offering_2.id
                                                )

        self.assertEqual(
                        list_disk_response,
                        None,
                        "Check if disk offering exists in listDiskOfferings"
                        )
        return
