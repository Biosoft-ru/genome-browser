
package biouml.standard;

import ru.biosoft.util.ServerPreferences;

public class SqlModuleType extends StandardModuleType
{
    
    @Override
    public boolean canCreateEmptyModule ( )
    {
        return false;
    }

    public SqlModuleType ( )
    {
        super( ServerPreferences.getGlobalValue( "ApplicationName" ) + " standard (SQL)" );
    }

}
