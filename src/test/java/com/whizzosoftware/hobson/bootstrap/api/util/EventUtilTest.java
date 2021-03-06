package com.whizzosoftware.hobson.bootstrap.api.util;

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.device.MockHobsonDevice;
import com.whizzosoftware.hobson.api.event.DeviceStartedEvent;
import com.whizzosoftware.hobson.api.event.DeviceStoppedEvent;
import com.whizzosoftware.hobson.api.event.EventTopics;
import com.whizzosoftware.hobson.api.plugin.MockHobsonPlugin;
import org.junit.Test;
import org.osgi.service.event.Event;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EventUtilTest {
    @Test
    public void testDeviceStartedToEventMapping() {
        MockHobsonPlugin plugin = new MockHobsonPlugin("pid");
        plugin.setDeviceManager(new MockDeviceManager());
        MockHobsonDevice device = new MockHobsonDevice(plugin, "did");
        DeviceStartedEvent dse = new DeviceStartedEvent(System.currentTimeMillis(), device.getContext());
        Event e = EventUtil.createEventFromHobsonEvent(dse);

        assertEquals(dse.getTopic(), e.getTopic());
        assertEquals(dse.getEventId(), e.getProperty("eventId"));
        assertEquals(dse.getDeviceContext(), e.getProperty(DeviceStartedEvent.PROP_DEVICE_CONTEXT));
    }

    @Test
    public void testEventToDeviceStartedMapping() {
        MockHobsonPlugin plugin = new MockHobsonPlugin("pid");
        plugin.setDeviceManager(new MockDeviceManager());
        MockHobsonDevice device = new MockHobsonDevice(plugin, "did");

        Map props = new HashMap();
        props.put(DeviceStartedEvent.PROP_EVENT_ID, DeviceStartedEvent.ID);
        props.put(DeviceStartedEvent.PROP_DEVICE_CONTEXT, device.getContext());
        Event e = new Event(EventTopics.STATE_TOPIC, props);

        DeviceStartedEvent dse = new DeviceStartedEvent(EventUtil.createMapFromEvent(e));

        assertEquals("pid", dse.getDeviceContext().getPluginId());
        assertEquals(DeviceStartedEvent.ID, dse.getEventId());
        assertEquals(EventTopics.STATE_TOPIC, dse.getTopic());
        assertEquals(device.getContext(), dse.getDeviceContext());
    }

    @Test
    public void testDeviceStoppedEventMapping() {
        MockHobsonPlugin plugin = new MockHobsonPlugin("pid");
        plugin.setDeviceManager(new MockDeviceManager());
        MockHobsonDevice device = new MockHobsonDevice(plugin, "did");

        DeviceStoppedEvent dse = new DeviceStoppedEvent(System.currentTimeMillis(), device.getContext());
        Event e = EventUtil.createEventFromHobsonEvent(dse);

        assertEquals(dse.getTopic(), e.getTopic());
        assertEquals(dse.getEventId(), e.getProperty("eventId"));
        assertEquals(dse.getDeviceContext(), e.getProperty(DeviceStoppedEvent.PROP_DEVICE_CONTEXT));
    }

    @Test
    public void testEventToDeviceStoppedMapping() {
        MockHobsonPlugin plugin = new MockHobsonPlugin("pid");
        plugin.setDeviceManager(new MockDeviceManager());
        MockHobsonDevice device = new MockHobsonDevice(plugin, "did");

        Map props = new HashMap();
        props.put(DeviceStoppedEvent.PROP_EVENT_ID, DeviceStoppedEvent.ID);
        props.put(DeviceStoppedEvent.PROP_DEVICE_CONTEXT, device.getContext());
        Event e = new Event(EventTopics.STATE_TOPIC, props);

        DeviceStoppedEvent dse = new DeviceStoppedEvent(EventUtil.createMapFromEvent(e));

        assertEquals("pid", dse.getDeviceContext().getPluginId());
        assertEquals(DeviceStoppedEvent.ID, dse.getEventId());
        assertEquals(EventTopics.STATE_TOPIC, dse.getTopic());
        assertEquals(device.getContext(), dse.getDeviceContext());
    }
}
