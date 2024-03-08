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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.openhab.binding.octoprint.internal.models.OctopiServer;
import org.openhab.binding.octoprint.internal.providers.OctoPrintChannelTypeProvider;
import org.openhab.binding.octoprint.internal.services.HttpRequestService;
import org.openhab.binding.octoprint.internal.services.PollRequestService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.*;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link OctoPrintHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim-Niclas Ruppert - Initial contribution
 */
@NonNullByDefault
public class OctoPrintHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OctoPrintHandler.class);
    private final OctoPrintChannelTypeProvider channelTypeProvider;
    private @Nullable PollRequestService pollRequestService;
    private @Nullable OctopiServer octopiServer;
    private @Nullable ScheduledFuture<?> pollingJob;
    private String selectedTool = "0";
    private @Nullable HttpRequestService httpRequestService;
    private @Nullable OctoPrintConfiguration config;

    public OctoPrintHandler(Thing thing, OctoPrintChannelTypeProvider octoPrintChannelTypeProvider) {
        super(thing);
        channelTypeProvider = octoPrintChannelTypeProvider;
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
        assert pollRequestService != null;
        try {
            pollRequestService.poll();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.error("Error: {}", e.toString());
        }
    }

    protected Map.Entry<String, ChannelType> channelEntry(String prefix, String channelName, String description) {
        String channelTypeId = String.format("%1$s%2$s", prefix.toLowerCase(), channelName);
        String label = String.format("%1$s Tool Temperature", prefix);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(THING_TYPE_OCTOPRINT.getBindingId(), channelTypeId);

        if (channelTypeProvider.hasChannelType(channelTypeUID)) {
            return Map.entry(prefix.toLowerCase(),
                    Objects.requireNonNull(channelTypeProvider.getChannelType(channelTypeUID, null)));
        }

        StateDescriptionFragment state = StateDescriptionFragmentBuilder.create().withPattern("%.1f °C")
                .withReadOnly(true).build();
        StateChannelTypeBuilder stateChannelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, "Number")
                .withCategory("Temperature").withStateDescriptionFragment(state).withDescription(description);
        ChannelType channelType = stateChannelTypeBuilder.build();
        channelTypeProvider.addChannelType(channelType);
        return Map.entry(prefix.toLowerCase(), channelType);
    }

    protected void addTemperatureChannels(ThingBuilder thingBuilder) {
        assert httpRequestService != null;
        Map<String, ChannelType> channelTypes = Map.ofEntries(
                channelEntry("Actual", "PrinterToolTemp", "Actual temperature of the printer tool"),
                channelEntry("Target", "PrinterToolTemp", "Target temperature of the printer tool"),
                channelEntry("Offset", "PrinterToolTemp", "Temperature offset of the printer tool"));
        // add tool temperature channels
        ContentResponse res = null;
        try {
            res = httpRequestService.getRequest("api/printer/tool");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error: {}", e.toString());
        }

        createTemperatureChannels(thingBuilder, Objects.requireNonNull(res), channelTypes, "api/printer/tool");

        // add bed temperature channel
        channelTypes = Map.ofEntries(channelEntry("Actual", "PrinterBedTemp", "Actual temperature of the printer bed"),
                channelEntry("Target", "PrinterBedTemp", "Target temperature of the printer bed"),
                channelEntry("Offset", "PrinterBedTemp", "Temperature offset of the printer tool"));

        try {
            res = httpRequestService.getRequest("api/printer/bed");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error: {}", e.toString());
        }
        createTemperatureChannels(thingBuilder, Objects.requireNonNull(res), channelTypes, "api/printer/bed");

        // add chamber temperature channel
        channelTypes = Map.ofEntries(
                channelEntry("Actual", "PrinterChamberTemp", "Actual temperature of the printer chamber"),
                channelEntry("Target", "PrinterChamberTemp", "Target temperature of the printer chamber"),
                channelEntry("Offset", "PrinterChamberTemp", "Temperature offset of the printer chamber"));

        try {
            res = httpRequestService.getRequest("api/printer/chamber");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error: {}", e.toString());
        }
        createTemperatureChannels(thingBuilder, Objects.requireNonNull(res), channelTypes, "api/printer/chamber");
    }

    protected void createTemperatureChannels(ThingBuilder thingBuilder, ContentResponse res,
            Map<String, ChannelType> channelTypes, String route) {
        if (res.getStatus() == 200) {
            JsonObject json = JsonParser.parseString(res.getContentAsString()).getAsJsonObject();
            json.asMap().forEach((jsonKey, jsonValue) -> {
                logger.debug("jsonKey: {}", jsonKey);
                JsonObject temps = jsonValue.getAsJsonObject();
                logger.debug("temps: {}", temps);

                temps.keySet().forEach(key -> {
                    String channelID = String.format("%1$s_temp_%2$s", key, jsonKey);
                    ChannelUID channelUID = new ChannelUID(thing.getUID(), channelID);
                    Map<String, String> properties = new HashMap<>();
                    properties.put("tool_name", jsonKey);
                    String jsonKeys = String.format("%1$s,%2$s", jsonKey, key);
                    properties.put("poll", jsonKeys);
                    properties.put("route", route);

                    ChannelType channelType = channelTypes.get(key);
                    ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID).withType(channelType.getUID())
                            .withProperties(properties).withAcceptedItemType(channelType.getItemType());

                    Channel channel = channelBuilder.build();
                    if (thing.getChannel(channelUID) == null) {
                        thingBuilder.withChannel(channel);
                    } else {
                        logger.warn("Can not add channel {}, it alredy exists.", channelUID.getId());
                    }
                });
            });
        } else {
            logger.error("Error in response: {}: {}", res.getStatus(), res.getContentAsString());
        }
    }

    @Override
    public void initialize() {
        // ToDo: Error Handling for everything
        config = getConfigAs(OctoPrintConfiguration.class);
        assert config != null;

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
            boolean thingReachable = true;
            octopiServer = new OctopiServer(config.ip, config.apiKey, config.username);
            httpRequestService = new HttpRequestService(octopiServer);
            logger.debug("Created {}", octopiServer);

            ThingBuilder thingBuilder = editThing();
            addTemperatureChannels(thingBuilder);
            updateThing(thingBuilder.build());

            pollRequestService = new PollRequestService(octopiServer, this);
            this.getThing().getChannels().stream()
                    .filter(c -> c.getProperties().containsKey("poll") && c.getProperties().containsKey("route"))
                    .forEach(c -> pollRequestService.addPollRequest(c));
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
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
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
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
