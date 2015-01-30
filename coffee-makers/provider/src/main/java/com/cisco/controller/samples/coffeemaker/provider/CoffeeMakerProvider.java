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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.notify.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.CmResponseType;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.CmSuppliesEvent;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.CmSuppliesEventBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.CoffeeMakerContext;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.Coffeemakers;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.CoffeemakersBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.CoffeemakersService;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.MakeCoffeeInput;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.MakeCoffeeOutput;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.MakeCoffeeOutputBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.coffeemakers.Coffeemaker;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.coffeemakers.CoffeemakerBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.coffeemakers.CoffeemakerKey;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.coffeemakers.coffeemaker.CoffeeLog;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.coffeemakers.coffeemaker.CoffeeLogBuilder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemakers.rev141119.coffeemakers.coffeemaker.CoffeeLogKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoffeeMakerProvider implements CoffeemakersService, AutoCloseable {

    private DataBroker dataBroker;
    private NotificationPublishService notificationPublishService;
    private final RpcProviderRegistry rpcRegistry;
    ListenerRegistration<DataChangeListener> registration;
    private final Logger LOG = LoggerFactory.getLogger(CoffeeMakerProvider.class);

    final private InstanceIdentifier<Coffeemaker>
            coffeeMakersId =
            InstanceIdentifier.create(Coffeemakers.class).child(Coffeemaker.class);


    public CoffeeMakerProvider(DataBroker dataBroker, NotificationPublishService notificationPublishService, final RpcProviderRegistry rpcRegistry){
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.rpcRegistry = rpcRegistry;
        LOG.info("Registering coffee maker listener");
        registration = this.dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, coffeeMakersId, new DataChangeListener() {
            @Override
            public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
                if (change.getCreatedData() != null) {
                    LOG.info("{}", change.getCreatedData());

                    Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();

                    Set<Map.Entry<InstanceIdentifier<?>, DataObject>> entries = createdData.entrySet();
                    for(Map.Entry<InstanceIdentifier<?>, DataObject> entry : entries){
                        createOperationalCoffeeMaker(((Coffeemaker) entry.getValue()).getOwner());

                        LOG.info("Registering Rpc Provider for {}", entry.getKey());
                        BindingAwareBroker.RoutedRpcRegistration<CoffeemakersService> reg = rpcRegistry.addRoutedRpcImplementation(CoffeemakersService.class, CoffeeMakerProvider.this);
                        reg.registerPath(CoffeeMakerContext.class, entry.getKey());
                    }
                }
            }
        }, AsyncDataBroker.DataChangeScope.SUBTREE);

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Coffeemakers.class), new CoffeemakersBuilder().build());
        writeTransaction.submit();
    }

    private void createOperationalCoffeeMaker(String owner){
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        CoffeemakerBuilder builder = new CoffeemakerBuilder();
        builder.setWaterLevel((short) 100);
        builder.setCoffeeSupplyLevel((short) 100);
        builder.setOwner(owner);
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Coffeemakers.class).child(Coffeemaker.class, new CoffeemakerKey(owner)), builder.build());
        writeTransaction.submit();
    }

    @Override
    public Future<RpcResult<MakeCoffeeOutput>> makeCoffee(MakeCoffeeInput makeCoffeeInput) {
        final String coffeeType = makeCoffeeInput.getCoffeeType();
        InstanceIdentifier<Coffeemaker> coffeeMakerId = (InstanceIdentifier<Coffeemaker>) makeCoffeeInput.getId();
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
                builder.setOwner(coffeemaker.getOwner());
                builder.setKey(new CoffeemakerKey(coffeemaker.getOwner()));

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

        return futureResult;
    }

    @Override
    public void close() throws Exception {
    }
}
