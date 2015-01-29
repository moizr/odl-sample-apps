package org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.commands.rev141114;

import com.cisco.controller.samples.commands.DependencyHolder;
import org.opendaylight.yang.gen.v1.http.cisco.coffeemaker.rev141119.CoffeemakerService;

public class CoffeemakercommandsModule extends org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.commands.rev141114.AbstractCoffeemakercommandsModule {
    public CoffeemakercommandsModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public CoffeemakercommandsModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.cisco.controller.samples.config.coffee.maker.commands.rev141114.CoffeemakercommandsModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        CoffeemakerService coffeemakerService = getRpcRegistryDependency().getRpcService(CoffeemakerService.class);

        DependencyHolder.setCoffeeMakerService(coffeemakerService);

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {

            }
        };
    }

}
