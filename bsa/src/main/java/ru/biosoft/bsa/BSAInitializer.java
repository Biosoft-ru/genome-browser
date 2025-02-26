package ru.biosoft.bsa;

import java.util.Collections;

import ru.biosoft.access.BeanRegistry;
import ru.biosoft.access.generic.TransformerRegistry;
import ru.biosoft.bsa.server.BSAService;
import ru.biosoft.bsa.server.TrackFinderProvider;
import ru.biosoft.server.ServiceRegistry;
import ru.biosoft.server.servlets.webservices.providers.WebProviderFactory;
import ru.biosoft.server.servlets.webservices.providers.WebTablesProvider;
import ru.biosoft.table.datatype.DataType;
import ru.biosoft.templates.TemplateFilter;
import ru.biosoft.templates.TemplateRegistry;
import ru.biosoft.util.Initializer;

public class BSAInitializer extends Initializer
{

    private static BSAInitializer instance;

    public static BSAInitializer getInstance()
    {
        if( instance == null )
            instance = new BSAInitializer();
        return instance;
    }

    public static void initialize()
    {
        getInstance().init();
    }

    @Override protected void initDataTypes()
    {
        DataType.addDataType("ru.biosoft.bsa.access.GenomeBrowserDataType");
    }

    @Override protected void initServices()
    {
        ServiceRegistry.registerService("bsa.service", new BSAService());
    }

    @Override protected void initTransformers()
    {
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
    }

    @Override protected void initTemplates()
    {
        //TemplateRegistry.registerTemplate("TEMPLATE NAME", "ANY CLASS WITH RESOURCE FOLDER INSTEAD OF PLUGIN NAME", "OTHER ARGUMENTS FOR TEMPLATE: FILE PATH, DESCRIPTION, IS BREAF, ORDER, FILTER");

        TemplateFilter filter = new TemplateFilter("ru.biosoft.bsa.SqlTrack", true, Collections.EMPTY_LIST, null);
        TemplateRegistry.registerTemplate("SQL track", "ru.biosoft.bsa.SqlTrack", "ru/biosoft/bsa/resources/sqltrack.vm", "SQL track information", false, 0, filter);
        TemplateRegistry.registerTemplate("SQL track simple", "ru.biosoft.bsa.SqlTrack", "sqltrack2.vm", "SQL track information for testing template", false, 40, filter);

        TemplateFilter BAMfilter = new TemplateFilter("ru.biosoft.bsa.BAMTrack", true, Collections.EMPTY_LIST, null);
        TemplateRegistry.registerTemplate("BAM track", "ru.biosoft.bsa.BAMTrack", "ru/biosoft/bsa/resources/bamtrack.vm", "BAM track information", false, 0, BAMfilter);
    }

    /*
     * <template name="SQL track" file="ru/biosoft/bsa/resources/sqltrack.vm"
     * description="SQL track information" isBrief="no" order="0"> <filter
     * class="ru.biosoft.bsa.SqlTrack" subclasses="yes"/> </template>
     */

    @Override protected void initProviders()
    {
        WebProviderFactory.registerProvider("track-finder", new TrackFinderProvider());
    }

    @Override protected void initBeanProviders()
    {
        BeanRegistry.registerBeanProvider("trackFinder/parameters", "ru.biosoft.bsa.finder.TrackFinderBeanProvider");
        BeanRegistry.registerBeanProvider("bsa/siteviewoptions", "ru.biosoft.bsa.server.SiteViewOptionsProvider");
        BeanRegistry.registerBeanProvider("bsa/genomebrowsercolors", "ru.biosoft.bsa.server.ColorSchemesBeanProvider");
    }

    @Override protected void initTableResolvers()
    {
        WebTablesProvider.addTableResolver("sites", "ru.biosoft.bsa.server.SitesTableResolver");
        WebTablesProvider.addTableResolver("track", "ru.biosoft.bsa.server.TrackTableResolver");
    }

}
