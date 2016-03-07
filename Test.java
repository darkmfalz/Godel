
public class Test {

	public static void main(String[] args){
		
		LogicTokenizer logos = new LogicTokenizer("A<=>B");
	
		while(logos.nextToken() != TokenType.EOL && logos.getTType() != null)
			System.out.print(logos.getSVal() + " ");
		
		return;
		
	}
	
}
