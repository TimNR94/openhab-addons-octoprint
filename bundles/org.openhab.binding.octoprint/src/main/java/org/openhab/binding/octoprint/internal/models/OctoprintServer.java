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

package org.openhab.binding.octoprint.internal.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctoprintServer} class defines the abstraction of the octoprint server.
 *
 * It contains the necessary Information to connect to an Octoprint-Server instance.
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class OctoprintServer {
    private final Logger logger = LoggerFactory.getLogger(OctoprintServer.class);
    public final String uri;
    public final String apiKey;
    public final String userName;
    String password;

    public OctoprintServer(String uri, String apiKey, String userName) {
        this.uri = uri;
        this.apiKey = apiKey;
        this.userName = userName;
    }
}
