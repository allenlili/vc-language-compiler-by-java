package VC.Scanner;

import java.io.FileWriter;

public class TestASCII {
	public static void main(String[] args) throws Exception {
		FileWriter fileWriter = new FileWriter("/Users/Allen/Dropbox/Data/Java/COMP9102/VC/src/VC/Scanner/testASCII.vc");
		char[] cbuf = new char[4];
		cbuf[0] = 07;
		cbuf[1] = 66;
		cbuf[2] = 67;
		cbuf[3] = 68;
		
		String string = "\n234\n";
		System.out.println(string);
		
		fileWriter.write(cbuf);
		fileWriter.write("\n");
		fileWriter.write("\"!!\"");
		fileWriter.write("\nend");
		System.out.println("done.");
		fileWriter.flush();
		fileWriter.close();
	}
}
