package ru.biosoft.bsa.transformer;

import java.io.File;
import java.util.Properties;

import ru.biosoft.access.file.AbstractFileTransformer;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.PropertiesHolder;
import ru.biosoft.access.core.PriorityTransformer;
import ru.biosoft.bsa.ChrNameMapping;
import ru.biosoft.bsa.Site;
import ru.biosoft.bsa.Track;
import ru.biosoft.bsa.track.big.BigBedTrack;
import ru.biosoft.bsa.track.big.BigTrack;
import ru.biosoft.util.ApplicationUtils;

public class BigBedFileTransformer extends AbstractFileTransformer<BigBedTrack> implements PriorityTransformer, PropertiesHolder
{

    private Properties properties;

    @Override
    public Class<? extends BigBedTrack> getOutputType()
    {
        return (Class<? extends BigBedTrack>)BigBedTrack.class;
    }

    @Override
    public BigBedTrack<Site> load(File input, String name, DataCollection<BigBedTrack> origin) throws Exception
    {
        Properties trackProperties = new Properties();
        if( properties != null )
            trackProperties.putAll( properties );
        trackProperties.setProperty( DataCollectionConfigConstants.NAME_PROPERTY, name );
        trackProperties.setProperty( BigTrack.PROP_BIGBED_PATH, input.getAbsolutePath() );
        return new BigBedTrack( origin, trackProperties );
    }

    @Override
    public void save(File output, BigBedTrack element) throws Exception
    {
        ApplicationUtils.linkOrCopyFile( output, new File(element.getFilePath()), null );
    }

    @Override
    public int getInputPriority(Class<? extends DataElement> inputClass, DataElement output)
    {
        return 1;
    }

    @Override
    public int getOutputPriority(String name)
    {
        if(name.toLowerCase().endsWith( ".bb" ))
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
