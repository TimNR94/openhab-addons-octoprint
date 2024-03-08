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

import org.openhab.core.thing.Channel;

/**
 * The {@link PollRequestInformation}.TODO
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class PollRequestInformation {
    public final Channel channel;
    public final String route;

    public PollRequestInformation(Channel channel, String route) {
        this.channel = channel;
        this.route = route;
    }
}
