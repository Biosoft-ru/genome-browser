package ru.biosoft.bsa.access;

//TODO: use another message bundle, no properties are taken from MessageBundle file, check!
import ru.biosoft.bsa.MessageBundle;

import com.developmentontheedge.beans.BeanInfoEx;

public class SequencesDatabaseInfoBeanInfo extends BeanInfoEx
{
    public SequencesDatabaseInfoBeanInfo()
    {
        super(SequencesDatabaseInfo.class, MessageBundle.class.getName());
        setBeanEditor(SequencesDatabaseInfoSelector.class);
        setSimple(true);
        setHideChildren(true);
    }
}