package ru.biosoft.templates;

import ru.biosoft.util.Initializer;

public class TemplatesInitializer extends Initializer
{
    private static TemplatesInitializer instance;

    public static TemplatesInitializer getInstance()
    {
        if( instance == null )
            instance = new TemplatesInitializer();
        return instance;
    }

    @Override
    protected void initTemplates()
    {
        //TemplateRegistry.registerTemplate("TEMPLATE NAME", "ANY CLASS WITH RESOURCE FOLDER INSTEAD OF PLUGIN NAME", "OTHER ARGUMENTS FOR TEMPLATE: FILE PATH, DESCRIPTION, IS BREAF, ORDER");
        TemplateRegistry.registerTemplate("Default", "ru.biosoft.templates.TemplateInfo", "resources/beaninfotemplate.vm", "Universal bean template", false, 1);
        TemplateRegistry.registerTemplate("Table info", "ru.biosoft.templates.TemplateInfo", "resources/tabletemplate.vm", "Table info templat", false, 2);
    }

    public static void initialize()
    {
        getInstance().init();
    }
}

/*
 * <extension id="Base template" point="ru.biosoft.templates.template">
 * <template name="Default"
 * file="ru/biosoft/templates/resources/beaninfotemplate.vm"
 * description="Universal bean template" isBrief="no" order="1"> </template>
 * </extension>
 * 
 * <extension id="Table template" point="ru.biosoft.templates.template">
 * <template name="Table info"
 * file="ru/biosoft/templates/resources/tabletemplate.vm"
 * description="Table info template" isBrief="no" order="2"> <filter
 * class="ru.biosoft.table.TableDataCollection" subclasses="yes"/> </template>
 * </extension>
 */
