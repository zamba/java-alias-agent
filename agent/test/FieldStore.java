package test;
public class FieldStore {
    static Banan banan = new Banan(); 
    public static void main(String [] args) {
	banan = new Banan();
	Apple apple = new Apple();
	apple.store();
    }
}

class Apple{
    Apple ko;
    Banan bananfromstatic;
    Banan banan = new Banan();
    void store() {
	this.ko=this;
	bananfromstatic = FieldStore.banan;
    }
}


class Banan {
    
}
