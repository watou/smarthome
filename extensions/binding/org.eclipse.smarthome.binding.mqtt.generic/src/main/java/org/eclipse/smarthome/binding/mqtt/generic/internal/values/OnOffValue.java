/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * Implements an on/off boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OnOffValue implements AbstractMqttThingValue {
    private OnOffType boolValue;
    private final String onValue;
    private final String offValue;
    private final boolean inverse;
    private final boolean receivesOnly;

    /**
     * Creates a switch On/Off type, that accepts "ON", "1" for on and "OFF","0" for off.
     */
    public OnOffValue() {
        this.inverse = false;
        this.onValue = "ON";
        this.offValue = "OFF";
        this.boolValue = OnOffType.OFF;
        this.receivesOnly = false;
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param isInversedOnOff If true, inverses ON/OFF interpretations.
     */
    public OnOffValue(@Nullable String onValue, @Nullable String offValue, @Nullable Boolean isInversedOnOff) {
        this.inverse = isInversedOnOff != null && isInversedOnOff;
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.boolValue = OnOffType.OFF;
        this.receivesOnly = false;
    }

    /**
     * Creates a new On/Off value that either corresponds to a SWITCH ESH type (if isSettable==true) or to a CONTACT
     * type otherwise.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param isInversedOnOff If true, inverses ON/OFF interpretations.
     * @param receivesOnly Determines the ESH type. SWITCH if true, CONTACT otherwise
     */
    private OnOffValue(@Nullable String onValue, @Nullable String offValue, @Nullable Boolean isInversedOnOff,
            boolean receivesOnly) {
        this.inverse = isInversedOnOff != null && isInversedOnOff;
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.boolValue = OnOffType.OFF;
        this.receivesOnly = receivesOnly;
    }

    /**
     * Creates a new CONTACT On/Off value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param isInversedOnOff If true, inverses ON/OFF interpretations.
     */
    public static OnOffValue createReceiveOnly(@Nullable String onValue, @Nullable String offValue,
            @Nullable Boolean isInversedOnOff) {
        return new OnOffValue(onValue, offValue, isInversedOnOff, true);
    }

    @Override
    public State getValue() {
        return boolValue;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            boolValue = ((OnOffType) command);
            boolValue = inverse ? (boolValue == OnOffType.ON ? OnOffType.OFF : OnOffType.ON) : boolValue;
        } else if (command instanceof OpenClosedType) {
            boolValue = ((OpenClosedType) command) == OpenClosedType.OPEN ? OnOffType.ON : OnOffType.OFF;
            boolValue = inverse ? (boolValue == OnOffType.ON ? OnOffType.OFF : OnOffType.ON) : boolValue;
        } else if (command instanceof StringType) {
            boolValue = (OnOffType) update(command.toString());
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for OnOffValue");
        }

        return (boolValue == OnOffType.ON) ? onValue : offValue;
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        if (onValue.equals(updatedValue) || "ON".equals(updatedValue.toUpperCase()) || "1".equals(updatedValue)) {
            boolValue = OnOffType.ON;
        } else if (offValue.equals(updatedValue) || "OFF".equals(updatedValue.toUpperCase())
                || "0".equals(updatedValue)) {
            boolValue = OnOffType.OFF;
        } else {
            throw new IllegalArgumentException("Didn't recognise the on/off value " + updatedValue);
        }

        boolValue = inverse ? (boolValue == OnOffType.ON ? OnOffType.OFF : OnOffType.ON) : boolValue;

        return boolValue;
    }

    @Override
    public String channelTypeID() {
        return receivesOnly ? CoreItemFactory.CONTACT : CoreItemFactory.SWITCH;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(null, null, null, "%s " + unit, receivesOnly || readOnly, Collections.emptyList());
    }
}
