import java.util.LinkedList;

public class Rocket {
    public static void main(String [] args) {
	Object obj = new Object();
	Foo foo = new Foo();
	foo.apa(obj);
	zoo(obj);
    }

    public static void zoo(Object obj) {
	obj = new Object();
    }

}



class Foo {
    int a = 34;
    public void apa(Object obj) {
	obj = new Object();
	Object a = new Object();
	
    }
}
