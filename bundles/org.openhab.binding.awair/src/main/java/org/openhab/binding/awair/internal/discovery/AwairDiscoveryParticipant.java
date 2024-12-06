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
package org.openhab.binding.awair.internal.discovery;

import static org.openhab.binding.awair.internal.AwairBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwairDiscoveryParticipant} class implements detection of AWAIR Things using MDNS
 *
 * @author Andreas Will - Initial contribution
 */

@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
// @Component(service = MDNSDiscoveryParticipant.class, immediate = true, configurationPid = "discovery.awair")
public class AwairDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(AwairDiscoveryParticipant.class);
    private Set<ThingTypeUID> supportedThingTypes;

    public AwairDiscoveryParticipant() {
        logger.debug("Activating ShellyDiscovery service");
        this.supportedThingTypes = SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Nullable
    @Override
    public DiscoveryResult createResult(ServiceInfo serviceInfo) {
        if (serviceInfo != null) {
            String name = serviceInfo.getName().toLowerCase();
            if (name != null) {
                if (name.startsWith("awair")) {
                    String[] hostAddresses = serviceInfo.getHostAddresses();
                    if (hostAddresses != null) {
                        if (hostAddresses.length > 0 && hostAddresses[0] != null) {
                            String address = hostAddresses[0];
                            if (address != null) {
                                if (!address.isEmpty()) {
                                    ThingUID thingUID = getThingUID(serviceInfo);
                                    if (thingUID != null) {
                                        Map<String, Object> properties = new HashMap<>(1);
                                        properties.put(HOSTNAME, address);
                                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                                .withProperties(properties).withLabel("Awair Element")
                                                .withRepresentationProperty(HOSTNAME).withThingType(THING_TYPE_ELEMENT)
                                                .build();
                                        return discoveryResult;
                                    }
                                }
                                logger.trace("AWAIR device discovered with empty address (service name={})", name);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public ThingUID getThingUID(@Nullable ServiceInfo serviceInfo) throws IllegalArgumentException {
        if (serviceInfo == null) {
            throw new IllegalArgumentException("Service must not be NULL!");
        }
        String serviceName = serviceInfo.getName();
        if (serviceName == null) {
            throw new IllegalArgumentException("serviceName must not be null!");
        }
        serviceName = serviceName.toLowerCase();
        if (!serviceName.contains(VENDOR.toLowerCase())) {
            logger.debug("Not an " + VENDOR + " device!");
            return null;
        }
        String[] serviceNames = serviceName.split("-");
        String serviceID = serviceNames[0];
        String devID = serviceNames[1];
        String uniqueID = serviceNames[2];
        if (devID.isEmpty() || uniqueID.isEmpty()) {
            logger.debug("serviceName has improper format: {}", serviceName);
            return null;
        }

        ThingUID thingUID = new ThingUID(serviceID, devID, uniqueID);
        return thingUID;
    }
}
