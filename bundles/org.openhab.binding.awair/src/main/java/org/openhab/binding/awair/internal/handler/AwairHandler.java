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

import static org.openhab.binding.awair.internal.AwairBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awair.internal.AwairConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AwairHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andreas Will - Initial contribution
 */
@NonNullByDefault
public class AwairHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AwairHandler.class);

    private @Nullable AwairConfiguration config;

    private @Nullable ScheduledFuture<?> refreshJob;

    public AwairHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
        }
    }

    private void updateChannels() {
        String data = getData();

        AirQualityReading aqr = null;
        aqr = new Gson().fromJson(data, AirQualityReading.class);

        if (aqr != null) {
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                String channelID = channelUID.getId();
                switch (channelID) {
                    case TIMESTAMP:
                        updateState(channelUID, new DateTimeType(aqr.getTimestamp()));
                        break;
                    case SCORE:
                        updateState(channelUID, new DecimalType(aqr.getScore()));
                        break;
                    case DEW_POINT:
                        updateState(channelUID, new QuantityType<>(aqr.getDewPoint(), SIUnits.CELSIUS));
                        break;
                    case TEMPERATURE:
                        updateState(channelUID, new QuantityType<>(aqr.getTemperature(), SIUnits.CELSIUS));
                        break;
                    case HUMIDITY:
                        updateState(channelUID, new QuantityType<>(aqr.getHumidity(), Units.PERCENT));
                        break;
                    case ABS_HUMIDITY:
                        updateState(channelUID, new DecimalType(aqr.getAbsolute_humidity()));
                        break;
                    case CO2:
                        updateState(channelUID, new QuantityType<>(aqr.getCO2(), Units.PARTS_PER_MILLION));
                        break;
                    case CO2_EST:
                        updateState(channelUID, new QuantityType<>(aqr.getCo2_est(), Units.PARTS_PER_MILLION));
                        break;
                    case CO2_EST_BASELINE:
                        updateState(channelUID, new DecimalType(aqr.getCo2_est_baseline()));
                        break;
                    case VOC:
                        updateState(channelUID, new QuantityType<>(aqr.getVoc(), Units.PARTS_PER_BILLION));
                        break;
                    case VOC_BASELINE:
                        updateState(channelUID, new DecimalType(aqr.getVoc_baseline()));
                        break;
                    case VOC_H2_RAW:
                        updateState(channelUID, new DecimalType(aqr.getVoc_h2_raw()));
                        break;
                    case VOC_ETHANOL_RAW:
                        updateState(channelUID, new DecimalType(aqr.getVoc_ethanol_raw()));
                        break;
                    case PM25:
                        updateState(channelUID, new QuantityType<>(aqr.getPM25(), Units.MICROGRAM_PER_CUBICMETRE));
                        break;
                    case PM10_EST:
                        updateState(channelUID, new QuantityType<>(aqr.getPM10(), Units.MICROGRAM_PER_CUBICMETRE));
                        break;

                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Invalid values / AQR returned NULL");
        }
    }

    private String getData() {
        String hostname = (String) getConfig().get("hostname");
        // int timeout = (int) getConfig().get("timeout");
        int timeout = 10000;
        String urlStr = "http://" + hostname + "/air-data/latest";
        logger.debug("URL = {}, Timeout = {}", urlStr, timeout);

        try {
            String response = HttpUtil.executeUrl("GET", urlStr, null, null, null, timeout);
            logger.debug("awairresponse = {}", response);
            if (response != null) {
                updateStatus(ThingStatus.ONLINE);
                return response;
            }
            return "";
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
        return "";
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(AwairConfiguration.class);

        freeRefreshJob();

        refreshJob = scheduler.scheduleWithFixedDelay(this::updateChannels, 0, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of the refresher");
        freeRefreshJob();
    }
}
