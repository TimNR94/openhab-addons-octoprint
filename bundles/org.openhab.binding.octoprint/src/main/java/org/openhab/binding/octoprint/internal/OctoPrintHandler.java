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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Response;
import org.openhab.binding.octoprint.internal.models.OctopiServer;
import org.openhab.binding.octoprint.internal.services.HttpRequestService;
import org.openhab.binding.octoprint.internal.services.PollRequestService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctoPrintHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim-Niclas Ruppert - Initial contribution
 */
@NonNullByDefault
public class OctoPrintHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OctoPrintHandler.class);
    private @Nullable PollRequestService pollRequestService;
    private @Nullable OctopiServer octopiServer;
    private @Nullable ScheduledFuture<?> pollingJob;
    private String selectedTool = "0";
    private @Nullable HttpRequestService httpRequestService;
    private @Nullable OctoPrintConfiguration config;

    public OctoPrintHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        switch (channelId) {
            case PRINT_JOB_START:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"start\" }";
                    Response res = httpRequestService.postRequest("api/job", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - There is already a running print job.", res.getStatus(),
                                res);
                    }
                }
                break;
            case PRINT_JOB_CANCEL:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"cancel\" }";
                    Response res = httpRequestService.postRequest("api/job", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - There is no running print job to cancel.", res.getStatus(),
                                res);
                    }
                }
                break;
            case PRINT_JOB_PAUSE:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"pause\", \"action\": \"pause\" }";
                    Response res = httpRequestService.postRequest("api/job", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - There is no print job to pause/resume/toggle.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINT_JOB_RESTART:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"pause\", \"action\": \"resume\" }";
                    Response res = httpRequestService.postRequest("api/job", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - There is no active print job that is currently paused.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_JOG_X:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"jog\", \"x\": %s, \"y\": 0, \"z\": 0 }", command);
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_JOG_Y:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"jog\", \"x\": 0, \"y\": %s, \"z\": 0 }", command);
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_JOG_Z:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"jog\", \"x\": 0, \"y\": 0, \"z\": %s }", command);
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_HOMING_X:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"home\", \"axes\": [\"x\"] }";
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_HOMING_Y:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"home\", \"axes\": [\"y\"] }";
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_HOMING_Z:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"home\", \"axes\": [\"z\"] }";
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                }
                break;
            case PRINTER_HOMING_XYZ:
                if (command instanceof StringType) {
                    String body = "{ \"command\": \"home\", \"axes\": [\"x\", \"y\", \"z\"] }";
                    Response res = httpRequestService.postRequest("api/printer/printhead", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                    System.out.printf("PRINTER HOMING XYZ: %s%n", res);
                }
                break;
            case PRINTER_TOOL_SELECT:
                System.out.println("RPINTER TOOL SELECT - DEBUG");
                if (command instanceof DecimalType) {
                    selectedTool = command.toString();
                    String body = String.format("{ \"command\": \"select\", \"tool\": \"tool%s\"}", selectedTool);
                    Response res = httpRequestService.postRequest("api/printer/tool", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational or is printing.",
                                res.getStatus(), res);
                    }
                    System.out.printf("PRINTER TOOL SELECT: %s%n", res);

                }
                break;
            case PRINTER_TOOL_FLOWRATE:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"flowrate\", \"factor\": %d}",
                            Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/tool", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER TOOL FLOWRATE: %s%n", res);
                }
                break;
            case PRINTER_TOOL_TEMP_TARGET:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"target\", \"tools\": {\"tool%s\": %d} }",
                            selectedTool, Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/tool", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER TOOL TEMP TARGET: %s%n", res);
                }
                break;
            case PRINTER_TOOL_TEMP_OFFSET:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"offset\", \"tools\": {\"tool%s\": %d} }",
                            selectedTool, Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/tool", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER TOOL TEMP OFFSET: %s%n", res);
                }
                break;
            case PRINTER_BED_TEMP_TARGET:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"target\", \"target\": %d }",
                            Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/bed", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER BED TEMP TARGET: %s%n", res);
                }
                break;
            case PRINTER_BED_TEMP_OFFSET:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"offset\", \"offset\": %d }",
                            Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/bed", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER BED TEMP OFFSET: %s%n", res);
                }
                break;
            case PRINTER_CHAMBER_TEMP_TARGET:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"target\", \"target\": %d} }",
                            Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/chamber", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER CHAMBER TEMP TARGET: %s%n", res);
                }
                break;
            case PRINTER_CHAMBER_TEMP_OFFSET:
                if (command instanceof DecimalType) {
                    String body = String.format("{ \"command\": \"offset\", \"offset\": %d} }",
                            Integer.parseInt(command.toString()));
                    Response res = httpRequestService.postRequest("api/printer/chamber", body);
                    if (res.getStatus() == 409) {
                        logger.warn("status: {}, body: {} - Printer is currently not operational.", res.getStatus(),
                                res);
                    }
                    System.out.printf("PRINTER CHAMBER TEMP OFFSET: %s%n", res.toString());
                }
                break;
            default:
                logger.warn("Framework sent command to unknown channel with id '{}'", channelUID.getId());
        }
    }

    public void updateChannel(String channelUID, State state) {
        updateState(channelUID, state);
    }

    private void pollingCode() {
        pollRequestService.poll();
    }

    @Override
    public void initialize() {
        // ToDo: Error Handling for everything
        config = getConfigAs(OctoPrintConfiguration.class);
        assert config != null;

        octopiServer = new OctopiServer(config.ip, config.apiKey, config.username);
        logger.warn("Created {}", octopiServer);
        pollRequestService = new PollRequestService(octopiServer, this);
        pollRequestService.addPollRequest(SERVER_VERSION, "api/server", new ArrayList<String>(List.of("version")),
                new StringType());
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval, TimeUnit.SECONDS);

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

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (pollRequestService != null) {
            pollRequestService.dispose();
            pollRequestService = null;
        }

        if (httpRequestService != null) {
            httpRequestService.dispose();
            httpRequestService = null;
        }
    }
}
