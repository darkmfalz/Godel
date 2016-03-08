import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class Godel {

	ArrayList<ArrayDeque<String>> knowledgeBase;
	HashMap<String, Double> literals;
	double trueValue;
	
	public Godel(){
		
		knowledgeBase = new ArrayList<ArrayDeque<String>>();
		literals = new HashMap<String, Double>();
		trueValue = 1.0;
		
	}
	
	public void tell(String fact){
		
		LogicTokenizer factTokenizer = new LogicTokenizer(fact);
		knowledgeBase.add(LogicConverter.shuntingYard(factTokenizer));
		//Add any new tokens to the list of literals
		factTokenizer = new LogicTokenizer(fact);
		while(factTokenizer.nextToken() != TokenType.EOL && factTokenizer.getTType() != null)
			if(factTokenizer.getTType() == TokenType.LITERAL && !literals.containsKey(factTokenizer.getSVal()))
				literals.put(factTokenizer.getSVal().intern(), 0.0);
		
	}
	
	public boolean askTruthTable(String fact){
		
		LogicTokenizer factTokenizer = new LogicTokenizer(fact);
		return truthTableEntails(LogicConverter.shuntingYard(factTokenizer), new LogicTokenizer(fact));
		
	}
	
	private boolean truthTableEntails(ArrayDeque<String> fact, LogicTokenizer factTokenizer){
		
		//Add all symbols in the KnowledgeBase and the queried fact
		HashMap<String, Double> symbols = new HashMap<String, Double>();
		symbols.putAll(literals);
		while(factTokenizer.nextToken() != TokenType.EOL && factTokenizer.getTType() != null)
			if(factTokenizer.getTType() == TokenType.LITERAL && !symbols.containsKey(factTokenizer.getSVal()))
				symbols.put(factTokenizer.getSVal().intern(), 0.0);
		
		HashMap<String, Double> model = new HashMap<String, Double>();
		
		return truthTableCheckAll(fact, symbols, model);
		
	}
	
	private boolean truthTableCheckAll(ArrayDeque<String> fact, HashMap<String, Double> symbols, HashMap<String, Double> model){
		
		if(symbols.isEmpty()){
			if(propLogicKnowledgeBase(model))
				return propLogicTrue(fact, model);
			else
				return true;
		}
		else{
			
			String p = symbols.keySet().toArray(new String[0])[0];
			HashMap<String, Double> rest = new HashMap<String, Double>();
			rest.putAll(symbols);
			rest.remove(p);
			
			boolean first = truthTableCheckAll(fact, rest, extend(p, true, model));
			boolean second = truthTableCheckAll(fact, rest, extend(p, false, model));
			
			return (first&&second);
			
		}
		
	}
	
	private boolean propLogicKnowledgeBase(HashMap<String, Double> model){
		
		boolean value = true;
		for(int i = 0; i < knowledgeBase.size(); i++){
			
			if(LogicConverter.evaluate(knowledgeBase.get(i), model) > 0.0)
				value = (value&&true);
			else
				value = (value&&false);
			
		}
		
		return value;
		
	}

	private boolean propLogicTrue(ArrayDeque<String> fact, HashMap<String, Double> model){
		
		if(LogicConverter.evaluate(fact, model) > 0.0)
			return true;
		else
			return false;
		
	}
	
	private HashMap<String, Double> extend(String p, boolean value, HashMap<String, Double> currentModel){
		
		HashMap<String, Double> model = new HashMap<String, Double>();
		model.putAll(currentModel);
		if(value)
			model.put(p, trueValue);
		else
			model.put(p, -1.0*trueValue);
		
		return model;
		
	}
	
}
