/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.cisco.controller.samples.coffeemaker.provider;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.notify.NotificationPublishService;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CmResponseType;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CmSuppliesEvent;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CmSuppliesEventBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.Coffeemaker;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CoffeemakerBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CoffeemakerService;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.MakeCoffeeInput;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.MakeCoffeeOutput;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.MakeCoffeeOutputBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.coffeemaker.CoffeeLog;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.coffeemaker.CoffeeLogBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.coffeemaker.CoffeeLogKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class CoffeeMakerProvider implements CoffeemakerService {

    private DataBroker dataBroker;
    private NotificationPublishService notificationPublishService;
    private static final short INITIAL_COFFEE_LEVEL = 100;
    private static final short INITIAL_WATER_LEVEL = 100;
    private static final String COFFEE_MAKER_OWNER_NAME = "opendaylight";

    final private InstanceIdentifier<Coffeemaker>
            coffeeMakerId =
            InstanceIdentifier.builder(Coffeemaker.class).build();


    public CoffeeMakerProvider(DataBroker dataBroker, NotificationPublishService notificationPublishService){
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        createCoffeeMaker(INITIAL_COFFEE_LEVEL, INITIAL_WATER_LEVEL, COFFEE_MAKER_OWNER_NAME);
    }

    private void createCoffeeMaker(short coffeeLevel, short waterLevel, String owner) {
        initConfig(owner);
        initOperational(coffeeLevel, waterLevel, owner);
    }

    private void initConfig(String owner) {
        CoffeemakerBuilder builder = new CoffeemakerBuilder();
        builder.setOwner(owner);
        Coffeemaker coffeeMaker = builder.build();

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, coffeeMakerId, coffeeMaker);

        writeTransaction.submit();
    }

    private void initOperational(short coffeeLevel, short waterLevel, String owner) {
        CoffeemakerBuilder builder = new CoffeemakerBuilder();
        builder.setOwner(owner);
        builder.setWaterLevel(waterLevel);
        builder.setCoffeeSupplyLevel(coffeeLevel);
        Coffeemaker coffeeMaker = builder.build();

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, coffeeMakerId, coffeeMaker);

        writeTransaction.submit();
    }


    @Override
    public Future<RpcResult<MakeCoffeeOutput>> makeCoffee(MakeCoffeeInput makeCoffeeInput) {
        final String coffeeType = makeCoffeeInput.getCoffeeType();
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        final SettableFuture<RpcResult<MakeCoffeeOutput>> futureResult = SettableFuture.create();
        try {
            Coffeemaker coffeemaker = tx.read(LogicalDatastoreType.OPERATIONAL, coffeeMakerId).get().get();

            short coffeeLevel = coffeemaker.getCoffeeSupplyLevel();
            short waterLevel = coffeemaker.getWaterLevel();

            if(coffeeLevel < 1 || waterLevel < 1) {
                // Error state can't make coffee anymore
                MakeCoffeeOutputBuilder outputBuilder = new MakeCoffeeOutputBuilder();
                outputBuilder.setCmResponse(CmResponseType.forValue(1));
                outputBuilder.setAdditionalInfo("Coffee make supplies are limited. Coffee maker can not make " + coffeeType);
                futureResult.set(RpcResultBuilder.success(outputBuilder.build()).build());
            } else {
                CoffeeLogBuilder logBuilder = new CoffeeLogBuilder();
                logBuilder.setType(coffeeType);
                logBuilder.setLastMakeTime(Calendar.getInstance().getTime().toString());
                logBuilder.setKey(new CoffeeLogKey(coffeeType));
                List<CoffeeLog> coffeeLog = coffeemaker.getCoffeeLog();
                if(coffeeLog == null) {
                    coffeeLog = new ArrayList<>();
                }
                coffeeLog.add(logBuilder.build());
                CoffeemakerBuilder builder = new CoffeemakerBuilder();

                final short newCoffeeLevel = (short)(coffeeLevel-1);
                final short newWaterLevel = (short) (waterLevel-1);
                builder.setCoffeeSupplyLevel(newCoffeeLevel);
                builder.setWaterLevel(newWaterLevel);
                builder.setCoffeeLog(coffeeLog);

                // place operational data in data store tree
                tx.put(LogicalDatastoreType.OPERATIONAL, coffeeMakerId, builder.build());
                Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(final Void result) {
                        MakeCoffeeOutputBuilder outputBuilder = new MakeCoffeeOutputBuilder();
                        outputBuilder.setCmResponse(CmResponseType.forValue(0));
                        outputBuilder.setAdditionalInfo(coffeeType + " is ready");
                        futureResult.set(RpcResultBuilder.success(outputBuilder.build()).build());

                        // Raise the notification
                        CmSuppliesEvent cmSuppliesEvent = new CmSuppliesEventBuilder().setCoffeeSupplyLevel(newCoffeeLevel).setWaterLevel(newWaterLevel).build();
                        notificationPublishService.publish(cmSuppliesEvent);
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        futureResult.set(RpcResultBuilder.<MakeCoffeeOutput>failed()
                                .withError(RpcError.ErrorType.APPLICATION, t.getMessage()).build());
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return futureResult;    }
}
