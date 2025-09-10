package biouml.workbench.perspective;

import java.util.List;

import org.json.JSONObject;

/**
 * Class representing the perspective
 * @author lan
 */
public interface Perspective
{
    public static final String IMPORTERS_ATTR = "importers";
    public static final String EXPORTERS_ATTR = "exporters";
    public static final String VIEWPARTS_ATTR = "viewparts";
    public static final String TAB_ATTR = "tab";
    public static final String REPOSITORY_ATTR = "repository";
    public static final String MESSAGEBUNDLE_ATTR = "messageBundle";
    public static final String PRIORITY_ATTR = "priority";
    public static final String NAME_ATTR = "name";
    public static final String INTRO_ATTR = "intro";
    public static final String ACTIONS_ATTR = "actions";
    public static final String PROJECTSELECTOR_ATTR = "projectSelector";
    public static final String TEMPLATE_ATTR = "template";
    public static final String HIDEDIAGRAMPANEL_ATTR = "hideDiagramPanel";
    public static final String CLOSEONLYONSESSIONEXPIRE_ATTR = "closeOnlyOnSessionExpire";

    /**
     * @return user-readable title
     */
    public String getTitle();

    /**
     * @return perspective priority (they will be sorted according to the priority)
     */
    public int getPriority();

    /**
     * @return unmodifiable list of repository tabs
     */
    public List<RepositoryTabInfo> getRepositoryTabs();

    /**
     * @param viewPartId identifier of the viewPart
     * @return true if viewPart can be displayed in this perspective
     */
    public boolean isViewPartAvailable(String viewPartId);

    /**
     * @param actionId identifier of the action
     * @return true if action is available in this perspective
     */
    public boolean isActionAvailable(String actionId);

    /**
     * @param importerId identifier of the importer
     * @return true if importer is available in this perspective
     */
    public boolean isImporterAvailable(String importerId);

    /**
     * @param exporterId identifier of the exporter
     * @return true if exporter is available in this perspective
     */
    public boolean isExporterAvailable(String exporterId);

    /**
     * @return template name default for this perspective
     */
    public String getDefaultTemplate();

    /**
     * @return name of 'start page' file
     */
    public String getIntroPage();

    /**
     * @return JsonObject-serialized form
     */
    public JSONObject toJSON();
}
