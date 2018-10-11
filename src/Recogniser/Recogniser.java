/***
 * 	Recogniser.java 
 *    	
 *  LI LI z3447294
 *  
 *  Grammar G  
 * 	Step1 Eliminating left recursion & common prefixes  -----> Grammar G'
 * 	Step2 Constructing First, Follow and Select Sets for G'
 *  Step3 A Recursive-Descent Parser
 ***/

/* At this stage, this parser accepts a subset of VC defined by
 * the following grammar. 
 *
 * You need to modify the supplied parsing methods (if necessary) and 
 * add the missing ones to obtain a parser for the VC language.
 *
 * (17---March---2017)
 *
 *	program       -> func-decl
 *	
 *	// declaration
 *	func-decl     -> void identifier "(" ")" compound-stmt
 *	identifier    -> ID
 *	
 *	// statements 
 *	compound-stmt -> "{" stmt* "}" 
 *	stmt          -> continue-stmt  |  expr-stmt
 *	continue-stmt -> continue ";"
 *	expr-stmt     -> expr? ";"
 *	
 *	// expressions 
 *	expr                -> assignment-expr
 *	assignment-expr     -> additive-expr
 *	additive-expr       -> multiplicative-expr  |  additive-expr "+" multiplicative-expr
 *	multiplicative-expr -> unary-expr           |  multiplicative-expr "*" unary-expr
 *	unary-expr          -> "-" unary-expr       |  primary-expr
 *	primary-expr        -> identifier           |  INTLITERAL                             | "(" expr ")"
 *
 */

package VC.Recogniser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;

import java.util.Arrays;
import java.util.HashSet;

import VC.ErrorReporter;


public class Recogniser {

	private Scanner scanner;
	private ErrorReporter errorReporter;
	private Token currentToken;
	private static HashSet<Integer> typeFirstSet;
	private static HashSet<Integer> exprFirstSet;
	
  	public Recogniser (Scanner lexer, ErrorReporter reporter) {
	    scanner = lexer;
	    errorReporter = reporter;
	    currentToken = scanner.getToken();
	    
	    typeFirstSet = new HashSet<Integer>(Arrays.asList(Token.VOID, Token.BOOLEAN, Token.INT, Token.FLOAT));
		
	    exprFirstSet = new HashSet<Integer>(Arrays.asList(Token.LPAREN, Token.PLUS, Token.MINUS, Token.NOT, Token.ID, 
				Token.INTLITERAL, Token.FLOATLITERAL, Token.BOOLEANLITERAL, Token.STRINGLITERAL));
  	}

	// match checks to see f the current token matches tokenExpected.
	// If so, fetches the next token.
	// If not, reports a syntactic error.
  	void match(int tokenExpected) throws SyntaxError {
  		if (currentToken.kind == tokenExpected) {
  			currentToken = scanner.getToken();
  		} else {
  			syntacticError("\"%\" expected here", Token.spell(tokenExpected));
  		}
  	}

  	// accepts the current token and fetches the next
	void accept() {
	    currentToken = scanner.getToken();
	}

