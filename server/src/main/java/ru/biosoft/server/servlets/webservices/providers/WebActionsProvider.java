package ru.biosoft.server.servlets.webservices.providers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.json.JSONArray;
import org.json.JSONObject;

//import com.developmentontheedge.application.ApplicationUtils;
import com.developmentontheedge.beans.model.ComponentFactory;
import com.developmentontheedge.beans.model.ComponentFactory.Policy;
import com.developmentontheedge.beans.model.ComponentModel;
//import com.eclipsesource.json.JSONArray;
//import com.eclipsesource.json.JSONObject;

//import biouml.model.Diagram;
//import biouml.model.DiagramType;
//import biouml.model.Module;
//import biouml.model.dynamics.EModelRoleSupport;
//import biouml.model.xml.XmlDiagramType;
import biouml.plugins.server.access.AccessProtocol;
import biouml.standard.type.Stub;
//import biouml.standard.diagram.DiagramUtility;
//import biouml.standard.type.Reaction;
//import biouml.standard.type.Stub;
//import biouml.workbench.diagram.DiagramDynamicActionProperties;
//import biouml.workbench.diagram.DiagramEditorHelper;
import biouml.workbench.perspective.Perspective;
import biouml.workbench.perspective.PerspectiveRegistry;
import one.util.streamex.StreamEx;
//import ru.biosoft.access.ClassLoading;
import ru.biosoft.access.core.CollectionFactory;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.Environment;
import ru.biosoft.access.security.SecurityManager;
import ru.biosoft.access.subaction.BackgroundDynamicAction;
import ru.biosoft.access.subaction.DynamicAction;
import ru.biosoft.access.subaction.DynamicActionFactory;
import ru.biosoft.access.task.JobControlTask;
import ru.biosoft.access.task.TaskPool;
import ru.biosoft.exception.ExceptionRegistry;
import ru.biosoft.exception.LoggedException;
import ru.biosoft.jobcontrol.JobControl;
import ru.biosoft.jobcontrol.JobControlEvent;
import ru.biosoft.jobcontrol.JobControlListenerAdapter;
import ru.biosoft.server.JSONUtils;
import ru.biosoft.server.servlets.webservices.BiosoftWebRequest;
import ru.biosoft.server.servlets.webservices.BiosoftWebResponse;
import ru.biosoft.server.servlets.webservices.JSONResponse;
import ru.biosoft.server.servlets.webservices.WebException;
import ru.biosoft.server.servlets.webservices.WebJob;
import ru.biosoft.server.servlets.webservices.WebServicesServlet;
import ru.biosoft.server.servlets.webservices.WebSession;
import ru.biosoft.util.ApplicationUtils;
import ru.biosoft.util.Cache;
//import ru.biosoft.util.JsonUtils;
import ru.biosoft.util.LazyValue;
import ru.biosoft.util.TextUtil2;

/**
 * Provides loading actions from plug-ins
 */
public class WebActionsProvider extends WebProviderSupport
{
    protected static final Logger log = Logger.getLogger( WebActionsProvider.class.getName() );

    public static final String SELECTION_BASE = "selectionBase";
    protected static final String ACTIONS_PATH = "files/actions/";
    private static boolean actionsInFiles = false;

    public static class ActionDescriptor
    {
        JSONObject actionInfo;
        String product;

        public ActionDescriptor()
        {
        }

        public ActionDescriptor(JSONObject actionInfo, String product)
        {
            this.actionInfo = actionInfo;
            this.product = product;
        }

        public boolean isSeparator()
        {
            return actionInfo == null;
        }

        public boolean isAvailable()
        {
            return product == null || product.isEmpty() || SecurityManager.isProductAvailable( product );
        }

        public JSONObject getActionInfo()
        {
            return actionInfo == null ? new JSONObject() : actionInfo;
        }
    }

    private static String getActionsPath()
    {
        Object actionsPathObj = Environment.getValue( "ActionsPath" );
        if( actionsPathObj != null )
        {
            actionsInFiles = true;
            return actionsPathObj.toString();
        }
        else
            return ACTIONS_PATH;
    }

