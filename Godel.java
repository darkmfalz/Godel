import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class Godel {

	ArrayList<ArrayDeque<String>> knowledgeBase;
	HashMap<String, Double> literals;
	double trueValue;
	boolean useTruthTable;
	ArrayDeque<String> proof;
	
	public Godel(){
		
		knowledgeBase = new ArrayList<ArrayDeque<String>>();
		literals = new HashMap<String, Double>();
		trueValue = 1.0;
		useTruthTable = true;
		
	}
	
	public Godel(boolean useTruthTable){

		knowledgeBase = new ArrayList<ArrayDeque<String>>();
		literals = new HashMap<String, Double>();
		trueValue = 1.0;
		this.useTruthTable = useTruthTable;
		
		if(!useTruthTable)
			proof = new ArrayDeque<String>();
	
	}
	
	public void tell(String fact){
		
		if(useTruthTable)
			tellTruthTable(fact);
		else
			tellResolution(fact);
		
	}
	
	public boolean ask(String query){
		
		if(useTruthTable)
			return askTruthTable(query);
		else
			return askResolution(query);
		
	}
	
	public void tellTruthTable(String fact){
		
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
	
	public boolean askTruthTable(String query){
		
		LogicTokenizer queryTokenizer = new LogicTokenizer(query);
		boolean value = truthTableEntails(LogicConverter.shuntingYard(queryTokenizer), new LogicTokenizer(query), query);
		if(value == checkTruthTable(query))
			System.out.println(query + " is " + (value?"valid":"unsatisfiable") + ".");
		else
			System.out.println(query + " is satisfiable but invalid.");
		return value;
		
	}
	
	private boolean truthTableEntails(ArrayDeque<String> query, LogicTokenizer queryTokenizer, String queryString){
		
		//Add all symbols in the KnowledgeBase and the queried fact
		HashMap<String, Double> symbols = new HashMap<String, Double>();
		symbols.putAll(literals);
		while(queryTokenizer.nextToken() != TokenType.EOL && queryTokenizer.getTType() != null)
			if(queryTokenizer.getTType() == TokenType.LITERAL && !symbols.containsKey(queryTokenizer.getSVal()))
				symbols.put(queryTokenizer.getSVal().intern(), 0.0);
		
		HashMap<String, Double> model = new HashMap<String, Double>();
		
		return truthTableCheckAll(query, symbols, model, queryString);
		
	}
	
	private boolean truthTableCheckAll(ArrayDeque<String> query, HashMap<String, Double> symbols, HashMap<String, Double> model, String queryString){
		
		if(symbols.isEmpty()){
			if(propLogicKnowledgeBase(model)){
				
				String[] keys = model.keySet().toArray(new String[0]);
				//Print Truth-Table
				for(int i = 0; i < keys.length; i++)
					System.out.print(keys[i] + ": " + (model.get(keys[i]) > 0.0 ? true + " " : false) + "\t");
				boolean value = propLogicTrue(query.clone(), model);
				System.out.println(queryString + ": " + (value ? true + " ": false));
				return value;
				
			}
			else{
				
				//If you wanna print out invalid entries in the truth table:
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
			
			boolean first = truthTableCheckAll(query, rest, extend(p, true, model), queryString);
			boolean second = truthTableCheckAll(query, rest, extend(p, false, model), queryString);
			
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

	private boolean propLogicTrue(ArrayDeque<String> query, HashMap<String, Double> model){
		
		if(LogicConverter.evaluate(query, model) > 0.0)
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
	
	public void tellResolution(String fact){
		
		if(checkTruthTable(fact)){
			
			LogicTokenizer factTokenizer = new LogicTokenizer(fact);
			ArrayDeque<String> factDeque = LogicConverter.convertCNF(LogicConverter.shuntingYard(factTokenizer));
			System.out.println(fact + " = " + LogicConverter.convertInfix(factDeque.clone()));
			if(factDeque.peekFirst().equals("TrueTrueTrue"))
				return;
			knowledgeBase.add(factDeque);
			//Add any new tokens to the list of literals
			factTokenizer = new LogicTokenizer(fact);
			while(factTokenizer.nextToken() != TokenType.EOL && factTokenizer.getTType() != null)
				if(factTokenizer.getTType() == TokenType.LITERAL && !literals.containsKey(factTokenizer.getSVal()))
					literals.put(factTokenizer.getSVal().intern(), 0.0);
		
		}
		
	}
	
	public boolean askResolution(String query){
		
		//Create the clauses!
		ArrayList<ArrayDeque<String>> clauses = new ArrayList<ArrayDeque<String>>();
		for(int i = 0; i < knowledgeBase.size(); i++){
			
			ArrayDeque<ArrayDeque<String>> kbClauses = LogicConverter.seperateClausesCNF(knowledgeBase.get(i).clone());
			while(!kbClauses.isEmpty())
				clauses.add(kbClauses.pollFirst());
			
		}
		//Add the query
		ArrayDeque<String> queryPositive = LogicConverter.shuntingYard(new LogicTokenizer(query));
		queryPositive.addLast("~");
		ArrayDeque<String> queryNegative = LogicConverter.convertCNF(queryPositive);
		ArrayDeque<ArrayDeque<String>> queryClauses = LogicConverter.seperateClausesCNF(queryNegative);
		while(!queryClauses.isEmpty())
			clauses.add(queryClauses.pollFirst());
		//PROVE STUFF.
		/*ArrayList<ArrayDeque<String>> result = resolutionEntails(clauses);
		
		if(result == null){
			
			System.out.println(query + " does not follow.");
			return false;
			
		}
		
		return true;*/
		
		boolean value = resolutionEntails(clauses);
		if(value){
			
			while(!proof.isEmpty())
				System.out.println(proof.pollFirst());
			System.out.println("Therefore " + query + " follows logically");
			
		}
		else
			System.out.println(query + " does not follow");
		
		proof = new ArrayDeque<String>();
		return value;
		
	}
	
	private boolean resolutionEntails(ArrayList<ArrayDeque<String>> kb){
		
		ArrayList<int[]> parents = new ArrayList<int[]>();
		for(int i = 0; i < kb.size(); i++){
			
			int[] nullParents = {-1, -1};
			parents.add(nullParents);
			
		}
		ArrayList<int[]> newParents = new ArrayList<int[]>();
		ArrayList<ArrayDeque<String>> newClauses = new ArrayList<ArrayDeque<String>>();
		int start = 0;
		
		while(true){
			
			for(int i = 0; i < kb.size() - 1; i++){
				
				ArrayDeque<String> clause1 = kb.get(i);
				for(int j = Math.max(i + 1, start); j < kb.size(); j++){
					
					ArrayDeque<String> clause2 = kb.get(j);
					ArrayList<ArrayDeque<String>> resolvents = LogicConverter.resolve(clause1, clause2);
					
					if(resolvents == null){
					
						String proofLine = "";
						proofLine = proofLine.concat(LogicConverter.convertInfix(clause1.clone()));
						proofLine = proofLine.concat(" and ");
						proofLine = proofLine.concat(LogicConverter.convertInfix(clause2.clone()));
						proofLine = proofLine.concat(" resolve to form a contradiction");
						proof.addFirst(proofLine);
						writeProof(kb, parents, j);
						writeProof(kb, parents, i);
						return true;
					
					}
					else if(!LogicConverter.equals(resolvents.get(0), clause1)){
						
						for(int k = 0; k < resolvents.size(); k++){
							
							if(!LogicConverter.contains(newClauses, resolvents.get(k))){
							
								newClauses.add(resolvents.get(k));
								int[] arrParents = {i, j};
								newParents.add(arrParents);
								
							}
							
						}
						
					}
					
				}
				
			}
			start = kb.size();
			
			boolean containsAll = true;
			for(int i = 0; i < newClauses.size(); i++)
				containsAll = containsAll && LogicConverter.contains(kb, newClauses.get(i));
			
			if(containsAll)
				return false;
			
			for(int i = 0; i < newClauses.size(); i++){
				
				if(!LogicConverter.contains(kb, newClauses.get(i))){
					
					kb.add(newClauses.get(i));
					parents.add(newParents.get(i));
					
				}
				
			}
			
		}
		
	}

	private void writeProof(ArrayList<ArrayDeque<String>> kb, ArrayList<int[]> parents, int i){
		
		String proofLine = "";
		int[] parentsI = parents.get(i);
		if(parentsI[0] == -1){
			
			proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(i).clone()));
			proofLine = proofLine.concat(" is given");
			proof.addFirst(proofLine);
			
		}
		else{
			
			proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(parentsI[0]).clone()));
			proofLine = proofLine.concat(" and ");
			proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(parentsI[1]).clone()));
			proofLine = proofLine.concat(" resolve to form ");
			proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(i).clone()));
			proof.addFirst(proofLine);
			
			writeProof(kb, parents, parentsI[1]);
			writeProof(kb, parents, parentsI[0]);
			
		}
		
	}
	
}
