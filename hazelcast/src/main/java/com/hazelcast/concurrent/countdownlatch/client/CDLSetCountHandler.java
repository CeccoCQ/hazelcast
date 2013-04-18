/*
 * Copyright (c) 2008-2010, Hazel Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.concurrent.countdownlatch.client;

import com.hazelcast.client.ClientCommandHandler;
import com.hazelcast.concurrent.countdownlatch.CountDownLatchService;
import com.hazelcast.instance.Node;
import com.hazelcast.nio.Protocol;

public class CDLSetCountHandler extends ClientCommandHandler {
    final CountDownLatchService countDownLatchService;
    public CDLSetCountHandler(CountDownLatchService countDownLatchService) {
        super();
        this.countDownLatchService = countDownLatchService;
    }

    @Override
    public Protocol processCall(Node node, Protocol protocol) {
        String name = protocol.args[0];
        int count = Integer.valueOf(protocol.args[1]);
        boolean isSet = countDownLatchService.createDistributedObjectForClient(name).trySetCount(count);
        return protocol.success(String.valueOf(isSet));
    }
}