/**
 *	Scanner.java
 *	Written by z3447294, Li Li, 2017/03/02                         
 **/

package VC.Scanner;


import VC.ErrorReporter;

public final class Scanner { 
	private SourceFile sourceFile;
	private boolean debug;
	private ErrorReporter errorReporter;
	private StringBuffer currentSpelling;
	private char currentChar;
	private SourcePosition sourcePos;
	private RealFMS realFMS;
	private int lineCounter;
	private int columnCounter;
	private int COMMENT = 0;
	private int SPACE = 1;
	private int TAB = 2;
	private int STRING_UNTERMINATED = 3;
	private int TABSIZE = 8;
	
	
	// =========================================================
	public Scanner(SourceFile source, ErrorReporter reporter) {
		sourceFile = source;
		errorReporter = reporter;
		currentChar = sourceFile.getNextChar();
		debug = false;
		lineCounter = 1;
		columnCounter = 1;
		realFMS = new RealFMS();
	}

	public void enableDebugging() {
		debug = true;
	}

	private void accept() {
		currentSpelling.append(currentChar == SourceFile.eof ? '$' : currentChar);
		currentChar = sourceFile.getNextChar();
		sourcePos.charFinish = columnCounter;
		sourcePos.lineFinish = lineCounter;
		columnCounter++;
	}
	
	private void ignore(int times){
		for(int i = 0; i < times; i++){
			if (currentChar == '\n') {
				columnCounter = 1;
				lineCounter++;
			} else {
				columnCounter++;
			}
			sourcePos.lineStart = lineCounter;
			sourcePos.lineFinish = lineCounter;
			sourcePos.charStart = columnCounter;
			sourcePos.charFinish = columnCounter;
			currentChar = sourceFile.getNextChar();
		}
	}

	// inspectChar returns the n-th character after currentChar in the input stream. 
	private char inspectChar(int nthChar) {
		return sourceFile.inspectChar(nthChar);
	}
	
	private int recognizeId(int result){
		if(Character.isLetter(currentChar) || currentChar == '_') {
			accept();
			while(Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_') {
				accept();
			}
			return Token.ID;
		}
		return result;
	}
	
	private int recognizeBool(int result){
		if (currentSpelling.toString().equals("true") || currentSpelling.toString().equals("false")) {
			return Token.BOOLEANLITERAL;
		} else {
			return result;
		}
	}
	
	private int recognizeReal(int result){
		realFMS.reset();
		int readlNumStart = columnCounter;
		int counter = 1;
		int markCounter = 0;
		char inspectChar = currentChar; 
		while (true) {
			while (true) {
				if (realFMS.isRealSet(inspectChar)) {
					realFMS.realNextStateOfNextChar = realFMS.next(realFMS.realState, inspectChar);
					break;
				} else {
					if (realFMS.realStateOfLastAccept != realFMS.STATE_FAILURE) {
						realFMS.realNextStateOfNextChar = realFMS.STATE_FAILURE;
						break;
					} else {
						return result;
					}
				}
			}
			if (realFMS.realState != realFMS.STATE_FAILURE) {
				if (realFMS.realAnchor = realFMS.isAccept(realFMS.realNextStateOfNextChar)) {
					realFMS.realPrevStateOfLastAccept = realFMS.realState;
					realFMS.realStateOfLastAccept = realFMS.realNextStateOfNextChar;
					markCounter = counter;
				}
				realFMS.realState = realFMS.realNextStateOfNextChar;
				inspectChar = inspectChar(counter++);
			} else {
				if (realFMS.realStateOfLastAccept == realFMS.STATE_FAILURE) {
					return result;
				} else {
					switch (realFMS.realStateOfLastAccept) {
						
						case 1:
							getValidReal(readlNumStart, markCounter);
							return Token.INTLITERAL;
						case 2:
						case 6:
							getValidReal(readlNumStart, markCounter);
							return Token.FLOATLITERAL;
						default:
							return result;
					}
				}
			}
		}
	}

	private void getValidReal(int readlNumStart, int markCounter) {
		int i = markCounter;
		sourcePos.charStart = readlNumStart;
		sourcePos.charFinish = readlNumStart + markCounter - 1 ;
		sourcePos.lineStart = sourcePos.lineFinish = lineCounter;
		while(i-- >= 1){
			currentSpelling.append(currentChar);
			currentChar = sourceFile.getNextChar();
		}
		columnCounter = sourcePos.charFinish + 1;
	} 
	
