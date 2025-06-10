package ru.biosoft.bsa.transformer;

import java.io.File;
import java.util.Properties;

import ru.biosoft.access.file.AbstractFileTransformer;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.PriorityTransformer;
import ru.biosoft.access.core.PropertiesHolder;
import ru.biosoft.bsa.ChrNameMapping;
import ru.biosoft.bsa.Track;
import ru.biosoft.bsa.track.VCFFileTrack;

public class VCFFileTransformer extends AbstractFileTransformer<VCFFileTrack> implements PriorityTransformer, PropertiesHolder
{
    private Properties properties;

    @Override
    public Class<? extends VCFFileTrack> getOutputType()
    {
        return VCFFileTrack.class;
    }

    @Override
    public VCFFileTrack load(File input, String name, DataCollection<VCFFileTrack> origin) throws Exception
    {
        Properties properties = (Properties) this.properties.clone();
        properties.setProperty( DataCollectionConfigConstants.NAME_PROPERTY, name );
        properties.setProperty( DataCollectionConfigConstants.FILE_PROPERTY, input.getAbsolutePath() );
        
        String configDir = origin.getInfo().getProperty( DataCollectionConfigConstants.CONFIG_PATH_PROPERTY );
        if( configDir != null )
            properties.setProperty(DataCollectionConfigConstants.CONFIG_PATH_PROPERTY, configDir);
        
        String seqBase = origin.getInfo().getProperty( Track.SEQUENCES_COLLECTION_PROPERTY );
        if(seqBase != null)
            properties.setProperty( Track.SEQUENCES_COLLECTION_PROPERTY, seqBase );
        
        return new VCFFileTrack( origin, properties );
    }

    @Override
    public void save(File output, VCFFileTrack element) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInputPriority(Class<? extends DataElement> inputClass, DataElement output)
    {
        return -1;
    }

    @Override
    public int getOutputPriority(String name)
    {
        if(name.toLowerCase().endsWith( ".vcf" ))
            return 2;
        return 0;
    }

    @Override
    public Properties getProperties()
    {
        return properties;
    }

    @Override
    public void setProperties(Properties props)
    {
        properties = props;
    }

    @Override
    public Properties createProperties()
    {
        Properties newProps = new Properties();
        newProps.setProperty( Track.SEQUENCES_COLLECTION_PROPERTY, "" );
        newProps.setProperty( ChrNameMapping.PROP_CHR_MAPPING, "" );
        return newProps;
    }
}
