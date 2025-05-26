package ru.biosoft.bsa.track;

import com.developmentontheedge.beans.BeanInfoEx;

public class GFFTrackBeanInfo extends BeanInfoEx
{

    public GFFTrackBeanInfo()
    {
        super(GFFTrack.class, true);
    }

    @Override protected void initProperties() throws Exception
    {
        property("name").readOnly().add();
        property("trackOptions");
    }

}
