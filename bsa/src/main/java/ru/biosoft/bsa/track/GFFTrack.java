package ru.biosoft.bsa.track;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;

import ru.biosoft.access.DataCollectionUtils;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.DataElementSupport;
import ru.biosoft.access.core.VectorDataCollection;
import ru.biosoft.bsa.Basis;
import ru.biosoft.bsa.LinearSequence;
import ru.biosoft.bsa.Nucleotide15LetterAlphabet;
import ru.biosoft.bsa.Precision;
import ru.biosoft.bsa.Sequence;
import ru.biosoft.bsa.Site;
import ru.biosoft.bsa.SiteImpl;
import ru.biosoft.bsa.StrandType;
import ru.biosoft.bsa.WritableTrack;
//import ru.biosoft.bsa.importer.GFFTrackImporter;
import ru.biosoft.bsa.view.DefaultTrackViewBuilder;
import ru.biosoft.bsa.view.TrackViewBuilder;
import ru.biosoft.exception.LoggedException;
import ru.biosoft.util.bean.StaticDescriptor;

public class GFFTrack extends DataElementSupport implements WritableTrack
{
    private File file;
    private boolean isInitialized = false;
    VectorDataCollection<Site> track;

    public GFFTrack(DataCollection<?> origin, Properties properties)
    {
        super( properties.getProperty( DataCollectionConfigConstants.NAME_PROPERTY, "null" ), origin );
        file = DataCollectionUtils.getChildFile( origin, getName() );
    }

    public GFFTrack(DataCollection<?> origin, String name, File file)
    {
        super( name, origin );
        this.file = file;
    }

    private void init()
    {
        if( isInitialized )
            return;
        synchronized( this )
        {
            if( isInitialized )
                return;
            track = new VectorDataCollection<>( getName(), Site.class, null );
            readFromFile( file, track );
            isInitialized = true;
        }
    }

    private void readFromFile(File trackFile, DataCollection<Site> track)
    {
        int i = 1;
        try (FileInputStream is = new FileInputStream( trackFile );
                BufferedReader input = new BufferedReader( new InputStreamReader( is, StandardCharsets.UTF_8 ) );
                FileChannel ch = is.getChannel())
        {
            String line;
            while( ( line = input.readLine() ) != null )
            {
                if( isComment( line ) )
                    continue;
                Site site = parseGFFLine(line);
                if( site == null )
                {
                    continue;
                }
                if( site.getOriginalSequence() == null )
                {
                    site = new SiteImpl( site.getOrigin(), i + "", site.getType(), site.getBasis(), site.getStart(), site.getLength(),
                            site.getPrecision(), site.getStrand(), getSequence( site.getName() ), site.getComment(), site.getProperties() );
                }
                track.put( site );
                i++;
            }
        }

        catch( Exception e )
        {
            //TODO:
        }
    }

    //TODO: copy code from GFFTrackImporter
    public static Site parseGFFLine(String line)
    {
        String[] fields = line.split("\t");
        if( fields.length < 5 )
            return null;
        String chrom = normalizeChromosome(fields[0]);
        String strand = fields.length < 7 ? "." : fields[6];
        if( !strand.equals("+") && !strand.equals("-") && !strand.equals(".") )
            return null;
        int start, end;
        try
        {
            start = Integer.parseInt(fields[3]);
            end = Integer.parseInt(fields[4]);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        if( start < 0 )
            start = 0;
        if( end < 0 )
            end = 0;
        DynamicPropertySet properties = new DynamicPropertySetAsMap();
        try
        {
            properties.add(new DynamicProperty(Site.SCORE_PD, Float.class, Float.parseFloat(fields[5])));
        }
        catch (Exception e)
        {
        }
        properties.add(new DynamicProperty(getDescriptor("source"), String.class, fields[1]));
        if( !fields[7].equals(".") )
            properties.add(new DynamicProperty(getDescriptor("frame"), String.class, fields[7]));
        HashMap<String, String> descrProps = getPropsFromDescr(fields[8]);
        for ( String dp : descrProps.keySet() )
        {
            properties.add(new DynamicProperty(getDescriptor(dp), String.class, descrProps.get(dp)));
        }
        return new SiteImpl(null, chrom, fields[2].equals("") ? null : fields[2], Basis.BASIS_USER, strand.equals("-") ? end : start, end - start + 1, Precision.PRECISION_EXACTLY,
                strand.equals("+") ? StrandType.STRAND_PLUS : strand.equals("-") ? StrandType.STRAND_MINUS : StrandType.STRAND_NOT_KNOWN, null, properties);
    }

    //TODO: copy code from TrackImporter
    protected static String normalizeChromosome(String name)
    {
        String smallName = name.toLowerCase();
        if( smallName.startsWith("chr") )
            name = name.substring("chr".length());
        return name.equals("M") ? "MT" : name;
    }

    //TODO: copy code from TrackImporter
    private static Map<String, PropertyDescriptor> descriptors = new ConcurrentHashMap<>();

    protected static PropertyDescriptor getDescriptor(String name)
    {
        PropertyDescriptor pd = descriptors.get(name);
        if( pd != null )
            return pd;
        pd = StaticDescriptor.create(name);
        descriptors.put(name, pd);
        return pd;
    }

    //TODO: copy code from GFFTrackImporter
    protected static HashMap<String, String> getPropsFromDescr(String gffDescription)
    {
        String[] pairs = gffDescription.split(";");
        HashMap<String, String> pm = new HashMap<>();
        //GFF2 format support
        if( pairs.length == 1 && gffDescription.indexOf("=") == -1 )
            pm.put("group", pairs[0]);
        else
            for ( String p : pairs )
            {
                String[] pair = p.split("=");
                if( pair.length != 2 )
                    continue;
                //TODO: resolve clashing of case-insensitive properties
                if( pair[0].equals("note") )
                {
                    pair[0] = "note_p";
                }
                pm.put(pair[0], pair[1]);
            }
        return pm;
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

    private boolean isComment(String line)
    {
        return line.startsWith( "track " ) || line.startsWith( "browser " ) || line.startsWith( "#" );
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
        track.stream().filter( s -> s.getSequence().getName().equals( sequenceName ) && s.getFrom() >= from && s.getTo() <= to )
                .forEach( s -> result.put( s ) );
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
        return track.get( siteName );
    }

    @Override
    public DataCollection<Site> getAllSites() throws UnsupportedOperationException
    {
        init();
        return track;
    }

    TrackViewBuilder viewBuilder = new DefaultTrackViewBuilder();
    @Override
    public TrackViewBuilder getViewBuilder()
    {
        return viewBuilder;
    }

    @Override
    public void addSite(Site site) throws LoggedException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public void finalizeAddition() throws LoggedException
    {
        throw new UnsupportedOperationException();
    }

}
