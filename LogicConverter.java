import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class LogicConverter {

	public static ArrayDeque<String> shuntingYard(LogicTokenizer tokens){
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		ArrayDeque<String> stack = new ArrayDeque<String>(); 
		
		//Precedence of Connectives: ~, &, |, =>, <=>
		while(tokens.nextToken() != TokenType.EOL && tokens.getTType() != null){
			//TODO: ADD ERROR HANDLING
			switch(tokens.getTType()){
				case LITERAL:
					output.addLast(tokens.getSVal().intern());
					break;
				case NEGATION:
					stack.addFirst(tokens.getSVal().intern());
					break;
				case CONJUNCTION:
					while(!stack.isEmpty() && "~&".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst().intern());
					stack.addFirst(tokens.getSVal().intern());
					break;
				case DISJUNCTION:
					while(!stack.isEmpty() && "~&|".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst().intern());
					stack.addFirst(tokens.getSVal().intern());
					break;
				case CONDITION:
					while(!stack.isEmpty() && "~&|".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst().intern());
					stack.addFirst(tokens.getSVal().intern());
					break;
				case BICONDITION:
					while(!stack.isEmpty() && "~&|=><=>".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst().intern());
					stack.addFirst(tokens.getSVal().intern());
					break;
				case lPARENTHESIS:
					stack.addFirst(tokens.getSVal().intern());
					break;
				case rPARENTHESIS:
					while(!stack.peekFirst().equals("("))
						output.addLast(stack.pollFirst().intern());
					if(stack.isEmpty())
						System.err.println("Error, unbalanced parentheses");
					stack.pollFirst();
					break;
				default:
					System.err.println("Error, unspecified shunting yard catastrophe");
					break;
			}
			
		}
		
		while(!stack.isEmpty()){
			
			if(stack.peekFirst().equals("("))
				System.err.println("Error, unbalanced parentheses");
			output.addLast(stack.pollFirst().intern());
			
		}
		
		return output;
		
	}
	
	public static ArrayDeque<String> convertCNFSAT(ArrayDeque<String> postfix, HashMap<String, String> literals){
		
		//Algorithm implemented according to this webpage:
		//https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
		
		if(postfix.size() == 1)
			return postfix;
		
		//Maintain a list of literals
		if(literals == null || literals.size() == 0){
			
			literals = new HashMap<String, String>();
			ArrayDeque<String> temp = postfix.clone();
			while(!temp.isEmpty()){
				
				String next = temp.pollFirst();
				if(!(next.equals("~") || next.equals("&") || next.equals("|") || next.equals("=>") || next.equals("<=>")))
					literals.put(next, next);
				
			}
			
		}
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		ArrayDeque<String> stack = new ArrayDeque<String>();
		ArrayDeque<String> first = new ArrayDeque<String>();
		ArrayDeque<String> second = new ArrayDeque<String>();
		String last = postfix.pollLast();
		switch(last){
			case "&":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("&");
				break;
			case "|":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				//if either argument is just a literal or a literal followed by a not
				if(first.size() <= 2 && second.size() <= 2){
					
					while(!first.isEmpty())
						output.addLast(first.pollFirst());
					while(!second.isEmpty())
						output.addLast(second.pollFirst());
					output.addLast("|");
					
				}
				else if(first.size() <= 2){
					
					//Seperate second into clauses
					ArrayDeque<ArrayDeque<String>> secondClauses = seperateClausesCNF(second.clone());
					
					boolean firstPass = true;
					while(!secondClauses.isEmpty()){
						
						ArrayDeque<String> temp = secondClauses.pollFirst();
						ArrayDeque<String> firstClone = first.clone();
						while(!firstClone.isEmpty())
							output.addLast(firstClone.pollFirst());
						while(!temp.isEmpty())
							output.addLast(temp.pollFirst());
						output.addLast("|");
						if(!firstPass)
							output.addLast("&");
						firstPass = false;
						
					}
					
				}
				else if(second.size() <= 2){
				
					//Seperate second into clauses
					ArrayDeque<ArrayDeque<String>> firstClauses = seperateClausesCNF(first.clone());
					
					boolean firstPass = true;
					while(!firstClauses.isEmpty()){
						ArrayDeque<String> temp = firstClauses.pollFirst();
						ArrayDeque<String> secondClone = second.clone();
						while(!secondClone.isEmpty())
							output.addLast(secondClone.pollFirst());
						while(!temp.isEmpty())
							output.addLast(temp.pollFirst());
						output.addLast("|");
						if(!firstPass)
							output.addLast("&");
						firstPass = false;
						
					}
					
				}
				else{
					
					//Use resolution to simplify
					//First create a dummy variable
					String alphanumeric = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
					String dummy = "";
					Random random = new Random();
					for(int i = 0; i < 10; i++)
						dummy = dummy + Character.toString(alphanumeric.charAt(random.nextInt(alphanumeric.length())));
					while(literals.containsKey(dummy))
						dummy = dummy + Character.toString(alphanumeric.charAt(random.nextInt(alphanumeric.length())));
					literals.put(dummy, dummy);
					//Put them in resolvable form
					first.addLast(dummy);
					first.addLast("|");
					second.addLast(dummy);
					second.addLast("~");
					second.addLast("|");
					//Put them together as (P|Z)&(Q|~Z)
					while(!first.isEmpty())
						output.addLast(first.pollFirst());
					while(!second.isEmpty())
						output.addLast(second.pollFirst());
					output.addLast("&");
					//Convert it
					output = LogicConverter.convertCNFSAT(output.clone(), literals);
					
				}
				break;
			case "~":
				if(postfix.size() == 1){
					
					postfix.addLast("~");
					output = postfix;
					
				}
				//Double Negative
				else if(postfix.peekLast().equals("~")){
					
					postfix.pollLast();
					output = LogicConverter.convertCNFSAT(postfix, literals);
					
				}
				//DeMorgan's Laws
				else{
					
					postfix = LogicConverter.convertCNFSAT(postfix.clone(), literals);
					//get the operator and clauses
					while(postfix.size() > 1){
						
						String next = postfix.pollFirst();
						String p;
						String q;
						switch(next){
							case "~":
								p = stack.pollFirst();
								stack.addFirst("( ~ " + p + " )");
								break;
							case "&":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							case "|":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							case "=>":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							case "<=>":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							default:
								stack.addFirst(next);
						}
						
					}
					second = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
					first = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
					if(postfix.peekLast().equals("&")){
						
						while(!first.isEmpty())
							output.addLast(first.pollFirst());
						output.addLast("~");
						while(!second.isEmpty())
							output.addLast(second.pollFirst());
						output.addLast("~");
						output.addLast("|");
						
					}
					else{
						
						while(!first.isEmpty())
							output.addLast(first.pollFirst());
						output.addLast("~");
						while(!second.isEmpty())
							output.addLast(second.pollFirst());
						output.addLast("~");
						output.addLast("&");
						
					}
					
					output = LogicConverter.convertCNFSAT(output.clone(), literals);
					
				}
				break;
			case "=>":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				output.addLast("~");
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("|");
				output = LogicConverter.convertCNFSAT(output.clone(), literals);
				break;
			case "<=>":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNFSAT(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				ArrayDeque<String> firstClone = first.clone();
				ArrayDeque<String> secondClone = second.clone();
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("&");
				while(!firstClone.isEmpty())
					output.addLast(firstClone.pollFirst());
				output.addLast("~");
				while(!secondClone.isEmpty())
					output.addLast(secondClone.pollFirst());
				output.addLast("~");
				output.addLast("&");
				output.addLast("|");
				output = LogicConverter.convertCNFSAT(output.clone(), literals);
				break;
			default:
				postfix.addLast(last);
				return postfix;
		}
		
		output = simplifyCNF(output.clone());
		return output;
		
	}
	
	public static ArrayDeque<String> convertCNF(ArrayDeque<String> postfix){
		
		//Algorithm implemented according to this webpage:
		//https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
		
		if(postfix.size() == 1)
			return postfix;
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		ArrayDeque<String> stack = new ArrayDeque<String>();
		ArrayDeque<String> first = new ArrayDeque<String>();
		ArrayDeque<String> second = new ArrayDeque<String>();
		String last = postfix.pollLast();
		switch(last){
			case "&":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("&");
				break;
			case "|":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				//if either argument is just a literal or a literal followed by a not
				if(first.size() <= 2 && second.size() <= 2){
					
					while(!first.isEmpty())
						output.addLast(first.pollFirst());
					while(!second.isEmpty())
						output.addLast(second.pollFirst());
					output.addLast("|");
					
				}
				else if(first.size() <= 2){
					
					//Seperate second into clauses
					ArrayDeque<ArrayDeque<String>> secondClauses = seperateClausesCNF(second.clone());
					
					boolean firstPass = true;
					while(!secondClauses.isEmpty()){
						
						ArrayDeque<String> temp = secondClauses.pollFirst();
						ArrayDeque<String> firstClone = first.clone();
						while(!firstClone.isEmpty())
							output.addLast(firstClone.pollFirst());
						while(!temp.isEmpty())
							output.addLast(temp.pollFirst());
						output.addLast("|");
						if(!firstPass)
							output.addLast("&");
						firstPass = false;
						
					}
					
				}
				else if(second.size() <= 2){
				
					//Seperate second into clauses
					ArrayDeque<ArrayDeque<String>> firstClauses = seperateClausesCNF(first.clone());
					
					boolean firstPass = true;
					while(!firstClauses.isEmpty()){
						
						ArrayDeque<String> temp = firstClauses.pollFirst();
						ArrayDeque<String> secondClone = second.clone();
						while(!secondClone.isEmpty())
							output.addLast(secondClone.pollFirst());
						while(!temp.isEmpty())
							output.addLast(temp.pollFirst());
						output.addLast("|");
						if(!firstPass)
							output.addLast("&");
						firstPass = false;
						
					}
					
				}
				else{
					
					//Seperate first into clauses
					ArrayDeque<ArrayDeque<String>> firstClauses = seperateClausesCNF(first.clone());
					
					//Seperate second into clauses
					ArrayDeque<ArrayDeque<String>> secondClauses = seperateClausesCNF(second.clone());
					
					boolean firstPass = true;
					while(!firstClauses.isEmpty()){
						
						first = firstClauses.pollFirst();
						ArrayDeque<ArrayDeque<String>> secondClausesClone = new ArrayDeque<ArrayDeque<String>>();
						ArrayDeque<ArrayDeque<String>> secondClausesClone1 = new ArrayDeque<ArrayDeque<String>>();
						while(!secondClauses.isEmpty()){
							
							ArrayDeque<String> temp = secondClauses.pollFirst();
							secondClausesClone.addLast(temp.clone());
							secondClausesClone1.addLast(temp.clone());
							
						}
						secondClauses = secondClausesClone1;
						
						while(!secondClausesClone.isEmpty()){
							
							ArrayDeque<String> temp = secondClausesClone.pollFirst();
							ArrayDeque<String> firstClone = first.clone();
							while(!firstClone.isEmpty())
								output.addLast(firstClone.pollFirst());
							while(!temp.isEmpty())
								output.addLast(temp.pollFirst());
							output.addLast("|");
							if(!firstPass)
								output.addLast("&");
							firstPass = false;
							
						}
					
					}
					
				}
				break;
			case "~":
				if(postfix.size() == 1){
					
					postfix.addLast("~");
					output = postfix;
					
				}
				//Double Negative
				else if(postfix.peekLast().equals("~")){
					
					postfix.pollLast();
					output = LogicConverter.convertCNF(postfix.clone());
					
				}
				//DeMorgan's Laws
				else{
					
					postfix = LogicConverter.convertCNF(postfix.clone());
					//get the operator and clauses
					while(postfix.size() > 1){
						
						String next = postfix.pollFirst();
						String p;
						String q;
						switch(next){
							case "~":
								p = stack.pollFirst();
								stack.addFirst("( ~ " + p + " )");
								break;
							case "&":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							case "|":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							case "=>":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							case "<=>":
								q = stack.pollFirst();
								p = stack.pollFirst();
								stack.addFirst("( " + p + " " + next + " " + q + " )");
								break;
							default:
								stack.addFirst(next);
						}
						
					}
					second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
					first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
					if(postfix.peekLast().equals("&")){
						
						while(!first.isEmpty())
							output.addLast(first.pollFirst());
						output.addLast("~");
						while(!second.isEmpty())
							output.addLast(second.pollFirst());
						output.addLast("~");
						output.addLast("|");
						
					}
					else{
						
						while(!first.isEmpty())
							output.addLast(first.pollFirst());
						output.addLast("~");
						while(!second.isEmpty())
							output.addLast(second.pollFirst());
						output.addLast("~");
						output.addLast("&");
						
					}
					
					output = LogicConverter.convertCNF(output.clone());
					
				}
				break;
			case "=>":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				output.addLast("~");
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("|");
				output = LogicConverter.convertCNF(output.clone());
				break;
			case "<=>":
				while(!postfix.isEmpty()){
					
					String next = postfix.pollFirst();
					String p;
					String q;
					switch(next){
						case "~":
							p = stack.pollFirst();
							stack.addFirst("( ~ " + p + " )");
							break;
						case "&":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "|":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						case "<=>":
							q = stack.pollFirst();
							p = stack.pollFirst();
							stack.addFirst("( " + p + " " + next + " " + q + " )");
							break;
						default:
							stack.addFirst(next);
					}
					
				}
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())));
				ArrayDeque<String> firstClone = first.clone();
				ArrayDeque<String> secondClone = second.clone();
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("&");
				while(!firstClone.isEmpty())
					output.addLast(firstClone.pollFirst());
				output.addLast("~");
				while(!secondClone.isEmpty())
					output.addLast(secondClone.pollFirst());
				output.addLast("~");
				output.addLast("&");
				output.addLast("|");
				output = LogicConverter.convertCNF(output.clone());
				break;
			default:
				postfix.addLast(last);
				return postfix;
		}
		
		output = simplifyCNF(output.clone());
		return output;
		
	}
	
	public static ArrayDeque<ArrayDeque<String>> seperateClausesCNF(ArrayDeque<String> postfix){
		
		ArrayDeque<ArrayDeque<String>> clauses = new ArrayDeque<ArrayDeque<String>>();
		
		if(!postfix.peekLast().equals("&")){
			
			clauses.addLast(postfix);
			return clauses;
			
		}
		

		ArrayDeque<ArrayDeque<String>> stack = new ArrayDeque<ArrayDeque<String>>();
		while(postfix.size() > 1){
			
			ArrayDeque<String> add = new ArrayDeque<String>();
			ArrayDeque<String> p;
			ArrayDeque<String> q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					p = stack.pollFirst();
					while(!p.isEmpty())
						add.addLast(p.pollFirst());
					add.addLast("~");
					stack.addFirst(add);
					break;
				case "&":
					q = stack.pollFirst();
					p = stack.pollFirst();
					while(!p.isEmpty())
						add.addLast(p.pollFirst());
					while(!q.isEmpty())
						add.addLast(q.pollFirst());
					add.addLast("&");
					stack.addFirst(add);
					break;
				case "|":
					q = stack.pollFirst();
					p = stack.pollFirst();
					while(!p.isEmpty())
						add.addLast(p.pollFirst());
					while(!q.isEmpty())
						add.addLast(q.pollFirst());
					add.addLast("|");
					stack.addFirst(add);
					break;
				default:
					add.addLast(next);
					stack.addFirst(add);
			}
			
		}
		
		ArrayDeque<ArrayDeque<String>> clauses2 = seperateClausesCNF(stack.pollFirst());
		ArrayDeque<ArrayDeque<String>> clauses1 = seperateClausesCNF(stack.pollFirst());
		
		while(!clauses1.isEmpty())
			clauses.addLast(clauses1.pollFirst());
		while(!clauses2.isEmpty())
			clauses.addLast(clauses2.pollFirst());
		
		return clauses;
		
	}
	
	public static ArrayDeque<String> simplifyCNF(ArrayDeque<String> postfix){
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		ArrayDeque<ArrayDeque<String>> cnfClauses = seperateClausesCNF(postfix.clone());
		boolean foundFirstAnd = false;
		
		while(!cnfClauses.isEmpty()){
			
			ArrayDeque<ArrayDeque<String>> disjClauses = seperateClausesDisjunction(cnfClauses.pollFirst().clone());
			ArrayDeque<String> add = new ArrayDeque<String>();
			HashMap<String, String> literals = new HashMap<String, String>();
			boolean foundFirstOr = false;
			disj:
			while(!disjClauses.isEmpty()){
				
				ArrayDeque<String> current = disjClauses.pollFirst();
				//Negated Literal
				if(current.size() == 2){
					
					//If the literal is contained in list of literals already added
					if(literals.containsKey(current.peekFirst() + "~")){
						
						//Skip it
						
					}
					//If the double negation of this literal was already added
					else if(literals.containsKey(current.peekFirst())){
						
						while(!add.isEmpty())
							add.pollFirst();
						add.addLast("TrueTrueTrue");
						break disj;
						
					}
					//The literal is unique
					else{
						
						//Add the literal
						literals.put(current.peekFirst() + "~", current.peekFirst() + "~");
						add.addLast(current.pollFirst());
						//Add the negation
						add.addLast(current.pollFirst());
						//If necessary, add the "or"
						if(foundFirstOr)
							add.addLast("|");
						foundFirstOr = true;
						
					}
					
				}
				else{

					//If the literal is contained in list of literals already added
					if(literals.containsKey(current.peekFirst())){
						
						//Skip it
						
					}
					//If the negation of this literal was already added
					else if(literals.containsKey(current.peekFirst() + "~")){
						
						while(!add.isEmpty())
							add.pollFirst();
						add.addLast("TrueTrueTrue");
						break disj;
						
					}
					//The literal is unique
					else{
						
						//Add the literal
						literals.put(current.peekFirst(), current.peekFirst());
						add.addLast(current.pollFirst());
						//If necessary, add the "or"
						if(foundFirstOr)
							add.addLast("|");
						foundFirstOr = true;
						
					}
					
				}
				
			}
			
			if(!add.peekFirst().equals("TrueTrueTrue")){
				
				while(!add.isEmpty())
					output.addLast(add.pollFirst());
				
				if(foundFirstAnd)
					output.addLast("&");
				foundFirstAnd = true;
				
			}
			
		}
		
		if(output.isEmpty())
			output.addLast("TrueTrueTrue");
		
		return output;
		
	}
	
	public static double evaluate(ArrayDeque<String> fact, HashMap<String, Double> model){
		
		ArrayDeque<Double> stack = new ArrayDeque<Double>();
		while(!fact.isEmpty()){
			
			String next = fact.pollFirst();
			switch(next){
				case "~":
					stack.addFirst(-1.0*stack.pollFirst());
					break;
				case "&":
					stack.addFirst(Math.min(stack.pollFirst(), stack.pollFirst()));
					break;
				case "|":
					stack.addFirst(Math.max(stack.pollFirst(), stack.pollFirst()));
					break;
				case "=>":
					stack.addFirst(Math.max(stack.pollFirst(), -1.0*stack.pollFirst()));
					break;
				case "<=>":
					double q = stack.pollFirst();
					double p = stack.pollFirst();
					stack.addFirst(Math.min(Math.max(p, -1.0*q), Math.max(-1.0*p, q)));
					break;
				default:
					if(next.equals("TrueTrueTrue"))
						stack.addFirst(Double.MAX_VALUE);
					else
						stack.addFirst(model.get(next));
			}
			
		}
		
		return stack.pollFirst();
		
	}
	
	public static ArrayList<ArrayDeque<String>> resolve(ArrayDeque<String> fact1, ArrayDeque<String> fact2){
		
		if(fact1.peekFirst().equals(fact2.peekFirst()) && (fact1.size() <= 2) && (fact2.size() <= 2) && (fact1.size() != fact2.size()))
			return null;
		
		ArrayList<ArrayDeque<String>> output = new ArrayList<ArrayDeque<String>>();
		ArrayDeque<ArrayDeque<String>> fact1Literals = seperateClausesDisjunction(fact1.clone());
		ArrayDeque<ArrayDeque<String>> collisions = new ArrayDeque<ArrayDeque<String>>();
		while(!fact1Literals.isEmpty()){
			
			ArrayDeque<String> fact1Literal = fact1Literals.pollFirst();
			ArrayDeque<ArrayDeque<String>> fact2Literals = seperateClausesDisjunction(fact2.clone());
			while(!fact2Literals.isEmpty()){
				
				ArrayDeque<String> fact2Literal = fact2Literals.pollFirst();
				if(fact1Literal.peekFirst().equals(fact2Literal.peekFirst())){
					
					if(fact1Literal.size() != fact2Literal.size())
						collisions.addLast(fact1Literal);
					
				}
				
			}
			
		}
		
		//NOW RESOLVE!
		while(!collisions.isEmpty()){
			
			ArrayDeque<String> avoid = collisions.pollFirst();
			ArrayDeque<String> literals = new ArrayDeque<String>();
			
			int counter = 0;
			fact1Literals = seperateClausesDisjunction(fact1.clone());
			while(!fact1Literals.isEmpty()){
				
				ArrayDeque<String> fact1Literal = fact1Literals.pollFirst();
				if(!fact1Literal.peekFirst().equals(avoid.peekFirst())){
					
					while(!fact1Literal.isEmpty())
						literals.addLast(fact1Literal.pollFirst());
					
					if(counter > 0)
						literals.addLast("|");
					counter++;
					
				}
				
			}
			
			ArrayDeque<ArrayDeque<String>> fact2Literals = seperateClausesDisjunction(fact2.clone());
			while(!fact2Literals.isEmpty()){
				
				ArrayDeque<String> fact2Literal = fact2Literals.pollFirst();
				if(!fact2Literal.peekFirst().equals(avoid.peekFirst())){
					
					while(!fact2Literal.isEmpty())
						literals.addLast(fact2Literal.pollFirst());
					
					if(counter > 0)
						literals.addLast("|");
					counter++;
					
				}
				
			}
			
			//Eliminate tautologies
			literals = simplifyCNF(literals);
			if(!literals.peekFirst().equals("TrueTrueTrue"))
				output.add(literals);
			
		}
		
		if(output.size() == 0)
			output.add(fact1.clone());
		
		return output;
		
	}
	
	public static ArrayDeque<ArrayDeque<String>> seperateClausesDisjunction(ArrayDeque<String> postfix){
		
		ArrayDeque<ArrayDeque<String>> clauses = new ArrayDeque<ArrayDeque<String>>();
		
		if(!postfix.peekLast().equals("|")){
			
			clauses.addLast(postfix);
			return clauses;
			
		}
		
		ArrayDeque<ArrayDeque<String>> stack = new ArrayDeque<ArrayDeque<String>>();
		while(postfix.size() > 1){
			
			ArrayDeque<String> add = new ArrayDeque<String>();
			ArrayDeque<String> p;
			ArrayDeque<String> q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					p = stack.pollFirst();
					while(!p.isEmpty())
						add.addLast(p.pollFirst());
					add.addLast("~");
					stack.addFirst(add);
					break;
				case "|":
					q = stack.pollFirst();
					p = stack.pollFirst();
					while(!p.isEmpty())
						add.addLast(p.pollFirst());
					while(!q.isEmpty())
						add.addLast(q.pollFirst());
					add.addLast("|");
					stack.addFirst(add);
					break;
				default:
					add.addLast(next);
					stack.addFirst(add);
			}
			
		}
		
		ArrayDeque<ArrayDeque<String>> clauses2 = seperateClausesDisjunction(stack.pollFirst());
		ArrayDeque<ArrayDeque<String>> clauses1 = seperateClausesDisjunction(stack.pollFirst());
		
		while(!clauses1.isEmpty())
			clauses.addLast(clauses1.pollFirst());
		while(!clauses2.isEmpty())
			clauses.addLast(clauses2.pollFirst());
		
		return clauses;
		
	}
	
	public static String convertInfix(ArrayDeque<String> postfix){
		
		ArrayDeque<String> postfixClone = postfix.clone();
		ArrayDeque<String> stack = new ArrayDeque<String>();
		while(!postfixClone.isEmpty()){
			
			String p;
			String q;
			String next = postfixClone.pollFirst();
			switch(next){
				case "~":
					String operand = stack.pollFirst();
					stack.addFirst( next + " " + operand );
					break;
				case "&":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + next + " " + q + " )");
					break;
				case "|":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + next + " " + q + " )");
					break;
				case "=>":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + next + " " + q + " )");
					break;
				case "<=>":
					q = stack.pollFirst();
					p = stack.pollFirst();;
					stack.addFirst("( " + p + " " + next + " " + q + " )");
					break;
				default:
					stack.addFirst(next);
			}
			
		}
		
		String infix = "";
		while(!stack.isEmpty())
			infix = infix + stack.pollFirst();
		
		return infix;
		
	}
	
	public static boolean contains(ArrayList<ArrayDeque<String>> clauses, ArrayDeque<String> query){
		
		boolean value = false;
		
		for(int i = 0; i < clauses.size(); i++){
			
			ArrayDeque<String> temp = clauses.get(i).clone();
			String[] tempArr = temp.toArray(new String[0]);
			String[] queryArr = query.toArray(new String[0]);
			
			value = value || Arrays.equals(tempArr, queryArr);
			
		}
		
		return value;
		
	}
	
	public static boolean equals(ArrayDeque<String> fact1, ArrayDeque<String> fact2){
		
		String[] fact1Arr = fact1.toArray(new String[0]);
		String[] fact2Arr = fact2.toArray(new String[0]);
		
		return Arrays.equals(fact1Arr, fact2Arr);
		
	}
	
}
