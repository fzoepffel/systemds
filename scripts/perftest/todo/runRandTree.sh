#!/bin/bash
#-------------------------------------------------------------
#
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
#
#-------------------------------------------------------------
set -e

CMD=systemds
BASE=$3

export HADOOP_CLIENT_OPTS="-Xmx2048m -Xms2048m -Xmn256m"

echo "running random forest"

#training
tstart=$SECONDS
${CMD} -f scripts/random-forest.dml --explain --stats --nvargs X=$1 Y=$2 fmt=csv M=${BASE}/M
ttrain=$(($SECONDS - $tstart - 3))
echo "RandomForest train on "$1": "$ttrain >> times.txt

#predict
tstart=$SECONDS
${CMD} -f ../../algorithms/random-forest-predict.dml --explain --stats --nvargs M=${BASE}/M X=$1_test Y=$2_test P=${BASE}/P
tpredict=$(($SECONDS - $tstart - 3))
echo "Randomforest predict on "$1": "$tpredict >> times.txt

