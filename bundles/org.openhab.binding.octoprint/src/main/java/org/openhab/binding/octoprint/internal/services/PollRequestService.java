/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.octoprint.internal.services;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.octoprint.internal.OctoPrintHandler;
import org.openhab.binding.octoprint.internal.models.OctoprintServer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PollRequestService} uses a {@link HttpRequestService} and a Map of {@link Channel}s
 * to make GET requests to an octoprint server instance and update the Channel states according to the
 * received Data.
 * <br>
 * The {@link Channel}s must have the following properties set:
 * - poll, value is a comma seperated list of json keys to the wanted data in the response body (i.e. job,file,name)
 * - route, value is the api endpoint the GET request is sent to (i.e. api/job)
 * 
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class PollRequestService {
    private final Logger logger = LoggerFactory.getLogger(PollRequestService.class);

    final HttpRequestService requestService;
    final OctoPrintHandler octoPrintHandler;
    HashMap<String, Channel> requests = new HashMap<>();

    public PollRequestService(OctoprintServer octoprintServer, OctoPrintHandler octoPrintHandler) {
        requestService = new HttpRequestService(octoprintServer);
        this.octoPrintHandler = octoPrintHandler;
    }

    /**
     * Adds a {@link Channel} to the Collection of Channels that is iterated over in every poll.
     *
     * @param channel must have the following properties set:
     *            <br>
     *            - poll, value is a comma seperated list of json keys to the wanted data in the response body (i.e.
     *            job,file,name)
     *            <br>
     *            - route, value is the api endpoint the GET request is sent to (i.e. api/job)
     */
    public void addPollRequest(Channel channel) {
        requests.putIfAbsent(channel.getUID().getId(), channel);
        logger.debug("added {} into poll requests as: [{}, {}]", channel.getUID(), channel.getUID(), channel);
    }

    public void poll() throws ExecutionException, InterruptedException, TimeoutException {
        for (var entry : requests.entrySet()) {
            String channelID = entry.getKey();
            Channel channel = entry.getValue();
            String acceptedItemType = channel.getAcceptedItemType();

            String route = channel.getProperties().get("route");
            if (route == null) {
                logger.error("{} has no jsonKeys as parameter value of route", channelID);
            }
            logger.debug("================ polling {} ({}) ================", channelID, route);

            ContentResponse res = requestService.getRequest(route);
            if (res.getStatus() == 200) {
                String result = res.getContentAsString();
                logger.warn("{}", result);

                JsonObject json = JsonParser.parseString(result).getAsJsonObject();
                var jsonKeys = channel.getProperties().get("poll");
                if (jsonKeys.isEmpty()) {
                    logger.error("{} has no jsonKeys as parameter value of poll", channelID);
                    continue;
                }
                String[] jsonKeyArray = jsonKeys.split(",");

                final JsonElement[] updatedValue = new JsonElement[1];
                updatedValue[0] = json;
                for (String s : jsonKeyArray) {
                    updatedValue[0] = updatedValue[0].getAsJsonObject().get(s.strip());
                    logger.debug("{}: {}", s, updatedValue[0]);
                }
                if (Objects.equals(acceptedItemType, "String")) {
                    if (updatedValue[0] == null || updatedValue[0].isJsonNull()) {
                        octoPrintHandler.updateChannel(channelID, StringType.valueOf("n.A."));
                    } else {
                        octoPrintHandler.updateChannel(channelID, StringType.valueOf(updatedValue[0].getAsString()));
                        logger.debug("Updated Channel {} to state {}", channelID, updatedValue[0].getAsString());
                    }
                } else if (Objects.equals(acceptedItemType, "Number")) {
                    if (updatedValue[0] == null || updatedValue[0].isJsonNull()) {
                        octoPrintHandler.updateChannel(channelID, DecimalType.valueOf("0.0"));
                    } else {
                        octoPrintHandler.updateChannel(channelID, DecimalType.valueOf(updatedValue[0].getAsString()));
                        logger.debug("Updated Channel {} to state {}", channelID, updatedValue[0].getAsString());
                    }
                }
            }
        }
    }

    public void dispose() {
        requestService.dispose();
    }
}
