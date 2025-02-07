package ru.biosoft.bsa;

import ru.biosoft.bsa.server.BSAService;
import ru.biosoft.server.ServiceRegistry;

public class BSAServiceInitializer
{
    public static void init()
    {
        ServiceRegistry.registerService("bsa", new BSAService());
    }
}
