package ru.biosoft.bsa.track;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.developmentontheedge.beans.PropertiesDPS;
import com.developmentontheedge.beans.annot.PropertyName;

import ru.biosoft.access.core.AbstractDataCollection;
import ru.biosoft.access.core.ClassIcon;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.VectorDataCollection;
import ru.biosoft.bsa.Basis;
import ru.biosoft.bsa.Precision;
import ru.biosoft.bsa.Sequence;
import ru.biosoft.bsa.Site;
import ru.biosoft.bsa.SiteImpl;
import ru.biosoft.bsa.SiteType;
import ru.biosoft.bsa.StrandType;
import ru.biosoft.bsa.Track;
import ru.biosoft.bsa.importer.VCFTrackImporter;
import ru.biosoft.bsa.view.DefaultTrackViewBuilder;
import ru.biosoft.bsa.view.TrackViewBuilder;
import ru.biosoft.util.TextUtil2;

/**
 * class refactored, @author anna
 */
@ClassIcon ( "resources/trackvcf.png" )
@PropertyName ( "track" )
public class VCFFileTrack extends AbstractDataCollection<DataElement> implements Track
{
    private File vcfFile;
    private static final Logger log = Logger.getLogger(VCFFileTrack.class.getName());
    private ChrCache chrCache;
    
    
    public VCFFileTrack(DataCollection<?> parent, Properties properties) throws IOException
    {
        super( parent, properties );
        
        String vcfFilePath = properties.getProperty( DataCollectionConfigConstants.FILE_PROPERTY );
        if(vcfFilePath == null)
            throw new IllegalArgumentException();
        vcfFile = new File(vcfFilePath);
        if(!vcfFile.exists())
            throw new FileNotFoundException();
        DataElementPath seqBase = DataElementPath.create( properties.getProperty( Track.SEQUENCES_COLLECTION_PROPERTY ) );
        chrCache = new ChrCache( seqBase );
    }

    @Override
    public List<String> getNameList()
    {
        return Collections.emptyList();
    }
    
    @Override
    protected DataElement doGet(String name) throws Exception
    {
        return null;
    }

    private Object readerLock = new Object();
    @Override
    public DataCollection<Site> getSites(String sequence, int from, int to)
    {
        String chrName = DataElementPath.create( sequence ).getName();
        
        synchronized(readerLock)
        {
            DataCollection<Site> allSites = getAllSites();
            VectorDataCollection<Site> result = new VectorDataCollection<>("sites");
            allSites.stream().filter(site -> {
                return (site.getOriginalSequence().getName().equals(chrName) && site.getFrom() >= from && site.getTo() <= to);
            }).forEach(result::put);
            return result;
        }
    }

    @Override
    public int countSites(String sequence, int from, int to) throws Exception
    {
        return getSites( sequence, from, to ).getSize();
    }

    @Override
    public Site getSite(String sequence, String siteName, int from, int to) throws Exception
    {
        return getSites(sequence, from ,to).get( siteName );
    }

    @Override
    public DataCollection<Site> getAllSites()
    {
        synchronized(readerLock)
        {
            return convertToDC(vcfFile);
        }
    }
    
    private VectorDataCollection<Site> convertToDC(File trackFile)
    {
        VectorDataCollection<Site> result = new VectorDataCollection<>( "sites" );

        Map<String, String> formatTypeMap = new HashMap<>();
        Map<String, String> infoTypeMap = new HashMap<>();
        List<String> sampleIdsList = new ArrayList<>();
        Properties properties = new Properties();

        try (FileInputStream is = new FileInputStream(trackFile);
                BufferedReader input = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                FileChannel ch = is.getChannel())
        {
            String line;

            int i = 1;
            while ( (line = input.readLine()) != null )
            {
                if( VCFTrackImporter.isComment(line, properties, infoTypeMap, sampleIdsList, formatTypeMap, vcfFile.getName()) )
                    continue;
                String siteName = String.valueOf(i++);
                Site site = VCFTrackImporter.parseLine(line, formatTypeMap, sampleIdsList, infoTypeMap,
                        Boolean.valueOf(getInfo().getProperties().getProperty("normalizeChromosome", "false")));
                if( site.getOriginalSequence() == null )
                {
                    Sequence seq = chrCache.getSequence(site.getName());
                    site = new SiteImpl(site.getOrigin(), siteName, site.getType(), site.getBasis(), site.getStart(), site.getLength(), site.getPrecision(), site.getStrand(), seq,
                            site.getComment(), site.getProperties());
                }
                result.put(site);
            }
            result.getInfo().getProperties().putAll(properties);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Can not create VCF track from file", e);
        }

        return result;
    }

