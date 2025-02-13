package ru.biosoft.server;

import biouml.plugins.server.access.AccessService;
import ru.biosoft.server.servlets.webservices.providers.ServiceProvider;
import ru.biosoft.server.servlets.webservices.providers.WebProviderFactory;
import ru.biosoft.util.Initializer;

public class ServerInitializer extends Initializer
{

    private static ServerInitializer instance;
    public static ServerInitializer getInstance()
    {
        if( instance == null )
            instance = new ServerInitializer();
        return instance;
    }

    @Override protected void initServices()
    {
        ServiceRegistry.registerService("access.service", new AccessService());
    }

    @Override protected void initProviders()
    {
        WebProviderFactory.registerProvider("data", new ServiceProvider());
    }

    public static void initialize()
    {
        getInstance().init();
    }
}
