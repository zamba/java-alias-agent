import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;

public class Test {
    public static void main(String[] args) {
	Foo foo = new Foo();

	String[] arg=new String[]{"klass", "lol"};
	// foo.dots(new Object[]{arg});
	foo.dots();
    }

}




class Foo {
    
    public Foo () {
    }

    public Object dots(Object... objs) {
	return null;
    }

}



class Yolo {
    Zolo zolo = new Zolo();
    public void alloc() {
	
    }
}

class Zolo {

}
