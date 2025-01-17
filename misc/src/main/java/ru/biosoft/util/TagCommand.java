package ru.biosoft.util;

public interface TagCommand
{
    void start( String tag );
    void addValue( String value );
    void complete( String tag );
    String getTag();
    String getTaggedValue();
    String getTaggedValue(String value);
}