	private int recognizeString(int result) {
		if (currentChar == '"') {
			int stringLineStart = lineCounter;
			int stringColumnStart = columnCounter;
			ignore(1);
			while(currentChar != '"'){
				if (currentChar == SourceFile.eof || currentChar == '\n') {
					errorReporter.reportError(
							currentSpelling.toString() + ": unterminated string", 
							null, 
							new SourcePosition(stringLineStart, stringColumnStart, stringColumnStart));
					sourcePos.charStart = stringColumnStart;
					return Token.STRINGLITERAL;
				}				
				if (currentChar == '\\') {
					char legal = escapeHandler(inspectChar(1));
					if (legal == '0'){
						errorReporter.reportError(
								"\\" + String.valueOf(inspectChar(1)) + ": illegal escape character", 
								null, 
								new SourcePosition(stringLineStart, stringColumnStart, sourcePos.charFinish + 1));
						accept();
						accept();
					} else {
						currentSpelling.append(legal);
						ignore(2);
					}
					continue;
				}
				accept();
			}
			sourcePos.charStart = stringColumnStart;
			sourcePos.charFinish = columnCounter;
			currentChar = sourceFile.getNextChar();
			columnCounter++;
			return Token.STRINGLITERAL;
		}
		return -1;
	}
	
	private char escapeHandler(char inspectChar) {
		switch (inspectChar) {
		case 'b':
			return '\b';
		case 'f':
			return '\f';
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		case '\'':
			return '\'';
		case '\"':
			return '\"';
		case '\\':
			return '\\';	
		default:
			return '0';
		}
	}

	private int nextToken() {
		// Tokens: separators, operators, literals, identifiers and keyworods
		switch (currentChar) {
			case '+':
				accept();
				return Token.PLUS;
			case '-':
				accept();
				return Token.MINUS;
			case '*':
				accept();
				return Token.MULT;
			case '/':
				if (inspectChar(1) == '*' || inspectChar(1) == '/') {
					return COMMENT;
				}
			    accept();
				return Token.DIV;
			case '!':
			    accept();
			    if (currentChar == '=') {
			    	accept();
					return Token.NOTEQ;
				}
				return Token.NOT;
			case '=':
			    accept();
			    if (currentChar == '=') {
			    	accept();
					return Token.EQEQ;
				}
				return Token.EQ;
			case '<':
			    accept();
			    if (currentChar == '=') {
			    	accept();
					return Token.LTEQ;
				}
				return Token.LT;
			case '>':
			    accept();
			    if (currentChar == '=') {
			    	accept();
					return Token.GTEQ;
				}
				return Token.GT;
			case '&':
			    accept();
			    if (currentChar == '&') {
			    	accept();
					return Token.ANDAND;
				}
				return Token.ERROR;
			case '|':
				accept();
				if (currentChar == '|') {
					accept();
		            return Token.OROR;
		  	  	}
		  		return Token.ERROR;
			case '{':
				accept();
		        return Token.LCURLY;
			case '}':
				accept();
		        return Token.RCURLY;
		    case '(':
				accept();
				return Token.LPAREN;
		    case ')':
				accept();
				return Token.RPAREN;
		    case '[':
				accept();
				return Token.LBRACKET;
		    case ']':
				accept();
				return Token.RBRACKET;
		    case ';':
				accept();
				return Token.SEMICOLON;
		    case ',':
				accept();
				return Token.COMMA;
		    case ' ':
		    	return SPACE;
		    case '\t':
		    	if (columnCounter > TABSIZE) {
		    		columnCounter = TABSIZE - (columnCounter - 1) % 8 + columnCounter;
				} else {
					columnCounter = TABSIZE + 1; 
				}
		    	currentChar = sourceFile.getNextChar();
		    	return TAB;	
		    case SourceFile.eof:
				accept();
				return Token.EOF;				
			default:
				break;
		}
		int result = -1;
		result = recognizeId(result);
		result = recognizeBool(result);
		if(result == Token.BOOLEANLITERAL){
			return Token.BOOLEANLITERAL;
		}
		if(result == Token.ID){
			return Token.ID;
		} 
		result = recognizeString(result);
		if (result == Token.STRINGLITERAL) {
			return Token.STRINGLITERAL;
		}
		if (result == STRING_UNTERMINATED) {
			return STRING_UNTERMINATED;
		}
		result = recognizeReal(result);
		if(result == Token.INTLITERAL) {
			return Token.INTLITERAL;
		}
		if (result == Token.FLOATLITERAL) {
			return Token.FLOATLITERAL;
		}
		skipSpaceAndComments();
		accept(); 
		return Token.ERROR;
	}

