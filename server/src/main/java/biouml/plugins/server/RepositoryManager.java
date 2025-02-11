package biouml.plugins.server;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.biosoft.access.core.CollectionFactory;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.exception.ExceptionRegistry;

public class RepositoryManager
{
    private static final Map<String, DataCollection<?>> repositoryMap = new HashMap<>();

    public static void initRepository(String path) throws Exception
    {
        if( !repositoryMap.containsKey(path) )
            repositoryMap.put(path, CollectionFactory.createRepository(path));
    }

    public static void initRepository(List<String> paths) throws Exception
    {
        for ( String path : paths )
        {
            try
            {
                initRepository(path);
            }
            catch (Exception e)
            {
                System.out.println("Unable to init " + path + ": " + ExceptionRegistry.log(e));
            }
        }
    }

    /**
     * Close all root collections
     */
    public static void closeRepositories() throws Exception
    {
        for ( DataCollection<?> dc : repositoryMap.values() )
            dc.close();
    }

    public static Set<String> getRepositoryPaths()
    {
        return repositoryMap.keySet();
    }
}
