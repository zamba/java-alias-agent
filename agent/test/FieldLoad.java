package test;
public class FieldLoad {
    static Banan banan = new Banan(); 
    public static void main(String [] args) {
	banan = new Banan();
	Apple apple = new Apple();
	apple.store();
	Banan temp = banan;
	innerFieldLoad inner = new innerFieldLoad();
	Object temp2 = inner.obj;
    }
}

class innerFieldLoad {
    Object obj = new Object();
    public innerFieldLoad() {
	Banan temp = FieldLoad.banan;
	Object objtemp = this.obj;
    }
}
