package test;
public class MethodEnter {
    static Banan banan = new Banan(); 
    public static void main(String [] args) {
	banan = new Banan();
	Apple apple = new Apple();
	apple.store();
	Foo foo = new Foo();
	foo.makeBar();
    }
}
