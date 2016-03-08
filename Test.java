import java.util.ArrayDeque;
import java.util.HashMap;

public class Test {

	public static void main(String[] args){
		
		Godel godel = new Godel();
		godel.tell("~P1,1");
		godel.tell("B1,1<=>(P1,2|P2,1)");
		godel.tell("B2,1<=>(P1,1|P2,2|P3,1)");
		godel.tell("~B1,1");
		godel.tell("B2,1");
		System.out.println(godel.askTruthTable("P1,2"));
		
	}
	
}
