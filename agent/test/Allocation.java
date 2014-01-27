package test;
public class Allocation {

    public static void main(String [] args) {
	Foo foo = new Foo();
	foo.makeBar();
	int [] intArr = new int[5];
	Foo [] fooArr = new Foo[6];
	Foo [][] fooMultiArr = new Foo[3][3];
    }

}
