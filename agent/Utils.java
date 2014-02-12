/**
 * @author Tobias Wrigstad (tobias.wrigstad@it.uu.se)
 * @date 2014-02-12
 */
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public class Utils {
    private static volatile long counter = 0; 
    private static final NonBlockingHashMap<String,Long> stringToInt = new NonBlockingHashMap<String,Long>();
    public static long stringToInt(final String string) {
	Long shorthand = stringToInt.get(string);
	if (shorthand == null) {
	    final long counter = ++Utils.counter;
	    stringToInt.put(string, counter);
	    return counter;
	}
	return shorthand;
    }
}
