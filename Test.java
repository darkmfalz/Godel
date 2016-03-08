import java.util.ArrayDeque;
import java.util.HashMap;

public class Test {

	public static void main(String[] args){
		
		Godel godel = new Godel();
		godel.tell("p");
		godel.tell("p=>q");
		godel.askTruthTable("q");
		
	}
	
}
