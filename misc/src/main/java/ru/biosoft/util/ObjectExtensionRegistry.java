package ru.biosoft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import ru.biosoft.access.ClassLoading;
import ru.biosoft.access.exception.InitializationException;
import ru.biosoft.exception.ExceptionRegistry;

public class ObjectExtensionRegistry<T> implements Iterable<T>
{

    protected volatile List<T> extensions;
    protected volatile Map<String, T> nameToExtension;
    private volatile boolean initialized = false;
    private volatile boolean initializing = false;
    private Class<T> clazz;

    public ObjectExtensionRegistry(String extensionPointId, String nameAttribute, Class<T> clazz)
    {
        this.clazz = clazz;
    }

    public ObjectExtensionRegistry(String extensionPointId, Class<T> clazz)
    {
        this(extensionPointId, "class", clazz);
    }

    

    protected final void init()
    {
        if( !initialized )
        {
            synchronized (this)
            {
                if( !initialized )
                {
                    if( initializing )
                        throw new InitializationException("Concurrent initialization of " + getClass().getName());
                    initializing = true;
                    try
                    {
                        extensions = new ArrayList<>();
                        nameToExtension = new HashMap<>();
                        postInit();
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

    /**
     * Subclass this method to define additional initialization steps
     */
    protected void postInit()
    {
    }
    
    @Override 
    public Iterator<T> iterator()
    {
        init();
        return Collections.unmodifiableList(extensions).iterator();
    }
    
    public StreamEx<T> stream()
    {
        init();
        return StreamEx.of( extensions );
    }

    public StreamEx<String> names()
    {
        init();
        return StreamEx.ofKeys( nameToExtension );
    }

    public EntryStream<String, T> entries()
    {
        init();
        return EntryStream.of( nameToExtension );
    }

    public void addElement(String name, String className)
    {
        Class<? extends T> elementClass = getClass(className, clazz);
        try
        {
            T obj = elementClass.newInstance();
            extensions.add(obj);
            nameToExtension.put(name, obj);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
        }

    }

    protected void addElement(String name, T element)
    {
        extensions.add(element);
        nameToExtension.put(name, element);
    }

    protected static <K> Class<? extends K> getClass(String className, @Nonnull Class<K> parentClass)
    {
        Class<? extends K> clazz;
        try
        {
            clazz = ClassLoading.loadSubClass(className, parentClass);
        }
        catch (Exception e)
        {
            throw ExceptionRegistry.translateException(e);
        }
        return clazz;
    }

    public T getExtension(String name)
    {
        if( name == null )
            return null;
        init();
        return nameToExtension.get(name);
    }
}
