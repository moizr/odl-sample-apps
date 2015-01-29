/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.cisco.controller.samples.commands;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.MakeCoffeeInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "coffee-maker", name = "make-coffee", description = "Make Coffee Command")
public class MakeCoffeeCommand extends OsgiCommandSupport{

    private final Logger LOG = LoggerFactory.getLogger(MakeCoffeeCommand.class);
    @Argument(name = "coffeeType", description = "Type of the coffee", required = true, multiValued = false)
    private String coffeeType;

    @Override
    protected Object doExecute() throws Exception {
        if(DependencyHolder.getCoffeemakerService() != null){
            DependencyHolder.getCoffeemakerService().makeCoffee(new MakeCoffeeInputBuilder().setCoffeeType(coffeeType).build());
        } else {
            LOG.error("Coffee maker not initialized");
        }

        return null;
    }
}
