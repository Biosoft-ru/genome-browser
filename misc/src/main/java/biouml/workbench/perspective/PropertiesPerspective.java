package biouml.workbench.perspective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class PropertiesPerspective implements Perspective
{
    private Map<String, Object> properties;
    private final List<Rule> viewPartRules = new ArrayList<>();
    private final List<Rule> actionRules = new ArrayList<>();
    private final List<Rule> importerRules = new ArrayList<>();
    private final List<Rule> exporterRules = new ArrayList<>();

    public PropertiesPerspective(HashMap<String, Object> properties)
    {
        this.properties = properties;
        initRules();
    }

    @Override
    public String getTitle()
    {
        return properties.containsKey( "title" ) ? (String) properties.get( "title" ) : "Unknown";
    }

    @Override
    public int getPriority()
    {
        return properties.containsKey( "priority" ) ? (int) properties.get( "priority" ) : 0;
    }

    @Override
    public List<RepositoryTabInfo> getRepositoryTabs()
    {
        return properties.containsKey( "repository" ) ? (List<RepositoryTabInfo>) properties.get( "repository" ) : null;
    }

    @Override
    public boolean isViewPartAvailable(String viewPartId)
    {
        return matchRules( viewPartRules, viewPartId );
    }

    @Override
    public boolean isActionAvailable(String actionId)
    {
        return matchRules( actionRules, actionId );
    }

    @Override
    public boolean isImporterAvailable(String importerId)
    {
        return matchRules( importerRules, importerId );
    }

    @Override
    public boolean isExporterAvailable(String exporterId)
    {
        return matchRules( exporterRules, exporterId );
    }

    private static boolean matchRules(List<Rule> rules, String id)
    {
        boolean result = true;
        for ( Rule rule : rules )
        {
            if( rule.isMatched( id ) )
                result = rule.isAllow();
        }
        return result;
    }

    @Override
    public String getDefaultTemplate()
    {
        return properties.containsKey( "template" ) ? (String) properties.get( "template" ) : null;
    }

    @Override
    public String getIntroPage()
    {
        return null;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject result = new JSONObject();
        result.put( NAME_ATTR, getTitle() );
        result.put( PRIORITY_ATTR, getPriority() );

        if( getDefaultTemplate() != null )
            result.put( TEMPLATE_ATTR, getDefaultTemplate() );
        result.put( HIDEDIAGRAMPANEL_ATTR, true );
        result.put( CLOSEONLYONSESSIONEXPIRE_ATTR, properties.getOrDefault( CLOSEONLYONSESSIONEXPIRE_ATTR, false ) );


        List<RepositoryTabInfo> tabInfo = getRepositoryTabs();
        JSONArray repository = new JSONArray();
        for ( RepositoryTabInfo tab : tabInfo )
        {
            repository.put( tab.toJSON() );
        }
        result.put( REPOSITORY_ATTR, repository );
        JSONArray viewParts = new JSONArray();
        for ( Rule rule : viewPartRules )
        {
            viewParts.put( rule.toJSON() );
        }
        result.put( VIEWPARTS_ATTR, viewParts );
        JSONArray actions = new JSONArray();
        for ( Rule rule : actionRules )
        {
            actions.put( rule.toJSON() );
        }
        result.put( ACTIONS_ATTR, actions );
        return result;
    }

    private void initRules()
    {
        Object items = properties.get( VIEWPARTS_ATTR );
        if( items != null && items.getClass().isArray() )
        {
            Object[] rules = (Object[]) items;
            for ( Object rule : rules )
            {
                viewPartRules.add( new Rule( (Map<String, Object>) rule ) );
            }
        }

        items = properties.get( ACTIONS_ATTR );
        if( items != null && items.getClass().isArray() )
        {
            Object[] rules = (Object[]) items;
            for ( Object rule : rules )
            {
                actionRules.add( new Rule( (Map<String, Object>) rule ) );
            }
        }

        items = properties.get( IMPORTERS_ATTR );
        if( items != null && items.getClass().isArray() )
        {
            Object[] rules = (Object[]) items;
            for ( Object rule : rules )
            {
                importerRules.add( new Rule( (Map<String, Object>) rule ) );
            }
        }

        items = properties.get( EXPORTERS_ATTR );
        if( items != null && items.getClass().isArray() )
        {
            Object[] rules = (Object[]) items;
            for ( Object rule : rules )
            {
                exporterRules.add( new Rule( (Map<String, Object>) rule ) );
            }
        }
    }

}
