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
package org.openhab.binding.awair.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AwairBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andreas Will - Initial contribution
 */
@NonNullByDefault
public class AwairBindingConstants {

    // General
    public static final String VENDOR = "Awair";
    public static final String BINDING_ID = "awair";
    public static final ThingTypeUID THING_TYPE_ELEMENT = new ThingTypeUID(BINDING_ID, "element");

    public static final String SERVICE_TYPE = "_http._tcp.local.";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ELEMENT);

    // List of all Channel ids
    public static final String AWAIR_JSON = "json";
    public static final String HOSTNAME = "hostname";

    public static final String TIMESTAMP = "timestamp";
    public static final String SCORE = "score";
    public static final String DEW_POINT = "dew_point";
    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String ABS_HUMIDITY = "abs_humid";
    public static final String CO2 = "co2";
    public static final String CO2_EST = "co2_est";
    public static final String CO2_EST_BASELINE = "co2_est_baseline";
    public static final String VOC = "voc";
    public static final String VOC_BASELINE = "voc_baseline";
    public static final String VOC_H2_RAW = "voc_h2_raw";
    public static final String VOC_ETHANOL_RAW = "voc_ethanol_raw";
    public static final String PM25 = "pm25";
    public static final String PM10_EST = "pm10_est";
}
