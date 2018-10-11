/*
 * Emitter.java    30 -*- MAY -*- 2017
 * Jingling Xue, School of Computer Science, UNSW, Australia
 */

package VC.CodeGen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import VC.ASTs.*;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Emitter implements Visitor {

	@SuppressWarnings("unused")
	private ErrorReporter errorReporter;
	@SuppressWarnings("unused")
	private String inputFilename;
	private String classname;
	@SuppressWarnings("unused")
	private String outputFilename;

	private Set<String> copSet = new HashSet<String>();
	private Map<String, String> aopMap = new HashMap<String, String>();
	private boolean isRet;
	
	public Emitter(String inputFilename, ErrorReporter reporter) {
		this.inputFilename = inputFilename;
		errorReporter = reporter;

		int i = inputFilename.lastIndexOf('.');
		if (i > 0)
			classname = inputFilename.substring(0, i);
		else
			classname = inputFilename;
		
		aop();
		cop();
		isRet = false;
	}

	private void aop() {
		aopMap.put("i+", JVM.IADD);
		aopMap.put("i*", JVM.IMUL);
		aopMap.put("i-", JVM.ISUB);
		aopMap.put("i/", JVM.IDIV);

		aopMap.put("f+", JVM.FADD);
		aopMap.put("f*", JVM.FMUL);
		aopMap.put("f-", JVM.FSUB);
		aopMap.put("f/", JVM.FDIV);
	}
	
	private void cop() {
		copSet.add("i>");
		copSet.add("i<");
		copSet.add("i>=");
		copSet.add("i<=");
		copSet.add("i==");
		copSet.add("i!=");
		
		copSet.add("f>");
		copSet.add("f<");
		copSet.add("f>=");
		copSet.add("f<=");
		copSet.add("f==");
		copSet.add("f!=");
	}
	
	private void instBuild(Expr expr, Frame frame) {
		boolean ret = expr instanceof CallExpr;
		boolean ret2 = expr.type.isVoidType();
		boolean ret3 = expr instanceof EmptyExpr;
		boolean ret4 = expr instanceof AssignExpr;
		if( ret && ret2 || ret3 || ret4) {
			return;
		} else {
			emit(JVM.POP);
			frame.pop();
		}
	}
	
	public final void gen(AST ast) {
		ast.visit(this, null); 
		JVM.dump(classname + ".j");
	}

	public Object visitStmtList(StmtList ast, Object o) {
		ast.S.visit(this, o);
		ast.SL.visit(this, o);
		return null;
	}

	public Object visitCompoundStmt(CompoundStmt ast, Object o) {
		Frame frame = (Frame) o; 

		String scopeStart = frame.getNewLabel();
		String scopeEnd = frame.getNewLabel();
		frame.scopeStart.push(scopeStart);
		frame.scopeEnd.push(scopeEnd);

		emit(scopeStart + ":");
		if (ast.parent instanceof FuncDecl) {
			if (((FuncDecl) ast.parent).I.spelling.equals("main")) {
				emit(JVM.VAR, "0 is argv [Ljava/lang/String; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
				emit(JVM.VAR, "1 is vc$ L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
				// Generate code for the initialiser vc$ = new classname();
				emit(JVM.NEW, classname);
				emit(JVM.DUP);
				frame.push(2);
				emit("invokenonvirtual", classname + "/<init>()V");
				frame.pop();
				emit(JVM.ASTORE_1);
				frame.pop();
			} else {
				emit(JVM.VAR, "0 is this L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
				((FuncDecl) ast.parent).PL.visit(this, o);
			}
		}
		ast.DL.visit(this, o);
		ast.SL.visit(this, o);
		emit(scopeEnd + ":");

		frame.scopeStart.pop();
		frame.scopeEnd.pop();
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt ast, Object o) {
		Frame frame = (Frame)o;
		/*
  int main() { return 0; } must be interpretted as 
  public static void main(String[] args) { return ; }
  Therefore, "return expr", if present in the main of a VC program
  must be translated into a RETURN rather than IRETURN instruction.
		 */

		if (frame.isMain())  {
			emit(JVM.RETURN);
			isRet = true;
			return null;
		}
		// Your other code goes here
		if(!ast.E.isEmptyExpr()) {
			ast.E.visit(this, o);
			boolean ret = ast.E.type.isFloatType();
			if(!ret) {
				emit(JVM.IRETURN);
			} else {
				emit(JVM.FRETURN);
			}
		} else {
			emit(JVM.RETURN);
		}
		return null;
	}

	public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
		return null;
	}

	public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
		return null;
	}

	public Object visitEmptyStmt(EmptyStmt ast, Object o) {
		return null;
	}

	// Expressions



	public Object visitEmptyExpr(EmptyExpr ast, Object o) {
		return null;
	}

	public Object visitIntExpr(IntExpr ast, Object o) {
		ast.IL.visit(this, o);
		return null;
	}

	public Object visitFloatExpr(FloatExpr ast, Object o) {
		ast.FL.visit(this, o);
		return null;
	}

	public Object visitBooleanExpr(BooleanExpr ast, Object o) {
		ast.BL.visit(this, o);
		return null;
	}

	public Object visitStringExpr(StringExpr ast, Object o) {
		ast.SL.visit(this, o);
		return null;
	}

	// Declarations

	public Object visitDeclList(DeclList ast, Object o) {
		ast.D.visit(this, o);
		ast.DL.visit(this, o);
		return null;
	}

	public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
		return null;
	}

	public Object visitFuncDecl(FuncDecl ast, Object o) {

		Frame frame; 

		if (ast.I.spelling.equals("main")) {

			frame = new Frame(true);

			// Assume that main has one String parameter and reserve 0 for it
			frame.getNewIndex(); 

			emit(JVM.METHOD_START, "public static main([Ljava/lang/String;)V"); 
			// Assume implicitly that
			//      classname vc$; 
			// appears before all local variable declarations.
			// (1) Reserve 1 for this object reference.

			frame.getNewIndex(); 

		} else {
			frame = new Frame(false);
			frame.getNewIndex();
			String retType = VCtoJavaType(ast.T);
			StringBuffer argsTypes = new StringBuffer("");
			List fpl = ast.PL;
			ArrayType arrayType = null;
			for (; ! fpl.isEmpty(); ) {
				if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType)){
					argsTypes.append("Z");      
					fpl = ((ParaList) fpl).PL;
				}else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType)){
					argsTypes.append("I");
					fpl = ((ParaList) fpl).PL;
				}else if (((ParaList) fpl).P.T.equals(StdEnvironment.floatType)){
					argsTypes.append("F"); 
					fpl = ((ParaList) fpl).PL;
				}else if(((ParaList) fpl).P.T.isArrayType()) {
					ParaList paraList = (ParaList) fpl;
					arrayType = (ArrayType)(paraList).P.T;
					if(arrayType.T.isFloatType()) {
						argsTypes.append("[F");
						fpl = ((ParaList) fpl).PL;
					} else if(arrayType.T.isIntType()) {
						argsTypes.append("[I");
						fpl = ((ParaList) fpl).PL;
					} else if(arrayType.T.isBooleanType()) {
						argsTypes.append("[Z");
						fpl = ((ParaList) fpl).PL;
					} else {
						fpl = ((ParaList) fpl).PL;
					}
				} else {
					fpl = ((ParaList) fpl).PL;
				}
			}
			emit(JVM.METHOD_START, ast.I.spelling + "(" + argsTypes + ")" + retType);
		}
		ast.S.visit(this, frame);
		if (ast.T.equals(StdEnvironment.voidType)) {
			emit("");
			emit("; return may not be present in a VC function returning void"); 
			emit("; The following return inserted by the VC compiler");
			emit(JVM.RETURN); 
		} else if (ast.I.spelling.equals("main") && !isRet) {
			emit(JVM.RETURN);
			isRet = true;
		} else
			emit(JVM.NOP); 
		emit("");
		emit("; set limits used by this method");
		emit(JVM.LIMIT, "locals", frame.getNewIndex());
		emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
		emit(".end method");
		return null;
	}

	public Object visitCallExpr(CallExpr ast, Object o) {
		Frame frame = (Frame) o;
		String fname = ast.I.spelling;
		if (fname.equals("getInt")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System.getInt()I");
			frame.push();
		} else if (fname.equals("putInt")) {
			ast.AL.visit(this, o);
			emit("invokestatic VC/lang/System.putInt(I)V");
			frame.pop();
		} else if (fname.equals("putIntLn")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/putIntLn(I)V");
			frame.pop();
		} else if (fname.equals("getFloat")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/getFloat()F");
			frame.push();
		} else if (fname.equals("putFloat")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/putFloat(F)V");
			frame.pop();
		} else if (fname.equals("putFloatLn")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/putFloatLn(F)V");
			frame.pop();
		} else if (fname.equals("putBool")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/putBool(Z)V");
			frame.pop();
		} else if (fname.equals("putBoolLn")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/putBoolLn(Z)V");
			frame.pop();
		} else if (fname.equals("putString")) {
			ast.AL.visit(this, o);
			emit(JVM.INVOKESTATIC, "VC/lang/System/putString(Ljava/lang/String;)V");
			frame.pop();
		} else if (fname.equals("putStringLn")) {
			ast.AL.visit(this, o);
			emit(JVM.INVOKESTATIC, "VC/lang/System/putStringLn(Ljava/lang/String;)V");
			frame.pop();
		} else if (fname.equals("putLn")) {
			ast.AL.visit(this, o); 
			emit("invokestatic VC/lang/System/putLn()V");
		} else { 
			FuncDecl fAST = (FuncDecl) ast.I.decl;
			if (frame.isMain()) 
				emit("aload_1"); 
			else
				emit("aload_0");
			frame.push();
			ast.AL.visit(this, o);
			String retType = VCtoJavaType(fAST.T);
			StringBuffer argsTypes = new StringBuffer("");
			int counter;
			List fpl = fAST.PL;
			ArrayType arrayType = null;
			for ( counter = 0; !fpl.isEmpty(); counter++) {
				if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType)) {
					argsTypes.append("Z");
					fpl = ((ParaList) fpl).PL;
				} else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType)){
					argsTypes.append("I");
					fpl = ((ParaList) fpl).PL;
				} else if (((ParaList) fpl).P.T.equals(StdEnvironment.floatType)){
					argsTypes.append("F");
					fpl = ((ParaList) fpl).PL;
				} else if(((ParaList) fpl).P.T.isArrayType()) {
					arrayType = (ArrayType)((ParaList) fpl).P.T;
					if(arrayType.T.isIntType()) {
						argsTypes.append("[I");
					} else if(arrayType.T.isFloatType()) {
						argsTypes.append("[F");
					} else if(arrayType.T.isBooleanType()) {
						argsTypes.append("[Z");
					}
					fpl = ((ParaList) fpl).PL;
				} else {
					fpl = ((ParaList) fpl).PL;
				}
			}
			emit("invokevirtual", classname + "/" + fname + "(" + argsTypes + ")" + retType);
			frame.pop(counter);
			if (! retType.equals("V"))
				frame.push();
		}
		return null;
	}
	
	
	public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
		// nothing to be done
		return null;
	}

	public Object visitParaList(ParaList ast, Object o) {
		ast.P.visit(this, o);
		ast.PL.visit(this, o);
		return null;
	}

	public Object visitParaDecl(ParaDecl ast, Object o) {
		Frame frame = (Frame) o;
		ast.index = frame.getNewIndex();
		String T = VCtoJavaType(ast.T);

		emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
		return null;
	}

	public Object visitEmptyParaList(EmptyParaList ast, Object o) {
		return null;
	}

	// Arguments

	public Object visitArgList(ArgList ast, Object o) {
		ast.A.visit(this, o);
		ast.AL.visit(this, o);
		return null;
	}

	public Object visitArg(Arg ast, Object o) {
		ast.E.visit(this, o);
		return null;
	}

	public Object visitEmptyArgList(EmptyArgList ast, Object o) {
		return null;
	}

	// Types

	public Object visitIntType(IntType ast, Object o) {
		return null;
	}

	public Object visitFloatType(FloatType ast, Object o) {
		return null;
	}

	public Object visitBooleanType(BooleanType ast, Object o) {
		return null;
	}

	public Object visitVoidType(VoidType ast, Object o) {
		return null;
	}

	public Object visitErrorType(ErrorType ast, Object o) {
		return null;
	}

	public Object visitIdent(Ident ast, Object o) {
		return null;
	}

	public Object visitIntLiteral(IntLiteral ast, Object o) {
		Frame frame = (Frame) o;
		emitICONST(Integer.parseInt(ast.spelling));
		frame.push();
		return null;
	}

	public Object visitFloatLiteral(FloatLiteral ast, Object o) {
		Frame frame = (Frame) o;
		emitFCONST(Float.parseFloat(ast.spelling));
		frame.push();
		return null;
	}

	public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
		Frame frame = (Frame) o;
		emitBCONST(ast.spelling.equals("true"));
		frame.push();
		return null;
	}

	public Object visitStringLiteral(StringLiteral ast, Object o) {
		Frame frame = (Frame) o;
		emit(JVM.LDC, "\"" + ast.spelling + "\"");
		frame.push();
		return null;
	}

	public Object visitOperator(Operator ast, Object o) {
		return null;
	}

	private void emit(String s) {
		JVM.append(new Instruction(s)); 
	}

	private void emit(String s1, String s2) {
		emit(s1 + " " + s2);
	}

	private void emit(String s1, int i) {
		emit(s1 + " " + i);
	}

	private void emit(String s1, float f) {
		emit(s1 + " " + f);
	}

	private void emit(String s1, String s2, int i) {
		emit(s1 + " " + s2 + " " + i);
	}

	private void emit(String s1, String s2, String s3) {
		emit(s1 + " " + s2 + " " + s3);
	}

	@Override
	public Object visitEmptyExprList(EmptyExprList ast, Object o) {
		return null;
	}


	@Override
	public Object visitBreakStmt(BreakStmt ast, Object o) {
		Frame frame = (Frame)o;
		String gotoC = JVM.GOTO;
		emit(gotoC, frame.brkStack.peek());
		return null;
	}

	@Override
	public Object visitContinueStmt(ContinueStmt ast, Object o) {
		Frame frame = (Frame) o;
		emit(JVM.GOTO, frame.conStack.peek());
		return null;
	}

	@Override
	public Object visitExprStmt(ExprStmt ast, Object o) {
		ast.E.visit(this, o);
		instBuild(ast.E, (Frame) o);
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr ast, Object o) {
		Frame frame = (Frame) o;
		String label1 = frame.getNewLabel();
		String label2 = frame.getNewLabel();
		ast.E.visit(this, o);
		String op = ast.O.spelling;
		if(op.equals("i2f")) {
			emit(JVM.I2F);
			return null;
		}
		if (op.equals("i-")){
			emit(JVM.INEG);
			return null;
		}
		if(op.equals("f-")) {
			emit(JVM.FNEG);
			return null;
		}
		if(op.equals("i!")) {
			emit(JVM.IFNE, label1);
			emitBCONST(true);
			emit(JVM.GOTO, label2);
			emit(label1);
			emitBCONST(false);
			emit(label2);
			return null;
		}
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr ast, Object o) {
		Frame frame = (Frame) o;
		String c = ast.O.spelling;
		if(c.equals("i&&")) {
			String L1 = frame.getNewLabel();
			String L2 = frame.getNewLabel();
			ast.E1.visit(this, o);
			emit(JVM.IFEQ, L1);
			ast.E2.visit(this, o);
			emit(JVM.IFEQ, L1);
			emitICONST(1);
			emit(JVM.GOTO, L2);
			emit(L1 + ":");
			emitICONST(0);
			emit(L2 + ":");
			frame.push();
		} else if(c.equals("i||")) {
			String L1 = frame.getNewLabel();
			String L2 = frame.getNewLabel();
			ast.E1.visit(this, o);
			emit(JVM.IFNE, L1);
			ast.E2.visit(this, o);
			emit(JVM.IFNE, L1);
			emitICONST(0);
			emit(JVM.GOTO, L2);
			emit(L1 + ":");
			emitICONST(1);
			emit(L2 + ":");
			frame.push();
		} else if(aopMap.containsKey(c)) {
			ast.E1.visit(this, o);
			ast.E2.visit(this, o);
			emit(aopMap.get(c));
			frame.pop();
		} else if(copSet.contains(c)) {
			ast.E1.visit(this, o);
			ast.E2.visit(this, o);
			if(c.contains("i")) {
				emitIF_ICMPCOND(c, frame);
				frame.pop();
			}
			if(c.contains("f")) {
				emitFCMP(c, frame);
				frame.pop();
			} 
		} 
		return null;
	}
	

	@Override
	public Object visitExprList(ExprList ast, Object o) {
		return null;
	}


	@Override
	public Object visitVarExpr(VarExpr ast, Object o) {
		ast.V.visit(this, o);
		return null;
	}


	@Override
	public Object visitStringType(StringType ast, Object o) {
		return null;
	}
	

	private void emitIF_ICMPCOND(String op, Frame frame) {
		String opcode;

		if (op.equals("i!="))
			opcode = JVM.IF_ICMPNE;
		else if (op.equals("i=="))
			opcode = JVM.IF_ICMPEQ;
		else if (op.equals("i<"))
			opcode = JVM.IF_ICMPLT;
		else if (op.equals("i<="))
			opcode = JVM.IF_ICMPLE;
		else if (op.equals("i>"))
			opcode = JVM.IF_ICMPGT;
		else // if (op.equals("i>="))
			opcode = JVM.IF_ICMPGE;

		String falseLabel = frame.getNewLabel();
		String nextLabel = frame.getNewLabel();

		emit(opcode, falseLabel);
		frame.pop(2); 
		emit("iconst_0");
		emit("goto", nextLabel);
		emit(falseLabel + ":");
		emit(JVM.ICONST_1);
		frame.push(); 
		emit(nextLabel + ":");
	}

	private void emitFCMP(String op, Frame frame) {
		String opcode;

		if (op.equals("f!="))
			opcode = JVM.IFNE;
		else if (op.equals("f=="))
			opcode = JVM.IFEQ;
		else if (op.equals("f<"))
			opcode = JVM.IFLT;
		else if (op.equals("f<="))
			opcode = JVM.IFLE;
		else if (op.equals("f>"))
			opcode = JVM.IFGT;
		else // if (op.equals("f>="))
			opcode = JVM.IFGE;

		String falseLabel = frame.getNewLabel();
		String nextLabel = frame.getNewLabel();

		emit(JVM.FCMPG);
		frame.pop(2);
		emit(opcode, falseLabel);
		emit(JVM.ICONST_0);
		emit("goto", nextLabel);
		emit(falseLabel + ":");
		emit(JVM.ICONST_1);
		frame.push();
		emit(nextLabel + ":");

	}

	private void emitILOAD(int index) {
		if (index >= 0 && index <= 3) 
			emit(JVM.ILOAD + "_" + index); 
		else
			emit(JVM.ILOAD, index); 
	}

	private void emitFLOAD(int index) {
		if (index >= 0 && index <= 3) 
			emit(JVM.FLOAD + "_"  + index); 
		else
			emit(JVM.FLOAD, index); 
	}
	
	private void emitALOAD(int index) {
		if (index >= 0 && index <= 3) 
			emit(JVM.ALOAD + "_"  + index); 
		else
			emit(JVM.ALOAD, index); 
	}
	
	private void emitASTORE(int index) {
		if(index >= 0 && index <= 3) {
			emit(JVM.ASTORE + "_" + index);
		} else {
			emit(JVM.ASTORE, index);
		}
	}
	
	
	private void emitGETSTATIC(String T, String I) {
		emit(JVM.GETSTATIC, classname + "/" + I, T); 
	}

	private void emitISTORE(Ident ast) {
		int index;
		if (ast.decl instanceof ParaDecl)
			index = ((ParaDecl) ast.decl).index; 
		else
			index = ((LocalVarDecl) ast.decl).index; 

		if (index >= 0 && index <= 3) 
			emit(JVM.ISTORE + "_" + index); 
		else
			emit(JVM.ISTORE, index); 
	}

	private void emitFSTORE(Ident ast) {
		int index;
		if (ast.decl instanceof ParaDecl)
			index = ((ParaDecl) ast.decl).index; 
		else
			index = ((LocalVarDecl) ast.decl).index; 
		if (index >= 0 && index <= 3) 
			emit(JVM.FSTORE + "_" + index); 
		else
			emit(JVM.FSTORE, index); 
	}

	private void emitPUTSTATIC(String T, String I) {
		emit(JVM.PUTSTATIC, classname + "/" + I, T); 
	}

	private void emitICONST(int value) {
		if (value == -1)
			emit(JVM.ICONST_M1); 
		else if (value >= 0 && value <= 5) 
			emit(JVM.ICONST + "_" + value); 
		else if (value >= -128 && value <= 127) 
			emit(JVM.BIPUSH, value); 
		else if (value >= -32768 && value <= 32767)
			emit(JVM.SIPUSH, value); 
		else 
			emit(JVM.LDC, value); 
	}

	private void emitFCONST(float value) {
		if(value == 0.0)
			emit(JVM.FCONST_0); 
		else if(value == 1.0)
			emit(JVM.FCONST_1); 
		else if(value == 2.0)
			emit(JVM.FCONST_2); 
		else 
			emit(JVM.LDC, value); 
	}

	private void emitBCONST(boolean value) {
		if (value)
			emit(JVM.ICONST_1);
		else
			emit(JVM.ICONST_0);
	}

	private String VCtoJavaType(Type t) {
		ArrayType at = null;
		if (t.equals(StdEnvironment.booleanType))
			return "Z";
		else if (t.equals(StdEnvironment.intType))
			return "I";
		else if (t.equals(StdEnvironment.floatType))
			return "F";
		else if(t.isArrayType()) {
			at = (ArrayType) t;
			if(at.T.isIntType()) {
				return "[I";
			} else if( at.T.isFloatType()) {
				return "[F";
			} else if(at.T.isBooleanType()) {
				return "[Z";
			} else {
				return null;
			}
		} else {// if (t.equals(StdEnvironment.voidType))
			return "V";
		}
	}

	
	// TODO MINE
	public Object visitProgram(Program ast, Object o) {
		/** This method works for scalar variables only. You need to modify
         it to handle all array-related declarations and initialisations.
		 **/ 

		// Generates the default constructor initialiser 
		emit(JVM.CLASS, "public", classname);
		emit(JVM.SUPER, "java/lang/Object");

		emit("");

		// Three subpasses:

		// (1) Generate .field definition statements since
		//     these are required to appear before method definitions
		List list = ast.FL;
		while (!list.isEmpty()) {
			DeclList dlAST = (DeclList) list;
			if (dlAST.D instanceof GlobalVarDecl) {
				GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
				emit(JVM.STATIC_FIELD, vAST.I.spelling, VCtoJavaType(vAST.T));
			}
			list = dlAST.DL;
		}

		emit("");

		// (2) Generate <clinit> for global variables (assumed to be static)

		emit("; standard class static initializer ");
		emit(JVM.METHOD_START, "static <clinit>()V");
		emit("");

		// create a Frame for <clinit>

		Frame frame = new Frame(false);

		list = ast.FL;
		while (!list.isEmpty()) {
			DeclList dlAST = (DeclList) list;
			if (dlAST.D instanceof GlobalVarDecl) {
				GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
				// TODO MINE
				boolean ret = vAST.T.isArrayType();
				ArrayType at = null;
				if(ret) {
					at = (ArrayType)vAST.T;
					at.visit(this, frame);
					if(vAST.E.isEmptyExpr()) {
						emitPUTSTATIC(VCtoJavaType(at), vAST.I.spelling);
						frame.pop();
						list = dlAST.DL;
						continue;
					} else {
						vAST.E.visit(this, frame);
						emitPUTSTATIC(VCtoJavaType(at), vAST.I.spelling);
						frame.pop();
						list = dlAST.DL;
						continue;
					}
				}
				// 
				if (!vAST.E.isEmptyExpr()) {
					vAST.E.visit(this, frame);
				} else {
					if (vAST.T.equals(StdEnvironment.floatType))
						emit(JVM.FCONST_0);
					else
						emit(JVM.ICONST_0);
					frame.push();
				}
				emitPUTSTATIC(VCtoJavaType(vAST.T), vAST.I.spelling);
				frame.pop();
			}
			list = dlAST.DL;
		}

		emit("");
		emit("; set limits used by this method");
		emit(JVM.LIMIT, "locals", frame.getNewIndex());

		emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
		emit(JVM.RETURN);
		emit(JVM.METHOD_END, "method");

		emit("");

		// (3) Generate Java bytecode for the VC program

		emit("; standard constructor initializer ");
		emit(JVM.METHOD_START, "public <init>()V");
		emit(JVM.LIMIT, "stack 1");
		emit(JVM.LIMIT, "locals 1");
		emit(JVM.ALOAD_0);
		emit(JVM.INVOKESPECIAL, "java/lang/Object/<init>()V");
		emit(JVM.RETURN);
		emit(JVM.METHOD_END, "method");

		return ast.FL.visit(this, o);
	}
	
	// TODO MINE
	@Override
	public Object visitArrayExpr(ArrayExpr ast, Object o) {
		Frame frame = (Frame) o;
		ast.V.visit(this, o);
		ast.E.visit(this, o);
		if(ast.type.isBooleanType()) {
			emit(JVM.BARRLOAD);
		} else if (ast.type.isFloatType()){
			emit(JVM.FARRLOAD);
		} else {
			emit(JVM.IARRLOAD);
		}
		frame.pop();
		return null;
	}

	// TODO MINE
	@Override
	public Object visitForStmt(ForStmt ast, Object o) {
		Frame frame = (Frame)o;
		String label1 = frame.getNewLabel();
		String label2 = frame.getNewLabel();
		String label3 = frame.getNewLabel();
		frame.conStack.push(label3);
		frame.brkStack.push(label2);
		ast.E1.visit(this, o);
		instBuild(ast.E1, frame);
		emit(label1 + ":");
		ast.E2.visit(this, o);
		if(ast.E2.isEmptyExpr()) {
			;
		} else {
			emit(JVM.IFEQ, label2);
		}
		ast.S.visit(this, o);
		emit(label3 + ":");
		ast.E3.visit(this, o);
		instBuild(ast.E3, frame);
		emit(JVM.GOTO, label1);
		emit(label2 + ":");
		frame.conStack.pop();
		frame.brkStack.pop();
		return null;
	}
	
	// TODO MINE
	@Override
	public Object visitArrayType(ArrayType ast, Object o) {
		Frame frame = (Frame) o;
		String str = ((IntExpr)ast.E).IL.spelling;
		int len = Integer.parseInt(str);
		emitICONST(len);
		frame.push();
		emit(JVM.MYARRAY, ast.T.toString());
		return null;
	}

	// TODO MINE
	@Override
	public Object visitIfStmt(IfStmt ast, Object o) {
		String label1 = ((Frame)o).getNewLabel();
		String label2 = ((Frame)o).getNewLabel();
		ast.E.visit(this, o);
		emit(JVM.IFEQ, label1);
		ast.S1.visit(this, o);
		emit(JVM.GOTO, label2);
		emit(label1 + ":");
		ast.S2.visit(this, o);
		emit(label2 + ":");
		return null;
	}
	
	// TODO mine
	@Override
	public Object visitAssignExpr(AssignExpr ast, Object o) {
		Frame frame = (Frame) o;
		if(ast.E1 instanceof VarExpr) {
			VarExpr varExpr = (VarExpr)ast.E1;
			SimpleVar simpleVar = (SimpleVar)varExpr.V;
			ast.E2.visit(this, o);
			boolean rett = ast.parent instanceof AssignExpr;
			if(rett) {
				emit(JVM.DUP);
			}
			boolean ret = simpleVar.I.decl instanceof GlobalVarDecl;
			if(!ret) {
				if(!ast.type.isFloatType()) {
					emitISTORE(simpleVar.I);
				} else {
					emitFSTORE(simpleVar.I);
				}
			} else {
				emitPUTSTATIC(VCtoJavaType(simpleVar.type), simpleVar.I.spelling);
				
			}
			return null;
		}
		if(ast.E1 instanceof ArrayExpr) {
			ArrayExpr arrayExpr = (ArrayExpr) ast.E1;
			arrayExpr.V.visit(this, o);
			arrayExpr.E.visit(this, o);
			ast.E2.visit(this, o);
			if (ast.E2.type.isBooleanType()){
				emit(JVM.BARRSTORE);
			} else if(ast.E2.type.isFloatType()) {
				emit(JVM.FARRSTORE);
			} else {
				emit(JVM.IARRSTORE);
			}
			frame.pop(3);
			return null;
		}
		return null;
	}

	// TODO MINE
	public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
		Frame frame = (Frame) o;
		ast.index = frame.getNewIndex();
		String T = VCtoJavaType(ast.T);

		emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
		// TODO MINE
		ArrayType at = null;
		boolean isArray = ast.T.isArrayType();
		if(!isArray) {
			;
		} else {
			at = (ArrayType) ast.T;
			at.visit(this, o);
			if(ast.E.isEmptyExpr()) {
				emitASTORE(ast.index);
				frame.pop();
				return null;
			} else {
				ast.E.visit(this, o);
				emitASTORE(ast.index);
				frame.pop();
				return null;
			}
		}

		if (!ast.E.isEmptyExpr()) {
			ast.E.visit(this, o);

			if (ast.T.equals(StdEnvironment.floatType)) {
				if (ast.index >= 0 && ast.index <= 3) 
					emit(JVM.FSTORE + "_" + ast.index); 
				else
					emit(JVM.FSTORE, ast.index); 
				frame.pop();
			} else {
				if (ast.index >= 0 && ast.index <= 3) 
					emit(JVM.ISTORE + "_" + ast.index); 
				else
					emit(JVM.ISTORE, ast.index); 
				frame.pop();
			}
		}
		return null;
	}
	
	
	// TODO MINE
	public Object visitSimpleVar(SimpleVar ast, Object o) {
		Frame frame = (Frame) o;
		boolean ret = ast.I.decl instanceof GlobalVarDecl;
		int num;
		if(!ret) {
			Decl decl = (Decl)ast.I.decl;
			num = decl.index;
			if(ret) {
				emitGETSTATIC(VCtoJavaType(ast.type), ast.I.spelling);
			} else {
				boolean rett = ast.type.isArrayType();
				boolean rett2 = ast.type.isFloatType();
				if(rett2) {
					emitFLOAD(num);
				} else if(rett) {
					emitALOAD(num);
				} else {
					emitILOAD(num);
				}
			}
		} else {
			emitGETSTATIC(VCtoJavaType(ast.type), ast.I.spelling);
		}
		frame.push();
		return null;
	}
	
	// TODO MINE
	@Override
	public Object visitInitExpr(InitExpr ast, Object o) {
		Frame frame = (Frame) o;
		List mylist = ast.IL;
		int counter;
		ExprList exprs = null;
		for( counter = 0;!mylist.isEmpty(); counter++) {
			exprs = (ExprList)mylist;
			emit(JVM.DUP);
			frame.push();
			emitICONST(counter);
			frame.push();
			exprs.E.visit(this, o);
			if(exprs.E.type.isBooleanType()) {
				emit(JVM.BARRSTORE);
				frame.pop(3);
				mylist = exprs.EL;
			} else if (exprs.E.type.isFloatType()){
				emit(JVM.FARRSTORE);
				frame.pop(3);
				mylist = exprs.EL;
			} else {
				emit(JVM.IARRSTORE);
				frame.pop(3);
				mylist = exprs.EL;
			}
		}
		return null;
	}
	
	// TODO MINE
	@Override
	public Object visitWhileStmt(WhileStmt ast, Object o) {
		Frame frame = (Frame)o;
		String label1 = frame.getNewLabel();
		String label2 = frame.getNewLabel();
		frame.conStack.push(label1);
		frame.brkStack.push(label2);
		emit(label1 + ":");
		ast.E.visit(this, o);
		emit(JVM.IFEQ, label2);
		ast.S.visit(this, o);
		emit(JVM.GOTO, label1);
		emit(label2 + ":");
		return null;
	}
}
