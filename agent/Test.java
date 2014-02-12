import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;

public class Test {
    public static void main(String[] args) {
	// float fop = 54.0f;
	// float fop2 = 43.43f;
	Foo foo = new Foo();
	foo.td();
    }

}




class Foo {
    String lol;
    Inner inner;
    Float floatobj;
    float floatprim;
    public Foo () {
	
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
