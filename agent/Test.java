import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException {
	Foo foo = new Foo();
	Object a = foo.arrMod(new Object[] {1 ,2});
	Class klass = java.lang.Class.forName("Foo");
	foo.testCast((Object) args);

    }
}




class Foo {
    Bar bar;
    public Foo ()throws ClassNotFoundException {
	// this.bar = new Bar();
	Bar temp = new Bar();
	temp.barMet();
    }
    
    public Object arrMod(Object[] arr) {
	int b = arr.length;
	return arr;
    }
    public void testCast(Object obj) {

    }

class Bar {
    public Bar() {

	java.lang.Class<?> klass;
    }
    public void barMet() throws ClassNotFoundException {
	MyClassLoader my = new MyClassLoader();
	Class klass2 = java.lang.Class.forName("Foo", true, my);
	int a = 45;
    }



}
}


class MyClassLoader extends ClassLoader{
    public MyClassLoader () {
	super();
	System.out.println("yoyo");
    }

    public MyClassLoader(ClassLoader parent) {
	super(parent);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException{
	System.out.println(name);
	return super.findClass(name);
    }
}






class Yolo {
    Zolo zolo = new Zolo();
    public void alloc() {
	
    }
}

class Zolo {

}
