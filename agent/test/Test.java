import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Vector;
import java.lang.reflect.Method;



public class Test {

    public static void main(String[] args) throws Exception{
	Foo foo = new Foo();
	foo.enen();
    }

}




class Foo {
    public class EnumTest {
	Day day;
    
	public EnumTest(Day day) {
	    this.day = day;
	}
    
	public void tellItLikeItIs() {
	    switch (day) {
            case MONDAY:
                System.out.println("Mondays are bad.");
                break;
                    
            case FRIDAY:
                System.out.println("Fridays are better.");
                break;
                         
            case SATURDAY: case SUNDAY:
                System.out.println("Weekends are best.");
                break;
                        
            default:
                System.out.println("Midweek days are so-so.");
                break;
	    }
	}
    }

    public enum Day {
	SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
	THURSDAY, FRIDAY, SATURDAY 
    }

    public void enen() {
	EnumTest firstDay = new EnumTest(Day.MONDAY);
        firstDay.tellItLikeItIs();
        EnumTest thirdDay = new EnumTest(Day.WEDNESDAY);
        thirdDay.tellItLikeItIs();
    }
	


}


class Zolo {
    String a = "zolo";
    public void print(Object obj) {
	System.out.println("zolo");
    }
}


class Yolo {
    Zolo zolo = new Zolo();
    public void alloc() {
	
    }
}
