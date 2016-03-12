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
			knowledgeBase.add(LogicConverter.convertCNF(LogicConverter.shuntingYard(factTokenizer)));
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
			
			ArrayDeque<ArrayDeque<String>> kbClauses = LogicConverter.seperateClausesCNF(knowledgeBase.get(i));
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
		ArrayList<ArrayDeque<String>> result = resolutionEntails(clauses);
		
		if(result == null){
			
			System.out.println(query + " does not follow.");
			return false;
			
		}
		
		while(!proof.isEmpty())
			System.out.println(proof.pollFirst());
		
		proof = new ArrayDeque<String>();
		
		return true;
		
	}
	
	private ArrayList<ArrayDeque<String>> resolutionEntails(ArrayList<ArrayDeque<String>> kb){
		
		for(int i = 0; i < kb.size() - 1; i++){
			
			for(int j = i + 1; j < kb.size(); j++){
				
				ArrayList<ArrayDeque<String>> newClauses = new ArrayList<ArrayDeque<String>>();
				newClauses.add(kb.get(i).clone());
				newClauses.add(kb.get(j).clone());
				ArrayList<ArrayDeque<String>> resolvent = LogicConverter.resolve(kb.get(i).clone(), kb.get(j).clone());
				if(resolvent == null){
					
					String proofLine = "";
					proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(i).clone()));
					proofLine = proofLine.concat(" and ");
					proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(j).clone()));
					proofLine = proofLine.concat(" resolve to form a contradiction");
					proof.addFirst(proofLine);
					return newClauses;
					
				}
				if(!resolvent.get(0).equals(kb.get(i))){
					
					for(int k = 0; k < resolvent.size(); k++){
					
						ArrayList<ArrayDeque<String>> newClausesK = new ArrayList<ArrayDeque<String>>();
						for(int a = 0; a < newClauses.size(); a++)
							newClausesK.add(newClauses.get(k).clone());
						newClausesK.add(resolvent.get(k).clone());
						newClausesK = resolutionEntails(newClausesK, kb);
						if(newClausesK != null){
							
							String proofLine = "";
							proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(i).clone()));
							proofLine = proofLine.concat(" and ");
							proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(j).clone()));
							proofLine = proofLine.concat(" resolve to form ");
							proofLine = proofLine.concat(LogicConverter.convertInfix(resolvent.get(k).clone()));
							proof.addFirst(proofLine);
							return newClausesK;
							
						}
					
					}
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
	private ArrayList<ArrayDeque<String>> resolutionEntails(ArrayList<ArrayDeque<String>> clauses, ArrayList<ArrayDeque<String>> kb){
		
		for(int i = 0; i < clauses.size(); i++){
			
			for(int j = 0; j < kb.size(); j++){
				
				if(!LogicConverter.contains(clauses, kb.get(j).clone())){
				
					ArrayList<ArrayDeque<String>> newClauses = new ArrayList<ArrayDeque<String>>();
					for(int k = 0; k < clauses.size(); k++)
						newClauses.add(clauses.get(k));
					newClauses.add(kb.get(j).clone());
					ArrayList<ArrayDeque<String>> resolvent = LogicConverter.resolve(clauses.get(i).clone(), kb.get(j).clone());
					if(resolvent == null){
						
						String proofLine = "";
						proofLine = proofLine.concat(LogicConverter.convertInfix(clauses.get(i).clone()));
						proofLine = proofLine.concat(" and ");
						proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(j).clone()));
						proofLine = proofLine.concat(" resolve to form a contradiction");
						proof.addFirst(proofLine);
						return newClauses;
						
					}
					if(!resolvent.get(0).equals(clauses.get(i))){
						
						for(int k = 0; k < resolvent.size(); k++){
						
							if(!LogicConverter.contains(newClauses, resolvent.get(k))){
							
								ArrayList<ArrayDeque<String>> newClausesK = new ArrayList<ArrayDeque<String>>();
								for(int a = 0; a < newClauses.size(); a++)
									newClausesK.add(newClauses.get(k).clone());
								newClausesK.add(resolvent.get(k).clone());
								newClausesK = resolutionEntails(newClausesK, kb);
								if(newClausesK != null){
									
									String proofLine = "";
									proofLine = proofLine.concat(LogicConverter.convertInfix(clauses.get(i).clone()));
									proofLine = proofLine.concat(" and ");
									proofLine = proofLine.concat(LogicConverter.convertInfix(kb.get(j).clone()));
									proofLine = proofLine.concat(" resolve to form ");
									proofLine = proofLine.concat(LogicConverter.convertInfix(resolvent.get(k).clone()));
									proof.addFirst(proofLine);
									return newClausesK;
								}
							
							}
								
						}
						
					}
				
				}
				
			}
			
		}
		
		for(int i = 0; i < clauses.size() - 1; i++){
			
			for(int j = i + 1; j < clauses.size(); j++){
				
				ArrayList<ArrayDeque<String>> newClauses = new ArrayList<ArrayDeque<String>>();
				for(int k = 0; k < clauses.size(); k++)
					newClauses.add(clauses.get(k));
				ArrayList<ArrayDeque<String>> resolvent = LogicConverter.resolve(clauses.get(i).clone(), clauses.get(j).clone());
				if(resolvent == null){
					
					String proofLine = "";
					proofLine = proofLine.concat(LogicConverter.convertInfix(clauses.get(i).clone()));
					proofLine = proofLine.concat(" and ");
					proofLine = proofLine.concat(LogicConverter.convertInfix(clauses.get(j).clone()));
					proofLine = proofLine.concat(" resolve to form a contradiction");
					proof.addFirst(proofLine);
					return newClauses;
					
				}
				if(!resolvent.get(0).equals(clauses.get(i))){
					
					for(int k = 0; k < resolvent.size(); k++){
					
						if(!LogicConverter.contains(newClauses, resolvent.get(k))){
						
							ArrayList<ArrayDeque<String>> newClausesK = new ArrayList<ArrayDeque<String>>();
							for(int a = 0; a < newClauses.size(); a++)
								newClausesK.add(newClauses.get(k).clone());
							newClausesK.add(resolvent.get(k));
							newClausesK = resolutionEntails(newClausesK, kb);
							if(newClausesK != null){
								
								String proofLine = "";
								proofLine = proofLine.concat(LogicConverter.convertInfix(clauses.get(i).clone()));
								proofLine = proofLine.concat(" and ");
								proofLine = proofLine.concat(LogicConverter.convertInfix(clauses.get(j).clone()));
								proofLine = proofLine.concat(" resolve to form ");
								proofLine = proofLine.concat(LogicConverter.convertInfix(resolvent.get(k).clone()));
								proof.addFirst(proofLine);
								return newClausesK;
								
							}
						
						}
							
					}
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
}
