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
package org.apache.servicemix.executors.impl;

import junit.framework.TestCase;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.servicemix.executors.Executor;
import org.apache.servicemix.executors.ExecutorFactory;
import org.apache.servicemix.executors.impl.ExecutorFactoryImpl;
import org.apache.servicemix.executors.impl.ExecutorImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * Test case to ensure that Executor will always log throwables from the executed Runnable (SM-1847)
 */
public class LogThrowableInExecutorTest extends TestCase {

    private static final String MESSAGE = "I'm a bad Runnable throwing errors at people";

    public void testThrowableLogged() throws InterruptedException {
        final List<LoggingEvent> events = new LinkedList<LoggingEvent>();

        // unit tests use LOG4J as the backend for SLF4J so add the appender to LOG4J
        Logger.getLogger(ExecutorImpl.class).addAppender(new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent event) {
                events.add(event);
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }

            @Override
            public void close() {
                //graciously do nothing
            }
        });

        ExecutorFactory factory = new ExecutorFactoryImpl();
        Executor executor = factory.createExecutor("test");
        executor.execute(new Runnable() {
            public void run() {
                throw new Error(MESSAGE);
            }
        });

        // let's wait to make sure the runnable is done
        Thread.sleep(500);

        assertEquals("Should have logged 1 message", 1, events.size());
        assertTrue("Exception message should have been logged",
                events.get(0).getThrowableStrRep()[0].contains(MESSAGE));

    }

}
