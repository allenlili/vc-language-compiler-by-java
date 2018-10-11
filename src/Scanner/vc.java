/*+*
 *
 * vc.java           67/2/2017
 * 
 * Jingling Xue, CSE, UNSW, Sydney NSW 2052, Australia.
 *
 *+*/

package VC.Scanner;

import VC.ErrorReporter;

public class vc {
/* **/
    private static Scanner scanner;
    private static ErrorReporter reporter;
    private static Token currentToken;
    private static String inputFilename; 
    
    public static void main(String[] args) {
    	inputFilename = args[0];
   
//    	inputFilename = "src/VC/Scanner/comment1.vc";
//    	inputFilename = "src/VC/Scanner/comment2.vc";
//    	inputFilename = "src/VC/Scanner/comment3.vc";
//    	inputFilename = "src/VC/Scanner/comment4.vc";
//    	inputFilename = "src/VC/Scanner/error1.vc";
//    	inputFilename = "src/VC/Scanner/error2.vc";
//    	inputFilename = "src/VC/Scanner/error3.vc";
//    	inputFilename = "src/VC/Scanner/error4.vc";
//    	inputFilename = "src/VC/Scanner/escape.vc";
//    	inputFilename = "src/VC/Scanner/longestmatch.vc";
//      inputFilename = "src/VC/Scanner/fib.vc";
//    	inputFilename = "src/VC/Scanner/string.vc";
//    	inputFilename = "src/VC/Scanner/tab.vc";
//    	inputFilename = "src/VC/Scanner/tokens.vc";
//    	inputFilename = "src/VC/Scanner/fib.vc";
    	
//    	inputFilename = "src/VC/Scanner/mytest01.vc";
//    	inputFilename = "src/VC/Scanner/mytest02.vc";
//    	inputFilename = "src/VC/Scanner/mytest03.vc";
//    	inputFilename = "src/VC/Scanner/mytest04.vc";
//    	inputFilename = "src/VC/Scanner/testASCII.vc";
    
        System.out.println("======= The VC compiler =======");

        SourceFile source = new SourceFile(inputFilename);

        reporter = new ErrorReporter();
        scanner  = new Scanner(source, reporter);
        scanner.enableDebugging();

        do 
        	currentToken = scanner.getToken();
        while (currentToken.kind != Token.EOF);
    }
}
