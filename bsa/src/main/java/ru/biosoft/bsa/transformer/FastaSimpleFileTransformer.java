package ru.biosoft.bsa.transformer;

import java.io.File;
import java.util.Properties;

import ru.biosoft.access.file.AbstractFileTransformer;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.PropertiesHolder;
import ru.biosoft.access.core.PriorityTransformer;
import ru.biosoft.util.ApplicationUtils;

public class FastaSimpleFileTransformer extends AbstractFileTransformer<FastaSimpleSequenceCollection> implements PriorityTransformer, PropertiesHolder
{

    private Properties properties;
	@Override
	public Class<? extends FastaSimpleSequenceCollection> getOutputType() {
		return FastaSimpleSequenceCollection.class;
	}

	@Override
	public int getInputPriority(Class<? extends DataElement> inputClass, DataElement output) {
		return 1;
	}

	@Override
	public int getOutputPriority(String name) {
		 if(name.endsWith( ".fa" ) || name.endsWith( ".fasta" ) || name.endsWith( ".fna" ))
	            return 2;
	        return 0;
	}

	@Override
	public FastaSimpleSequenceCollection load(File input, String name, DataCollection<FastaSimpleSequenceCollection> origin)
			throws Exception {
        Properties properties = FastaSimpleSequenceCollection.createProperties( name, input );
        if( this.properties != null )
            properties.putAll( this.properties );
        return new FastaSimpleSequenceCollection( origin, properties );
	}

	@Override
	public void save(File output, FastaSimpleSequenceCollection fasta) throws Exception {
		ApplicationUtils.linkOrCopyFile( output, fasta.getFile(), null );
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
        newProps.setProperty( FastaSequenceCollection.DO_GET_SEQUENCEID_ONLY, "true" );
        return newProps;
    }

}
