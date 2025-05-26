package ru.biosoft.bsa.transformer;

import java.io.File;

import ru.biosoft.access.file.AbstractFileTransformer;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.PriorityTransformer;
import ru.biosoft.bsa.GenbankSequenceCollection;
import ru.biosoft.bsa.SequenceImporter;

public class GenbankFileTransformer extends AbstractFileTransformer<GenbankSequenceCollection> implements PriorityTransformer
{

    @Override
    public GenbankSequenceCollection load(File input, String name, DataCollection<GenbankSequenceCollection> origin) throws Exception
    {
       return (GenbankSequenceCollection)SequenceImporter.createElement( origin, name, input, SequenceImporter.GB_FORMAT );
    }
    

    @Override
    public void save(File output, GenbankSequenceCollection element) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends GenbankSequenceCollection> getOutputType()
    {
        return GenbankSequenceCollection.class;
    }
    

    @Override
    public int getInputPriority(Class<? extends DataElement> inputClass, DataElement output)
    {
        return -1;
    }

    @Override
    public int getOutputPriority(String name)
    {
        if(name.toLowerCase().endsWith( ".gb" ))
            return 2;
        return 0;
    }
}
