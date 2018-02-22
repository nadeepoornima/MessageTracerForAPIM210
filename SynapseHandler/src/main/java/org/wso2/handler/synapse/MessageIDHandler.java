/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.handler.synapse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

public class MessageIDHandler extends AbstractSynapseHandler {
    private static final Log log = LogFactory.getLog(MessageIDHandler.class);
    private static final String __MESSAGE_ID__ = "__MESSAGE_ID__";
    private static final String LOG_KEY = "messageId";

    public boolean handleRequestInFlow(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC;
        axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        Map<String, String> headers =
                (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String messageId = headers.get("X-Transaction-ID");

        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
        Calendar cal = Calendar.getInstance();

        if (StringUtils.isEmpty(messageId)) {
            messageId = UUID.randomUUID().toString();
            headers.put("X-Transaction-ID", messageId);
        }

        messageContext.setProperty(__MESSAGE_ID__, messageId);
        MDC.put(LOG_KEY, messageId + " - [REQUEST]");
        String contextWithURL = (String) axis2MC.getProperty("TransportInURL");
        String calledMethod = (String) axis2MC.getProperty("HTTP_METHOD");

        if (log.isDebugEnabled()) {
            log.debug("transactionId=" + messageId + ", calledMethod=" + calledMethod + ", requestURI=" + contextWithURL + " ,requestTime=" + dateFormat.format(cal.getTime()));
        }
        return true;
    }

    public boolean handleRequestOutFlow(MessageContext messageContext) {
        String messageId = (String) messageContext.getProperty("__MESSAGE_ID__");
        String apiName = (String) messageContext.getProperty("api.ut.api");
        String apiVersion = (String) messageContext.getProperty("SYNAPSE_REST_API_VERSION");
        String apiContext = (String) messageContext.getProperty("REST_API_CONTEXT");
        String apiPublisher = (String) messageContext.getProperty("api.ut.apiPublisher");
        String applicationName = (String) messageContext.getProperty("api.ut.application.name");
        String applicationUser = (String) messageContext.getProperty("api.ut.userId");

        if (log.isDebugEnabled()) {
            log.debug("transactionId=" + messageId + ", apiName=" + apiName + ", version=" + apiVersion + ", apiContext=" + apiContext + ", apiPublisher=" + apiPublisher + ", applicationName=" + applicationName + ", appUser=" + applicationUser);
        }
        return true;
    }

    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    public boolean handleResponseOutFlow(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC;
        axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, String> headers =
                (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
        Calendar cal = Calendar.getInstance();

        String responseMessageId = (String) messageContext.getProperty("__MESSAGE_ID__");
        int responseCode = (Integer) axis2MC.getProperty("HTTP_SC");
        String responseMessage = (String) axis2MC.getProperty("HTTP_SC_DESC");
        String apiName = (String) messageContext.getProperty("API_NAME");
        String usingMethod = (String) messageContext.getProperty("api.ut.HTTP_METHOD");
        String errorMessage = (String) messageContext.getProperty("ERROR_MESSAGE");
        String apiVersion = (String) messageContext.getProperty("SYNAPSE_REST_API_VERSION");
        String contextWithUrl = (String) messageContext.getProperty("REST_FULL_REQUEST_PATH");

        if (responseMessage == null) {
            responseMessage = "Error";
        }
        if (errorMessage == null) {
            errorMessage = "No Error";
        }

        if (log.isDebugEnabled()) {
            log.debug("transactionId=" + responseMessageId + ", calledMethod=" + usingMethod + ", requestURI=" + contextWithUrl + ", responseTime=" + dateFormat.format(cal.getTime())
                    + ", responseCode=" + responseCode + " " + responseMessage + ", errorMessage=" + errorMessage + ", apiName=" + apiName + ", version=" + apiVersion);
        }
        return true;
    }
}
