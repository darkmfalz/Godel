import java.util.ArrayDeque;

public class Test {

	public static void main(String[] args){
		
		LogicTokenizer logos = new LogicTokenizer("p=>~q<=>~~r");
		ArrayDeque<String> logosQueue = LogicConverter.shuntingYard(logos);
	
		while(!logosQueue.isEmpty())
			System.out.print(logosQueue.pollFirst() + " ");
		
		return;
		
	}
	
}
