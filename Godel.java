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
		
		if(checkTruthTable(fact)){
		
			LogicTokenizer factTokenizer = new LogicTokenizer(fact);
			knowledgeBase.add(LogicConverter.shuntingYard(factTokenizer));
			//Add any new tokens to the list of literals
			factTokenizer = new LogicTokenizer(fact);
			while(factTokenizer.nextToken() != TokenType.EOL && factTokenizer.getTType() != null)
				if(factTokenizer.getTType() == TokenType.LITERAL && !literals.containsKey(factTokenizer.getSVal()))
					literals.put(factTokenizer.getSVal().intern(), 0.0);
		
		}
		
	}
	
	public boolean askTruthTable(String fact){
		
		LogicTokenizer factTokenizer = new LogicTokenizer(fact);
		boolean value = truthTableEntails(LogicConverter.shuntingYard(factTokenizer), new LogicTokenizer(fact), fact);
		if(value == checkTruthTable(fact))
			System.out.println(fact + " is " + (value?"valid":"unsatisfiable") + ".");
		else
			System.out.println(fact + " is satisfiable but invalid.");
		return value;
		
	}
	
	private boolean truthTableEntails(ArrayDeque<String> fact, LogicTokenizer factTokenizer, String factString){
		
		//Add all symbols in the KnowledgeBase and the queried fact
		HashMap<String, Double> symbols = new HashMap<String, Double>();
		symbols.putAll(literals);
		while(factTokenizer.nextToken() != TokenType.EOL && factTokenizer.getTType() != null)
			if(factTokenizer.getTType() == TokenType.LITERAL && !symbols.containsKey(factTokenizer.getSVal()))
				symbols.put(factTokenizer.getSVal().intern(), 0.0);
		
		HashMap<String, Double> model = new HashMap<String, Double>();
		
		return truthTableCheckAll(fact, symbols, model, factString);
		
	}
	
	private boolean truthTableCheckAll(ArrayDeque<String> fact, HashMap<String, Double> symbols, HashMap<String, Double> model, String factString){
		
		if(symbols.isEmpty()){
			if(propLogicKnowledgeBase(model)){
				
				String[] keys = model.keySet().toArray(new String[0]);
				//Print Truth-Table
				for(int i = 0; i < keys.length; i++)
					System.out.print(keys[i] + ": " + (model.get(keys[i]) > 0.0 ? true + " " : false) + "\t");
				boolean value = propLogicTrue(fact.clone(), model);
				System.out.println(factString + ": " + (value ? true + " ": false));
				return value;
				
			}
			else{
				
				//String[] keys = model.keySet().toArray(new String[0]);
				//Print Truth-Table
				//for(int i = 0; i < keys.length; i++)
				//	System.out.print(keys[i] + ": " + (model.get(keys[i]) > 0.0 ? true + " " : false) + "\t");
				//boolean value = propLogicTrue(fact.clone(), model);
				//System.out.println(factString + ": " + (value ? true + " ": false) + " [INVALID MODEL]");
				return true;
			}
		}
		else{
			
			String p = symbols.keySet().toArray(new String[0])[0];
			HashMap<String, Double> rest = new HashMap<String, Double>();
			rest.putAll(symbols);
			rest.remove(p);
			
			boolean first = truthTableCheckAll(fact, rest, extend(p, true, model), factString);
			boolean second = truthTableCheckAll(fact, rest, extend(p, false, model), factString);
			
			return (first&&second);
			
		}
		
	}
	
	public boolean checkTruthTable(String fact){
		
		LogicTokenizer factTokenizer = new LogicTokenizer(fact);
		return truthTableContradicts(LogicConverter.shuntingYard(factTokenizer), new LogicTokenizer(fact));
		
	}
	
	private boolean truthTableContradicts(ArrayDeque<String> fact, LogicTokenizer factTokenizer){
		
		//Add all symbols in the KnowledgeBase and the queried fact
		HashMap<String, Double> symbols = new HashMap<String, Double>();
		symbols.putAll(literals);
		while(factTokenizer.nextToken() != TokenType.EOL && factTokenizer.getTType() != null)
			if(factTokenizer.getTType() == TokenType.LITERAL && !symbols.containsKey(factTokenizer.getSVal()))
				symbols.put(factTokenizer.getSVal().intern(), 0.0);
		
		HashMap<String, Double> model = new HashMap<String, Double>();
		
		return truthTableCheckAll2(fact, symbols, model);
		
	}
	
	private boolean truthTableCheckAll2(ArrayDeque<String> fact, HashMap<String, Double> symbols, HashMap<String, Double> model){
		
		if(symbols.isEmpty()){
			if(propLogicKnowledgeBase(model))
				return propLogicTrue(fact.clone(), model);
			else
				return false;
		}
		else{
			
			String p = symbols.keySet().toArray(new String[0])[0];
			HashMap<String, Double> rest = new HashMap<String, Double>();
			rest.putAll(symbols);
			rest.remove(p);
			
			boolean first = truthTableCheckAll2(fact, rest, extend(p, true, model));
			boolean second = truthTableCheckAll2(fact, rest, extend(p, false, model));
			
			return (first||second);
			
		}
		
	}
	
	private boolean propLogicKnowledgeBase(HashMap<String, Double> model){
		
		boolean value = true;
		for(int i = 0; i < knowledgeBase.size(); i++){
			
			if(LogicConverter.evaluate(knowledgeBase.get(i).clone(), model) > 0.0)
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
