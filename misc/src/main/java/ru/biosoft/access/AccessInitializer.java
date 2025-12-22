package ru.biosoft.access;

import ru.biosoft.access.file.FileType;
import ru.biosoft.access.file.FileTypePriority;
import ru.biosoft.access.file.FileTypeRegistry;
import ru.biosoft.access.generic.DataElementTypeRegistry;
import ru.biosoft.access.generic.TransformerRegistry;
import ru.biosoft.table.datatype.DataType;
import ru.biosoft.util.Initializer;

public class AccessInitializer extends Initializer
{
    private static AccessInitializer instance;
    public static AccessInitializer getInstance()
    {
        if( instance == null )
            instance = new AccessInitializer();
        return instance;
    }

    public static void initialize()
    {
        getInstance().init();
    }

    @Override protected void initDataElementTypeDrivers()
    {
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementEntryTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementFileTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementGenericCollectionTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.DataElementSQLTypeDriver");
        DataElementTypeRegistry.registerDataElementTypeDriver("ru.biosoft.access.generic.RepositoryTypeDriver");
    }

    @Override protected void initTransformers()
    {
        //ACCESS
        TransformerRegistry.addTransformer( "Image", "ru.biosoft.access.support.FileImageTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.access.ImageDataElement" );
        //TransformerRegistry.addTransformer("HTML file", "ru.biosoft.access.support.FileHtmlTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.access.HtmlDataElement");
        TransformerRegistry.addTransformer( "Plain text", "ru.biosoft.access.file.FileTextTransformer", "ru.biosoft.access.file.FileDataElement",
                "ru.biosoft.access.core.TextDataElement");
        //TransformerRegistry.addTransformer("ZIP-archive with HTML pages", "ru.biosoft.access.support.FileZipHtmlTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.access.html.ZipHtmlDataCollection");
        //TransformerRegistry.addTransformer("Video", "ru.biosoft.access.support.FileZipHtmlTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.access.VideoDataElement");
    }

    protected void initBeanProviders()
    {
        BeanRegistry.registerBeanProvider("properties/fdc", "ru.biosoft.access.FDCBeanProvider");
    }

    /**
     * <extension id="Import zip-archive" point="ru.biosoft.access.import">
     * <export
     * format="ZIP-archive (*.zip)"
     * displayName="Archived file (*.zip, *.tar, *.gz etc.)"
     * importer="ru.biosoft.access.ZipFileImporter"
     * description="ru/biosoft/access/resources/ZIPFormat.html"
     * />
     * </extension>
     * 
     * 
     * <extension id="Import html file" point="ru.biosoft.access.import">
     * <export
     * format="HTML file (*.html, *.htm)"
     * importer="ru.biosoft.access.HtmlFileImporter"
     * description="ru/biosoft/access/resources/HtmlFormat.html"
     * />
     * </extension>
     */

    @Override
    protected void initImporters()
    {
        DataElementImporterRegistry.registerImporter( "Import generic file", "ru.biosoft.access.FileImporter", "Generic file", null );
        DataElementImporterRegistry.registerImporter( "Import image file", "ru.biosoft.access.ImageFileImporter", "Image file (*.png, *jpeg, *.gif etc)", null );
        DataElementImporterRegistry.registerImporter( "Import text file", "ru.biosoft.access.TextFileImporter", "Text file (*.txt)", null );

    }

    @Override
    protected void initFileTypes()
    {
        FileTypeRegistry.register( new FileType( "Image", new String[] { "png", "gif", "jpg", "bmp", "jpeg" }, "ru.biosoft.access.support.FileImageTransformer",
                FileTypePriority.HIGH_PRIORITY, "Image file" ) );
    }

    @Override
    protected void initDataTypes()
    {
        DataType.addDataType( "ru.biosoft.table.datatype.ChartDataType" );
        DataType.addDataType( "ru.biosoft.table.datatype.ViewDataType" );
        DataType.addDataType( "ru.biosoft.table.datatype.SetDataType" );
        DataType.addDataType( "ru.biosoft.table.datatype.PathDataType" );
        DataType.addDataType( "ru.biosoft.table.datatype.PathSetDataType" );
        DataType.addDataType( "ru.biosoft.table.datatype.DescribedStringDataType" );

    }

}
