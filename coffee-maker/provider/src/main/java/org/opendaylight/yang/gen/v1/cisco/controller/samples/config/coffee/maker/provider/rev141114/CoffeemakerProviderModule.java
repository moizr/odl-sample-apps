package org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.provider.rev141114;

import com.cisco.controller.samples.coffeemaker.provider.CoffeeMakerProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CoffeemakerService;

public class CoffeemakerProviderModule extends org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.provider.rev141114.AbstractCoffeemakerProviderModule {
    public CoffeemakerProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public CoffeemakerProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.provider.rev141114.CoffeemakerProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        final com.cisco.controller.samples.coffeemaker.provider.CoffeeMakerProvider coffeeMakerProvider
                = new CoffeeMakerProvider(getDataBrokerDependency(), getNotificationBrokerDependency());

        final BindingAwareBroker.RpcRegistration<CoffeemakerService> coffeemakerServiceRpcRegistration
                = getRpcRegistryDependency().addRpcImplementation(CoffeemakerService.class, coffeeMakerProvider);

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                coffeemakerServiceRpcRegistration.close();
            }
        };
    }

}
