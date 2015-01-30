package org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.makers.provider.rev141114;

import com.cisco.controller.samples.coffeemaker.provider.CoffeeMakerProvider;

public class CoffeemakerProviderModule extends org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.makers.provider.rev141114.AbstractCoffeemakerProviderModule {
    com.cisco.controller.samples.coffeemaker.provider.CoffeeMakerProvider coffeeMakerProvider;
    public CoffeemakerProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public CoffeemakerProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.makers.provider.rev141114.CoffeemakerProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final CoffeeMakerProvider coffeeMakerProvider
                = new CoffeeMakerProvider(getDataBrokerDependency(), getNotificationBrokerDependency(), getRpcRegistryDependency());

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                coffeeMakerProvider.close();
            }
        };
    }

}
