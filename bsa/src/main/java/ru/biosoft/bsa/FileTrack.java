package ru.biosoft.bsa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import ru.biosoft.access.DataCollectionUtils;
import ru.biosoft.access.core.AbstractDataCollection;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.VectorDataCollection;
import ru.biosoft.bsa.view.DefaultTrackViewBuilder;
import ru.biosoft.bsa.view.TrackViewBuilder;

public abstract class FileTrack extends AbstractDataCollection<DataElement> implements Track
{
    private File file;
    private VectorDataCollection<Site> sites;
    private boolean isInitialized = false;
    private TrackOptions trackOptions;
    private TrackViewBuilder viewBuilder = new DefaultTrackViewBuilder();

    public FileTrack(DataCollection<?> origin, Properties properties) throws IOException
    {
        super(origin, properties);
        String filePath = properties.getProperty(DataCollectionConfigConstants.FILE_PROPERTY);
        if( filePath != null )
        {
            file = new File(filePath);
        }
        else
            file = DataCollectionUtils.getChildFile(origin, getName());

        if( !file.exists() )
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " not found in track constructor");

        trackOptions = new TrackOptions(this, properties);
    }

    @Override
    public DataCollection<?> getOrigin()
    {
        return super.getOrigin();
    }

    private Object readerLock = new Object();
    private void init()
    {
        if( isInitialized )
            return;
        synchronized (readerLock)
        {
            if( isInitialized )
                return;
            sites = new VectorDataCollection<>(getName(), Site.class, null);
            readFromFile(file, sites);
            isInitialized = true;
        }
    }

    private synchronized void remapIfNeeded(String sequence)
    {
        if( trackOptions.isAutoMapping() )
        {
            Set<String> chrNames = sites.stream().map( s -> s.getSequence().getName() ).limit( 20 ).collect( Collectors.toSet() );
            Set<String> wantedNames = new HashSet<>();
            DataElementPath sequensePath = DataElementPath.create( sequence );
            wantedNames.addAll( sequensePath.getParentCollection().getNameList() );
            ChrNameMapping mapping = ChrNameMapping.autoDetectChrMappingBySequence( chrNames, wantedNames );
            if( mapping != null )
            {
                trackOptions.setChrNameMapping( mapping );
                VectorDataCollection newSites = new VectorDataCollection<>(getName(), Site.class, null);
                sites.stream().map( site ->  {
                    String oldName = site.getSequence().getName();
                    Sequence seq = getSequence( oldName );
                    if(seq != null)
                        return new SiteImpl( site.getOrigin(), site.getName(), site.getType(), site.getBasis(), site.getStart(), site.getLength(), site.getPrecision(),
                                site.getStrand(), seq, site.getComment(), site.getProperties() );
                    else
                        return null;
                } ).filter( Objects::nonNull ).forEach( s -> newSites.put( s ) );
                sites = newSites;
            }
            else
                trackOptions.setAutoMapping( false );

        }
    }

    protected abstract void readFromFile(File file, DataCollection<Site> sites);

    @Override
    public DataCollection<Site> getSites(String sequence, int from, int to)
    {
        init();
        remapIfNeeded( sequence );
        VectorDataCollection<Site> result = new VectorDataCollection<>("sites");
        String sequenceName = DataElementPath.create(sequence).getName();
        Interval fromTo = new Interval(from, to);
        sites.stream().filter( s -> s.getSequence().getName().equals( sequenceName ) && s.getInterval().intersects( fromTo ) ).forEach( s -> result.put( s ) );
        return result;
    }

    @Override
    public int countSites(String sequence, int from, int to) throws Exception
    {
        init();
        remapIfNeeded( sequence );
        return getSites(sequence, from, to).getSize();
    }

    @Override
    public Site getSite(String sequence, String siteName, int from, int to) throws Exception
    {
        init();
        remapIfNeeded( sequence );
        return sites.get(siteName);
    }

    @Override
    public DataCollection<Site> getAllSites() throws UnsupportedOperationException
    {
        init();
        return sites;
    }

    @Override
    public TrackViewBuilder getViewBuilder()
    {
        return viewBuilder;
    }

    public File getFile()
    {
        return file;
    }

    public TrackOptions getTrackOptions()
    {
        return trackOptions;
    }

    @Override
    public List<String> getNameList()
    {
        return getAllSites().getNameList();
    }

    @Override
    protected DataElement doGet(String name) throws Exception
    {
        return getAllSites().get(name);
    }

    protected Sequence getSequence(String name)
    {
        return trackOptions.getChromosomeSequence(trackOptions.internalToExternal(name));
    }
    
    protected Set<String> getChromosomeNames(File file)
    {
        return Collections.emptySet();
    }



}
