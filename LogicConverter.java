import java.util.ArrayDeque;

public class LogicConverter {

	public static ArrayDeque<String> shuntingYard(LogicTokenizer tokens){
		
		ArrayDeque<String> output = new ArrayDeque<String>();
		ArrayDeque<String> stack = new ArrayDeque<String>(); 
		
		//Precedence of Connectives: ~, &, |, =>, <=>
		while(tokens.nextToken() != TokenType.EOL && tokens.getTType() != null){
			//TODO: ADD ERROR HANDLING
			switch(tokens.getTType()){
				case LITERAL:
					output.addLast(tokens.getSVal());
					break;
				case NEGATION:
					stack.addFirst(tokens.getSVal());
					break;
				case CONJUNCTION:
					while(!stack.isEmpty() && "~&".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst());
					stack.addFirst(tokens.getSVal());
					break;
				case DISJUNCTION:
					while(!stack.isEmpty() && "~&|".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst());
					stack.addFirst(tokens.getSVal());
					break;
				case CONDITION:
					while(!stack.isEmpty() && "~&|".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst());
					stack.addFirst(tokens.getSVal());
					break;
				case BICONDITION:
					while(!stack.isEmpty() && "~&|=><=>".contains(stack.peekFirst()))
						output.addLast(stack.pollFirst());
					stack.addFirst(tokens.getSVal());
					break;
				case lPARENTHESIS:
					stack.addFirst(tokens.getSVal());
					break;
				case rPARENTHESIS:
					while(!stack.peekFirst().equals("("))
						output.addLast(stack.pollFirst());
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
			output.addLast(stack.pollFirst());
			
		}
		
		return output;
		
	}
	
}
