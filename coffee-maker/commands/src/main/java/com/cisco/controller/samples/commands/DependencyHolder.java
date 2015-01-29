/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.cisco.controller.samples.commands;

import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CoffeemakerService;

public class DependencyHolder {
    private static CoffeemakerService coffeemakerService = null;

    public static void setCoffeeMakerService(CoffeemakerService coffeemakerService) {
        DependencyHolder.coffeemakerService = coffeemakerService;
    }
    public static CoffeemakerService getCoffeemakerService(){
        return DependencyHolder.coffeemakerService;
    }
}
