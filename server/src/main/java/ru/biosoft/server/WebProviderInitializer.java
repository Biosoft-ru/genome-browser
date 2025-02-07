package ru.biosoft.server;

import ru.biosoft.server.servlets.webservices.providers.ServiceProvider;
import ru.biosoft.server.servlets.webservices.providers.WebProviderFactory;

public class WebProviderInitializer
{
    public static void initProviders()
    {
        WebProviderFactory.registerProvider("data", new ServiceProvider());
    }
}
