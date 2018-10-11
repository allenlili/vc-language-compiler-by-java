package VC.Checker;

import VC.ASTs.AST;
import VC.ASTs.Arg;
import VC.ASTs.ArgList;
import VC.ASTs.ArrayExpr;
import VC.ASTs.ArrayType;
import VC.ASTs.AssignExpr;
import VC.ASTs.BinaryExpr;
import VC.ASTs.BooleanExpr;
import VC.ASTs.BooleanLiteral;
import VC.ASTs.BooleanType;
import VC.ASTs.BreakStmt;
import VC.ASTs.CallExpr;
import VC.ASTs.CompoundStmt;
import VC.ASTs.ContinueStmt;
import VC.ASTs.Decl;
import VC.ASTs.DeclList;
import VC.ASTs.EmptyArgList;
import VC.ASTs.EmptyCompStmt;
import VC.ASTs.EmptyDeclList;
import VC.ASTs.EmptyExpr;
import VC.ASTs.EmptyExprList;
import VC.ASTs.EmptyParaList;
import VC.ASTs.EmptyStmt;
import VC.ASTs.EmptyStmtList;
import VC.ASTs.ErrorType;
import VC.ASTs.Expr;
import VC.ASTs.ExprList;
import VC.ASTs.ExprStmt;
import VC.ASTs.FloatExpr;
import VC.ASTs.FloatLiteral;
import VC.ASTs.FloatType;
import VC.ASTs.ForStmt;
import VC.ASTs.FuncDecl;
import VC.ASTs.GlobalVarDecl;
import VC.ASTs.Ident;
import VC.ASTs.IfStmt;
import VC.ASTs.InitExpr;
import VC.ASTs.IntExpr;
import VC.ASTs.IntLiteral;
import VC.ASTs.IntType;
import VC.ASTs.List;
import VC.ASTs.LocalVarDecl;
import VC.ASTs.Operator;
import VC.ASTs.ParaDecl;
import VC.ASTs.ParaList;
import VC.ASTs.Program;
import VC.ASTs.ReturnStmt;
import VC.ASTs.SimpleVar;
import VC.ASTs.Stmt;
import VC.ASTs.StmtList;
import VC.ASTs.StringExpr;
import VC.ASTs.StringLiteral;
import VC.ASTs.StringType;
import VC.ASTs.Type;
import VC.ASTs.UnaryExpr;
import VC.ASTs.Var;
import VC.ASTs.VarExpr;
import VC.ASTs.Visitor;
import VC.ASTs.VoidType;
import VC.ASTs.WhileStmt;
import VC.ErrorReporter;
import VC.Scanner.SourcePosition;
import VC.StdEnvironment;

