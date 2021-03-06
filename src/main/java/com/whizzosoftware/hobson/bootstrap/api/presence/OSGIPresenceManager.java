/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.bootstrap.api.presence;

import com.whizzosoftware.hobson.api.event.*;
import com.whizzosoftware.hobson.api.event.EventListener;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.presence.*;
import com.whizzosoftware.hobson.api.presence.store.PresenceStore;
import com.whizzosoftware.hobson.bootstrap.api.presence.store.MapDBPresenceStore;
import org.osgi.framework.FrameworkUtil;

import java.util.*;

/**
 * An OSGi implementation of PresenceManager.
 *
 * @author Dan Noguerol
 */
public class OSGIPresenceManager implements PresenceManager, EventListener {
    private PresenceStore presenceStore;
    private Map<PresenceEntityContext,PresenceLocationContext> entityLocations = new HashMap<>();

    private volatile PluginManager pluginManager;
    private volatile EventManager eventManager;

    public void start() {
        // listen for presence events
        eventManager.addListener(HubContext.createLocal(), this, new String[] {EventTopics.PRESENCE_TOPIC});

        // if a task store hasn't already been injected, create a default one
        if (presenceStore == null) {
            this.presenceStore = new MapDBPresenceStore(
                pluginManager.getDataFile(
                    PluginContext.createLocal(FrameworkUtil.getBundle(getClass()).getSymbolicName()),
                    "presenceEntities"
                )
            );
        }

    }

    public void stop() {
        eventManager.removeListener(HubContext.createLocal(), this, new String[]{EventTopics.PRESENCE_TOPIC});
    }

    public void setPresenceStore(PresenceStore presenceStore) {
        this.presenceStore = presenceStore;
    }

    @Override
    public Collection<PresenceEntity> getAllPresenceEntities(HubContext ctx) {
        return presenceStore.getAllPresenceEntities(ctx);
    }

    @Override
    public PresenceEntity getPresenceEntity(PresenceEntityContext ctx) {
        return presenceStore.getPresenceEntity(ctx);
    }

    @Override
    public PresenceEntityContext addPresenceEntity(HubContext ctx, String name) {
        PresenceEntityContext pec = PresenceEntityContext.create(ctx, UUID.randomUUID().toString());
        presenceStore.savePresenceEntity(new PresenceEntity(pec, name));
        return pec;
    }

    @Override
    public void deletePresenceEntity(PresenceEntityContext ctx) {
        presenceStore.deletePresenceEntity(ctx);
    }

    @Override
    public PresenceLocation getPresenceEntityLocation(PresenceEntityContext ctx) {
        return presenceStore.getPresenceLocation(entityLocations.get(ctx));
    }

    @Override
    public void updatePresenceEntityLocation(PresenceEntityContext ectx, PresenceLocationContext newLocationCtx) {
        PresenceLocation oldLocation = presenceStore.getPresenceLocation(entityLocations.get(ectx));

        // update entity's location
        PresenceLocationContext oldLocationCtx = oldLocation != null ? oldLocation.getContext() : null;
        entityLocations.put(ectx, newLocationCtx);

        // update entity's last update time
        PresenceEntity pe = presenceStore.getPresenceEntity(ectx);
        presenceStore.savePresenceEntity(new PresenceEntity(ectx, pe.getName(), System.currentTimeMillis()));

        // post an update event
        eventManager.postEvent(ectx.getHubContext(), new PresenceUpdateNotificationEvent(System.currentTimeMillis(), ectx, oldLocationCtx, newLocationCtx));
    }

    @Override
    public Collection<PresenceLocation> getAllPresenceLocations(HubContext ctx) {
        return presenceStore.getAllPresenceLocations(ctx);
    }

    @Override
    public PresenceLocation getPresenceLocation(PresenceLocationContext ctx) {
        return presenceStore.getPresenceLocation(ctx);
    }

    @Override
    public PresenceLocationContext addPresenceLocation(HubContext hctx, String name, Double latitude, Double longitude, Double radius, Integer beaconMajor, Integer beaconMinor) {
        PresenceLocationContext ctx = PresenceLocationContext.create(hctx, UUID.randomUUID().toString());
        PresenceLocation pl;
        if (beaconMajor != null && beaconMinor != null) {
            pl = new PresenceLocation(ctx, name, beaconMajor, beaconMinor);
        } else {
            pl = new PresenceLocation(ctx, name, latitude, longitude, radius);
        }
        presenceStore.savePresenceLocation(pl);
        return ctx;
    }

    @Override
    public void deletePresenceLocation(PresenceLocationContext ctx) {
        presenceStore.deletePresenceLocation(ctx);
    }

    @Override
    public void onHobsonEvent(HobsonEvent event) {
        if (event != null && event instanceof PresenceUpdateRequestEvent) {
            PresenceUpdateRequestEvent pure = (PresenceUpdateRequestEvent)event;
            updatePresenceEntityLocation(pure.getEntityContext(), pure.getLocation());
        }
    }
}
