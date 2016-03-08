import java.util.ArrayDeque;
import java.util.HashMap;

public class Test {

	public static void main(String[] args){
		
		LogicTokenizer logos = new LogicTokenizer("p=>q");
		ArrayDeque<String> logosQueue = LogicConverter.shuntingYard(logos);
	
		while(!logosQueue.isEmpty())
			System.out.print(logosQueue.pollFirst() + " ");
		
		HashMap<String, Double> model = new HashMap<String, Double>();
		model.put("p", 1.0);
		model.put("q", 1.0);
		
		System.out.println(LogicConverter.evaluate(LogicConverter.shuntingYard(new LogicTokenizer("p<=>q")), model));
		
		return;
		
	}
	
}
