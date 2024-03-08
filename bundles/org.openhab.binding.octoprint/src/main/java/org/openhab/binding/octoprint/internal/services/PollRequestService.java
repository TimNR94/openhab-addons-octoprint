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

import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.octoprint.internal.OctoPrintHandler;
import org.openhab.binding.octoprint.internal.models.OctopiServer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PollRequestService}.TODO
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class PollRequestService {
    private final Logger logger = LoggerFactory.getLogger(PollRequestService.class);

    final HttpRequestService requestService;
    final OctoPrintHandler octoPrintHandler;
    HashMap<String, Channel> requests = new HashMap<>();

    public PollRequestService(OctopiServer octopiServer, OctoPrintHandler octoPrintHandler) {
        requestService = new HttpRequestService(octopiServer);
        this.octoPrintHandler = octoPrintHandler;
    }

    public void addPollRequest(Channel channel) {
        requests.putIfAbsent(channel.getUID().getId(), channel);
        logger.debug("added {} into poll requests as: [{}, {}]", channel.getUID(), channel.getUID(), channel);
    }

    public void poll() {
        for (var entry : requests.entrySet()) {
            String channelID = entry.getKey();
            Channel channel = entry.getValue();
            String acceptedItemType = channel.getAcceptedItemType();

            String route = channel.getProperties().get("route");
            if (route == null) {
                logger.error("{} has no jsonKeys as parameter value of route", channelID);
            }

            ContentResponse res = requestService.getRequest(route);
            if (res.getStatus() == 200) {
                JsonObject json = JsonParser.parseString(res.getContentAsString()).getAsJsonObject();
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
                    System.out.printf("%1$s: %2$s%n%n", s, updatedValue[0]);
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
