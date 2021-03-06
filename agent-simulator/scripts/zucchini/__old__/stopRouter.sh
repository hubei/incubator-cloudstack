#!/usr/bin/env bash
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
usage() {
  printf "Stop Router: %s: -h management-server -i vmid\n" $(basename $0) >&2
}

iflag=
hflag=

host="127.0.0.1" #defaults to localhost
vmid=

while getopts 'h:i:' OPTION
do
 case $OPTION in
  h)	hflag=1
        host="$OPTARG"
        ;;
  i)	iflag=1
        vmid="$OPTARG"
        ;;        
  ?)	usage
		exit 2
		;;
  esac
done

if [[ $iflag != "1" ]]
then
 usage
 exit 2
fi
stop_vm="GET  http://$host:8096/client/?command=stopRouter&id=$vmid	HTTP/1.0\n\n"
echo -e $stop_vm | nc -v -w 60 $host 8096
