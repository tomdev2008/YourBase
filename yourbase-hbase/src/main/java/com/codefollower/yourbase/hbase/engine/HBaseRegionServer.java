/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.yourbase.hbase.engine;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

import com.codefollower.yourbase.hbase.server.HBaseTcpServer;

/**
 * 
 * 在hbase-site.xml中配置hbase.regionserver.impl = com.codefollower.yourbase.hbase.engine.HBaseRegionServer
 *
 */
public class HBaseRegionServer extends org.apache.hadoop.hbase.regionserver.HRegionServer {
    public HBaseRegionServer(Configuration conf) throws IOException, InterruptedException {
        super(conf);
    }

    @Override
    public void run() {
        HBaseTcpServer server = new HBaseTcpServer(this);
        server.start();
        try {
            super.run();
        } finally {
            server.stop();
        }
    }
}
