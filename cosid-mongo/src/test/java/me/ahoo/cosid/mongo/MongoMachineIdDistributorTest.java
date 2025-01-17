/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosid.mongo;

import me.ahoo.cosid.machine.ClockBackwardsSynchronizer;
import me.ahoo.cosid.machine.MachineIdDistributor;
import me.ahoo.cosid.machine.MachineStateStorage;
import me.ahoo.cosid.test.machine.distributor.MachineIdDistributorSpec;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;

class MongoMachineIdDistributorTest extends MachineIdDistributorSpec {
    MongoDatabase mongoDatabase;
    MachineIdDistributor machineIdDistributor;
    MongoMachineInitializer machineInitializer;
    
    @BeforeEach
    void setup() {
        mongoDatabase = MongoClients.create(MongoLauncher.getConnectionString()).getDatabase("cosid_db");
        machineInitializer = new MongoMachineInitializer(mongoDatabase);
        machineInitializer.ensureMachineCollection();
        machineIdDistributor = new MongoMachineIdDistributor(
            new MongoMachineCollection(mongoDatabase.getCollection(MachineCollection.COLLECTION_NAME)),
            MachineStateStorage.IN_MEMORY,
            ClockBackwardsSynchronizer.DEFAULT);
    }
    
    @Override
    protected MachineIdDistributor getDistributor() {
        return machineIdDistributor;
    }
    
    @Override
    protected Duration getSafeGuardDuration() {
        if (System.getenv().containsKey("CI")) {
            return Duration.ofSeconds(10);
        }
        return super.getSafeGuardDuration();
    }
}