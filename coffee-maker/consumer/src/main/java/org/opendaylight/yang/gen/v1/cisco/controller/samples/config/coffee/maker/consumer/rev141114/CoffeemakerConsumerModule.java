package org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.consumer.rev141114;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CmSuppliesEvent;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.Coffeemaker;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoffeemakerConsumerModule extends org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.consumer.rev141114.AbstractCoffeemakerConsumerModule {
    private final Logger LOG = LoggerFactory.getLogger(CoffeemakerConsumerModule.class);
    public CoffeemakerConsumerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public CoffeemakerConsumerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.consumer.rev141114.CoffeemakerConsumerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final ListenerRegistration<NotificationListener<CmSuppliesEvent>> notificationListenerListenerRegistration = getNotificationBrokerDependency().registerNotificationListener(CmSuppliesEvent.class, new NotificationListener<CmSuppliesEvent>() {
            @Override
            public void onNotification(CmSuppliesEvent cmSuppliesEvent) {
                LOG.info(String.format("Received a supplies event, waterLevel = %s, coffeeLevel = %s",
                        cmSuppliesEvent.getWaterLevel(), cmSuppliesEvent.getCoffeeSupplyLevel()));
            }
        });

        getDataBrokerDependency().registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Coffeemaker.class), new DataChangeListener() {
            @Override
            public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
                LOG.info(change.getUpdatedSubtree().toString());
            }
        }, AsyncDataBroker.DataChangeScope.BASE);

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                notificationListenerListenerRegistration.close();
            }
        };
    }

}
