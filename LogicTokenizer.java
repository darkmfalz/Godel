import java.util.ArrayDeque;

public class LogicTokenizer{

	private String string;
	private ArrayDeque<String> tokens;
	private String sval;
	private TokenType ttype;
	
	public LogicTokenizer(String string){
		
		string = string.replaceAll("\\s+","");
		this.string = string;
		tokens = new ArrayDeque<String>();
		tokenize();
		
	}
	
	private void tokenize(){
		
		String possibleLiteralChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_,.";
		String current = "";
		TokenType currentType = TokenType.EOL;
		TokenType prevType = TokenType.EOL;
		int numParentheses = 0;
		
		for(int i = 0; i < string.length(); i++){
			
			char currentChar = string.charAt(i);
			//Ends a literal
			if(!possibleLiteralChars.contains(Character.toString(currentChar)) && currentType == TokenType.LITERAL){
				
				tokens.addLast(current.intern());
				current = "";
				prevType = TokenType.LITERAL;
				currentType = TokenType.EOL;
				
			}
			boolean error = false;
			
			switch(currentChar){
				case '~':
					if(prevType != TokenType.LITERAL && prevType != TokenType.rPARENTHESIS && current.equals("")){
						
						tokens.addLast(Character.toString(currentChar).intern());
						prevType = TokenType.NEGATION;
						
					}
					else
						error = true;
					break;
				case '&':
				case '|':
					if((prevType == TokenType.LITERAL || prevType == TokenType.rPARENTHESIS) && current.equals("")){
						
						tokens.addLast(Character.toString(currentChar).intern());
						if(currentChar == '&')
							prevType = TokenType.CONJUNCTION;
						else
							prevType = TokenType.DISJUNCTION;
						
					}
					else
						error = true;
					break;
				case '<':
					if(prevType == TokenType.LITERAL || prevType == TokenType.rPARENTHESIS){
						
						if(current.equals("")){
							
							current = current.concat(Character.toString(currentChar));
							currentType = TokenType.BICONDITION;
						
						}
						else
							error = true;
						
					}
					else
						error = true;
					break;
				case '=':
					if(prevType == TokenType.LITERAL || prevType == TokenType.rPARENTHESIS){
						
						if(current.equals("<"))
							current = current.concat(Character.toString(currentChar));
						else if(current.equals("")){
							
							current = current.concat(Character.toString(currentChar));
							currentType = TokenType.CONDITION;
							
						}
						else
							error = true;
						
					}
					else
						error = true;
					break;
				case '>':
					if(prevType == TokenType.LITERAL || prevType == TokenType.rPARENTHESIS){
						
						if(current.equals("<=")){
							
							current = current.concat(Character.toString(currentChar));
							tokens.addLast(current.intern());
							current = "";
							prevType = TokenType.BICONDITION;
							currentType = TokenType.EOL;
							
						}
						else if(current.equals("=")){
							
							current = current.concat(Character.toString(currentChar));
							tokens.addLast(current.intern());
							current = "";
							prevType = TokenType.CONDITION;
							currentType = TokenType.EOL;
							
						}
						else
							error = true;
						
					}
					else
						error = true;
					break;
				case '(':
					if(prevType != TokenType.rPARENTHESIS && prevType != TokenType.LITERAL && current.equals("")){
						
						tokens.addLast(Character.toString(currentChar).intern());
						prevType = TokenType.lPARENTHESIS;
						numParentheses++;
						
					}
					else
						error = true;
					break;
				case ')':
					if((prevType == TokenType.LITERAL || prevType == TokenType.rPARENTHESIS) && current.equals("")){
						
						tokens.addLast(Character.toString(currentChar).intern());
						prevType = TokenType.rPARENTHESIS;
						numParentheses--;
						
					}
					else
						error = true;
					break;
				case ';':
					if((prevType == TokenType.LITERAL || prevType == TokenType.rPARENTHESIS) && current.equals("")){
						
						tokens.addLast(Character.toString(currentChar).intern());
						prevType = TokenType.EOL;
						
					}
					else
						error = true;
					break;
				default:
					if(currentChar != '/'){
						
						current = current.concat(Character.toString(currentChar));
						currentType = TokenType.LITERAL;
						
					}
					else
						error = true;
			}
			
			if(error){
				
				System.err.println("Error at position " + i + ": Illegal character '" + currentChar + "'.");
				tokens = null;
				string = null;
				break;
				
			}
			
		}
		
		if(numParentheses > 0){
			
			System.err.println("Error, too many '('");
			tokens = null;
			string = null;
			
		}
		else if(numParentheses < 0){
			
			System.err.println("Error, too many ')'");
			tokens = null;
			string = null;
			
		}
		
	}

	public TokenType nextToken(){
		
		sval = tokens.pollFirst();
		if(sval == null)
			return null;
		
		switch(sval){
			case "~":
				ttype = TokenType.NEGATION;
			case "&":
				ttype = TokenType.CONJUNCTION;
			case "|":
				ttype = TokenType.DISJUNCTION;
			case "<=>":
				ttype = TokenType.BICONDITION;
			case "=>":
				ttype = TokenType.CONDITION;
			case "(":
				ttype = TokenType.lPARENTHESIS;
			case ")":
				ttype = TokenType.rPARENTHESIS;
			case ";":
				ttype = TokenType.EOL;
			default:
				ttype = TokenType.LITERAL;
		}
		
		return ttype;
		
	}
	
	public TokenType getTType(){
		
		return ttype;
		
	}
	
	public String getSVal(){
		
		return sval;
		
	}
	
}
