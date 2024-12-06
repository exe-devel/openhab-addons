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
package org.openhab.binding.awair.internal.handler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The {@link AirQualityReading} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andreas Will - Initial contribution
 */
public class AirQualityReading {
    private String timestamp;
    private Integer score;
    private Double dew_point;
    private Double temp;
    private Double humid;
    private Double abs_humid;
    private Integer co2;
    private Integer co2_est;
    private Integer co2_est_baseline;
    private Integer voc;
    private Integer voc_baseline;
    private Integer voc_h2_raw;
    private Integer voc_ethanol_raw;
    private Integer pm25;
    private Integer pm10_est;

    public ZonedDateTime getTimestamp() {
        // return ZonedDateTime.parse(timestamp);
        return Instant.parse(timestamp).atZone(ZoneId.systemDefault());
    }

    public Integer getScore() {
        return score;
    }

    public Double getDewPoint() {
        return dew_point;
    }

    public Double getTemperature() {
        return temp;
    }

    public Double getHumidity() {
        return humid;
    }

    public Integer getCO2() {
        return co2;
    }

    public Integer getTvoc() {
        if (voc < 0) {
            voc *= -1;
        }
        return voc;
    }

    public Integer getPM25() {
        return pm25;
    }
}
