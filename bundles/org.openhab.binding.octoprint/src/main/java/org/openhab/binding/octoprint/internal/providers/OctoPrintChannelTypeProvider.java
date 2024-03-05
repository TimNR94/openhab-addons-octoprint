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

package org.openhab.binding.octoprint.internal.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link OctoPrintChannelTypeProvider} TODO
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
@Component(service = { OctoPrintChannelTypeProvider.class, ChannelTypeProvider.class })

public class OctoPrintChannelTypeProvider implements ChannelTypeProvider {
    private final Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    public boolean hasChannelType(ChannelTypeUID channelTypeUID) {
        return channelTypes.containsKey(channelTypeUID);
    }

    public void addChannelType(ChannelType channelType) {
        channelTypes.put(channelType.getUID(), channelType);
    }
}
