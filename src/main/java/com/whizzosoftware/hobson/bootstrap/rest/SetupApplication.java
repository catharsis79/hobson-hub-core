/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.bootstrap.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

/**
 * A Restlet application for the Hobson Hub setup wizard.
 *
 * @author Dan Noguerol
 */
public class SetupApplication extends Application {
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router();
        router.attach("/", new ClassLoaderOverrideDirectory(getContext(), "clap://class/www/", getClass().getClassLoader()));

        ChallengeAuthenticator auth = new AlterableStatusCodeChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "Hobson (default is local/local)");
        auth.setVerifier(new HobsonVerifier());
        auth.setNext(router);

        return auth;
    }
}
