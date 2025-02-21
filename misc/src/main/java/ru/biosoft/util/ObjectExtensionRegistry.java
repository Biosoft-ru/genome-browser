package ru.biosoft.util;

import ru.biosoft.exception.ExceptionRegistry;

public class ObjectExtensionRegistry<T> extends ExtensionRegistrySupport<T>
{

    private Class<T> baseClass;

    public ObjectExtensionRegistry(String extensionPointId, String nameAttribute, Class<T> clazz)
    {
        super(extensionPointId, nameAttribute);
        this.baseClass = clazz;
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
    
    @Override public void addElement(String name, String className)
    {
        try
        {
            registerElement(name, className, null);
        }
        catch (Exception e)
        {
            throw ExceptionRegistry.translateException(e);
        }
    }

    @Override protected T registerElement(String elementName, String className, Object... args) throws Exception
    {
        init();
        Class<? extends T> elementClass = getClass(className, baseClass);
        T obj = elementClass.newInstance();
        addElementInternal(elementName, obj);
        return obj;
    }
}
