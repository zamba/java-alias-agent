/**
 * @author Tobias Wrigstad (tobias.wrigstad@it.uu.se)
 * @date 2014-02-12
 */
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public class Utils {
    private static volatile long counter = 0; 
    private static final NonBlockingHashMap<String,Long> stringToLong = new NonBlockingHashMap<String,Long>();
    private static final NonBlockingHashMap<String,Long> stringToString = new NonBlockingHashMap<String,String>();

    public static long stringToLong(final String string) {
	final Long shorthand = stringToLong.get(string);
	if (shorthand == null) {
	    final long counter = ++Utils.counter;
	    stringToLong.put(string, counter);
	    return counter;
	}
	return shorthand;
    }

    public static String stringToString(final String string) {
	String shorthand = stringToLong.get(string);
	if (shorthand == null) {
	    shorthand = "" + ++Utils.counter; // UGLY HACK!!!
	    stringToLong.put(string, shorthand); 
	}
	return shorthand;
    }
}
