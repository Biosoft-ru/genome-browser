package ru.biosoft.access;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.developmentontheedge.beans.annot.PropertyName;

import ru.biosoft.access.core.ClassLoading;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.PluginEntry;
import ru.biosoft.exception.ExceptionRegistry;
import ru.biosoft.exception.InternalException;
import ru.biosoft.exception.LoggedClassCastException;
import ru.biosoft.exception.LoggedClassNotFoundException;
import ru.biosoft.util.entry.RegularFileEntry;

public class SimpleClassLoading implements ClassLoading
{
    @Override
    public Class<?> loadClass(String className) throws LoggedClassNotFoundException
    {
        if( className == null )
            throw new InternalException( "Null encountered where non-null expected: className" );

        Class<?> result = tryLoad( className );
        if( result != null )
            return result;
        throw new LoggedClassNotFoundException( className, null );
    }

    @Override
    public Class<?> loadClass(String className, String pluginNames) throws LoggedClassNotFoundException
    {
        Class<?> c = tryLoad( className );

        if( c == null )
            throw new LoggedClassNotFoundException( className, pluginNames );

        try
        {
            c.getConstructors(); // trigger class initialization
        }
        catch (Throwable t)
        {
            throw new LoggedClassNotFoundException( ExceptionRegistry.translateException( t ), className, pluginNames );
        }

        return c;
    }

    @Override
    public <T> Class<? extends T> loadClass(String className, Class<T> superClass) throws LoggedClassNotFoundException, LoggedClassCastException
    {
        Class<?> clazz = loadClass( className );
        if( superClass.isAssignableFrom( clazz ) )
            return (Class<? extends T>) clazz;
        throw new LoggedClassCastException( className, superClass.getName() );
    }

    @Override
    public <T> Class<? extends T> loadClass(String className, String pluginNames, Class<T> superClass) throws LoggedClassNotFoundException, LoggedClassCastException
    {
        Class<?> clazz = loadClass( className, pluginNames );
        if( superClass.isAssignableFrom( clazz ) )
            return (Class<? extends T>) clazz;
        throw new LoggedClassCastException( className, superClass.getName() );
    }

    @Override
    public String getResourceLocation(Class<?> clazz, String resource)
    {
        String plugin = getPluginForClass( clazz );
        return (plugin == null ? "default" : plugin) + ":" + clazz.getPackage().getName().replace( ".", "/" ) + "/" + resource;
    }

    private static Map<Class<? extends DataElement>, String> classTitleCache = new ConcurrentHashMap<>();
    @Override
    public String getClassTitle(Class<?> clazz)
    {
        if( DataElement.class.isAssignableFrom( clazz ) )
        {
            Class<? extends DataElement> deClass = (Class<? extends DataElement>) clazz;
            if( deClass == null )
                return ru.biosoft.access.core.DataElement.class.getAnnotation( PropertyName.class ).value();
            String title = classTitleCache.get( deClass );
            if( title != null )
                return title;
            Deque<Class<? extends DataElement>> classes = new LinkedList<>();
            classes.add( deClass );
            while ( !classes.isEmpty() )
            {
                Class<? extends DataElement> curClass = classes.pop();
                if( curClass == ru.biosoft.access.core.DataElement.class )
                    continue;
                PropertyName annotation = curClass.getAnnotation( PropertyName.class );
                if( annotation != null )
                {
                    title = annotation.value();
                    classTitleCache.put( deClass, title );
                    return title;
                }
                if( curClass.getSuperclass() != null && ru.biosoft.access.core.DataElement.class.isAssignableFrom( curClass.getSuperclass() ) )
                {
                    classes.add( (Class<? extends DataElement>) curClass.getSuperclass() );
                }
                Stream.of( curClass.getInterfaces() ).flatMap( Clazz.of( ru.biosoft.access.core.DataElement.class )::selectClass ).forEach( classes::add );
            }
            title = ru.biosoft.access.core.DataElement.class.getAnnotation( PropertyName.class ).value();
            classTitleCache.put( deClass, title );
            return title;
        }
        else
            return clazz.getSimpleName();
    }

    @Override
    public String getPluginForClass(Class<?> clazz)
    {
        return getPluginForClass( clazz.getName() );
    }

    @Override
    public String getPluginForClass(String className)
    {
        return null;
    }

    @Override
    public PluginEntry resolvePluginPath(String pluginPath, String parentPath)
    {
        if( pluginPath == null )
            return null;
        int colonPos = pluginPath.indexOf( ':' );
        if( colonPos < 3 )
        {
            File f = parentPath.isEmpty() ? new File( pluginPath ) : new File( parentPath, pluginPath );
            return new RegularFileEntry( f );
        }
        return new RegularFileEntry( new File( pluginPath ) );
    }

    private Class<?> tryLoad(String className)
    {
        try
        {
            return Class.forName( className );
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }

}
