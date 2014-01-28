import java.io.FileNotFoundException;
import java.util.LinkedList;

public class Test {



    public static void main(String[] args) {
	
	

	// list.add(2);
	
	Bar bar = new Bar();
	// bar.alloc();
	bar.alloc();


	// int[] arr = new int[5];

	// Zolo[][] arr2 = new Zolo[3][5];

	// bar.alloc();
	// Zolo temp = bar.zolo;
	// Yolo yolo = new Yolo(new Zolo());

    }
}


class Bar {
    Zolo zolo;
    int [] arr;
    LinkedList<Integer> list = new LinkedList<Integer>();

    public Bar() {
	// this.zolo = new Zolo();
	// this.arr = new int[5];
	list.add(2);
    }
    void alloc() {
	this.arr = new int[5];
	Zolo zolo = new Zolo();
	// int[] arr = new int[5];

	// Zolo[][] arr2 = new Zolo[3][5];
	// Zolo zolo = new Zolo();
	// LinkedList<Integer> list = new LinkedList<Integer>();
    }
}




class Zolo {

}

class Yolo {
    Zolo zolo;
    public Yolo(Zolo zolo) {

    }
}
