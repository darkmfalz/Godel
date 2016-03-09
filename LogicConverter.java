import java.util.ArrayDeque;
import java.util.HashMap;

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
	
	public static ArrayDeque<String> convertCNF(ArrayDeque<String> postfix){
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		ArrayDeque<String> temp = new ArrayDeque<String>();
		//Fix Double Negation
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
		System.out.println();
		postfix = temp.clone();
		temp = new ArrayDeque<String>();
		//DeMorgan's Laws
		ArrayDeque<String> stack = new ArrayDeque<String>();
		while(!postfix.isEmpty()){
			
			String p;
			String q;
			String next = postfix.pollFirst();
			switch(next){
				case "~":
					String operand = stack.pollFirst();
					//if(operand.charAt(0) == '('){
						
						
						
					//}
					//else
					stack.addFirst("( " + operand + " " + next + " )");
					break;
				case "&":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + q + " " + next + " )");
					break;
				case "|":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + q + " " + next + " )");
					break;
				case "=>":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + q + " " + next + " )");
					break;
				case "<=>":
					q = stack.pollFirst();
					p = stack.pollFirst();
					stack.addFirst("( " + p + " " + q + " " + next + " )");
					break;
				default:
					stack.addFirst(next);
			}
			
		}
		
		while(!stack.isEmpty())
			System.out.print(stack.pollFirst());
		System.out.println();
		
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
	
}
