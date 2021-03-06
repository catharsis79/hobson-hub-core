/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.bootstrap.util;

import ch.qos.logback.contrib.json.JsonFormatter;

import java.util.Map;

/**
 * A Logback JsonFormatter implementation that creates simple JSON via String concatenation.
 *
 * @author Dan Noguerol
 */
public class HobsonLogJsonFormatter implements JsonFormatter {
    @Override
    public String toJsonString(Map map) throws Exception {
        StringBuilder sb = new StringBuilder(150);
        sb.append("{\"time\":\"");
        sb.append(map.get("timestamp"));
        sb.append("\",\"thread\":\"");
        sb.append(map.get("thread"));
        sb.append("\",\"level\":\"");
        sb.append(map.get("level"));
        sb.append("\",\"message\":");
        sb.append(JsonUtil.escape(map.get("message").toString()));
        if (map.containsKey("exception")) {
            sb.append(",\"exception\":");
            sb.append(JsonUtil.escape(map.get("exception").toString()));
        }
        sb.append("}\n");
        return sb.toString();
    }
}
