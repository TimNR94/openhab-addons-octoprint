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
package org.openhab.binding.octoprint.internal;

import static org.openhab.binding.octoprint.internal.OctoPrintBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

/**
 * The {@link OctoPrintHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim-Niclas Ruppert - Initial contribution
 */
@NonNullByDefault
public class OctoPrintHandler extends BaseThingHandler {

    public enum JobState {
        IDLE,
        STARTED,
        PAUSED,
        RUNNING,
        CANCELED
    }

    private JobState jobState = JobState.IDLE;
    private final Logger logger = LoggerFactory.getLogger(OctoPrintHandler.class);

    private HttpRequestService httpRequestService;
    private @Nullable OctoPrintConfiguration config;

    public OctoPrintHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        try {
            switch (channelId) {
                case PRINT_JOB_START:
                    if (command instanceof StringType) {
                        httpRequestService.postRequest("/api/job",)
                        jobState = JobState.STARTED;
                    }
                    break;
                case PRINT_JOB_CANCEL:
                    if (command instanceof StringType) {
                        // binding specific logic goes here
                        jobState = JobState.CANCELED;
                    }
                    break;
                case PRINT_JOB_PAUSE:
                    if (command instanceof StringType) {
                        // binding specific logic goes here
                        jobState = jobState.PAUSED;
                    }
                    break;
                case PRINT_JOB_RESTART:
                    if (command instanceof StringType) {
                        // binding specific logic goes here
                        jobState = jobState.RUNNING;
                    }
                    break;
                case PRINTER_HOMING:
                    if (command instanceof StringType) {
                        String jsonString = "{ \"command\": \"home\", \"axes\": [\"x\", \"y\", \"z\"] }";
                        httpRequestService.postRequest();
                    }
                    break;
                default:
                    logger.warn("Framework sent command to unknown channel with id '{}'", channelUID.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateStates() {
        logger.trace("updating states of {}", getThing().getUID());
    }

    @Override
    public void initialize() {
        config = getConfigAs(OctoPrintConfiguration.class);

        httpRequestService = new HttpRequestService(octopiServer);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
