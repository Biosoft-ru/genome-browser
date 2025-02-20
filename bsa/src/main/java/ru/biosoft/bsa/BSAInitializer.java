package ru.biosoft.bsa;

import ru.biosoft.access.generic.TransformerRegistry;
import ru.biosoft.bsa.server.BSAService;
import ru.biosoft.server.ServiceRegistry;
import ru.biosoft.table.datatype.DataType;
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
        TemplateRegistry.registerTemplate("SQL track", "ru.biosoft.bsa.SqlTrack", "sqltrack2.vm", "SQL track information", false, 0);
    }

    /*
     * <template name="SQL track" file="ru/biosoft/bsa/resources/sqltrack.vm"
     * description="SQL track information" isBrief="no" order="0"> <filter
     * class="ru.biosoft.bsa.SqlTrack" subclasses="yes"/> </template>
     */

    public static void initialize()
    {
        getInstance().init();
    }

}
