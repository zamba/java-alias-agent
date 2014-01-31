package test;
public class MethodExit {
    static Banan banan = new Banan(); 
    public static void main(String [] args) {
	banan = new Banan();
	Apple apple = new Apple();
	apple.store();
	Foo foo = new Foo();
	foo.makeBar();
    }
}
