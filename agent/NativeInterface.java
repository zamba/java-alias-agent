import java.util.LinkedList;

public class NativeInterface {
    //not used yet
    public static native void methodExit(Object returned,
					 String name,
					 String desc,
					 String staticcallee,
					 Object callee,
					 Object[] outOfScopes,
					 Thread thread);

    //rdy
    public static native void methodEnter(String name,
    					  String desc,
    					  String staticcallee,
    					  //String staticcaller,
    					  Object callee,
    					  //Object caller,
    					  Object []args,
					  Thread thread);



    public static native void newObj(String desc,
				     Object created,
				     String staticcaller,
				     Object caller,
				     Thread thread);

    public static native void storeField(Object objref,
					 Object value,
					 Object oldvalue,
					 String owner,
					 String name,
					 String desc,
					 String static_caller,
					 Object caller,
					 Thread thread
					 );

    public static native void loadField(Object objref,
					Object value,
					String owner,
					String name,
					String desc,
					String static_caller,
					Object caller,
					Thread thread
					);


    public static native void storeVar(Object stored,
				       Object old_value,
				       String method,
				       String Ddesc,
				       String static_callee,
				       Object callee,
				       Thread thread);

    public static int exists(int var,LinkedList l) {
	// for (Integer i:l) {
	//     if (var == i) {
	// 	return 1;
	//     }
	// }
	return 0;
    }

    public static native void empty();

}
