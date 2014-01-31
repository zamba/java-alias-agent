package test;
public class VarStore {
    static Banan banan = new Banan(); 
    public static void main(String [] args) {
	banan = new Banan();
	Apple apple = new Apple();
	apple.store();
	Foo foo = new Foo();
	foo.makeBar();
	int shouldnotberecorded = 2424;
	int [] shouldbe= new int[3];
    }
}
