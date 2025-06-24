package ru.biosoft.bsa;

import java.util.Collections;

import biouml.plugins.server.access.AccessService;
import ru.biosoft.access.BeanRegistry;
import ru.biosoft.access.file.FileTypePriority;
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
        TransformerRegistry.addTransformer( "FastaFile", "ru.biosoft.bsa.transformer.FastaSimpleFileTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.transformer.FastaSimpleSequenceCollection" );
        TransformerRegistry.addTransformer( "FastaFileWithIndex", "ru.biosoft.bsa.transformer.FastaFileTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.transformer.FastaSequenceCollection" );
        TransformerRegistry.addTransformer("BAMFile", "ru.biosoft.bsa.transformer.BAMFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.BAMTrack");
        TransformerRegistry.addTransformer("VCFFile", "ru.biosoft.bsa.transformer.VCFFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.VCFFileTrack");
        TransformerRegistry.addTransformer("WiggleFile", "ru.biosoft.bsa.transformer.WiggleFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.WiggleFileTrack");
        //TransformerRegistry.addTransformer("BCFFile", "ru.biosoft.bsa.transformer.BCFFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.BCFFileTrack");

        TransformerRegistry.addTransformer( "BigBedFile", "ru.biosoft.bsa.transformer.BigBedFileTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.track.big.BigBedTrack" );
        TransformerRegistry.addTransformer( "BigWigFile", "ru.biosoft.bsa.transformer.BigWigFileTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.track.big.BigWigTrack" );

        TransformerRegistry.addTransformer("GenebankFile", "ru.biosoft.bsa.transformer.GenbankFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.GenbankSequenceCollection");
        TransformerRegistry.addTransformer("Combined track", "ru.biosoft.bsa.transformer.CombinedTrackTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.combined.CombinedTrack");
        
        TransformerRegistry.addTransformer("Genome browser view", "ru.biosoft.bsa.transformer.ProjectTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.project.Project");
        TransformerRegistry.addTransformer("Site model", "ru.biosoft.bsa.transformer.SiteModelTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.SiteModel");
        TransformerRegistry.addTransformer("Frequency matrix", "ru.biosoft.bsa.transformer.WeightMatrixTransformer", "ru.biosoft.access.Entry", "ru.biosoft.bsa.analysis.FrequencyMatrix");
        TransformerRegistry.addTransformer("ChrNameMapping", "ru.biosoft.bsa.ChrNameMappingTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.ChrNameMapping");

        TransformerRegistry.addTransformer( "GCContentFile", "ru.biosoft.bsa.transformer.GCContentFileTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.bsa.track.GCContentTrack" );
    }

    @Override protected void initTemplates()
    {
        //TemplateRegistry.registerTemplate("TEMPLATE NAME", "ANY CLASS WITH RESOURCE FOLDER INSTEAD OF PLUGIN NAME", "OTHER ARGUMENTS FOR TEMPLATE: FILE PATH, DESCRIPTION, IS BREAF, ORDER, FILTER");

        TemplateFilter filter = new TemplateFilter("ru.biosoft.bsa.SqlTrack", true, Collections.EMPTY_LIST, null);
        TemplateRegistry.registerTemplate("SQL track", "ru.biosoft.bsa.SqlTrack", "ru/biosoft/bsa/resources/sqltrack.vm", "SQL track information", false, 0, filter);
        TemplateRegistry.registerTemplate("SQL track simple", "ru.biosoft.bsa.SqlTrack", "sqltrack2.vm", "SQL track information for testing template", false, 40, filter);

        TemplateFilter BAMfilter = new TemplateFilter("ru.biosoft.bsa.BAMTrack", true, Collections.EMPTY_LIST, null);
        TemplateRegistry.registerTemplate("BAM track", "ru.biosoft.bsa.BAMTrack", "ru/biosoft/bsa/resources/bamtrack.vm", "BAM track information", false, 0, BAMfilter);

        TemplateFilter BigTrackFilter = new TemplateFilter( "ru.biosoft.bsa.track.big.BigBedTrack", true, Collections.EMPTY_LIST, null );
        TemplateRegistry.registerTemplate( "BigBed track", "ru.biosoft.bsa.track.big.BigBedTrack", "ru/biosoft/bsa/track/big/resources/bigbed_track.vm", "BigBed track information",
                false, 0,
                BigTrackFilter );

        TemplateFilter BigWigTrackFilter = new TemplateFilter( "ru.biosoft.bsa.track.big.BigWigTrack", true, Collections.EMPTY_LIST, null );
        TemplateRegistry.registerTemplate( "BigWig track", "ru.biosoft.bsa.track.big.BigWigTrack", "ru/biosoft/bsa/track/big/resources/bigwig_track.vm", "BigWig track information",
                false, 0, BigWigTrackFilter );

        TemplateFilter matrixFilter = new TemplateFilter( "ru.biosoft.bsa.analysis.FrequencyMatrix", true, Collections.EMPTY_LIST, null );
        TemplateRegistry.registerTemplate( "Matrix", "ru.biosoft.bsa.analysis.FrequencyMatrix", "ru/biosoft/bsa/resources/matrix.vm", "Weight matrix", false, 0, matrixFilter );

        TemplateFilter fileTrackFilter = new TemplateFilter( "ru.biosoft.bsa.FileTrack", true, Collections.EMPTY_LIST, null );
        TemplateRegistry.registerTemplate( "File track", "ru.biosoft.bsa.FileTrack", "ru/biosoft/bsa/resources/filetrack.vm", "File track information", false, 0, fileTrackFilter );
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
                .register( new FileType( "Fasta", new String[] { "fa", "fasta", "fna" }, "ru.biosoft.bsa.transformer.FastaSimpleFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "FASTA file"));
        FileTypeRegistry.register( new FileType( "Fasta with index file", new String[] { "fa", "fasta", "fna" }, "ru.biosoft.bsa.transformer.FastaFileTransformer",
                FileTypePriority.MEDIUM_PRIORITY, "FASTA file with index" ) );
        FileTypeRegistry
                .register( new FileType( "BED track", new String[] { "bed" }, "ru.biosoft.bsa.transformer.BedFileTransformer", FileTypePriority.HIGH_PRIORITY, "BED track file" ) );
        FileTypeRegistry
                .register( new FileType( "GFF track", new String[] { "gff", "gtf" }, "ru.biosoft.bsa.transformer.GFFFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "Generic Feature Format (gff) track file"));
        FileTypeRegistry
                .register( new FileType( "BAM track", new String[] { "bam" }, "ru.biosoft.bsa.transformer.BAMFileTransformer", FileTypePriority.HIGH_PRIORITY, "BAM track file" ) );
        FileTypeRegistry
                .register(
                        new FileType( "SAM track", new String[] { "sam" }, "ru.biosoft.bsa.transformer.BAMFileTransformer", FileTypePriority.MEDIUM_PRIORITY, "SAM track file" ) );
        FileTypeRegistry.register( new FileType( "VCF track", new String[] { "vcf" }, "ru.biosoft.bsa.transformer.VCFFileTransformer", FileTypePriority.HIGH_PRIORITY,
                "Variant Call Format (vcf) track file"));
        FileTypeRegistry
                .register( new FileType( "Wiggle track", new String[] { "wig" }, "ru.biosoft.bsa.transformer.WiggleFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "Wiggle track file" ) );
        FileTypeRegistry
                .register( new FileType( "GenBank track", new String[] { "gb" }, "ru.biosoft.bsa.transformer.GenbankFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "GenBank track file" ) );
        FileTypeRegistry
                .register( new FileType( "ChrNameMappingFile", new String[] { "txt" }, "ru.biosoft.bsa.ChrNameMappingTransformer", FileTypePriority.LOW_PRIORITY,
                        "Chr name mapping" ) );

        FileTypeRegistry.register(
                new FileType( "BigBed track", new String[] { "bb", "BigBed" }, "ru.biosoft.bsa.transformer.BigBedFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "BigBed file" ) );
        FileTypeRegistry.register(
                new FileType( "BigWig track", new String[] { "bw", "BigWig" }, "ru.biosoft.bsa.transformer.BigWigFileTransformer", FileTypePriority.HIGH_PRIORITY,
                        "BigWig file" ) );
        FileTypeRegistry.register( new FileType( "CombinedTrack", new String[] { "comb" }, "ru.biosoft.bsa.transformer.CombinedTrackTransformer",
                FileTypePriority.HIGH_PRIORITY, "Combined track file" ) );

        FileTypeRegistry.register(
                new FileType( "GCContent", new String[] { "gc" }, "ru.biosoft.bsa.transformer.GCContentFileTransformer", FileTypePriority.HIGH_PRIORITY, "GCContent file" ) );
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
