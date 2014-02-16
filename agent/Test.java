import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;
import java.lang.reflect.Method;

public class Test {
    static int cool = 45;

    public static void main(String[] args) throws Exception{
	// float fop = 54.0f;
	// float fop2 = 43.43f;
	cool++;
	Foo foo = new Foo();
	foo.td();
	try {
	    int a = 34;
	    String[] argz = { "one", "two" };
	    foo.lok(new Object[] {argz});
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

}




class Foo {
    String lol;
    Inner inner;
    Float floatobj;
    float floatprim;
    Object [] ok;
    private final Method met = null;
    public Foo () throws Exception {
	Test.cool++;
	Class<?> cls = String[].class;
	Object obj = cls;
	cls = Foo.class;
	inner = new Inner() {
		final int xy = 34;
		// final Object obj;
		// {
		//     yolo$a = new Yolo();
		//     obj = new Object();
	
		// }
		public int compareTo(Object obj) {
		    return 1;
		}
	    };
	floatobj = new Float(34.34f);
	floatprim = 12.0f;
	// Object[] objs = new Object[] {"hej".equals("lol") ? 5 : 7};
	// Class<?> clazz = Class.forName("Monkey",true,null);
	// Method print = clazz.getMethod("print",Object.class);
    }

    public void lok(Object[] arr) {
	ok = arr;
    }

    public void td() {
	final int x = 34;
	final Object obj = new Object();
	Thread thread = new Thread(new Runnable() {
		public void run() {
		    synchronized(Foo.this){
			System.out.println("hello byte code" + obj);
		    }
		}
	    });
	Thread thread2 = new Thread(new Runnable() {
		public void run() {
		    synchronized(Foo.this){
			System.out.println("hello byte code");
		    }
		}
	    });
	thread.start();
	thread2.start();
    }

    public void print() {
	System.out.println("calledFromInner");
    }
	


}

    class Inner implements Comparable{
	float float$a;
	Yolo yolo$a;
	float [] float$arr;
	public Inner() {
	    float$a = 45.45f;
	    yolo$a = new Yolo();
	    float$arr = new float[10];
	    // float$arr[2] = float$a;
	}
	public int compareTo(Object obj) {
	    return 0;
	}
    }

class Yolo {
    Zolo zolo = new Zolo();
    public void alloc() {
	
    }
}


class Zolo {
    String a = "zolo";
    public void print(Object obj) {
	System.out.println("zolo");
    }
}
