import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Godel {

	private boolean error;
	private String errormsg;
	
	public ArrayDeque<String> shuntAlgorithm(String s) throws IOException{
		
		//create the tokenizer
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));
		
		ArrayDeque<String> output = new ArrayDeque<String>(); //creates some useful data structures for the shunting yard algorithm
		ArrayDeque<String> operators = new ArrayDeque<String>();
		
		while(tokenizer.nextToken() != StreamTokenizer.TT_EOF){ //while the end of the stream has not been reached
			
			String token;
			
			switch(tokenizer.ttype){
				case StreamTokenizer.TT_NUMBER: //if the current token is a number
					token = tokenizer.sval;
					output.addLast(token);
					break;
				case StreamTokenizer.TT_WORD: //if it's a "word" -- reserved for sine, cosine, tangent, log, ln, but also gets e and pi
					token = tokenizer.sval;
					if(token.equals("pi") || token.equals("e"))
						output.addLast(token);
					else
						operators.push(token);
					break;
				default: //reserved for the single character operators
					token = tokenizer.sval;
					if(token.equals(")")){ //if it's a right parenthesis
						
						int nergal = 0; //counter variable
						
						makise:
						while(!operators.isEmpty()){
							
							if(operators.peek().equals("(")){ //if the next operator is a left parenthesis
								
								nergal++; //increment the counter
								
								operators.pop(); //remove the left parenthesis
								
								if(!operators.isEmpty()){ //if there are still more operators
									//if the next operator is a function like sin, cos, tan, log, ln...
									if(operators.peek().equals("sin") || operators.peek().equals("cos") || operators.peek().equals("tan") || operators.peek().equals("ln") || operators.peek().equals("log"))
									//adds it to the output -- because then the parentheses were associated with the function
										output.addLast(operators.pop());
								
								}
								//now, break the loop
								break makise;
								
							}
							//just addLast the next operator
							output.addLast(operators.pop());
							
						}
						//if the operators is empty and the counter is zero (so a left parenthesis was never found)...
						if(operators.isEmpty() && nergal == 0){
							//returns an error
							error = true;
							errormsg = errormsg.concat(" <Invalid operator argument, at least one unclosed parenthesis>");
							
						}
						
					}
					else{
						
						if(!token.equals("(")) //if it's not a left parenthesis
							operandArrayDeque(token, output, operators); //for non-parenthetical operators, performs the precedence-popping as specified by the algorithm
						operators.push(token); //then just pushes the current token onto the stack
						
					}
			}
			
		}
		
		while(!operators.isEmpty())
			output.addLast(operators.pop());
		
		return output;
		
	}
	
	public void operandArrayDeque(String s, ArrayDeque<String> output, ArrayDeque<String> operators){
		
		String[] op = {"!", "^", "*/%", "+-", "sincostanlnlog", "<>", "=", "&", "|", "()"}; //contains the operator as ordered by precedence (order of operations)
	
		okarin:
		while(!operators.isEmpty()){ //while there are still operators to be considered
			
			String token = operators.peek(); //get the next token, just for comparison
			
			int cindex = op.length*2; //initializes indices far outside what a valid operator would ever return
			int pindex = op.length*2;
			
			for(int i = 0; i < op.length; i++){
				//checks the index of the input s and of the token against the operators, to find their precedence
				if(op[i].contains(s))
					cindex = i;
				if(op[i].contains(token))
					pindex = i;
				
			}
			
			if(cindex > op.length){ //if the index is too large, whatever was read clearly is not a valid operator
				
				errormsg = errormsg.concat(" <Invalid operator: " + s + ">");
				error = true;
				
			}
			
			if(cindex >= pindex || (cindex > pindex && s.equals("!"))){ //compares precedence
				token = operators.pop(); // if the input has a higher precedence, pop the next token
				output.addLast(token);	//and addLast it to the output
			}
			else{
				break okarin; //otherwise, break the loop, you don't need to addLast any further
			}
			
		}
		
	}
	
	public double reversePolish(ArrayDeque<String> input){
		
		ArrayDeque<String> operands = new ArrayDeque<String>();
		String digits = "0123456789.pie";
		//String[] op = {"!", "^", "*", "/", "%", "+", "-", "<", ">", "=", "&", "|", "sin", "cos", "tan"}; //this is just for reference to operator precedence
		
		while(!input.isEmpty()){ //while there's still something in the input queue
			
			String token = input.pollFirst(); //pollFirst the first token
			
			if(digits.contains(Character.toString(token.charAt(token.length() - 1)))) //if the token is a number, based on the first digit...
				operands.push(token); //pushes it onto the operand stack
			else{
				
				double result; //creates some useful variables
				double first;
				double second;
				
				switch(token){
					case "!":
						
						first = parseArrayDeque(operands); //receives the value from the operand stack
						if((first == 0 || first == 1)) //if the value is 0 or 1
							result = 1.0-first; //creates the result
						else{ //otherwise, returns an error
							
							error = true;
							errormsg = errormsg.concat(" <Invalid input, logical operator ! receives only 0 or 1>");
							
							result = 0;
							
						}
						break;
					case "^":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						result = Math.pow(first, second);
						break;
					case "*":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						result = first*second;
						break;
					case "/":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						result = first/second;
						break;
					case "%":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						result = first % second;
						break;
					case "+":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						result = first + second;
						break;
					case "-":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						result = first - second;
						break;
					case "<":
						second = parseArrayDeque(operands);
						second = Double.parseDouble(round.format(second));
						first = parseArrayDeque(operands);
						first = Double.parseDouble(round.format(first));
						if(first < second)
							result = 1;
						else
							result = 0;
						break;
					case ">":
						second = parseArrayDeque(operands);
						second = Double.parseDouble(round.format(second));
						first = parseArrayDeque(operands);
						first = Double.parseDouble(round.format(first));
						if(first > second)
							result = 1;
						else
							result = 0;
						break;
					case "=":
						second = parseArrayDeque(operands);
						second = Double.parseDouble(round.format(second));
						first = parseArrayDeque(operands);
						first = Double.parseDouble(round.format(first));
						//ROUNDING ERROR
						if(first == second)
							result = 1;
						else
							result = 0;
						break;
					case "&":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						
						if((second == 0 || second == 1) && (first == 0 || first == 1)){
							
							boolean f = (first == 1) ? true:false;
							boolean s = (second == 1) ? true:false;
							boolean r = f && s;
							if(r)
								result = 1;
							else
								result = 0;
							
						}
						else{
							
							error = true;
							errormsg.concat("<Invalid input, logical operator & receives only 0 or 1>");
							
							result = 0;
							
						}
						break;
					case "|":
						second = parseArrayDeque(operands);
						first = parseArrayDeque(operands);
						

						if((second == 0 || second == 1) && (first == 0 || first == 1)){
							
							boolean ft = (first == 1) ? true:false;
							boolean st = (second == 1) ? true:false;
							boolean rt = ft || st;
							if(rt)
								result = 1;
							else
								result = 0;
							
						}
						else{
							
							error = true;
							errormsg.concat("<Invalid input, logical operator | receives only 0 or 1>");
							
							result = 0;
							
						}
						break;
					case "sin":
						first = parseArrayDeque(operands);
						result = Math.sin(first);
						break;
					case "cos":
						first = parseArrayDeque(operands);
						result = Math.cos(first);
						break;
					case "tan":
						first = parseArrayDeque(operands);
						result = Math.tan(first);
						break;
					case "ln":
						first = parseArrayDeque(operands);
						result = Math.log(first);
						break;
					case "log":
						first = parseArrayDeque(operands);
						result = Math.log10(first);
						break;
					default:
						result = 0;
						if(!token.equals("("))
							errormsg = errormsg.concat(" <Invalid operator argument: " + token + ">");
						else
							errormsg = errormsg.concat(" <Invalid operator argument, at least one unclosed parenthesis>");
						error = true;
				}
				
				operands.push(Double.toString(result));
				
			}
			
		}
		
		double result = parseArrayDeque(operands);
		if(operands.isEmpty())
			return result;
		else{
			
			error = true;
			errormsg = errormsg.concat(" <Unexpected Error>");
			
			return 0;
			
		}
		
	}
	
	public double parseArrayDeque(ArrayDeque<String> operands){
		
		double result;
		
		if(!operands.isEmpty() && !error){ //if there are still operands and no errors...
		
			if(operands.peek().equals("pi")) //if the first operand is "pi", result is pi
				result = Math.PI;
			else if(operands.peek().equals("e")) //if it's "e", result is e
				result = Math.E;
			else //otherwise parse the operand on the stack
				result = Double.parseDouble(operands.peek());
			
			operands.pop(); //then remove the operand
		
			return result; //now return it
			
		}
		else{
			
			if(!error){ //otherwise, some error-handling that can be described only as "NEAT!"
			
				errormsg = errormsg.concat(" <Invalid input, not enough operands>");
				error = true;
			
			}
			
			return 0;
			
		}
		
		
	}
	
}
