package ru.biosoft.bsa;

import ru.biosoft.access.core.DataCollection;
import biouml.standard.type.RNA;
import ru.biosoft.access.core.ClassIcon;

@ClassIcon( "resources/transcript.gif" )
public class Transcript extends RNA
{
    private Site site;
    
    public Transcript(DataCollection origin, String name)
    {
        super(origin, name);
        this.site = site;
    }
    
    @Override
    public String getType()
    {
        return TYPE_TRANSCRIPT;
    }
    
}
