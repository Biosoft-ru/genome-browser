package ru.biosoft.templates;

import java.io.Reader;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

//TODO: refactored, can be removed?
public class ClasspathResourceLoader extends ResourceLoader
{
    @Override
    public long getLastModified(Resource resource)
    {
        return 0;
    }

    //    @Override
    //    public InputStream getResourceStream(String source) throws ResourceNotFoundException
    //    {
    //        return ClasspathResourceLoader.class.getResourceAsStream(source);
    //    }
    //
    //    @Override
    //    public void init(ExtendedProperties configuration)
    //    {
    //        
    //    }

    @Override
    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    @Override public void init(ExtProperties configuration)
    {
        // TODO Auto-generated method stub
        
    }

    @Override public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
