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
package org.openhab.binding.upb.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.upb.internal.message.UPBMessage;

/**
 * Callback interface for received UPB messages.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public interface MessageListener {
    void incomingMessage(UPBMessage msg);

    void onError(Throwable t);
}
