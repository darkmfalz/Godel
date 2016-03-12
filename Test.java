import java.util.ArrayDeque;

public class Test {

	public static void main(String[] args){
		
		/*ArrayDeque<String> test = LogicConverter.shuntingYard(new LogicTokenizer("p|p|q"));
		test = LogicConverter.convertCNF(test.clone());
		LogicConverter.convertInfix(test.clone());
		while(!test.isEmpty())
			System.out.print(test.pollFirst() + " ");*/
		
		Godel godel = new Godel(false);
		/*godel.tell("Amy<=>(Amy&Cal)");
		godel.tell("Bob<=>~Cal");
		godel.tell("Cal<=>(Bob|~Amy)");
		if(!godel.ask("Amy"))
			godel.ask("~Amy");
		if(!godel.ask("Bob"))
			godel.ask("~Bob");
		if(!godel.ask("Cal"))
		 	godel.ask("~Cal");*/
		
		/*godel.tell("Amy<=>~Cal");
		godel.tell("Bob<=>Amy&Cal");
		godel.tell("Cal<=>Bob");
		if(!godel.ask("Amy"))
			godel.ask("~Amy");
		if(!godel.ask("Bob"))
			godel.ask("~Bob");
		if(!godel.ask("Cal"))
		 	godel.ask("~Cal");*/
		
		//Unicorns
		/*godel.tell("Mythical=>~Mortal");
		godel.tell("~Mythical=>Mortal&Mammal");
		godel.tell("(~Mortal|Mammal)=>Horned");
		godel.tell("Horned=>Magical");
		godel.ask("Mythical");
		godel.ask("Magical");
		godel.ask("Horned");*/
		
		/*godel.tell("X|Y|Z|W");
		godel.tell("A<=>X");
		godel.tell("C=>A&(B|C|D|E|F|G|H)");
		godel.tell("G=>(C=>(A|B|D|E|F|G|H|~A|~B|~D|~E|~F|~G|~H|X|Y|Z|W|~X|~Y|~Z|~W))");
		godel.tell("H<=>(G&H=>A)");
		godel.ask("X");
		godel.ask("Y");
		godel.ask("Z");
		godel.ask("W");*/
		
		/*godel.tell("~P1,1");
		godel.tell("B1,1 <=> (P1,2 | P2,1)");
		godel.tell("B2,1 <=> (P1,1 | P2,2 | P3,1)");
		godel.tell("~B1,1");
		godel.tell("B2,1");
		godel.ask("~P1,2");*/
		
		godel.tell("X|Y|Z|W");
		godel.tell("A<=>X");
		godel.tell("B<=>Y|Z");
		godel.tell("C<=>A&B");
		godel.tell("D<=>X&Y");
		godel.tell("E<=>X&Z");
		godel.tell("F<=>((D|E)&~(D&E))");
		godel.tell("G<=>(C=>F)");
		godel.tell("H<=>((G&H)=>A)");
		
		if(!godel.ask("X"))
			godel.ask("~X");
		if(!godel.ask("Y"))
			godel.ask("~Y");
		if(!godel.ask("Z"))
			godel.ask("~Z");
		if(!godel.ask("W"))
			godel.ask("~W");
		
	}
	
}
