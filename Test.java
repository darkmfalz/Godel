import java.util.ArrayDeque;

public class Test {

	public static void main(String[] args){
		
		ArrayDeque<String> test = LogicConverter.shuntingYard(new LogicTokenizer("(p<=>q)=>(~p&r)"));
		LogicConverter.convertInfix(LogicConverter.convertCNF(test.clone(), null));
		
		/*Godel godel = new Godel();
		godel.tell("X|Y|Z|W");
		godel.tell("A<=>X");
		godel.tell("C<=>A&(B|C|D|E|F|G|H)");
		godel.tell("G<=>(C=>(A|B|D|E|F|G|H|~A|~B|~D|~E|~F|~G|~H|X|Y|Z|W|~X|~Y|~Z|~W))");
		godel.tell("H<=>(G&H=>A)");
		godel.askTruthTable("X");
		godel.askTruthTable("Y");
		godel.askTruthTable("Z");
		godel.askTruthTable("W");*/
		
	}
	
}