    private Site parseLine(String line, Map<String, String> infoTypeMap, List<String> sampleIdsList, Map<String, String> formatTypeMap)
    {
        String[] fields = TextUtil2.split(line, '\t');
        if( fields.length < 8 )
            return null;
        String chr = normalizeChromosome(fields[0]);
        int start;
        try
        {
            start = Integer.parseInt(fields[1]);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        Properties parameters = new Properties();
        putParameter(parameters, "name", fields[2]);
        String altAllele = fields[4];
        if( !VCFTrackImporter.NUCLEOTIDE_PATTERN.matcher(altAllele).matches() )
            return null;
        putParameter(parameters, "AltAllele", altAllele, true);
        if( !fields[5].equals(".") )
        {
            try
            {
                if( Double.isNaN(Double.parseDouble(fields[5])) )
                    return null;
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        putParameter(parameters, "Quality", fields[5]);
        putParameter(parameters, "Filter", fields[6].replace(",", ";"));
        String refAllele = fields[3];
        if( !VCFTrackImporter.NUCLEOTIDE_PATTERN.matcher(refAllele).matches() )
            return null;
        int length = refAllele.length();
        putParameter(parameters, "RefAllele", refAllele, true);
        for ( String info : TextUtil2.split(fields[7], ';') )
        {
            int pos = info.indexOf("=");
            if( pos > 0 )
            {
                String value = info.substring(pos + 1);
                String key = info.substring(0, pos);
                if( key.equals("END") )
                {
                    try
                    {
                        length = Integer.parseInt(value) - start;
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
                String fieldType = infoTypeMap.getOrDefault(key, null);
                Object realValue = VCFTrackImporter.getValueWithRealType(value, fieldType);
                parameters.put("Info_" + key, realValue);
            }
            else
                parameters.put("Info_" + info, "+");
        }
        if( fields.length > 9 )
        {
            String[] formatFields = TextUtil2.split(fields[8], ':');
            for ( int j = 0; j < fields.length - 9; j++ )
            {

                String[] formatValues = TextUtil2.split(fields[j + 9], ':');
                String sampleName = (sampleIdsList.size() > j) ? sampleIdsList.get(j) : j + "";
                for ( int i = 0; i < Math.min(formatFields.length, formatValues.length); i++ )
                {
                    String fieldType = formatTypeMap.getOrDefault(formatFields[i], null);
                    Object value = VCFTrackImporter.getValueWithRealType(formatValues[i], fieldType);
                    parameters.put(VCFTrackImporter.VCF_FORMAT_PREFIX + formatFields[i] + "_" + sampleName, value);
                }
            }
        }
        String type = SiteType.TYPE_VARIATION;
        if( altAllele.startsWith("<") && altAllele.endsWith(">") )
            type = altAllele.substring(1, altAllele.length() - 1);
        return new SiteImpl(null, chr, type, Basis.BASIS_USER, start, length, Precision.PRECISION_EXACTLY, StrandType.STRAND_NOT_APPLICABLE, null, new PropertiesDPS(parameters));
    }

    private void putParameter(Properties parameters, String id, String s)
    {
        putParameter(parameters, id, s, false);
    }

    private void putParameter(Properties parameters, String id, String s, boolean putEmpty)
    {
        if( s != null && !s.isEmpty() && !s.equals(".") )
            parameters.put(id, s);
        else if( putEmpty )
            parameters.put(id, "");
    }

    protected static String normalizeChromosome(String name)
    {
        String smallName = name.toLowerCase();
        if( smallName.startsWith("chr") )
            name = name.substring("chr".length());
        return name.equals("M") ? "MT" : name;
    }

    
    private TrackViewBuilder viewBuilder = new DefaultTrackViewBuilder();
    @Override
    public TrackViewBuilder getViewBuilder()
    {
        return viewBuilder;
    }
}