public final class Checker
  implements Visitor
{
  private String[] errMesg = { "*0: main function is missing", "*1: return type of main is not int", "*2: identifier redeclared", "*3: identifier declared void", "*4: identifier declared void[]", "*5: identifier undeclared", "*6: incompatible type for =", "*7: invalid lvalue in assignment", "*8: incompatible type for return", "*9: incompatible type for this binary operator", "*10: incompatible type for this unary operator", "*11: attempt to use an array/fuction as a scalar", "*12: attempt to use a scalar/function as an array", "*13: wrong type for element in array initialiser", "*14: invalid initialiser: array initialiser for scalar", "*15: invalid initialiser: scalar initialiser for array", "*16: excess elements in array initialiser", "*17: array subscript is not an integer", "*18: array size missing", "*19: attempt to reference a scalar/array as a function", "*20: if conditional is not boolean", "*21: for conditional is not boolean", "*22: while conditional is not boolean", "*23: break must be in a while/for", "*24: continue must be in a while/for", "*25: too many actual parameters", "*26: too few actual parameters", "*27: wrong type for actual parameter", "*28: misc 1", "*29: misc 2", "*30: statement(s) not reached", "*31: missing return statement" };
  private SymbolTable idTable;
  private static SourcePosition dummyPos = new SourcePosition();
  private ErrorReporter reporter;
  private int whileLevel = 0;
  
  public Checker(ErrorReporter paramErrorReporter)
  {
    this.reporter = paramErrorReporter;
    this.idTable = new SymbolTable();
    establishStdEnvironment();
  }
  
  private Expr i2f(Expr paramExpr)
  {
    UnaryExpr localUnaryExpr = new UnaryExpr(new Operator("i2f", paramExpr.position), paramExpr, paramExpr.position);
    localUnaryExpr.type = StdEnvironment.floatType;
    localUnaryExpr.parent = paramExpr;
    return localUnaryExpr;
  }
  
  private Expr checkAssignment(Type paramType, Expr paramExpr, String paramString, SourcePosition paramSourcePosition)
  {
    if (!paramType.assignable(paramExpr.type)) {
      this.reporter.reportError(paramString, "", paramSourcePosition);
    } else if (!paramType.equals(paramExpr.type)) {
      return i2f(paramExpr);
    }
    return paramExpr;
  }
  
  private void declareVariable(Ident paramIdent, Decl paramDecl)
  {
    IdEntry localIdEntry = this.idTable.retrieveOneLevel(paramIdent.spelling);
    if (localIdEntry != null) {
      this.reporter.reportError(this.errMesg[2] + ": %", paramIdent.spelling, paramIdent.position);
    }
    this.idTable.insert(paramIdent.spelling, paramDecl);
  }
  
  private void declareFunction(Ident paramIdent, Decl paramDecl)
  {
    IdEntry localIdEntry = this.idTable.retrieveOneLevel(paramIdent.spelling);
    if (localIdEntry != null) {
      this.reporter.reportError(this.errMesg[2] + ": %", paramIdent.spelling, paramIdent.position);
    }
    this.idTable.insert(paramIdent.spelling, paramDecl);
  }
  
  void reportError(String paramString1, Type paramType, String paramString2, SourcePosition paramSourcePosition)
  {
    if (paramType == StdEnvironment.errorType) {
      this.reporter.reportError(paramString1, "", paramSourcePosition);
    } else {
      this.reporter.reportError(paramString1 + " (found: " + paramType + ", required: " + paramString2 + ")", "", paramSourcePosition);
    }
  }
  
  public void check(AST paramAST)
  {
    paramAST.visit(this, null);
  }
  
  public Object visitProgram(Program paramProgram, Object paramObject)
  {
    paramProgram.FL.visit(this, null);
    
    Decl localDecl = this.idTable.retrieve("main");
    if ((localDecl == null) || (!(localDecl instanceof FuncDecl))) {
      this.reporter.reportError(this.errMesg[0], "", paramProgram.position);
    } else if (!StdEnvironment.intType.equals(((FuncDecl)localDecl).T)) {
      this.reporter.reportError(this.errMesg[1], "", paramProgram.position);
    }
    return null;
  }
  
  public Object visitIfStmt(IfStmt paramIfStmt, Object paramObject)
  {
    Type localType = (Type)paramIfStmt.E.visit(this, null);
    if (!localType.equals(StdEnvironment.booleanType)) {
      this.reporter.reportError(this.errMesg[20] + " (found: " + localType.toString() + ")", "", paramIfStmt.E.position);
    }
    paramIfStmt.S1.visit(this, paramObject);
    paramIfStmt.S2.visit(this, paramObject);
    return null;
  }
  
  public Object visitCompoundStmt(CompoundStmt paramCompoundStmt, Object paramObject)
  {
    this.idTable.openScope();
    if ((paramObject != null) && ((paramObject instanceof FuncDecl)))
    {
      FuncDecl localFuncDecl = (FuncDecl)paramObject;
      localFuncDecl.PL.visit(this, null);
      paramCompoundStmt.DL.visit(this, null);
      paramCompoundStmt.SL.visit(this, (Type)localFuncDecl.T.visit(this, null));
    }
    else
    {
      paramCompoundStmt.DL.visit(this, null);
      paramCompoundStmt.SL.visit(this, paramObject);
    }
    this.idTable.closeScope();
    return null;
  }
  
  public Object visitStmtList(StmtList paramStmtList, Object paramObject)
  {
    paramStmtList.S.visit(this, paramObject);
    if (((paramStmtList.S instanceof ReturnStmt)) && ((paramStmtList.SL instanceof StmtList))) {
      this.reporter.reportError(this.errMesg[30], "", paramStmtList.SL.position);
    }
    paramStmtList.SL.visit(this, paramObject);
    return null;
  }
  
  public Object visitForStmt(ForStmt paramForStmt, Object paramObject)
  {
    this.whileLevel += 1;
    paramForStmt.E1.visit(this, null);
    Type localType = (Type)paramForStmt.E2.visit(this, null);
    if ((!paramForStmt.E2.isEmptyExpr()) && (!localType.equals(StdEnvironment.booleanType))) {
      this.reporter.reportError(this.errMesg[21] + " (found: " + localType.toString() + ")", "", paramForStmt.E2.position);
    }
    paramForStmt.E3.visit(this, null);
    paramForStmt.S.visit(this, paramObject);
    this.whileLevel -= 1;
    return null;
  }
  
  public Object visitWhileStmt(WhileStmt paramWhileStmt, Object paramObject)
  {
    this.whileLevel += 1;
    Type localType = (Type)paramWhileStmt.E.visit(this, null);
    if (!localType.equals(StdEnvironment.booleanType)) {
      this.reporter.reportError(this.errMesg[22] + " (found: " + localType.toString() + ")", "", paramWhileStmt.E.position);
    }
    paramWhileStmt.S.visit(this, paramObject);
    this.whileLevel -= 1;
    return null;
  }
  
  public Object visitBreakStmt(BreakStmt paramBreakStmt, Object paramObject)
  {
    if (this.whileLevel < 1) {
      this.reporter.reportError(this.errMesg[23], "", paramBreakStmt.position);
    }
    return null;
  }
  
  public Object visitContinueStmt(ContinueStmt paramContinueStmt, Object paramObject)
  {
    if (this.whileLevel < 1) {
      this.reporter.reportError(this.errMesg[24], "", paramContinueStmt.position);
    }
    return null;
  }
  
  public Object visitReturnStmt(ReturnStmt paramReturnStmt, Object paramObject)
  {
    Type localType = (Type)paramObject;
    paramReturnStmt.E.visit(this, paramObject);
    paramReturnStmt.E = checkAssignment(localType, paramReturnStmt.E, this.errMesg[8], paramReturnStmt.position);
    return null;
  }
  
  public Object visitExprStmt(ExprStmt paramExprStmt, Object paramObject)
  {
    paramExprStmt.E.visit(this, paramObject);
    return null;
  }
  
  public Object visitEmptyCompStmt(EmptyCompStmt paramEmptyCompStmt, Object paramObject)
  {
    this.idTable.openScope();
    if ((paramObject != null) && ((paramObject instanceof FuncDecl)))
    {
      FuncDecl localFuncDecl = (FuncDecl)paramObject;
      localFuncDecl.PL.visit(this, null);
    }
    this.idTable.closeScope();
    return null;
  }
  
  public Object visitEmptyStmt(EmptyStmt paramEmptyStmt, Object paramObject)
  {
    return null;
  }
  
  public Object visitEmptyStmtList(EmptyStmtList paramEmptyStmtList, Object paramObject)
  {
    return null;
  }
  
  public Object visitAssignExpr(AssignExpr paramAssignExpr, Object paramObject)
  {
    paramAssignExpr.E1.visit(this, paramObject);
    paramAssignExpr.E2.visit(this, null);
    if ((!(paramAssignExpr.E1 instanceof VarExpr)) && (!(paramAssignExpr.E1 instanceof ArrayExpr)))
    {
      this.reporter.reportError(this.errMesg[7], "", paramAssignExpr.position);
    }
    else if ((paramAssignExpr.E1 instanceof VarExpr))
    {
      SimpleVar localSimpleVar = (SimpleVar)((VarExpr)paramAssignExpr.E1).V;
      Decl localDecl = (Decl)localSimpleVar.I.decl;
      if ((localDecl instanceof FuncDecl)) {
        this.reporter.reportError(this.errMesg[7] + ": %", localSimpleVar.I.spelling, paramAssignExpr.position);
      }
    }
    paramAssignExpr.E2 = checkAssignment(paramAssignExpr.E1.type, paramAssignExpr.E2, this.errMesg[6], paramAssignExpr.position);
    
    paramAssignExpr.type = paramAssignExpr.E2.type;
    return paramAssignExpr.type;
  }
  
  public Object visitBinaryExpr(BinaryExpr paramBinaryExpr, Object paramObject)
  {
    Type localType1 = (Type)paramBinaryExpr.E1.visit(this, paramObject);
    Type localType2 = (Type)paramBinaryExpr.E2.visit(this, paramObject);
    Type localType3 = localType1;
    String str = paramBinaryExpr.O.spelling;
    int i = 0;
    
    int j = (str.equals("&&")) || (str.equals("||")) ? 1 : 0;
    int k = (str.equals("==")) || (str.equals("!=")) ? 1 : 0;
    int m = (str.equals("<=")) || (str.equals(">=")) || (str.equals("<")) || (str.equals(">")) ? 1 : 0;
    if ((localType1.isErrorType()) || (localType2.isErrorType()))
    {
      localType3 = StdEnvironment.errorType;
    }
    else if ((localType1.isVoidType()) || (localType2.isVoidType()))
    {
      i = 1;
    }
    else if ((localType1.isStringType()) || (localType2.isStringType()))
    {
      i = 1;
    }
    else if ((localType1.isArrayType()) || (localType2.isArrayType()))
    {
      i = 1;
    }
    else if ((localType1.isBooleanType()) || (localType2.isBooleanType()))
    {
      if ((!localType1.equals(localType2)) || ((j == 0) && (k == 0))) {
        i = 1;
      }
      paramBinaryExpr.O.spelling = ("i" + paramBinaryExpr.O.spelling);
    }
    else if (j != 0)
    {
      i = 1;
    }
    else if (!localType1.equals(localType2))
    {
      localType3 = StdEnvironment.floatType;
      paramBinaryExpr.O.spelling = ("f" + paramBinaryExpr.O.spelling);
      if (!localType3.equals(localType1)) {
        paramBinaryExpr.E1 = i2f(paramBinaryExpr.E1);
      } else {
        paramBinaryExpr.E2 = i2f(paramBinaryExpr.E2);
      }
    }
    else if (localType1.isFloatType())
    {
      paramBinaryExpr.O.spelling = ("f" + paramBinaryExpr.O.spelling);
    }
    else
    {
      paramBinaryExpr.O.spelling = ("i" + paramBinaryExpr.O.spelling);
    }
    if (i != 0)
    {
      this.reporter.reportError(this.errMesg[9] + ": %", str, paramBinaryExpr.position);
      localType3 = StdEnvironment.errorType;
    }
    paramBinaryExpr.type = ((k != 0) || (m != 0) ? StdEnvironment.booleanType : localType3);
    return paramBinaryExpr.type;
  }
  
  public Object visitUnaryExpr(UnaryExpr paramUnaryExpr, Object paramObject)
  {
    Type localType = (Type)paramUnaryExpr.E.visit(this, paramObject);
    String str = paramUnaryExpr.O.spelling;
    int i = 0;
    if (localType.isErrorType()) {
      localType = StdEnvironment.errorType;
    } else if ((localType.isVoidType()) || (localType.isStringType()) || (localType.isArrayType())) {
      i = 1;
    } else if (((str.equals("!")) && (!localType.isBooleanType())) || ((!str.equals("!")) && (localType.isBooleanType()))) {
      i = 1;
    }
    if (i != 0)
    {
      this.reporter.reportError(this.errMesg[10] + ": %", str, paramUnaryExpr.position);
      localType = StdEnvironment.errorType;
    }
    else if (localType.isFloatType())
    {
      paramUnaryExpr.O.spelling = ("f" + paramUnaryExpr.O.spelling);
    }
    else
    {
      paramUnaryExpr.O.spelling = ("i" + paramUnaryExpr.O.spelling);
    }
    paramUnaryExpr.type = localType;
    return paramUnaryExpr.type;
  }
  
  public Object visitCallExpr(CallExpr paramCallExpr, Object paramObject)
  {
    Decl localDecl = (Decl)paramCallExpr.I.visit(this, null);
    if (localDecl == null)
    {
      this.reporter.reportError(this.errMesg[5] + ": %", paramCallExpr.I.spelling, paramCallExpr.position);
      paramCallExpr.type = StdEnvironment.errorType;
    }
    else if ((localDecl instanceof FuncDecl))
    {
      paramCallExpr.AL.visit(this, ((FuncDecl)localDecl).PL);
      paramCallExpr.type = ((FuncDecl)localDecl).T;
    }
    else
    {
      this.reporter.reportError(this.errMesg[19] + ": %", paramCallExpr.I.spelling, paramCallExpr.I.position);
      paramCallExpr.type = StdEnvironment.errorType;
    }
    return paramCallExpr.type;
  }
  
  public Object visitArrayExpr(ArrayExpr paramArrayExpr, Object paramObject)
  {
    Type localType1 = (Type)paramArrayExpr.V.visit(this, paramObject);
    if (localType1.isArrayType())
    {
      localType1 = ((ArrayType)localType1).T;
    }
    else if (!localType1.isErrorType())
    {
      this.reporter.reportError(this.errMesg[12], "", paramArrayExpr.position);
      localType1 = StdEnvironment.errorType;
    }
    Type localType2 = (Type)paramArrayExpr.E.visit(this, paramObject);
    if ((!localType2.isIntType()) && (!localType2.isErrorType())) {
      this.reporter.reportError(this.errMesg[17], "", paramArrayExpr.position);
    }
    paramArrayExpr.type = localType1;
    return localType1;
  }
  
  public Object visitInitExpr(InitExpr paramInitExpr, Object paramObject)
  {
    Type localType = (Type)paramObject;
    if (!localType.isArrayType())
    {
      this.reporter.reportError(this.errMesg[14], " ", paramInitExpr.position);
      paramInitExpr.type = StdEnvironment.errorType;
      return paramInitExpr.type;
    }
    return paramInitExpr.IL.visit(this, ((ArrayType)localType).T);
  }
  
  public Object visitExprList(ExprList paramExprList, Object paramObject)
  {
    Type localType = (Type)paramObject;
    paramExprList.E.visit(this, paramObject);
    paramExprList.E = checkAssignment(localType, paramExprList.E, this.errMesg[13] + ": at position " + paramExprList.index, paramExprList.E.position);
    if ((paramExprList.EL instanceof ExprList))
    {
      ((ExprList)paramExprList.EL).index = (paramExprList.index + 1);
      return paramExprList.EL.visit(this, paramObject);
    }
    return new Integer(paramExprList.index + 1);
  }
  
  public Object visitEmptyExprList(EmptyExprList paramEmptyExprList, Object paramObject)
  {
    return null;
  }
  
  public Object visitEmptyExpr(EmptyExpr paramEmptyExpr, Object paramObject)
  {
    if ((paramEmptyExpr.parent instanceof ReturnStmt)) {
      paramEmptyExpr.type = StdEnvironment.voidType;
    } else {
      paramEmptyExpr.type = StdEnvironment.errorType;
    }
    return paramEmptyExpr.type;
  }
  
  public Object visitBooleanExpr(BooleanExpr paramBooleanExpr, Object paramObject)
  {
    paramBooleanExpr.type = StdEnvironment.booleanType;
    return paramBooleanExpr.type;
  }
  
  public Object visitIntExpr(IntExpr paramIntExpr, Object paramObject)
  {
    paramIntExpr.type = StdEnvironment.intType;
    return paramIntExpr.type;
  }
  
  public Object visitFloatExpr(FloatExpr paramFloatExpr, Object paramObject)
  {
    paramFloatExpr.type = StdEnvironment.floatType;
    return paramFloatExpr.type;
  }
  
  public Object visitVarExpr(VarExpr paramVarExpr, Object paramObject)
  {
    paramVarExpr.type = ((Type)paramVarExpr.V.visit(this, null));
    return paramVarExpr.type;
  }
  
  public Object visitStringExpr(StringExpr paramStringExpr, Object paramObject)
  {
    paramStringExpr.type = StdEnvironment.stringType;
    return paramStringExpr.type;
  }
  
  public Object visitFuncDecl(FuncDecl paramFuncDecl, Object paramObject)
  {
    declareFunction(paramFuncDecl.I, paramFuncDecl);
    if ((paramFuncDecl.S.isEmptyCompStmt()) && 
      (!paramFuncDecl.T.equals(StdEnvironment.voidType))) {
      this.reporter.reportError(this.errMesg[31], "", paramFuncDecl.position);
    }
    paramFuncDecl.S.visit(this, paramFuncDecl);
    
    return null;
  }
  
  public Object visitDeclList(DeclList paramDeclList, Object paramObject)
  {
    paramDeclList.D.visit(this, null);
    paramDeclList.DL.visit(this, null);
    return null;
  }
  
  public Object visitEmptyDeclList(EmptyDeclList paramEmptyDeclList, Object paramObject)
  {
    return null;
  }
  
  public Object visitGlobalVarDecl(GlobalVarDecl paramGlobalVarDecl, Object paramObject)
  {
    declareVariable(paramGlobalVarDecl.I, paramGlobalVarDecl);
    if (paramGlobalVarDecl.T.isVoidType())
    {
      this.reporter.reportError(this.errMesg[3] + ": %", paramGlobalVarDecl.I.spelling, paramGlobalVarDecl.I.position);
    }
    else if (paramGlobalVarDecl.T.isArrayType())
    {
      if (((ArrayType)paramGlobalVarDecl.T).T.isVoidType()) {
        this.reporter.reportError(this.errMesg[4] + ": %", paramGlobalVarDecl.I.spelling, paramGlobalVarDecl.I.position);
      }
      if ((((ArrayType)paramGlobalVarDecl.T).E.isEmptyExpr()) && (!(paramGlobalVarDecl.E instanceof InitExpr))) {
        this.reporter.reportError(this.errMesg[18] + ": %", paramGlobalVarDecl.I.spelling, paramGlobalVarDecl.I.position);
      }
    }
    Object localObject = paramGlobalVarDecl.E.visit(this, paramGlobalVarDecl.T);
    if (paramGlobalVarDecl.T.isArrayType())
    {
      if ((paramGlobalVarDecl.E instanceof InitExpr))
      {
        Integer localInteger = (Integer)localObject;
        ArrayType localArrayType = (ArrayType)paramGlobalVarDecl.T;
        if (localArrayType.E.isEmptyExpr())
        {
          localArrayType.E = new IntExpr(new IntLiteral(localInteger.toString(), dummyPos), dummyPos);
        }
        else
        {
          int i = Integer.parseInt(((IntExpr)localArrayType.E).IL.spelling);
          int j = localInteger.intValue();
          if (i < j) {
            this.reporter.reportError(this.errMesg[16] + ": %", paramGlobalVarDecl.I.spelling, paramGlobalVarDecl.position);
          }
        }
      }
      else if (!paramGlobalVarDecl.E.isEmptyExpr())
      {
        this.reporter.reportError(this.errMesg[15] + ": %", paramGlobalVarDecl.I.spelling, paramGlobalVarDecl.position);
      }
    }
    else {
      paramGlobalVarDecl.E = checkAssignment(paramGlobalVarDecl.T, paramGlobalVarDecl.E, this.errMesg[6], paramGlobalVarDecl.position);
    }
    return null;
  }
  
  public Object visitLocalVarDecl(LocalVarDecl paramLocalVarDecl, Object paramObject)
  {
    declareVariable(paramLocalVarDecl.I, paramLocalVarDecl);
    if (paramLocalVarDecl.T.isVoidType())
    {
      this.reporter.reportError(this.errMesg[3] + ": %", paramLocalVarDecl.I.spelling, paramLocalVarDecl.I.position);
    }
    else if (paramLocalVarDecl.T.isArrayType())
    {
      if (((ArrayType)paramLocalVarDecl.T).T.isVoidType()) {
        this.reporter.reportError(this.errMesg[4] + ": %", paramLocalVarDecl.I.spelling, paramLocalVarDecl.I.position);
      }
      if ((((ArrayType)paramLocalVarDecl.T).E.isEmptyExpr()) && (!(paramLocalVarDecl.E instanceof InitExpr))) {
        this.reporter.reportError(this.errMesg[18] + ": %", paramLocalVarDecl.I.spelling, paramLocalVarDecl.I.position);
      }
    }
    Object localObject = paramLocalVarDecl.E.visit(this, paramLocalVarDecl.T);
    if (paramLocalVarDecl.T.isArrayType())
    {
      if ((paramLocalVarDecl.E instanceof InitExpr))
      {
        Integer localInteger = (Integer)localObject;
        ArrayType localArrayType = (ArrayType)paramLocalVarDecl.T;
        if (localArrayType.E.isEmptyExpr())
        {
          localArrayType.E = new IntExpr(new IntLiteral(localInteger.toString(), dummyPos), dummyPos);
        }
        else
        {
          int i = Integer.parseInt(((IntExpr)localArrayType.E).IL.spelling);
          int j = localInteger.intValue();
          if (i < j) {
            this.reporter.reportError(this.errMesg[16] + ": %", paramLocalVarDecl.I.spelling, paramLocalVarDecl.position);
          }
        }
      }
      else if (!paramLocalVarDecl.E.isEmptyExpr())
      {
        this.reporter.reportError(this.errMesg[15] + ": %", paramLocalVarDecl.I.spelling, paramLocalVarDecl.position);
      }
    }
    else {
      paramLocalVarDecl.E = checkAssignment(paramLocalVarDecl.T, paramLocalVarDecl.E, this.errMesg[6], paramLocalVarDecl.position);
    }
    return null;
  }
  
  public Object visitParaList(ParaList paramParaList, Object paramObject)
  {
    paramParaList.P.visit(this, null);
    paramParaList.PL.visit(this, null);
    return null;
  }
  
  public Object visitParaDecl(ParaDecl paramParaDecl, Object paramObject)
  {
    declareVariable(paramParaDecl.I, paramParaDecl);
    if (paramParaDecl.T.isVoidType()) {
      this.reporter.reportError(this.errMesg[3] + ": %", paramParaDecl.I.spelling, paramParaDecl.I.position);
    } else if ((paramParaDecl.T.isArrayType()) && 
      (((ArrayType)paramParaDecl.T).T.isVoidType())) {
      this.reporter.reportError(this.errMesg[4] + ": %", paramParaDecl.I.spelling, paramParaDecl.I.position);
    }
    return null;
  }
  
  public Object visitEmptyParaList(EmptyParaList paramEmptyParaList, Object paramObject)
  {
    return null;
  }
  
  public Object visitEmptyArgList(EmptyArgList paramEmptyArgList, Object paramObject)
  {
    List localList = (List)paramObject;
    if (!localList.isEmptyParaList()) {
      this.reporter.reportError(this.errMesg[26], "", paramEmptyArgList.position);
    }
    return null;
  }
  
  public Object visitArgList(ArgList paramArgList, Object paramObject)
  {
    List localList = (List)paramObject;
    if (localList.isEmptyParaList())
    {
      this.reporter.reportError(this.errMesg[25], "", paramArgList.position);
    }
    else
    {
      paramArgList.A.visit(this, ((ParaList)localList).P);
      paramArgList.AL.visit(this, ((ParaList)localList).PL);
    }
    return null;
  }
  
  public Object visitArg(Arg paramArg, Object paramObject)
  {
    ParaDecl localParaDecl = (ParaDecl)paramObject;
    Type localType1 = (Type)paramArg.E.visit(this, null);
    
    int i = 0;
    
    Type localType2 = localParaDecl.T;
    if (localType2.isArrayType())
    {
      if (!localType1.isArrayType())
      {
        i = 1;
      }
      else
      {
        Type localType3 = ((ArrayType)localType2).T;
        Type localType4 = ((ArrayType)localType1).T;
        if (!localType3.assignable(localType4)) {
          i = 1;
        }
      }
    }
    else if (!localParaDecl.T.assignable(localType1)) {
      i = 1;
    }
    if (i != 0) {
      this.reporter.reportError(this.errMesg[27] + ": %", localParaDecl.I.spelling, paramArg.E.position);
    }
    if ((localParaDecl.T.equals(StdEnvironment.floatType)) && (localType1.equals(StdEnvironment.intType))) {
      paramArg.E = i2f(paramArg.E);
    }
    return null;
  }
  
  public Object visitErrorType(ErrorType paramErrorType, Object paramObject)
  {
    return StdEnvironment.errorType;
  }
  
  public Object visitBooleanType(BooleanType paramBooleanType, Object paramObject)
  {
    return StdEnvironment.booleanType;
  }
  
  public Object visitIntType(IntType paramIntType, Object paramObject)
  {
    return StdEnvironment.intType;
  }
  
  public Object visitFloatType(FloatType paramFloatType, Object paramObject)
  {
    return StdEnvironment.floatType;
  }
  
  public Object visitStringType(StringType paramStringType, Object paramObject)
  {
    return StdEnvironment.stringType;
  }
  
  public Object visitVoidType(VoidType paramVoidType, Object paramObject)
  {
    return StdEnvironment.voidType;
  }
  
  public Object visitArrayType(ArrayType paramArrayType, Object paramObject)
  {
    return paramArrayType;
  }
  
  public Object visitIdent(Ident paramIdent, Object paramObject)
  {
    Decl localDecl = this.idTable.retrieve(paramIdent.spelling);
    if (localDecl != null) {
      paramIdent.decl = localDecl;
    }
    return localDecl;
  }
  
  public Object visitBooleanLiteral(BooleanLiteral paramBooleanLiteral, Object paramObject)
  {
    return StdEnvironment.booleanType;
  }
  
  public Object visitIntLiteral(IntLiteral paramIntLiteral, Object paramObject)
  {
    return StdEnvironment.intType;
  }
  
  public Object visitFloatLiteral(FloatLiteral paramFloatLiteral, Object paramObject)
  {
    return StdEnvironment.floatType;
  }
  
  public Object visitStringLiteral(StringLiteral paramStringLiteral, Object paramObject)
  {
    return StdEnvironment.stringType;
  }
  
  public Object visitOperator(Operator paramOperator, Object paramObject)
  {
    return null;
  }
  
  public Object visitSimpleVar(SimpleVar paramSimpleVar, Object paramObject)
  {
    paramSimpleVar.type = StdEnvironment.errorType;
    Decl localDecl = (Decl)paramSimpleVar.I.visit(this, null);
    if (localDecl == null) {
      this.reporter.reportError(this.errMesg[5] + ": %", paramSimpleVar.I.spelling, paramSimpleVar.position);
    } else if ((localDecl instanceof FuncDecl)) {
      this.reporter.reportError(this.errMesg[11] + ": %", paramSimpleVar.I.spelling, paramSimpleVar.I.position);
    } else {
      paramSimpleVar.type = localDecl.T;
    }
    if ((paramSimpleVar.type.isArrayType()) && ((paramSimpleVar.parent instanceof VarExpr)) && (!(paramSimpleVar.parent.parent instanceof Arg))) {
      this.reporter.reportError(this.errMesg[11] + ": %", paramSimpleVar.I.spelling, paramSimpleVar.I.position);
    }
    return paramSimpleVar.type;
  }
  
  private FuncDecl declareStdFunc(Type paramType, String paramString, List paramList)
  {
    FuncDecl localFuncDecl = new FuncDecl(paramType, new Ident(paramString, dummyPos), paramList, new EmptyStmt(dummyPos), dummyPos);
    
    this.idTable.insert(paramString, localFuncDecl);
    return localFuncDecl;
  }
  
  private static final Ident dummyI = new Ident("x", dummyPos);
  
  private void establishStdEnvironment()
  {
    StdEnvironment.booleanType = new BooleanType(dummyPos);
    StdEnvironment.intType = new IntType(dummyPos);
    StdEnvironment.floatType = new FloatType(dummyPos);
    StdEnvironment.stringType = new StringType(dummyPos);
    StdEnvironment.voidType = new VoidType(dummyPos);
    StdEnvironment.errorType = new ErrorType(dummyPos);
    
    StdEnvironment.getIntDecl = declareStdFunc(StdEnvironment.intType, "getInt", new EmptyParaList(dummyPos));
    
    StdEnvironment.putIntDecl = declareStdFunc(StdEnvironment.voidType, "putInt", new ParaList(new ParaDecl(StdEnvironment.intType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putIntLnDecl = declareStdFunc(StdEnvironment.voidType, "putIntLn", new ParaList(new ParaDecl(StdEnvironment.intType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.getFloatDecl = declareStdFunc(StdEnvironment.floatType, "getFloat", new EmptyParaList(dummyPos));
    
    StdEnvironment.putFloatDecl = declareStdFunc(StdEnvironment.voidType, "putFloat", new ParaList(new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putFloatLnDecl = declareStdFunc(StdEnvironment.voidType, "putFloatLn", new ParaList(new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putBoolDecl = declareStdFunc(StdEnvironment.voidType, "putBool", new ParaList(new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putBoolLnDecl = declareStdFunc(StdEnvironment.voidType, "putBoolLn", new ParaList(new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putStringLnDecl = declareStdFunc(StdEnvironment.voidType, "putStringLn", new ParaList(new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putStringDecl = declareStdFunc(StdEnvironment.voidType, "putString", new ParaList(new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos), new EmptyParaList(dummyPos), dummyPos));
    
    StdEnvironment.putLnDecl = declareStdFunc(StdEnvironment.voidType, "putLn", new EmptyParaList(dummyPos));
  }
}
