import java.util.ArrayDeque;
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
	
	public static ArrayDeque<String> convertCNFdeprecated(ArrayDeque<String> postfix){
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		
		//Remove Bicondition operator and convert to infix
		ArrayDeque<String> stack = new ArrayDeque<String>();
		while(!postfix.isEmpty()){
			
			String p;
			String q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					String operand = stack.pollFirst();
					stack.addFirst("( " + next + " " + operand + " )");
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
					stack.addFirst("( ( " + p + " => " + q + " ) & ( " + q + " => " + p + " ) )");
					break;
				default:
					stack.addFirst(next);
			}
			
		}
		
		String infix = "";
		while(!stack.isEmpty())
			infix = infix + stack.pollFirst();
		System.out.println(infix);
		postfix = LogicConverter.shuntingYard(new LogicTokenizer(infix));
		stack = new ArrayDeque<String>();
		
		//Remove Condition operator
		stack = new ArrayDeque<String>();
		while(!postfix.isEmpty()){
			
			String p;
			String q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					String operand = stack.pollFirst();
					stack.addFirst("( " + next + " " + operand + " )");
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
					stack.addFirst("( ~ " + p + " | " + q + " )");
					break;
				default:
					stack.addFirst(next);
			}
			
		}
		
		infix = "";
		while(!stack.isEmpty())
			infix = infix + stack.pollFirst();
		System.out.println(infix);
		postfix = LogicConverter.shuntingYard(new LogicTokenizer(infix));
		stack = new ArrayDeque<String>();
		
		//DeMorgan's Laws
		boolean converted = false;
		while(!postfix.isEmpty()){
			
			String p;
			String q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					if(!converted){
						String operand = stack.pollFirst();
						stack.addFirst("( " + next + " " + operand + " )");
					}
					break;
				case "&":
					q = stack.pollFirst();
					p = stack.pollFirst();
					if(!postfix.isEmpty() && postfix.peekFirst().equals("~")){
						stack.addFirst("( ( ~ " + p + " ) | ( ~ " + q + " ) )");
						converted = true;
					}
					else
						stack.addFirst("( " + p + " " + next + " " + q +  " )");
					break;
				case "|":
					q = stack.pollFirst();
					p = stack.pollFirst();
					if(!postfix.isEmpty() && postfix.peekFirst().equals("~")){
						stack.addFirst("( ( ~ " + p + " ) & ( ~ " + q + " ) )");
						converted = true;
					}
					else
						stack.addFirst("( " + p + " " + next + " " + q +  " )");
					break;
				default:
					stack.addFirst(next);
			}
			
		}
		
		infix = "";
		while(!stack.isEmpty())
			infix = infix + stack.pollFirst();
		System.out.println(infix);
		postfix = LogicConverter.shuntingYard(new LogicTokenizer(infix));
		stack = new ArrayDeque<String>();
		
		ArrayDeque<String> temp = new ArrayDeque<String>();
		//Remove Double Negation
		while(!postfix.isEmpty()){
			
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					if(!postfix.isEmpty() && postfix.peekFirst().equals("~"))
						postfix.pollFirst();
					else
						temp.addLast(next);
					break;
				default:
					temp.addLast(next);
			}
			
		}
		ArrayDeque<String> temp2 = temp.clone();
		while(!temp2.isEmpty())
			System.out.print(temp2.pollFirst());
		postfix = temp.clone();
		
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
					ArrayDeque<ArrayDeque<String>> secondClauses = new ArrayDeque<ArrayDeque<String>>();
					ArrayDeque<ArrayDeque<String>> stackClauses = new ArrayDeque<ArrayDeque<String>>();
					boolean foundFirst = false;
					
					while(!second.isEmpty()){
						
						ArrayDeque<String> add = new ArrayDeque<>();
						ArrayDeque<String> p;
						ArrayDeque<String> q;
						String next = second.pollFirst();
						switch(next){
							case "~":
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								add.addLast("~");
								stackClauses.addFirst(add);
								break;
							case "&":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								if(!foundFirst)
									secondClauses.addLast(p.clone());
								secondClauses.addLast(q.clone());
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("&");
								stackClauses.addFirst(add);
								foundFirst = true;
								break;
							case "|":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("|");
								stackClauses.addFirst(add);
								if(second.isEmpty())
									secondClauses.addLast(add);
								break;
							default:
								add.addLast(next);
								stackClauses.addFirst(add);
						}
						
					}
					
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
					ArrayDeque<ArrayDeque<String>> firstClauses = new ArrayDeque<ArrayDeque<String>>();
					ArrayDeque<ArrayDeque<String>> stackClauses = new ArrayDeque<ArrayDeque<String>>();
					boolean foundFirst = false;
					
					while(!first.isEmpty()){
						
						ArrayDeque<String> add = new ArrayDeque<>();
						ArrayDeque<String> p;
						ArrayDeque<String> q;
						String next = first.pollFirst();
						switch(next){
							case "~":
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								add.addLast("~");
								stackClauses.addFirst(add);
								break;
							case "&":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								if(!foundFirst)
									firstClauses.addLast(p.clone());
								firstClauses.addLast(q.clone());
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("&");
								stackClauses.addFirst(add);
								foundFirst = true;
								break;
							case "|":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("|");
								stackClauses.addFirst(add);
								if(second.isEmpty())
									firstClauses.addLast(add);
								break;
							default:
								add.addLast(next);
								stackClauses.addFirst(add);
						}
						
					}
					
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
		
		return output;
		
	}
	
	public static ArrayDeque<String> convertCNF(ArrayDeque<String> postfix, HashMap<String, String> literals){
		System.out.println("Input:");
		convertInfix(postfix.clone());
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
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
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
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
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
					ArrayDeque<ArrayDeque<String>> secondClauses = new ArrayDeque<ArrayDeque<String>>();
					ArrayDeque<ArrayDeque<String>> stackClauses = new ArrayDeque<ArrayDeque<String>>();
					boolean foundFirst = false;
					
					while(!second.isEmpty()){
						
						ArrayDeque<String> add = new ArrayDeque<>();
						ArrayDeque<String> p;
						ArrayDeque<String> q;
						String next = second.pollFirst();
						switch(next){
							case "~":
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								add.addLast("~");
								stackClauses.addFirst(add);
								break;
							case "&":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								if(!foundFirst)
									secondClauses.addLast(p.clone());
								secondClauses.addLast(q.clone());
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("&");
								stackClauses.addFirst(add);
								foundFirst = true;
								break;
							case "|":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("|");
								stackClauses.addFirst(add);
								if(second.isEmpty())
									secondClauses.addLast(add);
								break;
							default:
								add.addLast(next);
								stackClauses.addFirst(add);
						}
						
					}
					
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
					ArrayDeque<ArrayDeque<String>> firstClauses = new ArrayDeque<ArrayDeque<String>>();
					ArrayDeque<ArrayDeque<String>> stackClauses = new ArrayDeque<ArrayDeque<String>>();
					boolean foundFirst = false;
					
					while(!first.isEmpty()){
						
						ArrayDeque<String> add = new ArrayDeque<>();
						ArrayDeque<String> p;
						ArrayDeque<String> q;
						String next = first.pollFirst();
						switch(next){
							case "~":
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								add.addLast("~");
								stackClauses.addFirst(add);
								break;
							case "&":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								if(!foundFirst)
									firstClauses.addLast(p.clone());
								firstClauses.addLast(q.clone());
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("&");
								stackClauses.addFirst(add);
								foundFirst = true;
								break;
							case "|":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("|");
								stackClauses.addFirst(add);
								if(second.isEmpty())
									firstClauses.addLast(add);
								break;
							default:
								add.addLast(next);
								stackClauses.addFirst(add);
						}
						
					}
					
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
					ArrayDeque<ArrayDeque<String>> firstClauses = new ArrayDeque<ArrayDeque<String>>();
					ArrayDeque<ArrayDeque<String>> stackClauses = new ArrayDeque<ArrayDeque<String>>();
					boolean foundFirst = false;
					
					while(!first.isEmpty()){
						
						ArrayDeque<String> add = new ArrayDeque<>();
						ArrayDeque<String> p;
						ArrayDeque<String> q;
						String next = first.pollFirst();
						switch(next){
							case "~":
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								add.addLast("~");
								stackClauses.addFirst(add);
								break;
							case "&":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								if(!foundFirst)
									firstClauses.addLast(p.clone());
								firstClauses.addLast(q.clone());
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("&");
								stackClauses.addFirst(add);
								foundFirst = true;
								break;
							case "|":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("|");
								stackClauses.addFirst(add);
								if(second.isEmpty())
									firstClauses.addLast(add);
								break;
							default:
								add.addLast(next);
								stackClauses.addFirst(add);
						}
						
					}
					
					//Seperate second into clauses
					ArrayDeque<ArrayDeque<String>> secondClauses = new ArrayDeque<ArrayDeque<String>>();
					stackClauses = new ArrayDeque<ArrayDeque<String>>();
					foundFirst = false;
					
					while(!second.isEmpty()){
						
						ArrayDeque<String> add = new ArrayDeque<>();
						ArrayDeque<String> p;
						ArrayDeque<String> q;
						String next = second.pollFirst();
						switch(next){
							case "~":
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								add.addLast("~");
								stackClauses.addFirst(add);
								break;
							case "&":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								if(!foundFirst)
									secondClauses.addLast(p.clone());
								secondClauses.addLast(q.clone());
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("&");
								stackClauses.addFirst(add);
								foundFirst = true;
								break;
							case "|":
								q = stackClauses.pollFirst();
								p = stackClauses.pollFirst();
								while(!p.isEmpty())
									add.addLast(p.pollFirst());
								while(!q.isEmpty())
									add.addLast(q.pollFirst());
								add.addLast("|");
								stackClauses.addFirst(add);
								if(second.isEmpty())
									secondClauses.addLast(add);
								break;
							default:
								add.addLast(next);
								stackClauses.addFirst(add);
						}
						
					}
					
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
					output = LogicConverter.convertCNF(postfix, literals);
					
				}
				//DeMorgan's Laws
				else{
					
					postfix = LogicConverter.convertCNF(postfix.clone(), literals);
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
					second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
					first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
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
					
					output = LogicConverter.convertCNF(output.clone(), literals);
					
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
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				while(!first.isEmpty())
					output.addLast(first.pollFirst());
				output.addLast("~");
				while(!second.isEmpty())
					output.addLast(second.pollFirst());
				output.addLast("|");
				output = LogicConverter.convertCNF(output.clone(), literals);
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
				second = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
				first = LogicConverter.convertCNF(LogicConverter.shuntingYard(new LogicTokenizer(stack.pollFirst())), literals);
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
				output = LogicConverter.convertCNF(output.clone(), literals);
				break;
			default:
				postfix.addLast(last);
				return postfix;
		}
		
		System.out.println("Output:");
		convertInfix(output.clone());
		
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
					stack.addFirst(model.get(next));
			}
			
		}
		
		return stack.pollFirst();
		
	}
	
	public static ArrayDeque<String> convertInfix(ArrayDeque<String> postfix){
		
		ArrayDeque<String> stack = new ArrayDeque<String>();
		while(!postfix.isEmpty()){
			
			String p;
			String q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					String operand = stack.pollFirst();
					stack.addFirst("( " + next + " " + operand + " )");
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
		System.out.println(infix);
		LogicTokenizer tokens = new LogicTokenizer(infix);
		ArrayDeque<String> output = new ArrayDeque<String>();
		while(tokens.nextToken() != TokenType.EOL && tokens.getTType() != null)
			output.add(tokens.getSVal());

		return output;
		
	}
	
}
