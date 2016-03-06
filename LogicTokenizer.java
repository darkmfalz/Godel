import java.util.ArrayDeque;
import java.util.Arrays;

/*
 * InfixCalculator
 *
 * Version 1.0
 * 
 * Copyright Adeeb Sheikh, 2015
 * 
 * Course: CSC172 Spring 2015
 * 
 * Assignment: Project02
 * 
 * Author: Adeeb Sheikh
 * 
 * Lab Session: MW 6:15 - 7:30
 * 
 * Lab TA: Gabriel Morales
 * 
 * Last Revised: 4 March 2015
 */

public class LogicTokenizer{
	
	ArrayDeque<String> tokens;
	ArrayDeque<Integer> tokenTypes;
	String s;
	public String cval;
	public enum TT{
		
		TT_OPERAND, 
		TT_OPERATOR, 
		TT_NEGATION, 
		TT_lPARENTHESIS, 
		TT_rPARENTHESIS,
		TT_NULL;
		
	}
	public TT ttype;
	
	public LogicTokenizer(String s){
		
		this.s = s;
		
		tokens = new ArrayDeque<String>();
		tokenTypes = new ArrayDeque<Integer>();
		
		createTokens();
		
	}
	
	public void createTokens(){
		
		String operands = "0123456789abcdefghijklmnopqrstuvwxyz";
		String operators = "!()&&||-><>|-|=";
		
		String token = "";
		
		boolean isOperand = false;
		boolean isOperator = false;
		
		TT tempTT;
		tempTT = TT_NULL;
		
		TT[] comp1 = {TT_OPERAND, TT_OPERATOR, TT_OPERAND};
		TT[] comp2 = {TT_rPARENTHESIS, TT_CHAR, TT_NUMBER};
		TT[] comp3 = {TT_WORD, TT_CHAR, TT_NUMBER};
		TT[] comp4 = {TT_NUMBER, TT_CHAR, TT_WORD};
		TT[] comp5 = {TT_rPARENTHESIS, TT_CHAR, TT_WORD};
		TT[] comp6 = {TT_WORD, TT_CHAR, TT_WORD};
		TT[] comp7 = {TT_NUMBER, TT_CHAR, TT_lPARENTHESIS};
		TT[] comp8 = {TT_rPARENTHESIS, TT_CHAR, TT_lPARENTHESIS};
		TT[] comp9 = {TT_WORD, TT_CHAR, TT_lPARENTHESIS};
		TT[] cont = {-1, -1, -1};
		
		for(int i = 0; i < s.length(); i++){
			
			char temp = s.charAt(i);
			
			if(operands.contains(Character.toString(temp))){
				
				if(isNumber)
					token = token + temp;
				else{
					
					isWord = false;
					isNumber = true;
					
					if(!token.equals("")){
						
						tokens.enqueue(token);
						
						if(operands.contains(Character.toString(token.charAt(0)))){
							
							tokenTypes.enqueue(TT_NUMBER);
							
							tempTT = TT_NUMBER;
							
						}
						else if(functions.contains(Character.toString(token.charAt(0)))){
							
							tokenTypes.enqueue(TT_WORD);
						
							tempTT = TT_WORD;
							
						}
						else if(token.equals("(")){
						
							tokenTypes.enqueue(TT_lPARENTHESIS);
						
							tempTT = TT_lPARENTHESIS;
							
						}
						else if(token.equals(")")){
							
							tokenTypes.enqueue(TT_rPARENTHESIS);
						
							tempTT = TT_rPARENTHESIS;
							
						}
						else{
						
							tokenTypes.enqueue(TT_CHAR);
						
							tempTT = TT_CHAR;
							
						}
						
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = tempTT;
						
					}
					
					token = Character.toString(temp);
					
					if(isNegative){
						
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = TT_NUMBER;
						
						if(!Arrays.equals(cont, comp1) && !Arrays.equals(cont, comp2) && !Arrays.equals(cont, comp3)){
							
							tokens.enqueue("-1.0");
							tokenTypes.enqueue(TT_NUMBER);
							tokens.enqueue("*");
							tokenTypes.enqueue(TT_CHAR);
							
						}
						else{
							
							tokens.enqueue("-");
							tokenTypes.enqueue(TT_CHAR);
							
						}
						
						isNegative = false;
						
					}
					
				}
			}
			else if(functions.contains(Character.toString(temp))){
					
				if(isWord)
					token = token + temp;
				else{
						
					isNumber = false;
					isWord = true;
						
					if(!token.equals("")){
						
						tokens.enqueue(token);
						
						if(operands.contains(Character.toString(token.charAt(0)))){
							
							tokenTypes.enqueue(TT_NUMBER);
							
							tempTT = TT_NUMBER;
							
						}
						else if(functions.contains(Character.toString(token.charAt(0)))){
							
							tokenTypes.enqueue(TT_WORD);
						
							tempTT = TT_WORD;
							
						}
						else if(token.equals("(")){
						
							tokenTypes.enqueue(TT_lPARENTHESIS);
						
							tempTT = TT_lPARENTHESIS;
							
						}
						else if(token.equals(")")){
						
							tokenTypes.enqueue(TT_rPARENTHESIS);
						
							tempTT = TT_rPARENTHESIS;
							
						}
						else{
						
							tokenTypes.enqueue(TT_CHAR);
						
							tempTT = TT_CHAR;
							
						}
						
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = tempTT;
						
					}
					
					token = Character.toString(temp);
					
					if(isNegative){
						
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = TT_WORD;
						
						if(!Arrays.equals(cont, comp4) && !Arrays.equals(cont, comp5) && !Arrays.equals(cont, comp6)){
							
							tokens.enqueue("-1.0");
							tokenTypes.enqueue(TT_NUMBER);
							tokens.enqueue("*");
							tokenTypes.enqueue(TT_CHAR);
							
						}
						else{
							
							tokens.enqueue("-");
							tokenTypes.enqueue(TT_CHAR);
							
						}
						
						isNegative = false;
						
					}
						
				}
				
			}
			else{
				
				isNumber = false;
				isWord = false;
					
				if(!token.equals("")){
					
					tokens.enqueue(token);
					
					if(operands.contains(Character.toString(token.charAt(0)))){
						
						tokenTypes.enqueue(TT_NUMBER);
						
						tempTT = TT_NUMBER;
						
					}
					else if(functions.contains(Character.toString(token.charAt(0)))){
						
						tokenTypes.enqueue(TT_WORD);
					
						tempTT = TT_WORD;
						
					}
					else if(token.equals("(")){
						
						tokenTypes.enqueue(TT_lPARENTHESIS);
					
						tempTT = TT_lPARENTHESIS;
						
					}
					else if(token.equals(")")){
					
						tokenTypes.enqueue(TT_rPARENTHESIS);
					
						tempTT = TT_rPARENTHESIS;
						
					}
					else{
					
						tokenTypes.enqueue(TT_CHAR);
					
						tempTT = TT_CHAR;
						
					}
					
					cont[0] = cont[1];
					cont[1] = cont[2];
					cont[2] = tempTT;
					
				}
				
				token = Character.toString(temp);
				
				if(token.equals("-")){
					
					if(!isNegative){
					
						isNegative = true;
					
						token = "";
					
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = TT_CHAR;
					
					}
					else{
						
						tokens.enqueue(token);
						tokenTypes.enqueue(TT_CHAR);
						
						token = "";
						
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = TT_CHAR;
						
					}
					
				}
				else if(token.equals("(")){
					
					if(isNegative){
						
						cont[0] = cont[1];
						cont[1] = cont[2];
						cont[2] = TT_lPARENTHESIS;
						
						if(!Arrays.equals(cont, comp7) && !Arrays.equals(cont, comp8) && !Arrays.equals(cont, comp9)){
							
							tokens.enqueue("-1.0");
							tokenTypes.enqueue(TT_NUMBER);
							tokens.enqueue("*");
							tokenTypes.enqueue(TT_CHAR);
							
						}
						else{
							
							tokens.enqueue("-");
							tokenTypes.enqueue(TT_CHAR);
							
						}
						
						isNegative = false;
						
					}
					
				}
				
			}
			
			if(i == s.length() - 1){
				
				if(!token.equals("")){
					
					tokens.enqueue(token);
					
					if(operands.contains(Character.toString(token.charAt(0)))){
						
						tokenTypes.enqueue(TT_NUMBER);
						
						tempTT = TT_NUMBER;
						
					}
					else if(functions.contains(Character.toString(token.charAt(0)))){
						
						tokenTypes.enqueue(TT_WORD);
					
						tempTT = TT_WORD;
						
					}
					else if(token.equals("(")){
						
						tokenTypes.enqueue(TT_lPARENTHESIS);
					
						tempTT = TT_lPARENTHESIS;
						
					}
					else if(token.equals(")")){
					
						tokenTypes.enqueue(TT_rPARENTHESIS);
					
						tempTT = TT_rPARENTHESIS;
						
					}
					else{
					
						tokenTypes.enqueue(TT_CHAR);
					
						tempTT = TT_CHAR;
						
					}
					
					cont[0] = cont[1];
					cont[1] = cont[2];
					cont[2] = tempTT;
					
				}
				
			}
				
		}
		
		token = " ";
		tokens.enqueue(token);
		tokenTypes.enqueue(TT_EOF);
		
	}
	
	public int nextToken(){
		
		cval = tokens.dequeue();
		ttype = tokenTypes.dequeue();
		
		return ttype;
		
	}

}
