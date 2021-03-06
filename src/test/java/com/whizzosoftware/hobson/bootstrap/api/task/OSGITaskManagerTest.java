/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.bootstrap.api.task;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerSet;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.api.task.condition.ConditionClassType;
import com.whizzosoftware.hobson.api.task.condition.ConditionEvaluationContext;
import com.whizzosoftware.hobson.api.task.condition.TaskConditionClass;
import com.whizzosoftware.hobson.api.task.condition.TaskConditionClassProvider;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class OSGITaskManagerTest {
    @Test
    public void testTaskStartup() {
        MockTaskStore store = new MockTaskStore();

        List<PropertyContainer> taskConditions = new ArrayList<>();
        final PropertyContainerClassContext pccc = PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "cc1");
        taskConditions.add(new PropertyContainer(pccc, null));

        final HobsonTask task = new HobsonTask(TaskContext.createLocal("task1"), "task", null, null, taskConditions, null);
        store.saveTask(task);

        MockPluginManager pm = new MockPluginManager();
        final MockHobsonPlugin plugin = new MockHobsonPlugin("plugin1");
        MockTaskProvider taskProvider = new MockTaskProvider();
        plugin.setTaskProvider(taskProvider);
        pm.addLocalPlugin(plugin);

        OSGITaskManager tm = new OSGITaskManager();
        tm.setPluginManager(pm);
        tm.setTaskStore(store);
        tm.setTaskRegistrationContext(new TaskRegistrationContext() {
            @Override
            public Collection<HobsonTask> getAllTasks(HubContext ctx) {
                return Collections.singletonList(task);
            }

            @Override
            public boolean isTaskFullyResolved(HobsonTask task) {
                return true;
            }

            @Override
            public HobsonPlugin getPluginForTask(HobsonTask task) {
                return plugin;
            }
        });
        tm.start();

        // the task creation event is async so we need to wait until the callback is completed
        synchronized (taskProvider) {
            try {
                tm.queueTaskRegistration();
                taskProvider.wait();
            } catch (InterruptedException ignored) {}
        }

        assertEquals(1, taskProvider.getCreatedTasks().size());
        assertEquals(task, taskProvider.getCreatedTasks().get(0));
    }

    @Test
    public void testCreateTask() {
        final PluginContext pctx = PluginContext.createLocal("plugin1");
        MockTaskStore store = new MockTaskStore();
        MockPluginManager pm = new MockPluginManager();
        OSGITaskManager tm = new OSGITaskManager();
        tm.setPluginManager(pm);
        tm.setTaskStore(store);
        tm.setTaskRegistrationExecutor(new TaskRegistrationExecutor(HubContext.createLocal(), null));
        tm.setTaskConditionClassProvider(new TaskConditionClassProvider() {
            @Override
            public TaskConditionClass getConditionClass(PropertyContainerClassContext ctx) {
                final List<TypedProperty> props = new ArrayList<>();
                props.add(new TypedProperty.Builder("id1", "name1", "desc1", TypedProperty.Type.STRING).build());
                return new MockTaskConditionClass(pctx, ConditionClassType.trigger) {
                    public List<TypedProperty> createProperties() {
                        return props;
                    }
                };
            }
        });

        List<PropertyContainer> conds = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("id1", "bar");
        conds.add(new PropertyContainer(PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "cc1"), map));

        List<PropertyContainer> lpc = new ArrayList<>();
        PropertyContainerSet actions = new PropertyContainerSet("id1", lpc);

        try {
            tm.createTask(HubContext.createLocal(), null, null, null, null);
            fail("Should have thrown exception");
        } catch (HobsonInvalidRequestException ignored) {}
        try {
            tm.createTask(HubContext.createLocal(), "name", null, null, null);
            fail("Should have thrown exception");
        } catch (HobsonInvalidRequestException ignored) {}
        try {
            tm.createTask(HubContext.createLocal(), "name", "desc", null, null);
            fail("Should have thrown exception");
        } catch (HobsonInvalidRequestException ignored) {}
        tm.createTask(HubContext.createLocal(), "name", "desc", conds, actions);

        Collection<HobsonTask> tasks = store.getAllTasks(HubContext.createLocal());
        assertEquals(1, tasks.size());
        HobsonTask task = tasks.iterator().next();
        assertEquals("name", task.getName());
        assertEquals("desc", task.getDescription());
        List<PropertyContainer> pcs = task.getConditions();
        assertEquals(1, pcs.size());
        PropertyContainer pc = pcs.get(0);
        assertEquals("bar", pc.getStringPropertyValue("id1"));
    }
}