	void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
		SourcePosition pos = currentToken.position;
		errorReporter.reportError(messageTemplate, tokenQuoted, pos);
		throw(new SyntaxError());
	}

  	// ========================== PROGRAMS ========================
	public void parseProgram() {
	    try {
	    	while(currentToken.kind != Token.EOF){
	    		parseCommonPrefixes();
	    		if (currentToken.kind == Token.SEMICOLON) {
					match(Token.SEMICOLON);
	    			continue;
				}
	    		if (currentToken.kind == Token.LPAREN) {
	    			parseRestFuncDecl();
				} else {
					parseRestVardecl();
				}
	    	}
	    	if (currentToken.kind != Token.EOF) {
	    		syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
	    	}
	    } catch (SyntaxError s) {  
	    	;
	    }
	}
  
	/*
	 * A -> ab1 | ab2
	 * A -> r
	 * transformed: 
	 * A -> aA'
	 * a -> r
	 * A' -> b1|b2;
	 */
	private void parseCommonPrefixes() throws SyntaxError{
		parseType();
		parseIdent();
	}
	
	// ========================== DECLARATIONS ========================
	void parseFuncDecl() throws SyntaxError {
		parseType();
		parseIdent();
		parseParaList();
		parseCompoundStmt();
	}
	
	void parseRestFuncDecl() throws SyntaxError {
		parseParaList();
		parseCompoundStmt();
	}	

	void parseVardecl() throws SyntaxError {
		parseType();
		parseInitDeclaratorList();
		match(Token.SEMICOLON);
	}
	
	void parseRestVardecl() throws SyntaxError {
		if(currentToken.kind == Token.LBRACKET) {
			accept();
			if(currentToken.kind == Token.INTLITERAL) {
				parseIntLiteral();
			}
			match(Token.RBRACKET);
		}
		if(currentToken.kind == Token.EQ) {
			accept();
			parseInitialiser();
		}
		while(currentToken.kind == Token.COMMA) {
			accept();
			parseInitDeclarator();
		}
		match(Token.SEMICOLON);
	}	

	void parseInitDeclaratorList() throws SyntaxError {
		parseInitDeclarator();
		while (currentToken.kind == Token.COMMA) {
			match(Token.COMMA);
			parseInitDeclarator();
		}
	}

	void parseInitDeclarator() throws SyntaxError {
		parseDeclarator();
		if (currentToken.kind == Token.EQ) {
			match(Token.EQ);
			parseInitialiser();
		}
	}

	void parseDeclarator() throws SyntaxError {
		parseIdent();
		if (currentToken.kind == Token.LBRACKET) {
			match(Token.LBRACKET);
			if (currentToken.kind == Token.INTLITERAL) {
				parseIntLiteral();
			}
			match(Token.RBRACKET);
		} 
	}

	void parseInitialiser() throws SyntaxError {
		if (currentToken.kind == Token.LCURLY) {
			match(Token.LCURLY);
			parseExpr();
			while (currentToken.kind == Token.COMMA) {
				match(Token.COMMA);
				parseExpr();
			}
			match(Token.RCURLY);
		} else {
			parseExpr();
		}
	}

	//======================== PRIMITIVE TYPES ========================
	void parseType() throws SyntaxError{
		if (typeFirstSet.contains(currentToken.kind)) {
			match(currentToken.kind);
		} else {
			syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
		}
	}
	
	// ======================= IDENTIFIERS ======================
  	// Call parseIdent rather than match(Token.ID). 
  	// In Assignment 3, an Identifier node will be constructed in here.
  	void parseIdent() throws SyntaxError {
  		if (currentToken.kind == Token.ID) {
  			currentToken = scanner.getToken();
  		} else { 
  			syntacticError("identifier expected here", "");
  		}
  	}	
	
	//========================  STATEMENTS  ==============================
	void parseCompoundStmt() throws SyntaxError {
		match(Token.LCURLY);	
		parseStmtList();
		match(Token.RCURLY);
	}

	//Here, a new nontermial has been introduced to define { stmt } *
	void parseStmtList() throws SyntaxError {
		while(currentToken.kind != Token.RCURLY){
			if(!typeFirstSet.contains(currentToken.kind)) {
				break;
			}
			parseVardecl();
		}
		while(currentToken.kind != Token.RCURLY){
			parseStmt();
		}
	}

	void parseStmt() throws SyntaxError {
		switch (currentToken.kind) {
		case Token.LCURLY:
			parseCompoundStmt();
			break;
		case Token.IF:
			parseIfStmt();
			break;
		case Token.FOR:
			parseForStmt();
			break;
		case Token.WHILE:
			parseWhileStmt();
			break;
		case Token.BREAK:
			parseBreakStmt();
			break;
		case Token.CONTINUE:
			parseContinueStmt();
			break;
		case Token.RETURN:
			parseReturnStmt();
			break;
		default:
			parseExprStmt();
			break;
		}
	}

	void parseIfStmt() throws SyntaxError {
		match(Token.IF);
		match(Token.LPAREN);
		parseExpr();
		match(Token.RPAREN);
		parseStmt();
		if (currentToken.kind == Token.ELSE) {
			accept();
			parseStmt();
		}
	}
	
	void parseForStmt() throws SyntaxError {
		match(Token.FOR);
		match(Token.LPAREN);
		if (exprFirstSet.contains(currentToken.kind)) {
			parseExpr();
		}
		match(Token.SEMICOLON);
		if (exprFirstSet.contains(currentToken.kind)) {
			parseExpr();
		}
		match(Token.SEMICOLON);
		if (exprFirstSet.contains(currentToken.kind)) {
			parseExpr();
		}
		match(Token.RPAREN);
		parseStmt();
	}
	
	void parseWhileStmt() throws SyntaxError {
		match(Token.WHILE);
		match(Token.LPAREN);
		parseExpr();
		match(Token.RPAREN);
		parseStmt();
	}
	
	void parseBreakStmt() throws SyntaxError {
		match(Token.BREAK);
		match(Token.SEMICOLON);
	}
	
	void parseContinueStmt() throws SyntaxError {
		match(Token.CONTINUE);
		match(Token.SEMICOLON);
	}
	
	void parseReturnStmt() throws SyntaxError {
		match(Token.RETURN);
		if (exprFirstSet.contains(currentToken.kind)) {
			parseExpr();
		}
		match(Token.SEMICOLON);
	}

  	void parseExprStmt() throws SyntaxError {
  		if (exprFirstSet.contains(currentToken.kind)) {
  			parseExpr();
  			match(Token.SEMICOLON);
		} else {
			match(Token.SEMICOLON);
		}
  	}

  	// ======================= OPERATORS ======================
  	// Call acceptOperator rather than accept(). 
  	// In Assignment 3, an Operator Node will be constructed in here.
  	void acceptOperator() throws SyntaxError {
  		currentToken = scanner.getToken();
  	}

  	// ======================= EXPRESSIONS ======================
  	void parseExpr() throws SyntaxError {
  		parseAssignExpr();
  	}

  	void parseAssignExpr() throws SyntaxError {
  		parseCondOrExpr();
		while(currentToken.kind == Token.EQ) {
			acceptOperator();
			parseCondOrExpr();
		}
  	}
 	
  	/*
  	 * left recursion
  	 * A -> a    		-------|
  	 * 						   |-----> A -> a(b1|b2)* 
  	 * 						   |
  	 * A -> Ab1 | Ab2   -------|
  	 * 						
  	 * cond-or-expr -> cond-and-expr
  	 * 				|  cond-or-expr "||" cond-and-expr
  	 * 
  	 * equal to:
  	 * A            -> a
  	 * cond-or-expr -> cond-and-expr
  	 * A 			-> A            b1
  	 * cond-or-expr -> cond-or-expr "||" cond-and-expr
  	 * 
  	 * A  = cond-or-expr
  	 * a  = cond-and-expr
  	 * b1 = "||" cond-and-expr
  	 * First(b1) = "||"
  	 * parse_a();
  	 * while (currentToken.kind == First(b1)){
  	 * 		accept();
  	 * 		parse_a();
  	 * }
  	 * 
  	 * Most expressions have left-recursion structure, which should be transformed.
  	 */
  	void parseCondOrExpr() throws SyntaxError {
  		parseCondAndExpr();
  		while (currentToken.kind == Token.OROR) {
  			acceptOperator();
  			parseCondAndExpr();
		}
	}

	void parseCondAndExpr() throws SyntaxError {
		parseEqualityExpr();
		while (currentToken.kind == Token.ANDAND){
			acceptOperator();
			parseEqualityExpr();
		}
	}

	void parseEqualityExpr() throws SyntaxError {
		parseRelExpr();
		while (currentToken.kind == Token.EQEQ || currentToken.kind == Token.NOTEQ) {
			acceptOperator();
			parseRelExpr();
		}
	}

	void parseRelExpr() throws SyntaxError {
		parseAdditiveExpr();
		while (currentToken.kind == Token.LT || currentToken.kind == Token.LTEQ ||
				currentToken.kind == Token.GT || currentToken.kind == Token.GTEQ) {
			acceptOperator();
			parseAdditiveExpr();
		}
	}

	void parseAdditiveExpr() throws SyntaxError {
  		parseMultiplicativeExpr();
  		while (currentToken.kind == Token.PLUS || currentToken.kind == Token.MINUS) {
  			acceptOperator();
  			parseMultiplicativeExpr();
  		}
  	}

  	void parseMultiplicativeExpr() throws SyntaxError {
  		parseUnaryExpr();
  		while (currentToken.kind == Token.MULT || currentToken.kind == Token.DIV) {
  			acceptOperator();
  			parseUnaryExpr();
  		}
  	}

	void parseUnaryExpr() throws SyntaxError {
	    switch (currentToken.kind) {
	      case Token.PLUS:
	    	  acceptOperator();
	          parseUnaryExpr();
	          break;
	      case Token.MINUS:
	    	  acceptOperator();
	          parseUnaryExpr();
	          break;
	      case Token.NOT:
	    	  acceptOperator();
	          parseUnaryExpr();
	          break;
	      default:
	    	  parsePrimaryExpr();
	    	  break;
	    }
	}

	void parsePrimaryExpr() throws SyntaxError {
	    switch (currentToken.kind) {
	      	case Token.ID:
	      		parseIdent();
	      		if (currentToken.kind == Token.LPAREN) {
	      			parseArgList();
				} else if (currentToken.kind == Token.LBRACKET) {
					match(Token.LBRACKET);
					parseExpr();
					match(Token.RBRACKET);
				} else {
					;
				}
	      		break;
	      	case Token.LPAREN:
	      		match(Token.LPAREN);
	          	parseExpr();
	          	match(Token.RPAREN);
	          	break;
	      	case Token.INTLITERAL:
	      		parseIntLiteral();
	    	  	break;
	      	case Token.FLOATLITERAL:
	      		parseFloatLiteral();
	    	  	break;
	      	case Token.BOOLEANLITERAL:
	      		parseBooleanLiteral();
	    	  	break;
	      	case Token.STRINGLITERAL:
	      		parseStringLiteral();
	    	  	break;  	
	      	default:
	      		syntacticError("illegal parimary expression", currentToken.spelling);
	      		break;
	    }
	}

	// ========================== LITERALS ========================

  	// Call these methods rather than accept().  In Assignment 3, 
  	// literal AST nodes will be constructed inside these methods. 

	void parseIntLiteral() throws SyntaxError {
		if (currentToken.kind == Token.INTLITERAL) {
		  currentToken = scanner.getToken();
		} else {
		  syntacticError("integer literal expected here", "");
		
		}
	}

	void parseFloatLiteral() throws SyntaxError {
	
		if (currentToken.kind == Token.FLOATLITERAL) {
		  currentToken = scanner.getToken();
		} else {
		  syntacticError("float literal expected here", "");
		}
	}

	void parseBooleanLiteral() throws SyntaxError {
	
	    if (currentToken.kind == Token.BOOLEANLITERAL) {
	    	currentToken = scanner.getToken();
	    } else {
	    	syntacticError("boolean literal expected here", "");
	    }
	}
	
	void parseStringLiteral() throws SyntaxError {
		if (currentToken.kind == Token.STRINGLITERAL) {
			currentToken = scanner.getToken();
		} else {
		    syntacticError("string literal expected here", "");
		}		
	}

  	// ======================== parameters ========================

	void parseParaList() throws SyntaxError {
		match(Token.LPAREN);
		if (typeFirstSet.contains(currentToken.kind)) {
			parseProperParaList();
		}
		match(Token.RPAREN);
	}

	void parseProperParaList() throws SyntaxError {
		parseParadecl();
		while (currentToken.kind == Token.COMMA) {
			match(Token.COMMA);
			parseParadecl();
		}
	}

	void parseParadecl() throws SyntaxError {
		parseType();
		parseDeclarator();
	}
	
	void parseArgList() throws SyntaxError{
		match(Token.LPAREN);
		if (exprFirstSet.contains(currentToken.kind)) {
			parseProperArgList();
		}
		match(Token.RPAREN);
	}
	
	void parseProperArgList() throws SyntaxError{
		parseArg();
		while(currentToken.kind == Token.COMMA){
			match(Token.COMMA);
			parseArg();
		}
	}

	void parseArg() throws SyntaxError {
		parseExpr();
	}

}


