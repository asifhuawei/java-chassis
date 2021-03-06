/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.config.client;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class TestMemberDiscovery {
  @BeforeClass
  public static void setUpClass() {
    ConfigCenterConfig.setConcurrentCompositeConfiguration(ConfigUtil.createLocalConfig());
  }

  @Test
  public void testGetConfigServerURIs() {
    MemberDiscovery dc = new MemberDiscovery(ConfigCenterConfig.INSTANCE.getServerUri());
    assertNotNull(dc.getConfigServer());
  }

  @Test
  public void testServerChange() {
    MemberDiscovery dc = new MemberDiscovery(Arrays.asList("http://127.0.0.1:30103", "http://127.0.0.1:30104"));
    String server1 = dc.getConfigServer();
    EventManager.post(new ConnFailEvent("connect failed."));
    String server2 = dc.getConfigServer();
    Assert.assertNotEquals(server1, server2);
    EventManager.post(new ConnFailEvent("connect failed."));
    server2 = dc.getConfigServer();
    Assert.assertEquals(server1, server2);

    dc.refreshMembers(new JsonObject(
        "{\"instances\":"
            + "[{\"status\":\"UP\",\"endpoints\":[\"rest://0.0.0.0:30109\"],\"hostName\":\"125292-0.0.0.0\",\"serviceName\":\"configServer\",\"isHttps\":false}"
            + ",{\"status\":\"UP\",\"endpoints\":[\"rest://0.0.0.0:30108\"],\"hostName\":\"125293-0.0.0.0\",\"serviceName\":\"configServer\",\"isHttps\":false}"
            + "]}"));
    server1 = dc.getConfigServer();
    EventManager.post(new ConnFailEvent("connect failed."));
    server2 = dc.getConfigServer();
    Assert.assertNotEquals(server1, server2);
    EventManager.post(new ConnFailEvent("connect failed."));
    server2 = dc.getConfigServer();
    Assert.assertEquals(server1, server2);
  }

  @Test
  public void testDiscoverywithHttp() {
    MemberDiscovery discoveryClient1 = new MemberDiscovery(null);
    discoveryClient1.refreshMembers(new JsonObject(
        "{\"instances\":[{\"status\":\"UP\",\"endpoints\":[\"rest://0.0.0.0:30103\"],\"hostName\":\"125292-0.0.0.0\",\"serviceName\":\"configServer\",\"isHttps\":false}]}"));
    Assert.assertEquals("http://0.0.0.0:30103", discoveryClient1.getConfigServer());
  }

  @Test
  public void testDiscoverywithHttps() {
    MemberDiscovery discoveryClient1 = new MemberDiscovery(null);
    discoveryClient1.refreshMembers(new JsonObject(
        "{\"instances\":[{\"status\":\"UP\",\"endpoints\":[\"rest://0.0.0.0:30103\"],\"hostName\":\"125292-0.0.0.0\",\"serviceName\":\"configServer\",\"isHttps\":true}]}"));
    Assert.assertEquals("https://0.0.0.0:30103", discoveryClient1.getConfigServer());
  }
}
