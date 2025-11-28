package ru.biosoft.server;

import biouml.plugins.server.access.AccessService;
import ru.biosoft.server.servlets.webservices.providers.DocumentProvider;
import ru.biosoft.server.servlets.webservices.providers.HtmlTemplateProvider;
import ru.biosoft.server.servlets.webservices.providers.ImageProvider;
import ru.biosoft.server.servlets.webservices.providers.ImportProvider;
import ru.biosoft.server.servlets.webservices.providers.PerspectivesProvider;
import ru.biosoft.server.servlets.webservices.providers.PreferencesProvider;
import ru.biosoft.server.servlets.webservices.providers.ServiceProvider;
import ru.biosoft.server.servlets.webservices.providers.WebActionsProvider;
import ru.biosoft.server.servlets.webservices.providers.WebBeanProvider;
import ru.biosoft.server.servlets.webservices.providers.WebProviderFactory;
import ru.biosoft.server.servlets.webservices.providers.WebTablesProvider;
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
        WebProviderFactory.registerProvider("bean", new WebBeanProvider());
        WebProviderFactory.registerProvider("html", new HtmlTemplateProvider());
        WebProviderFactory.registerProvider("img", new ImageProvider());
        WebProviderFactory.registerProvider("table", new WebTablesProvider());
        WebProviderFactory.registerProvider("preferences", new PreferencesProvider());
        WebProviderFactory.registerProvider("perspective", new PerspectivesProvider());
        WebProviderFactory.registerProvider("action", new WebActionsProvider());
        WebProviderFactory.registerProvider("doc", new DocumentProvider());
        WebProviderFactory.registerProvider( "import", new ImportProvider() );
    }

    public static void initialize()
    {
        getInstance().init();
    }

    @Override protected void initBeans()
    {
        //BeanRegistry.registerBeanProvider("NAME", "BEAN CLASS");
    }

    protected void initCommonClasses()
    {
        AccessService.addCommonClass("ru.biosoft.access.core.DataCollection");
        AccessService.addCommonClass("ru.biosoft.access.file.FileDataElement");
        AccessService.addCommonClass("ru.biosoft.access.FileCollection");
        AccessService.addCommonClass("ru.biosoft.access.core.TransformedDataCollection");
        AccessService.addCommonClass("ru.biosoft.access.LocalRepository");
        AccessService.addCommonClass("ru.biosoft.access.security.NetworkRepository");
        AccessService.addCommonClass("ru.biosoft.access.SqlDataCollection");
        AccessService.addCommonClass("ru.biosoft.access.security.NetworkDataCollection");
        AccessService.addCommonClass("ru.biosoft.access.core.TextDataElement");
        AccessService.addCommonClass( "ru.biosoft.access.ImageDataElement" );
        // class="ru.biosoft.access.HtmlDataElement"
    }

}
