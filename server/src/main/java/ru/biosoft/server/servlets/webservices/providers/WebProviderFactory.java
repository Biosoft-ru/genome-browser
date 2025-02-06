package ru.biosoft.server.servlets.webservices.providers;

import java.util.HashMap;
import java.util.Map;

import ru.biosoft.access.exception.InitializationException;

//TODO: new class instead of Registry-based ru.biosoft.server.servlets.webservices.providers.WebProviderFactory
public class WebProviderFactory
{
    private static Map<String, WebProvider> providers;

    private volatile boolean initialized = false;
    private volatile boolean initializing = false;

    protected final void init()
    {
        if( !initialized )
        {
            synchronized (this)
            {
                if( !initialized )
                {
                    if( initializing )
                        throw new InitializationException("Concurrent initialization of WebProviderFactory");
                    initializing = true;
                    try
                    {
                        providers = new HashMap<>();
                    }
                    finally
                    {
                        initializing = false;
                        initialized = true;
                    }
                }
            }
        }
    }

    public static void registerProvider(String name, WebProvider provider)
    {
        providers.put(name, provider);
    }

    public static WebProvider getProvider(String name)
    {
        return providers.get(name);
    }

}
