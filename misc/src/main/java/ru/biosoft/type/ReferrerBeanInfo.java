package ru.biosoft.type;

import java.util.logging.Logger;

public class ReferrerBeanInfo<T extends Referrer> extends GenericEntityBeanInfo<T>
{
    protected static final Logger log = Logger.getLogger(ReferrerBeanInfo.class.getName());

    protected ReferrerBeanInfo(Class<? extends T> r)
    {
        super(r);
    }
    
    protected ReferrerBeanInfo(Class beanClass, String key)
    {
        super(beanClass, key);
    }

    @Override
    public void initProperties() throws Exception
    {
        super.initProperties();

        property("description").htmlDisplayName("DE").add(4);
        //property("databaseReferences").editor(DatabaseReferencesPropertyEditor.class).htmlDisplayName("DR").add();
        //property("literatureReferences").htmlDisplayName("RF").editor(LiteratureReferencesEditor.class).add();
    }

}
