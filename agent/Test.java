import java.io.FileNotFoundException;
import java.util.LinkedList;

public class Test {



    public static void main(String[] args) {
	
	LinkedList<Integer> list = new LinkedList<Integer>();

	Bar bar = new Bar();



	int[] arr = new int[5];

	Zolo[][] arr2 = new Zolo[3][5];

	bar.alloc();

    }

}


class Bar {
    void alloc() {
	int[] arr = new int[5];

	Zolo[][] arr2 = new Zolo[3][5];
	Zolo zolo = new Zolo();
	LinkedList<Integer> list = new LinkedList<Integer>();
    }
}




class Zolo {

}
