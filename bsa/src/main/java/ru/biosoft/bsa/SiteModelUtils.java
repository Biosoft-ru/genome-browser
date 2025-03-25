package ru.biosoft.bsa;

import org.json.JSONObject;

//import com.developmentontheedge.application.Application;
import com.developmentontheedge.beans.Preferences;
//import com.eclipsesource.json.JsonObject;

import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.util.ServerPreferences;
import ru.biosoft.util.TextUtil2;
//import ru.biosoft.util.JsonUtils;

public class SiteModelUtils
{
    private static DataElementPath defaultProfilePath = null;
    private static boolean isInitialized = false;

    public static DataElementPath getDefaultProfile()
    {
        initProfile();
        if( defaultProfilePath != null )
            return defaultProfilePath;
        Preferences preferences = ServerPreferences.getPreferences();
        DataElementPath value = DataElementPath.create(preferences.getStringValue(Const.LAST_PROFILE_PREFERENCE, ""));
        return value.exists() ? value : DataElementPath.create(Const.DEFAULT_PROFILE);
    }

    private static void initProfile()
    {
        if( isInitialized )
            return;
        try
        {
            String confFile = System.getProperty( "biouml.server.path" ) + "/appconfig/bsa/profile.json";
            JSONObject json = TextUtil2.fromFile(confFile);
            String pathStr = json.optString("defaultProfile", "");
            if( !pathStr.isEmpty() )
            {
                DataElementPath path = DataElementPath.create( pathStr );
                    defaultProfilePath = path;
                    return;
            }
        }
        catch( Exception e )
        {
        }
        isInitialized = true;
    }
}