    private static final Function<String, ActionDescriptor[]> actionsTypeMap = Cache.hard( type -> {
        String actionsPath = getActionsPath();
        String namesFile = actionsPath + type + "/names.txt";
        log.log( Level.SEVERE, "Actions path!!! " + namesFile );
        File actionsFile = new File( namesFile );
        log.log( Level.SEVERE, "Actions file!!! " + actionsFile.getAbsolutePath() );
        InputStream is = null;
        if( actionsInFiles )
        {
            try
            {
                is = new FileInputStream( actionsFile );
            }
            catch (FileNotFoundException e)
            {
            }
        }
        else
        {
            is = WebSession.class.getResourceAsStream( namesFile );
        }
        if( is == null )
        {
            log.log( Level.SEVERE, "Action not found: " + namesFile );
            return null;
        }
        try (BufferedReader br = new BufferedReader( new InputStreamReader( is ) ))
        {
            return br.lines().map( String::trim ).map( name -> {
                if( name.isEmpty() )
                {
                    //separator
                    return new ActionDescriptor();
                }
                else
                {
                    String[] fields = TextUtil2.split( name, ':' );
                    String fileName = actionsPath + type + "/" + fields[0];
                    try
                    {
                        String data = ApplicationUtils
                                .readAsString( actionsInFiles ? new FileInputStream( new File( fileName ) ) : WebSession.class.getResourceAsStream( fileName ) );
                        data = data.replaceAll( "//.*?[\r\n]", "\n" );
                        String filtered = data.replace( '\n', ' ' ).replace( '\r', ' ' ).replaceAll( "/\\*.*?\\*/", "" );
                        JSONObject object = new JSONObject( filtered );
                        return new ActionDescriptor( object, fields.length > 1 ? fields[1] : null );
                    }
                    catch (Exception e)
                    {
                        log.log( Level.SEVERE, "Can not create JSON object from file " + fileName + ":" + ExceptionRegistry.log( e ) );
                        return null;
                    }
                }
            } ).filter( Objects::nonNull ).toArray( ActionDescriptor[]::new );
        }
        catch (IOException | UncheckedIOException e)
        {
            log.log( Level.SEVERE, "Can not read " + namesFile + ":" + ExceptionRegistry.log( e ) );
            return new ActionDescriptor[0];
        }
    } );

    private static final LazyValue<JSONObject[]> dynamicActions = new LazyValue<>( "Dynamic actions",
            () -> DynamicActionFactory.dynamicActions()
                    .map( action -> new JSONObject().put( "id", action.getTitle() ).put( "label", getLabel( action ) )
                            .put( "icon", "../biouml/web/action?type=dynamic&action=toolbar_icon&name=" + action.getTitle().replaceAll( " ", "%20" ) )
                            .put( "numSelected", action.getNumSelected() ).put( "acceptReadOnly", action.isAcceptReadOnly() ) )
                    .toArray( JSONObject[]::new ) );

    public static ActionDescriptor[] loadActions(String key)
    {
        return actionsTypeMap.apply( key );
    }

    public static void getToolbarIcon(String actionName, BiosoftWebResponse resp)
    {
        try
        {
            DynamicAction action = DynamicActionFactory.getDynamicAction( actionName );
            Object iconObj = action.getValue( Action.SMALL_ICON );
            if( iconObj instanceof ImageIcon )
            {
                sendIcon( resp, (ImageIcon) iconObj );
            }
        }
        catch (IOException e)
        {
            log.log( Level.SEVERE, "Can not send icon image", e );
        }
    }

    protected static void sendIcon(BiosoftWebResponse resp, ImageIcon icon) throws IOException
    {
        int width = icon.getIconWidth();
        if( width < 0 )
            width = 0;
        int height = icon.getIconHeight();
        if( height < 0 )
            height = 0;
        if( width > 0 && height > 0 )
        {
            BufferedImage b = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
            icon.paintIcon( null, b.createGraphics(), 0, 0 );
            resp.setContentType( "image/png" );
            ImageIO.write( b, "PNG", resp.getOutputStream() );
        }
    }

    public static boolean isDynamicActionVisible(String actionName, Object model)
    {
        DynamicAction action = DynamicActionFactory.getDynamicAction( actionName );
        Perspective perspective = PerspectiveRegistry.getCurrentPerspective();
        return perspective.isActionAvailable( action.getTitle() ) ? action.isApplicable( model ) : false;
    }

