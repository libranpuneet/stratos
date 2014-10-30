/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.messaging.message.receiver.application.status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.broker.subscribe.TopicSubscriber;
import org.apache.stratos.messaging.listener.EventListener;
import org.apache.stratos.messaging.util.Constants;

public class ApplicationStatusEventReceiver implements Runnable {
    private static final Log log = LogFactory.getLog(ApplicationStatusEventReceiver.class);

    private ApplicationStatusEventMessageDelegator messageDelegator;
    private ApplicationStatusEventMessageListener messageListener;
    private TopicSubscriber topicSubscriber;
    private boolean terminated;

    public ApplicationStatusEventReceiver() {
        ApplicationStatusEventMessageQueue messageQueue = new ApplicationStatusEventMessageQueue();
        this.messageDelegator = new ApplicationStatusEventMessageDelegator(messageQueue);
        this.messageListener = new ApplicationStatusEventMessageListener(messageQueue);
    }

    public void addEventListener(EventListener eventListener) {
        messageDelegator.addEventListener(eventListener);
    }

    @Override
    public void run() {
        try {
            // Start topic subscriber thread
            topicSubscriber = new TopicSubscriber(Constants.APPLICATION_STATUS_TOPIC);
            topicSubscriber.setMessageListener(messageListener);
            Thread subscriberThread = new Thread(topicSubscriber);
            subscriberThread.start();
            if (log.isDebugEnabled()) {
                log.debug("Application status event message receiver thread started");
            }

            // Start Application status event message delegator thread
            Thread receiverThread = new Thread(messageDelegator);
            receiverThread.start();
            if (log.isDebugEnabled()) {
                log.debug("Application status event message delegator thread started");
            }

            // Keep the thread live until terminated
            while (!terminated) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Application status failed", e);
            }
        }
    }

    public void terminate() {
        topicSubscriber.terminate();
        messageDelegator.terminate();
        terminated = true;
    }
}
