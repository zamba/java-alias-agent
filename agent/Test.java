import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;
import java.lang.reflect.Method;



public class Test {

    public static void main(String[] args) throws Exception{
	Foo foo = new Foo();
	foo.met();
    }

}




class Foo {
    private Object lock = new Object();
    private int a = 34;
    public void met() {
	synchronized (lock) {
	    if (a < 353)
	    	System.out.println(a);
	}
    }

}


class Zolo {
    String a = "zolo";
    public void print(Object obj) {
	System.out.println("zolo");
    }
}


class Yolo {
    Zolo zolo = new Zolo();
    public void alloc() {
	
    }
}