    public static JSONArray getVisibleActions(Object model)
    {
        JSONArray result = new JSONArray();
        Perspective perspective = PerspectiveRegistry.getCurrentPerspective();
        DynamicActionFactory.dynamicActions().filter( action -> perspective.isActionAvailable( action.getTitle() ) ).filter( action -> action.isApplicable( model ) )
                .forEach( action -> {
                    if( !result.isEmpty() && !DynamicActionFactory.getDynamicAction( result.getString( result.length() - 1 ) ).isSameGroup( action ) )
                        result.put( "" );
                    result.put( action.getTitle() );
                } );
        return result;
    }

    public static void validateDynamicAction(String actionName, DataElement actionModel, List<DataElement> selectedItems, JSONArray properties, JSONResponse response)
            throws IOException, WebException
    {
        DynamicAction action = DynamicActionFactory.getDynamicAction( actionName );
        if( action != null )
        {
            action.validateParameters( actionModel, selectedItems );
            Object pd = getActionProperties( actionName, action, actionModel, selectedItems, properties == null );
            //            if( pd != null && !pd.getClass().equals(DiagramDynamicActionProperties.class))
            //            {
            //                try
            //                {
            //                    ComponentModel model = ComponentFactory.getModel(pd, Policy.DEFAULT, true);
            //                    if( properties != null )
            //                    {
            //                        JSONUtils.correctBeanOptions(model, properties);
            //                    }
            //                    JSONArray jsonProperties = JSONUtils.getModelAsJSON(model);
            //                    response.sendJSONBean(jsonProperties);
            //                }
            //                catch( Exception e )
            //                {
            //                    throw new WebException(e, "EX_INTERNAL_DURING_ACTION", actionName);
            //                }
            //            }
            //            else
            //            {
            String confirmation = action.getConfirmationMessage( actionModel, selectedItems );
            if( confirmation != null )
            {
                response.sendJSON( new JSONObject().put( "confirm", confirmation ) );
            }
            else
            {
                response.send( new byte[0], 0 );
            }
            //            }
        }
    }

    protected static Object getActionProperties(String actionName, DynamicAction action, DataElement actionModel, List<DataElement> selectedItems, boolean isNew)
    {
        String completeName = "dynamicAction/" + actionName + "/" + DataElementPath.create( actionModel );
        Object pd = isNew ? null : WebServicesServlet.getSessionCache().getObject( completeName );
        if( pd == null )
        {
            pd = action.getProperties( actionModel, selectedItems );
            if( pd != null )
            {
                WebServicesServlet.getSessionCache().addObject( completeName, pd, true );
            }
        }
        return pd;
    }

    public static String runDynamicAction(final String actionName, final ru.biosoft.access.core.DataElement actionModel, List<DataElement> selectedItems, JSONArray properties,
            String jobID) throws WebException
    {
        DynamicAction action = DynamicActionFactory.getDynamicAction( actionName );
        if( action == null )
            throw new WebException( "EX_QUERY_NO_ACTION", actionName );
        try
        {
            action.validateParameters( actionModel, selectedItems );
            Object pd = action.getProperties( actionModel, selectedItems );
            if( properties != null && pd != null )
            {
                ComponentModel model = ComponentFactory.getModel( pd, Policy.DEFAULT, true );
                JSONUtils.correctBeanOptions( model, properties );
            }
            if( action instanceof BackgroundDynamicAction && jobID != null )
            {
                //                if(actionModel instanceof Diagram && pd instanceof DiagramDynamicActionProperties)
                //                {
                //                    Diagram diagram = (Diagram)actionModel;
                //                    DiagramEditorHelper helper = new DiagramEditorHelper(diagram);
                //                    ((DiagramDynamicActionProperties)pd).setHelper(helper);
                //                }
                final JobControl jc = ((BackgroundDynamicAction) action).getJobControl( actionModel, selectedItems, pd );
                final WebJob webJob = WebJob.getWebJob( jobID );
                webJob.setJobControl( jc );
                jc.addListener( new JobControlListenerAdapter()
                {
                    @Override
                    public void jobTerminated(JobControlEvent event)
                    {
                        if( event.getStatus() == JobControl.TERMINATED_BY_ERROR && event.getException() != null )
                        {
                            webJob.addJobMessage( "ERROR - " + ExceptionRegistry.log( event.getException().getError() ) + "\n" );
                        }
                    }
                } );
                TaskPool.getInstance().submit( new JobControlTask( "Action: " + actionName + " (user: " + SecurityManager.getSessionUser() + ")", jc )
                {
                    @Override
                    public void doRun()
                    {
                        try
                        {
                            //                            if(actionModel instanceof Diagram)
                            //                            {
                            //                                WebDiagramsProvider.performTransaction((Diagram)actionModel, actionName, jc);
                            //                            } else
                            jc.run();
                        }
                        catch (Exception e)
                        {
                            log.log( Level.SEVERE, e.getMessage(), e );
                        }
                    }
                } );
                return "job started";
            }
            action.performAction( actionModel, selectedItems, pd );
            return "action finished";
        }
        catch (LoggedException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new WebException( e, "EX_INTERNAL_DURING_ACTION", actionName );
        }
    }

