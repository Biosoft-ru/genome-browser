package ru.biosoft.access.generic;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import one.util.streamex.StreamEx;

import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.PriorityTransformer;
import ru.biosoft.access.core.Transformer;
import ru.biosoft.access.file.FileDataElement;
import ru.biosoft.util.Clazz;
import ru.biosoft.util.ExtensionRegistrySupport;

/**
 * Registry which stores information on different transformers Useful to handle
 * elements within GenericDataCollection
 * 
 * @author lan
 * @author anna refactored
 */
public class TransformerRegistry extends ExtensionRegistrySupport<TransformerRegistry.TransformerInfo> //extends ExtensionRegistrySupport<TransformerRegistry.TransformerInfo>
{
    private TransformerRegistry()
    {
        super("ru.biosoft.access.transformer", "name");
        // TODO Auto-generated constructor stub
    }

    public static final String INPUT_CLASS = "inputClass";
    public static final String OUTPUT_CLASS = "outputClass";
    public static final String TRANSFORMER_CLASS = "transformerClass";
    
    private static final TransformerRegistry instance = new TransformerRegistry();

    public static class TransformerInfo
    {
        @SuppressWarnings ( "rawtypes" )
        final Class<? extends Transformer> transformerClass;
        final Class<? extends DataElement> inputClass;
        final Class<? extends DataElement> outputClass;
        final String name;
        
        @SuppressWarnings ( "rawtypes" )
        public TransformerInfo(String name, Class<? extends Transformer> transformerClass, Class<? extends DataElement> inputClass,
                Class<? extends DataElement> outputClass)
        {
            super();
            this.transformerClass = transformerClass;
            this.inputClass = inputClass;
            this.outputClass = outputClass;
            this.name = name;
        }

        @SuppressWarnings ( "rawtypes" )
        public Class<? extends Transformer> getTransformerClass()
        {
            return transformerClass;
        }

        public Class<? extends DataElement> getInputClass()
        {
            return inputClass;
        }

        public Class<? extends DataElement> getOutputClass()
        {
            return outputClass;
        }

        public String getName()
        {
            return name;
        }
    }

    /**
     * Obtain transformer class for given input/output classes
     * @param inputClass transformer input class (like FileDataElement)
     * @param outputClass transformer output class (like FileTableDataCollection)
     * @return list of class implementing Transformer interface which is able to transform from input to output class or null if no such transformer found.
     * If several transformers applicable, only the first found is returned.
     */
    @SuppressWarnings ( "rawtypes" )
    public static @Nonnull List<Class<? extends Transformer>> getTransformerClass(Class<? extends DataElement> inputClass,
            Class<? extends DataElement> outputClass)
    {
        return instance.stream().filter( info -> info.inputClass.equals( inputClass ) && info.outputClass.isAssignableFrom( outputClass ) )
                .map( TransformerInfo::getTransformerClass ).collect( Collectors.toList() );
    }
    
    @SuppressWarnings ( "rawtypes" )
    public static @Nonnull List<Class<? extends Transformer>> getTransformerProbableClass(Class<? extends DataElement> inputClass, Class<? extends DataElement> outputClass)
    {
        return instance.stream()
                .filter( info -> info.inputClass.equals( inputClass ) && (info.outputClass.isAssignableFrom( outputClass ) || (outputClass.isAssignableFrom( info.outputClass ))) )
                .map( TransformerInfo::getTransformerClass ).collect( Collectors.toList() );
    }

    @SuppressWarnings("rawtypes")
    public static Transformer getBestTransformer(DataElement output, Class<? extends DataElement> inputType) throws Exception
    {
        return StreamEx.of(TransformerRegistry.getTransformerClass(inputType, output.getClass()))
                .map( Clazz.of( Transformer.class )::create )
                .max( Comparator.comparingInt( Clazz.of(PriorityTransformer.class).toInt( transformer ->
                        transformer.getInputPriority( inputType, output ), 1 ) ) ).orElse( null );
    }
    
