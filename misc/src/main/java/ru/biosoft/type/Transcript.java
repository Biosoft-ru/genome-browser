package ru.biosoft.type;

import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.ClassIcon;

import ru.biosoft.bsa.Site;

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
