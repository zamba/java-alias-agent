public class NativeInterface {
    public static native void methodExit(Object returned,
					 String name,
					 String staticcallee,
					 Object callee,
					 int [] arr,
					 Thread thread);

    public static native void methodEnter(String name,
    					  String staticcallee,
    					  Object callee,
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
					 String static_caller,
					 Object caller,
					 Thread thread
					 );

    public static native void loadField(Object objref,
					Object value,
					String owner,
					String name,
					String static_caller,
					Object caller,
					Thread thread
					);


    public static native void storeVar(Object stored,
				       int old,
				       String static_callee,
				       Object callee,
				       Thread thread);

}
