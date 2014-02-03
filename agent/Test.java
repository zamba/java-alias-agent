import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;

public class Test {
    public static void main(String[] args) {
	Foo foo = new Foo();
    }
}




class Foo {
    Bar bar;
    public Foo () {
	// this.bar = new Bar();
	Bar temp = new Bar();
    }

class Bar {
    public Bar() {
	
    }
    public void barMet() {
	int a = 45;
    }


}
}






class Yolo {
    Zolo zolo = new Zolo();
    public void alloc() {
	
    }
}

class Zolo {

}
