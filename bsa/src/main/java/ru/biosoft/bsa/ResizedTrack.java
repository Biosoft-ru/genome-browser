package ru.biosoft.bsa;

/**
 * Resize each site on track to the specified value
 */
public class ResizedTrack extends TransformedTrack
{
    private final int enlargeStart;
    private final int enlargeEnd;
    private final String shrinkMode;

    //TODO: copy, copied from ProcessTrackParameters to avoid analysis dependency
    public static final String NO_SHRINK = "No shrink";
    public static final String SHRINK_TO_START = "Shrink to start";
    public static final String SHRINK_TO_CENTER = "Shrink to center";
    public static final String SHRINK_TO_END = "Shrink to end";
    public static final String SHRINK_TO_SUMMIT = "Shrink to summit";

    public ResizedTrack(Track source, String shrinkMode, int enlargeStart, int enlargeEnd)
    {
        super(source);
        this.enlargeStart = enlargeStart;
        this.enlargeEnd = enlargeEnd;
        this.shrinkMode = shrinkMode;
    }
    
    @Override
    protected Site transformSite(Site s)
    {
        int direction = s.getStrand() == StrandType.STRAND_MINUS ? -1 : 1;
        int start = s.getStart();
        int length = s.getLength();
        switch(shrinkMode)
        {
        case SHRINK_TO_START:
            length = 0;
            break;
        case SHRINK_TO_END:
            start += length*direction;
            length = 0;
            SHRINK_TO_CENTER:
            start += length*direction/2;
            length = 0;
            break;
        case SHRINK_TO_SUMMIT:
            Number summit = (Number)s.getProperties().getValue( "summit" );
            if(summit == null)
                summit = length/2;;
            start += direction*summit.intValue();
            length = 0;
            break;
        default:
        }
        start -= direction * enlargeStart;
        length = Math.max(length + enlargeStart + enlargeEnd, 0);
        return new SiteImpl(s.getOrigin(), s.getName(), s.getType(), s.getBasis(), start, length, s.getPrecision(), s.getStrand(), s
                .getOriginalSequence(), s.getComment(), s.getProperties());
    }
}
