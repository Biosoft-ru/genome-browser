package ru.biosoft.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.SimpleBeanInfo;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;


import ru.biosoft.access.ClassLoading;
import ru.biosoft.access.core.ClassIcon;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementDescriptor;
import ru.biosoft.access.core.DataElementPath;

public class IconFactory
{
    public static final ImageIcon leafIcon, folderIcon, openIcon;

    static
    {
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        leafIcon = getImageIcon( renderer.getDefaultLeafIcon() );
        folderIcon = getImageIcon( renderer.getClosedIcon() );
        openIcon = getImageIcon( renderer.getOpenIcon() );
    }
    
    static Logger log = Logger.getLogger( IconFactory.class.getName() );

    protected ImageIcon doGetIcon(DataElementPath path)
    {
        return doGetIconById(doGetIconId(path));
    }
    
    protected String doGetIconId(DataElementPath path)
    {
        DataCollection<?> dc = path.optParentCollection();
        if(dc == null) return null;
        DataElementDescriptor descriptor = dc.getDescriptor(path.getName());
        if(descriptor == null) return null;
        String result = descriptor.getIconId();
        result = result == null ? null : result.indexOf(":") == -1 && !result.startsWith("/") ? dc.getInfo().getProperties()
                .getProperty(DataCollectionConfigConstants.CONFIG_PATH_PROPERTY, ".")
                + File.separator + result : result;
        return result;
    }
    
    private static ImageIcon getImageIcon(Icon defIcon)
    {
        ImageIcon icon;
        BufferedImage image = new BufferedImage( defIcon.getIconWidth(), defIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
        Graphics g = image.createGraphics();
        try
        {
            defIcon.paintIcon( null, g, 0, 0 );
            icon = new ImageIcon( image );
        }
        finally
        {
            g.dispose();
        }
        return icon;
    }
    /**
     * Returns default icon for specified class. You should specify icon path using ClassIcon class annotation
     */
    private final Map<Class<? extends DataElement>,String> iconIdCache = new ConcurrentHashMap<>();
    protected String getIconIdByClass(Class<? extends DataElement> clazz)
    {
        if(iconIdCache.containsKey(clazz))
        {
            String iconId = iconIdCache.get(clazz);
            return iconId.isEmpty()?null:iconId;
        }
        Deque<Class<? extends DataElement>> classes = new LinkedList<>();
        classes.add(clazz);
        // We cannot simply use clazz.getAnnotation(ClassIcon.class),
        // because we need to know exactly where that annotation is defined
        // as specified icon path is relative to class location
        while(!classes.isEmpty())
        {
            Class<? extends DataElement> curClass = classes.pop();
            for(Annotation annotation: curClass.getDeclaredAnnotations())
            {
                if(annotation instanceof ClassIcon)
                {
                    String resource = ClassLoading.getResourceLocation( curClass, ( (ClassIcon)annotation ).value() );
                    iconIdCache.put(clazz, resource);
                    return resource;
                }
            }
            if(curClass.getSuperclass() != null && ru.biosoft.access.core.DataElement.class.isAssignableFrom(curClass.getSuperclass()))
            {
                classes.add((Class<? extends DataElement>)curClass.getSuperclass());
            }
            for(Class<?> iface: curClass.getInterfaces())
            {
                if(DataElement.class.isAssignableFrom(iface))
                    classes.add((Class<? extends DataElement>)iface);
            }
        }
        return null;
    }
    
    protected String doGetClassIconId(Class<?> clazz)
    {
        if( DataElement.class.isAssignableFrom(clazz) )// || Document.class.isAssignableFrom( clazz ))
            return getIconIdByClass((Class<? extends DataElement>)clazz);
        //TODO:
        //        if(ReferenceType.class.isAssignableFrom(clazz))
        //            return ReferenceTypeRegistry.getReferenceType((Class<? extends ReferenceType>)clazz).getIconId();
        return null;
    }

    protected ImageIcon doGetIconById(String id)
    {
        return id==null?null:getImageIcon(id);
    }
    
    //code copied from ApplicationUtil
    public static ImageIcon getImageIcon(URL url)
    {
        if( url == null )
            return null;

        ImageIcon imageIcon = new ImageIcon(url);
        return imageIcon;
    }

    protected static Map<String, ImageIcon> imageMap = new ConcurrentHashMap<>();

    public static ImageIcon getImageIcon(String imagename)
    {
        ImageIcon imageIcon = imageMap.get(imagename);

        if( imageIcon != null )
            return imageIcon;

        int idx = imagename.indexOf(':');
        if( idx > 2 )
        {
            String pluginName = imagename.substring(0, idx);
            log.fine( "Loading image from plugin " + pluginName );
            String resource = imagename.substring(idx + 1);
            if(pluginName.equals("default"))
            {
                URL url = IconFactory.class.getClassLoader().getResource(resource);
                if( url != null )
                {
                    imageIcon = getImageIcon(url);
                    imageMap.put(imagename, imageIcon);
                    return imageIcon;
                }
            }
        }

        URL url = ClassLoader.getSystemResource(imagename);

        if( url != null )
        {
            imageIcon = getImageIcon(url);
            imageMap.put(imagename, imageIcon);
            return imageIcon;
        }

        SimpleBeanInfo sbi = new SimpleBeanInfo();
        Image img = sbi.loadImage(imagename);

        if( img != null )
        {

            imageIcon = new ImageIcon(img);
            imageMap.put(imagename, imageIcon);
            return imageIcon;
        }
        
        //In some cases we try to get image by the path to file. Here we check if such file exists 
        //However not sure what should happened if such file does not exist  or is of wrong type
        //TODO: do something in that regard
        if( !new File( imagename ).exists() )
        {   
            log.log( Level.SEVERE, "Image file doesn't exists: " + imagename, new Exception() );
        }   
        imageIcon = new ImageIcon( imagename );
        imageMap.put(imagename, imageIcon);
        return imageIcon;
    }
    

    private static IconFactory factory = new IconFactory();
    
    public static void init(IconFactory factory)
    {
        IconFactory.factory = factory;
    }
    
    public static ImageIcon getIcon(DataElementPath path)
    {
        return factory.doGetIcon(path);
    }
    
    public static ImageIcon getIcon(DataElementPath path, boolean open)
    {
        ImageIcon icon = factory.doGetIcon(path);
        if(icon == null)
        {
            DataElementDescriptor descriptor = path.getDescriptor();
            return descriptor == null || descriptor.isLeaf() ? leafIcon : open ? openIcon : folderIcon;
        }
        return icon;
    }
    
    public static String getIconId(DataElementPath path)
    {
        return factory.doGetIconId(path);
    }
    
    public static String getClassIconId(Class<?> clazz)
    {
        return factory.doGetClassIconId(clazz);
    }
    
    public static ImageIcon getIconById(String id)
    {
        return factory.doGetIconById(id);
    }
}
