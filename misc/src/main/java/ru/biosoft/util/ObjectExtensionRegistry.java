package ru.biosoft.util;

import ru.biosoft.exception.ExceptionRegistry;

public class ObjectExtensionRegistry<T> extends ExtensionRegistrySupport<T>
{

    private Class<T> clazz;

    public ObjectExtensionRegistry(String extensionPointId, String nameAttribute, Class<T> clazz)
    {
        super(extensionPointId, nameAttribute);
        this.clazz = clazz;
    }

    public ObjectExtensionRegistry(String extensionPointId, Class<T> clazz)
    {
        this(extensionPointId, "class", clazz);
    }

    /**
     * Subclass this method to define additional initialization steps
     */
    protected void postInit()
    {
    }
    
    protected void addElement(String name, String className)
    {
        try
        {
            registerElement(className, className, null);
        }
        catch (Exception e)
        {
            throw ExceptionRegistry.translateException(e);
        }

    }

    @Override public T registerElement(String elementName, String className, Object... args) throws Exception
    {
        init();
        Class<? extends T> elementClass = getClass(className, clazz);
        T obj = elementClass.newInstance();
        addElement(elementName, obj);
        return obj;
    }
}
