/*
 *Licensed to the Apache Software Foundation (ASF) under one
 *or more contributor license agreements.  See the NOTICE file
 *distributed with this work for additional information
 *regarding copyright ownership.  The ASF licenses this file
 *to you under the Apache License, Version 2.0 (the
 *"License"); you may not use this file except in compliance
 *with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.apache.stratos.status.monitor.agent.clients.service;

import org.apache.stratos.status.monitor.agent.clients.common.ServiceLoginClient;
import org.apache.stratos.status.monitor.agent.constants.StatusMonitorAgentConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.status.monitor.agent.internal.core.MySQLConnector;
import org.apache.stratos.status.monitor.core.StatusMonitorConfigurationBuilder;
import org.apache.stratos.status.monitor.core.beans.AuthConfigBean;
import org.apache.stratos.status.monitor.core.constants.StatusMonitorConstants;
import org.apache.stratos.status.monitor.core.jdbc.MySQLConnectionInitializer;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.Properties;
import javax.jms.QueueConnectionFactory;

/**
 * Status Monitor Agent client class for Complex Event Processing Server
 */
public class CEPServerClient extends Thread{
    private static final Log log = LogFactory.getLog(CEPServerClient.class);
    private static String tcpUserName;

    public void run() {
        while (true) {
            try {
                executeService();

                // return from while loop if the thread is interrupted
                if (isInterrupted()) {
                    break;
                }
                // let the thread sleep for 15 mins
                try {
                    sleep(StatusMonitorConstants.SLEEP_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static void executeService() throws SQLException {
        int serviceID = MySQLConnectionInitializer.getServiceID(StatusMonitorConstants.CEP);
        AuthConfigBean authConfigBean = StatusMonitorConfigurationBuilder.getAuthConfigBean();

        String userName = authConfigBean.getUserName();
        tcpUserName = userName.replace('@','!');


        //check whether login success
        if (ServiceLoginClient.loginChecker(StatusMonitorConstants.CEP_HOST, serviceID)) {


            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, StatusMonitorAgentConstants.QPID_ICF);
            properties.put(StatusMonitorAgentConstants.CF_NAME_PREFIX +
                    StatusMonitorAgentConstants.CF_NAME,
                    getTCPConnectionURL(tcpUserName,
                            authConfigBean.getPassword()));

            InitialContext ctx;
            try {
                ctx = new InitialContext(properties);

                // Lookup connection factory
                QueueConnectionFactory connFactory =
                        (QueueConnectionFactory) ctx.lookup(StatusMonitorAgentConstants.CF_NAME);
                QueueConnection queueConnection = connFactory.createQueueConnection();
                queueConnection.start();
                QueueSession queueSession =
                        queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

                // Send message
                Queue queue = queueSession.createQueue(StatusMonitorAgentConstants.QUEUE_NAME_CEP +
                        ";{create:always, node:{durable: True}}");

                // create the message to send
                TextMessage textMessage = queueSession.createTextMessage("Test Message Hello");
                javax.jms.QueueSender queueSender = queueSession.createSender(queue);
                queueSender.setTimeToLive(100000000);

                QueueReceiver queueReceiver = queueSession.createReceiver(queue);
                queueSender.send(textMessage);

                TextMessage message = (TextMessage) queueReceiver.receiveNoWait();
                if (log.isDebugEnabled()) {
                    log.debug("Message in the execute() of CEPServer Client: " + message.getText());
                }
                if (message.getText().equals("Test Message Hello")) {
                    MySQLConnector.insertStats(serviceID, true);
                    MySQLConnector.insertState(serviceID, true, "");
                } else {
                    MySQLConnector.insertStats(serviceID, false);
                    MySQLConnector.insertState(serviceID, false, "Send or retrieve messages failed");
                }
                queueSender.close();
                queueSession.close();
                queueConnection.close();

            } catch (JMSException e) {
                MySQLConnector.insertStats(serviceID, false);
                MySQLConnector.insertState(serviceID, false, e.getMessage());
                String msg = "JMS Exception in inserting stats into the DB for the CEPServerClient";
                log.warn(msg, e);
            } catch (NamingException e) {
                MySQLConnector.insertStats(serviceID, false);
                MySQLConnector.insertState(serviceID, false, e.getMessage());
                String msg = "Naming Exception in inserting stats into the DB for the CEPServerClient";
                log.warn(msg, e);
            }
        }
    }

    private static String getTCPConnectionURL(String username, String password) {
        return new StringBuffer()
                .append("amqp://").append(tcpUserName).append(":").append(password).append("@").
                        append(StatusMonitorAgentConstants.CARBON_CLIENT_ID).append("/").
                        append(StatusMonitorAgentConstants.CARBON_VIRTUAL_HOST_NAME).
                        append("?brokerlist='tcp://").append(StatusMonitorConstants.CEP_HOST).
                        append(":").append(StatusMonitorAgentConstants.CEP_DEFAULT_PORT).
                        append("'").toString();
    }
}
