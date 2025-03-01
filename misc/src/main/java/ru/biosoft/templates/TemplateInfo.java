package ru.biosoft.templates;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import ru.biosoft.exception.InternalException;
import ru.biosoft.util.LazyValue;

/**
 * Full info about template.
 */
public class TemplateInfo
{

    private static Logger log = Logger.getLogger(TemplateInfo.class.getName());
    protected String name;
    protected String description;
    protected boolean isBrief;
    protected Class resourceClass;
    protected String filePath;
    protected TemplateFilter filter;
    protected int order;

    public TemplateInfo(String name, String description, boolean isBrief, Class clazz, String filePath, TemplateFilter filter, int order)
    {
        this.name = name;
        this.description = description;
        this.isBrief = isBrief;
        this.resourceClass = clazz;
        this.filePath = filePath;
        this.filter = filter;
        this.order = order;
    }

    public String getName()
    {
        return name;
    }
    public String getDescription()
    {
        return description;
    }
    public boolean isBrief()
    {
        return isBrief;
    }

    public boolean isSuitable(Object obj)
    {
        return filter == null || filter.isSuitable(obj) > 0;
    }

    public int getOrder()
    {
        return order;
    }

    protected LazyValue<Template> template = new LazyValue<Template>("template")
    {
        @Override
        protected Template doGet() throws Exception
        {
            SimpleNode node;
            URL resource;

            if( templateResolver != null )
            {
                resource = templateResolver.getResource(filePath);
                if( resource == null )
                    throw new InternalException( "Error loading template, file not found, template=" + name + ", file=" + filePath + ", templateResolver=" + templateResolver);
            }
            else
            {
                resource = resourceClass.getClassLoader().getResource(filePath);
                if(resource == null)
                    throw new InternalException( "Error loading template "+name+": path "+filePath+" not found" );
            }

            Template template = new Template();
            template.setName(name);
            try(Reader reader = new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))
            {
                node = runtimeServices.parse(reader, template);
            }
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            try
            {
                template.initDocument();
            }
            catch (Exception e)
            {
                log.log(Level.SEVERE, "Error parsing template ", e);
            }
            return template;
        }
    };
    protected static final RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();

    public @Nonnull Template getTemplate() throws Exception
    {
        return template.get();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // TemplateResolver
    // it is used to find template files, when Eclipse platform is not used
    //
    
    public interface TemplateResolver
    {
        public URL getResource(String filePath);
    }
    
    static TemplateResolver templateResolver;
    public static TemplateResolver getTemplateResolver()
    {
        return templateResolver;
    }
    public static void setTemplateResolver(TemplateResolver templateResolver)
    {
        SecurityManager sm = System.getSecurityManager();
        if( sm != null )
        {
            sm.checkPermission(new RuntimePermission("setupApplication"));
        }
        TemplateInfo.templateResolver = templateResolver;
    }
    
}