    @SuppressWarnings ( {"rawtypes", "unchecked"} )
    public static Transformer getTransformer(DataCollection primary, DataCollection derived, Class<? extends DataElement> inputClass,
            Class<? extends DataElement> outputClass) throws Exception
    {
        List<Class<? extends Transformer>> cl = getTransformerClass(inputClass, outputClass);
        if( !cl.isEmpty() )
        {
            Transformer transformer = cl.get(0).newInstance();
            transformer.init(primary, derived);
            return transformer;
        }
        return null;
    }
    
    public static List<Class<? extends DataElement>> getSupportedOutputClasses(Class<? extends DataElement> inputClass)
    {
        return instance.stream().filter( info -> info.inputClass == inputClass ).map( TransformerInfo::getOutputClass ).distinct()
                .collect( Collectors.toList() );
    }

    public static StreamEx<String> getSupportedTransformers(Class<? extends DataElement> inputClass)
    {
        return instance.stream().filter( info -> info.inputClass == inputClass ).map( TransformerInfo::getName );
    }
    
    /**
     * @param name name previously returned by getSupportedTransformers
     */
    public static TransformerInfo getTransformerInfo(String name)
    {
        return instance.getTransformerInternal(name);
    }
    
    private TransformerInfo getTransformerInternal(String name)
    {
        return getExtension(name);
    }

    public static TransformerInfo getTransformerInfo(Class<? extends Transformer> clazz)
    {
        return instance.stream().findAny( info -> info.transformerClass == clazz ).orElse( null );
    }
    
    public static TransformerInfo detectFileTransformer(String fileName)
    {
        TransformerInfo result = null;
        int bestPriority = 0;
        for(TransformerInfo ti : instance)
            if(ti.inputClass == FileDataElement.class && PriorityTransformer.class.isAssignableFrom( ti.getTransformerClass() ))
            {
                PriorityTransformer t;
                try
                {
                    t = (PriorityTransformer)ti.transformerClass.newInstance();
                }
                catch( InstantiationException | IllegalAccessException e )
                {
                    continue;
                }
                int priority = t.getOutputPriority( fileName );
                if(priority > bestPriority)
                {
                    result =ti;
                    bestPriority = priority;
                }
            }
        return result;
    }

    //    @Override
    //    protected TransformerInfo loadElement(IConfigurationElement element, String elementName) throws Exception
    //    {
    //        Class<? extends DataElement> inputClass = getClassAttribute( element, INPUT_CLASS, DataElement.class );
    //        Class<? extends DataElement> outputClass = getClassAttribute( element, OUTPUT_CLASS, DataElement.class );
    //        @SuppressWarnings ( "rawtypes" )
    //        Class<? extends Transformer> transformerClass = getClassAttribute( element, TRANSFORMER_CLASS, Transformer.class );
    //        return new TransformerInfo(elementName, transformerClass, inputClass, outputClass);
    //    }


    public static void addTransformer(TransformerInfo ti)
    {
        instance.init();
        instance.addElementInternal(ti.getName(), ti);
    }

    public static void addTransformer(String name, Class<? extends Transformer> transformerClass, Class<? extends DataElement> inputClass, Class<? extends DataElement> outputClass)
    {
        addTransformer(new TransformerInfo(name, transformerClass, inputClass, outputClass));
    }

    public static void addTransformer(String name, String transformerClassName, String inputClassName, String outputClassName)
    {
        addTransformer(new TransformerInfo(name, getClass(transformerClassName, Transformer.class), getClass(inputClassName, DataElement.class),
                getClass(outputClassName, DataElement.class)));
    }

    @Override protected TransformerInfo registerElement(String elementName, String className, Object... args) throws Exception
    {
        return null;
    }

    @Override public void addElement(String elementName, String className)
    {

    }
}
