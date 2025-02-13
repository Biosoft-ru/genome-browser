package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import biouml.plugins.server.RepositoryManager;
import biouml.plugins.server.access.AccessService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.biosoft.access.AccessCoreInit;
import ru.biosoft.access.generic.DataElementTypeRegistry;
import ru.biosoft.access.generic.TransformerRegistry;
import ru.biosoft.bsa.server.BSAService;
import ru.biosoft.server.ServiceRegistry;
import ru.biosoft.server.servlets.webservices.WebServletHandler;
import ru.biosoft.server.servlets.webservices.providers.ServiceProvider;
import ru.biosoft.server.servlets.webservices.providers.WebProviderFactory;
import ru.biosoft.table.datatype.DataType;

@WebServlet(urlPatterns = { "/genomebrowser/*" }, initParams = { @WebInitParam(name = "configPath", value = "config2.yml") })
public class RunServicesServlet extends HttpServlet
{
    private String configPath;
    private Map<String, Object> yaml;

    @Override public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        AccessCoreInit.init();

        configPath = config.getInitParameter("configPath");
        if( configPath == null )
            configPath = "config.yml";
        readConfigFile();
        if(yaml.get("repositories") != null)
        {
            Object repoObj = yaml.get("repositories");
            List<String> repos = (List<String>) yaml.get("repositories");
            try
            {
                RepositoryManager.initRepository(repos);
            }
            catch (Exception e)
            {
            }
        }



        initProviders();
        initServices();
        initTransformers();
        initDataTypes();
        initDataElementTypeDrivers();
        //TODO: initCommonClasses

    }


    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String target = request.getRequestURI();
        if( target.startsWith("/genomebrowser/web/test") )
        {
            PrintWriter ow = response.getWriter();
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject obj = new JSONObject();
            obj.put("type", "ok");
            obj.write(ow);
            ow.flush();
        }
        else
            WebServletHandler.handle(request, response, "GET");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String target = request.getRequestURI();
        if( target.startsWith("/genomebrowser/web/test") )
        {
            PrintWriter ow = response.getWriter();
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject obj = new JSONObject();
            obj.put("type", "ok");
            obj.write(ow);
            ow.flush();
        }
        else if( target.startsWith("/genomebrowser/web/parameter") )
        {
            String pName = request.getParameter("parameter_name");

            JSONArray vals = new JSONArray();
            if( yaml.get(pName) != null )
            {
                Object pValue = yaml.get(pName);
                if( pValue instanceof List )
                {
                    for ( String val : (List<String>) pValue )
                    {
                        vals.put(val);
                    }
                }
                else
                {
                    vals.put(pValue.toString());
                }

            }
            JSONObject responseObject = new JSONObject();
            responseObject.put(pName, vals);
            responseObject.put("type", "ok");
            PrintWriter ow = response.getWriter();
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            responseObject.write(ow);
            ow.flush();
        }
        else
            WebServletHandler.handle(request, response, "POST");
    }

    private void readConfigFile()
    {
        yaml = Collections.emptyMap();
        try
        {

            URL cfg = getClass().getClassLoader().getResource(configPath);
            if( cfg != null )
            {
                Path path = Paths.get(cfg.toURI());
                Yaml parser = new Yaml();

                Object root = parser.load(Files.readString(path));
                if( !(root instanceof Map) )
                    throw new IllegalArgumentException("Yaml should be a map of key-values, but get " + root);

                yaml = (Map<String, Object>) root;
            }
        }
        catch (IOException ioe)
        {

        }
        catch (URISyntaxException use)
        {

        }
    }

    private void initProviders()
    {
        WebProviderFactory.registerProvider("data", new ServiceProvider());
    }

    private void initServices()
    {
        ServiceRegistry.registerService("bsa.service", new BSAService());
        ServiceRegistry.registerService("access.service", new AccessService());
    }

    private void initTransformers()
    {
        //ACCESS
        //TransformerRegistry.addTransformer("Image", "ru.biosoft.access.support.FileImageTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.access.ImageDataElement");
        //TransformerRegistry.addTransformer("HTML file", "ru.biosoft.access.support.FileHtmlTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.access.HtmlDataElement");
        //TransformerRegistry.addTransformer("Plain text", "ru.biosoft.access.support.FileTextTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.access.ImageDataElement");
        //TransformerRegistry.addTransformer("ZIP-archive with HTML pages", "ru.biosoft.access.support.FileZipHtmlTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.access.html.ZipHtmlDataCollection");
        //TransformerRegistry.addTransformer("Video", "ru.biosoft.access.support.FileZipHtmlTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.access.VideoDataElement");

        //???
        //TransformerRegistry.addTransformer("Analysis method element", "ru.biosoft.analysiscore.AnalysisMethodTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.analysiscore.AnalysisMethodElement");

        //BSA
        TransformerRegistry.addTransformer("BedFile", "ru.biosoft.bsa.transformer.BedFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.track.BedTrack");
        TransformerRegistry.addTransformer("GFFFile", "ru.biosoft.bsa.transformer.GFFFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.track.GFFTrack");
        TransformerRegistry.addTransformer("FastaFile", "ru.biosoft.bsa.transformer.FastaFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.transformer.FastaSequenceCollection");
        TransformerRegistry.addTransformer("BAMFile", "ru.biosoft.bsa.transformer.BAMFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.BAMTrack");
        //TransformerRegistry.addTransformer("VCFFile", "ru.biosoft.bsa.transformer.VCFFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.VCFFileTrack");
        //TransformerRegistry.addTransformer("BCFFile", "ru.biosoft.bsa.transformer.BCFFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.BCFFileTrack");
        TransformerRegistry.addTransformer("GenebankFile", "ru.biosoft.bsa.transformer.GenbankFileTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.GenbankSequenceCollection");
        TransformerRegistry.addTransformer("Combined track", "ru.biosoft.bsa.transformer.CombinedTrackTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.track.combined.CombinedTrack");
        
        TransformerRegistry.addTransformer("Genome browser view", "ru.biosoft.bsa.transformer.ProjectTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.project.Project");
        TransformerRegistry.addTransformer("Site model", "ru.biosoft.bsa.transformer.SiteModelTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.SiteModel");
        TransformerRegistry.addTransformer("Frequency matrix", "ru.biosoft.bsa.transformer.WeightMatrixTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.analysis.FrequencyMatrix");
        
        
        //??TransformerRegistry.addTransformer("Filtered track", "ru.biosoft.access.XMLTransformer", "ru.biosoft.access.FileDataElement", "ru.biosoft.bsa.analysis.FilteredTrack");
        
        //        TransformerRegistry.addTransformer("NAME", "TRANSFORMER", "ru.biosoft.access.Entry", "OUTPUT");
        //        TransformerRegistry.addTransformer("NAME", "TRANSFORMER", "ru.biosoft.access.Entry", "OUTPUT");
        //        
        //        TransformerRegistry.addTransformer("NAME", "TRANFORMER", "ru.biosoft.access.FileDataElement", "OUTPUT");
        //        TransformerRegistry.addTransformer("NAME", "TRANFORMER", "ru.biosoft.access.FileDataElement", "OUTPUT");
        
    }

    private void initDataTypes()
    {
        DataType.addDataType("ru.biosoft.bsa.access.GenomeBrowserDataType");
        //DataType.addDataType("DT");
    }

    private void initDataElementTypeDrivers()
    {
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementEntryTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementFileTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementGenericCollectionTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementSQLTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.RepositoryTypeDriver");

    }
}
