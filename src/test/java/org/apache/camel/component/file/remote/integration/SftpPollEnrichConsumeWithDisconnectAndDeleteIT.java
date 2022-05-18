/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file.remote.integration;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.remote.sftp.integration.SftpServerTestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf(value = "org.apache.camel.component.file.remote.services.SftpEmbeddedService#hasRequiredAlgorithms")
public class SftpPollEnrichConsumeWithDisconnectAndDeleteIT extends SftpServerTestSupport {
    @Timeout(value = 30)
    @Test
    public void testSftpSimpleConsume() throws Exception {
        String expected = "Hello World";

        // create file using regular file
        template.sendBodyAndHeader("file://" + service.getFtpRootDir(), expected, Exchange.FILE_NAME, "hello.txt");

        ProducerTemplate triggerTemplate = context.createProducerTemplate();
        triggerTemplate.sendBody("vm:trigger", "");

        File file = ftpFile("archive/hello.txt").toFile();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertTrue(file.exists(), "The file should have been deleted"));
    }

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        TestProcessor processor = new TestProcessor();
        return new RouteBuilder[] { new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("vm:trigger")
                        .pollEnrich("sftp://localhost:{{ftp.server.port}}/{{ftp.root.dir}}?username=admin&password=admin&delay=10000&disconnect=true&move=archive")
                        .process(processor);
            }
        } };
    }

    private static class TestProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            boolean setOutMessage = false;
            if(setOutMessage){
                DefaultMessage msg = new DefaultMessage(exchange);
                exchange.setOut(msg);
                exchange.getIn().removeHeaders("CamelFile*");
                msg.setHeaders(exchange.getIn().getHeaders());
                msg.setBody(exchange.getIn().getBody());
            }
        }
    }
}
