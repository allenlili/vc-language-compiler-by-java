/*
 * vc.java           
 *
 * F O R     A S S I G N M E N T    2
 * 
 * 
 * Jingling Xue, CSE, UNSW, Sydney NSW 2052, Australia.
 * 
 */

package VC.Recogniser;

import VC.Scanner.Scanner;
import VC.Scanner.SourceFile;
import VC.ErrorReporter;

public class vc {

    private static Scanner scanner;
    private static ErrorReporter reporter;
    private static Recogniser recogniser;

    private static String inputFilename; 

    public static void main(String[] args) {
        if (args.length != 1) {
          System.out.println("Usage: java VC.vc filename\n");
          System.exit(1); 
        } else {
        	inputFilename = args[0];
        }
        
//      inputFilename = args[0];	
//      inputFilename = "src/VC/Recogniser/t1.vc";
//    	inputFilename = "src/VC/Recogniser/t2.vc";
//    	inputFilename = "src/VC/Recogniser/t3.vc";
//    	inputFilename = "src/VC/Recogniser/t4.vc";
//		inputFilename = "src/VC/Recogniser/t5.vc";
//		inputFilename = "src/VC/Recogniser/t6.vc";
//		inputFilename = "src/VC/Recogniser/t7.vc";
//		inputFilename = "src/VC/Recogniser/t8.vc";
//		inputFilename = "src/VC/Recogniser/t10.vc";
//		inputFilename = "src/VC/Recogniser/t11.vc";
//      inputFilename = "src/VC/Recogniser/t12.vc";
//      inputFilename = "src/VC/Recogniser/t13.vc";
//      inputFilename = "src/VC/Recogniser/t14.vc";
//      inputFilename = "src/VC/Recogniser/t18.vc";
//      inputFilename = "src/VC/Recogniser/t19.vc";
//      inputFilename = "src/VC/Recogniser/mytest3.vc";
//      inputFilename = "src/VC/Recogniser/t21.vc";
//      inputFilename = "src/VC/Recogniser/ASS2-TESTS1/test39";
//      inputFilename = "src/VC/Recogniser/ASS2-TESTS1/test52";
        
        System.out.println("======= The VC compiler =======");

        SourceFile source = new SourceFile(inputFilename);

        reporter = new ErrorReporter();
        scanner  = new Scanner(source, reporter);
        
        recogniser = new Recogniser(scanner, reporter);

        recogniser.parseProgram();

        if (reporter.numErrors == 0)
           System.out.println ("Compilation was successful.");
        else
           System.out.println ("Compilation was unsuccessful.");
    }
}