    private JSONObject[] getActions(String type)
    {
        return StreamEx.of( actionsTypeMap.apply( type ) ).filter( ActionDescriptor::isAvailable ).map( ActionDescriptor::getActionInfo ).toArray( JSONObject[]::new );
    }

    protected static JSONArray getActionProperties(BiosoftWebRequest arguments) throws WebException
    {
        return arguments.optJSONArray( "properties" );
    }

    protected static List<DataElement> getSelectedItems(BiosoftWebRequest arguments) throws WebException
    {
        DataCollection<?> selectionBase = arguments.getDataCollection( TextUtil2.isEmpty( arguments.get( SELECTION_BASE ) ) ? AccessProtocol.KEY_DE : SELECTION_BASE );
        String[] rows = arguments.optStrings( "jsonrows" );
        if( rows == null )
            return Collections.emptyList();
        if( rows.length == 1 && rows[0].equals( "all" ) )
            return selectionBase.stream().filter( DataElement.class::isInstance ).map( e -> (DataElement) e ).collect( Collectors.toList() );
        return StreamEx.of( rows ).map( name -> {
            DataElement de = CollectionFactory.getDataElement( name, selectionBase );
            if( de == null )
                de = new Stub( selectionBase, name );
            return de;
        } ).toList();
    }

    private static String getLabel(DynamicAction action)
    {
        Object label = action.getValue( Action.NAME );
        if( label == null )
            label = action.getValue( Action.SHORT_DESCRIPTION );
        if( label == null )
            label = action.getTitle();
        return label.toString();
    }

    @Override
    public void process(BiosoftWebRequest arguments, BiosoftWebResponse resp) throws Exception
    {
        String type = arguments.getString( "type" );
        JSONObject[] actions = null;
        JSONResponse response = new JSONResponse( resp );
        if( type.equals( "dynamic" ) )
        {
            String action = arguments.getAction();
            if( action.equals( "load" ) )
            {
                actions = dynamicActions.get();
                response.sendActions( actions );
                return;
            }
            else if( action.equals( "toolbar_icon" ) )
            {
                String path = arguments.getString( "name" );
                getToolbarIcon( path, resp );
                return;
            }

            DataElement dc = arguments.getDataElement();
            if( action.equals( "visibleall" ) )
            {
                response.sendJSON( getVisibleActions( dc ) );
                return;
            }

            String actionName = arguments.getString( "name" );
            if( action.equals( "visible" ) )
            {
                if( isDynamicActionVisible( actionName, dc ) )
                    response.send( new byte[0], 0 );
                else
                    response.error( "Action is not visible" );
                return;
            }
            if( action.equals( "validate" ) )
            {
                validateDynamicAction( actionName, dc, getSelectedItems( arguments ), getActionProperties( arguments ), response );
            }
            else if( action.equals( "run" ) )
            {
                String result = runDynamicAction( actionName, dc, getSelectedItems( arguments ), getActionProperties( arguments ), arguments.get( "jobID" ) );
                response.sendString( result );
            }
            else
                throw new WebException( "EX_QUERY_PARAM_INVALID_VALUE", BiosoftWebRequest.ACTION );
        }
        else
        {
            actions = getActions( type );
        }

        if( actions != null )
        {
            response.sendActions( actions );
        }
        return;
    }
}