	public void skipSpaceAndComments() {
		if (currentChar == '/' && inspectChar(1) == '*') {
			int terminatedColumnStart = columnCounter;
			ignore(1);
			char tempChar1 = 0, tempChar2 = 0;
			int counter = 1;
			int lineNum = 0, columnNum = columnCounter;
			boolean flag = true;
			while (true) {
				tempChar1 = inspectChar(counter);
				tempChar2 = inspectChar(counter + 1);
				if (tempChar1 == '*' && tempChar2 == '/') {
					counter++;
					columnNum = columnNum + 2;
					break;
				}
				else if (tempChar1 == '\n') {
					lineNum ++;
					columnNum = 1;
				}
				else if (SourceFile.eof == tempChar1) {
					flag = false;
					break;
				} else {
					columnNum++;
				}
				counter++;
			}
			if (flag == false) {
				errorReporter.reportError(": unterminated comment", 
						String.valueOf(inspectChar(1)), 
						new SourcePosition(lineCounter, terminatedColumnStart, terminatedColumnStart));
				while(true) {
					if (SourceFile.eof == currentChar) {
						break;
					}
					ignore(1);
				}
			} else {
				lineCounter = lineCounter + lineNum;
				columnCounter = columnNum + 1;
				while(counter-- >= 0){
					currentChar = sourceFile.getNextChar();
				}
			}
		}
		if (currentChar == '/' && inspectChar(1) == '/'){
			ignore(2);
			while (currentChar != '\n') {
				ignore(1);
			}
			ignore(1);
		}
		while (currentChar == ' ' || currentChar == '\n'){
			ignore(1);
		}
	}

	public Token getToken() {
		Token tok;
		int kind;
		while(true){
			sourcePos = new SourcePosition();
			currentSpelling = new StringBuffer("");
			sourcePos.charStart = columnCounter;
			sourcePos.lineStart = lineCounter;
			skipSpaceAndComments();
			kind = nextToken();
			if (kind != COMMENT && kind != SPACE && kind != TAB && kind != STRING_UNTERMINATED) {
				break;
			}
		}	
		tok = new Token(kind, currentSpelling.toString(), sourcePos);
		// * do not remove these three lines
		if (debug)
			System.out.println(tok);
		return tok;
	}

	class RealFMS{
		private final int ASCII_NUM = 128;
		private final int STATE_NUM = 7;
		private int[][] fmsTable = new int[STATE_NUM][ASCII_NUM];
		public final int STATE_FAILURE = -1;
		private boolean[] accept = new boolean[] {false, true, true, false, false, false, true};
		public int realState = 0;
		public int realStateOfLastAccept = STATE_FAILURE;
		public int realPrevStateOfLastAccept = STATE_FAILURE;
		public int realNextStateOfNextChar = STATE_FAILURE;
		public boolean realAnchor = false;
		public boolean endReads = false;
		public void reset(){
			this.realState = 0;
			this.realStateOfLastAccept = STATE_FAILURE;
			this.realPrevStateOfLastAccept = STATE_FAILURE;
			this.realNextStateOfNextChar = STATE_FAILURE;
			this.realAnchor = false;
		}
		public RealFMS(){
			for(int i = 0; i < STATE_NUM; i++){
				for (int j = 0; j < ASCII_NUM; j++) {
					fmsTable[i][j] = STATE_FAILURE;
				}
			}
			initStateByDigit(0, 1);
			initStateByDigit(1, 1);
			initStateByDigit(2, 2);
			initStateByDigit(3, 2);
			initStateByDigit(4, 6);
			initStateByDigit(5, 6);
			initStateByDigit(6, 6);	
			initStateBySpecial(0, '.', 3);
			initStateBySpecial(1, '.', 2);
			initStateBySpecial(1, 'E', 4);
			initStateBySpecial(1, 'e', 4);
			initStateBySpecial(2, 'E', 4);
			initStateBySpecial(2, 'e', 4);
			initStateBySpecial(4, '+', 5);
			initStateBySpecial(4, '-', 5);
		}
		
		private void initStateByDigit(int previousState, int nextState){
			for (int i = 0; i < 10; i++) {
				fmsTable[previousState]['0' + i] = nextState; 
			}
		}
		
		public boolean isRealSet(char currentChar){
			if (Character.isDigit(currentChar)){
				return true;
			} else if (currentChar == 'E'){
				return true;
			} else if (currentChar == 'e') {
				return true;
			} else if (currentChar == '+') {
				return true;
			} else if (currentChar == '-') {
				return true;
			} else if (currentChar == '.') {
				return true;
			} else {
				return false;
			}
		}
		
		private void initStateBySpecial(int previousState, int value, int nextState){
			fmsTable[previousState][value] = nextState; 
		}
		
		public int next(int state, char currentChar){
			if (state == STATE_FAILURE || currentChar >= ASCII_NUM) {
				return STATE_FAILURE;
			}
			return fmsTable[state][currentChar];
		}
		
		public boolean isAccept(int state) {
			if (state == STATE_FAILURE) {
				return false;
			}
			return accept[state];
		}
	}
}


