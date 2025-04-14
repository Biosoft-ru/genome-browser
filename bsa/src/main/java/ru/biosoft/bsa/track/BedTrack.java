package ru.biosoft.bsa.track;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ru.biosoft.access.DataCollectionUtils;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.DataElementSupport;
import ru.biosoft.access.core.VectorDataCollection;
import ru.biosoft.bsa.Interval;
import ru.biosoft.bsa.LinearSequence;
import ru.biosoft.bsa.Nucleotide15LetterAlphabet;
import ru.biosoft.bsa.Sequence;
import ru.biosoft.bsa.Site;
import ru.biosoft.bsa.SiteImpl;
import ru.biosoft.bsa.StrandType;
import ru.biosoft.bsa.Track;
import ru.biosoft.bsa.WritableTrack;
import ru.biosoft.bsa.importer.BEDTrackImporter;
import ru.biosoft.bsa.view.DefaultTrackViewBuilder;
import ru.biosoft.bsa.view.TrackViewBuilder;
import ru.biosoft.exception.ExceptionRegistry;
import ru.biosoft.exception.LoggedException;

public class BedTrack extends DataElementSupport implements WritableTrack
{
    private File file;
    private BufferedWriter writer;
    private int nextId = 1;

    private VectorDataCollection<Site> sites;
    private boolean isInitialized = false;
    private TrackViewBuilder viewBuilder = new DefaultTrackViewBuilder();

    public BedTrack(DataCollection<?> origin, Properties properties)
    {
        super( properties.getProperty(DataCollectionConfigConstants.NAME_PROPERTY, "null"), origin );
        file = DataCollectionUtils.getChildFile( origin, getName() );
    }
    
    public BedTrack(DataCollection<?> origin, String name, File file)
    {
        super( name, origin );
        this.file = file;
    }
    
    public File getFile()
    {
        return file;
    }

    @Override
    public DataCollection<Site> getSites(String sequence, int from, int to)
    {
        init();
        VectorDataCollection<Site> result = new VectorDataCollection<>( "sites" );
        String sequenceName = DataElementPath.create( sequence ).getName();
        Interval fromTo = new Interval( from, to );
        sites.stream().filter( s -> s.getSequence().getName().equals( sequenceName ) && s.getInterval().intersects( fromTo ) ).forEach( s -> result.put( s ) );
        return result;
    }

    @Override
    public int countSites(String sequence, int from, int to) throws Exception
    {
        init();
        return getSites( sequence, from, to ).getSize();
    }

    @Override
    public Site getSite(String sequence, String siteName, int from, int to) throws Exception
    {
        init();
        return sites.get( siteName );
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

    private Object readerLock = new Object();

    private void init()
    {
        if( isInitialized )
            return;
        synchronized (readerLock)
        {
            if( isInitialized )
                return;
            sites = new VectorDataCollection<>( getName(), Site.class, null );
            readFromFile( file, sites );
            isInitialized = true;
        }
    }

    @Override
    public void addSite(Site site) throws LoggedException
    {
        try
        {
            initWriter();
            Sequence chr = site.getOriginalSequence();
            
            char strand;
            switch (site.getStrand())
            {
                case StrandType.STRAND_PLUS: strand = '+'; break;
                case StrandType.STRAND_MINUS: strand = '-'; break;
                default: strand = '.';
            }
            String siteName = site.getName();
            if(siteName == null)
                siteName = String.valueOf(nextId++); 

            writer.append( site.getSequence().getName() )
            .append( '\t' )
            .append( String.valueOf(site.getFrom() - chr.getStart()) )//zero based inclusive
            .append('\t')
            .append( String.valueOf(site.getTo() - chr.getStart() + 1) )//zero based exclusive;
            .append('\t')
            .append( siteName )
            .append( '\t' )
            .append( String.valueOf(site.getScore()) )
            .append('\t')
            .append(strand)
            .append( '\n' );
        }
        catch( IOException e )
        {
            throw ExceptionRegistry.translateException( e );
        }
    }

    private void initWriter() throws IOException
    {
        if(writer == null)
            writer = new BufferedWriter( new FileWriter( file, true ) );

    }

    @Override
    public void finalizeAddition() throws LoggedException
    {
        try
        {
            initWriter();
            writer.flush();
        }
        catch( IOException e )
        {
            throw ExceptionRegistry.translateException( e );
        }
    }

    private final Map<String, Sequence> sequenceCache = new HashMap<>();

    private Sequence getSequence(String name)
    {
        Sequence result = sequenceCache.get( name );
        if( result != null )
            return result;

        result = new LinearSequence( name, new byte[0], Nucleotide15LetterAlphabet.getInstance() );
        sequenceCache.put( name, result );

        return result;
    }

    protected void readFromFile(File file, DataCollection<Site> sites)
    {
        int i = 1;
        try (FileInputStream is = new FileInputStream( file );
                BufferedReader input = new BufferedReader( new InputStreamReader( is, StandardCharsets.UTF_8 ) );
                FileChannel ch = is.getChannel())
        {
            String line;
            while ( (line = input.readLine()) != null )
            {
                if( isComment( line ) )
                    continue;
                Site site = BEDTrackImporter.parseBEDLine( line );
                if( site == null )
                {
                    continue;
                }
                if( site.getOriginalSequence() == null )
                {
                    Sequence seq = getSequence( site.getName() );
                    site = new SiteImpl( site.getOrigin(), i + "", site.getType(), site.getBasis(), site.getStart(), site.getLength(), site.getPrecision(), site.getStrand(), seq,
                            site.getComment(), site.getProperties() );
                }
                sites.put( site );
                i++;
            }
        }

        catch (Exception e)
        {
            //TODO:
        }

    }

    private boolean isComment(String line)
    {
        return line.startsWith( "track " ) || line.startsWith( "browser " ) || line.startsWith( "#" );
    }
}
