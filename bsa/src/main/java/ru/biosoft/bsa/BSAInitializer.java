package ru.biosoft.bsa;

import java.util.Collections;

import biouml.plugins.server.access.AccessService;
import ru.biosoft.access.BeanRegistry;
import ru.biosoft.access.core.FileTypePriority;
import ru.biosoft.access.file.FileType;
import ru.biosoft.access.file.FileTypeRegistry;
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
        TransformerRegistry.addTransformer("BedFile", "ru.biosoft.bsa.transformer.BedFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.BedTrack");
        TransformerRegistry.addTransformer("GFFFile", "ru.biosoft.bsa.transformer.GFFFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.GFFTrack");
        TransformerRegistry.addTransformer("FastaFile", "ru.biosoft.bsa.transformer.FastaFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.transformer.FastaSequenceCollection");
        TransformerRegistry.addTransformer("BAMFile", "ru.biosoft.bsa.transformer.BAMFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.BAMTrack");
        TransformerRegistry.addTransformer("VCFFile", "ru.biosoft.bsa.transformer.VCFFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.VCFFileTrack");
        TransformerRegistry.addTransformer("WiggleFile", "ru.biosoft.bsa.transformer.WiggleFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.WiggleFileTrack");
        //TransformerRegistry.addTransformer("BCFFile", "ru.biosoft.bsa.transformer.BCFFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.BCFFileTrack");
        TransformerRegistry.addTransformer("GenebankFile", "ru.biosoft.bsa.transformer.GenbankFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.GenbankSequenceCollection");
        TransformerRegistry.addTransformer("Combined track", "ru.biosoft.bsa.transformer.CombinedTrackTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.combined.CombinedTrack");
        
        TransformerRegistry.addTransformer("Genome browser view", "ru.biosoft.bsa.transformer.ProjectTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.project.Project");
        TransformerRegistry.addTransformer("Site model", "ru.biosoft.bsa.transformer.SiteModelTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.SiteModel");
        TransformerRegistry.addTransformer("Frequency matrix", "ru.biosoft.bsa.transformer.WeightMatrixTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.analysis.FrequencyMatrix");
        TransformerRegistry.addTransformer("ChrNameMapping", "ru.biosoft.bsa.ChrNameMappingTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.ChrNameMapping");
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

    @Override
    protected void initFileTypes()
    {
        //String[] extensions, String transformerClassName, FileTypePriority priority, String description
        FileTypeRegistry
                .register(new FileType("fasta", new String[] { "fa", "fasta", "fna" }, "ru.biosoft.bsa.transformer.FastaFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "FASTA file"));
        FileTypeRegistry.register(new FileType("bed", new String[] { "bed" }, "ru.biosoft.bsa.transformer.BedFileTransformer", FileTypePriority.HIGH_PRIORITY, "BED track file"));
        FileTypeRegistry
                .register(new FileType("gff", new String[] { "gff", "gtf" }, "ru.biosoft.bsa.transformer.GFFFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "Generic Feature Format (gff) track file"));
        FileTypeRegistry.register(new FileType("bam", new String[] { "bam" }, "ru.biosoft.bsa.transformer.BAMFileTransformer", FileTypePriority.HIGH_PRIORITY, "BAM track file"));
        FileTypeRegistry.register(new FileType("vcf", new String[] { "vcf" }, "ru.biosoft.bsa.transformer.VCFFileTransformer", FileTypePriority.HIGH_PRIORITY,
                "Variant Call Format (vcf) track file"));
        FileTypeRegistry
                .register(new FileType("wigggle", new String[] { "wig" }, "ru.biosoft.bsa.transformer.WiggleFileTransformer", FileTypePriority.HIGH_PRIORITY, "Wiggle track file"));
        FileTypeRegistry
                .register(new FileType("genbank", new String[] { "gb" }, "ru.biosoft.bsa.transformer.GenbankFileTransformer", FileTypePriority.HIGH_PRIORITY, "VCF track file"));
        FileTypeRegistry
                .register(new FileType("ChrNameMapping", new String[] { "txt" }, "ru.biosoft.bsa.ChrNameMappingTransformer", FileTypePriority.LOW_PRIORITY, "Chr name mapping"));
        //?? combined track
    }

    @Override
    protected void initCommonClasses()
    {
        AccessService.addCommonClass("ru.biosoft.bsa.SqlTrack");
        AccessService.addCommonClass("ru.biosoft.bsa.AnnotatedSequence");
        AccessService.addCommonClass("ru.biosoft.bsa.SiteModel");
        AccessService.addCommonClass("ru.biosoft.bsa.SiteModelCollection");
        AccessService.addCommonClass("ru.biosoft.bsa.analysis.WeightMatrixCollection");
        AccessService.addCommonClass("ru.biosoft.bsa.analysis.FrequencyMatrix");
        AccessService.addCommonClass("ru.biosoft.bsa.SequenceCollection");
    }
}
