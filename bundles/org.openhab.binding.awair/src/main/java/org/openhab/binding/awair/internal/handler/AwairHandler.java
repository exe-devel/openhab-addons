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
 * @author Hans Heinz - Initial contribution
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
            // switch (channelUID.getId()) {
            // case AWAIR_JSON:
            //
            // // TODO: handle command
            //
            // // Note: if communication with thing fails for some reason,
            // // indicate that by setting the status with detail information:
            // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // // "Could not control device at IP address x.x.x.x");
            //
            // State state;
            // state = getAwairJSON();
            // // state = new StringType("test");
            //
            // updateState(channelUID, state);
            // break;
            // }
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
                    case TEMPERATURE:
                        updateState(channelUID, new QuantityType<>(aqr.getTemperature(), SIUnits.CELSIUS));
                        break;
                    case HUMIDITY:
                        updateState(channelUID, new QuantityType<>(aqr.getHumidity(), Units.PERCENT));
                        break;
                    case CO2:
                        updateState(channelUID, new QuantityType<>(aqr.getCO2() * 1000000, Units.PARTS_PER_MILLION));
                        break;
                    case TVOC:
                        updateState(channelUID, new QuantityType<>(aqr.getTvoc() * 1000000, Units.PARTS_PER_MILLION));
                        break;
                    case PM25:
                        updateState(channelUID, new QuantityType<>(aqr.getPM25() * 1000000, Units.PARTS_PER_MILLION));
                        break;
                    case DEW_POINT:
                        updateState(channelUID, new DecimalType(aqr.getDewPoint()));
                        break;
                    case SCORE:
                        updateState(channelUID, new DecimalType(aqr.getScore()));
                        break;
                    case TIMESTAMP:
                        updateState(channelUID, new DateTimeType(aqr.getTimestamp()));
                        break;
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Invalid values");
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
