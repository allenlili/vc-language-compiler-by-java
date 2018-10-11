package VC.Parser;

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
import VC.ASTs.EmptyStmtList;
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
import VC.ASTs.Type;
import VC.ASTs.UnaryExpr;
import VC.ASTs.Var;
import VC.ASTs.VarExpr;
import VC.ASTs.VoidType;
import VC.ASTs.WhileStmt;
import VC.ErrorReporter;
import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;

public class Parser
{
  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken;
  private SourcePosition previousTokenPosition;
  private SourcePosition dummyPos = new SourcePosition();
  
  public Parser(Scanner paramScanner, ErrorReporter paramErrorReporter)
  {
    this.scanner = paramScanner;
    this.errorReporter = paramErrorReporter;
    
    this.previousTokenPosition = new SourcePosition();
    
    this.currentToken = this.scanner.getToken();
  }
  
  void match(int paramInt)
    throws SyntaxError
  {
    if (this.currentToken.kind == paramInt)
    {
      this.previousTokenPosition = this.currentToken.position;
      this.currentToken = this.scanner.getToken();
    }
    else
    {
      syntacticError("\"%\" expected here", Token.spell(paramInt));
    }
  }
  
  void accept()
  {
    this.previousTokenPosition = this.currentToken.position;
    this.currentToken = this.scanner.getToken();
  }
  
  void syntacticError(String paramString1, String paramString2)
    throws SyntaxError
  {
    SourcePosition localSourcePosition = this.currentToken.position;
    this.errorReporter.reportError(paramString1, paramString2, localSourcePosition);
    throw new SyntaxError();
  }
  
  void start(SourcePosition paramSourcePosition)
  {
    paramSourcePosition.lineStart = this.currentToken.position.lineStart;
    paramSourcePosition.charStart = this.currentToken.position.charStart;
  }
  
  void finish(SourcePosition paramSourcePosition)
  {
    paramSourcePosition.lineFinish = this.previousTokenPosition.lineFinish;
    paramSourcePosition.charFinish = this.previousTokenPosition.charFinish;
  }
  
  void copyStart(SourcePosition paramSourcePosition1, SourcePosition paramSourcePosition2)
  {
    paramSourcePosition2.lineStart = paramSourcePosition1.lineStart;
    paramSourcePosition2.charStart = paramSourcePosition1.charStart;
  }
  
  Type cloneType(Type paramType)
  {
    SourcePosition localSourcePosition = paramType.position;
    Object localObject;
    if ((paramType instanceof IntType)) {
      localObject = new IntType(localSourcePosition);
    } else if ((paramType instanceof FloatType)) {
      localObject = new FloatType(localSourcePosition);
    } else if ((paramType instanceof BooleanType)) {
      localObject = new BooleanType(localSourcePosition);
    } else {
      localObject = new VoidType(localSourcePosition);
    }
    return (Type)localObject;
  }
  
  public Program parseProgram()
  {
    Program localProgram = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    try
    {
      List localList = parseDeclList();
      finish(localSourcePosition);
      localProgram = new Program(localList, localSourcePosition);
      if (this.currentToken.kind != 39) {
        syntacticError("\"%\" unknown type", this.currentToken.spelling);
      }
    }
    catch (SyntaxError localSyntaxError)
    {
      return null;
    }
    return localProgram;
  }
  
  List parseDeclList()
    throws SyntaxError
  {
    Decl localDecl = null;
    Object localObject1 = null;
    Object localObject2 = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    Object localObject3;
    if ((this.currentToken.kind == 9) || (this.currentToken.kind == 0) || (this.currentToken.kind == 7) || (this.currentToken.kind == 4))
    {
      localObject3 = parseType();
      Ident localIdent = parseIdent();
      if (this.currentToken.kind == 27) {
        localDecl = parseRestFuncDecl((Type)localObject3, localIdent);
      } else {
        localObject1 = parseRestVarDecl((Type)localObject3, localIdent, true);
      }
    }
    if ((this.currentToken.kind == 9) || (this.currentToken.kind == 0) || (this.currentToken.kind == 7) || (this.currentToken.kind == 4)) {
      localObject2 = parseDeclList();
    } else {
      localObject2 = new EmptyDeclList(this.dummyPos);
    }
    if (localDecl != null)
    {
      finish(localSourcePosition);
      localObject1 = new DeclList(localDecl, (List)localObject2, localSourcePosition);
    }
    else if (localObject1 != null)
    {
      localObject3 = (DeclList)localObject1;
      while (!(((DeclList)localObject3).DL instanceof EmptyDeclList)) {
        localObject3 = (DeclList)((DeclList)localObject3).DL;
      }
      if (!(localObject2 instanceof EmptyDeclList)) {
        ((DeclList)localObject3).DL = ((DeclList)localObject2);
      }
    }
    else
    {
      localObject1 = localObject2;
    }
    return (List)localObject1;
  }
  
  Decl parseRestFuncDecl(Type paramType, Ident paramIdent)
    throws SyntaxError
  {
    FuncDecl localFuncDecl = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    localSourcePosition = paramType.position;
    
    List localList = parseParaList();
    Stmt localStmt = parseCompoundStmt();
    finish(localSourcePosition);
    localFuncDecl = new FuncDecl(paramType, paramIdent, localList, localStmt, localSourcePosition);
    return localFuncDecl;
  }
  
  List parseRestVarDecl(Type paramType, Ident paramIdent, boolean paramBoolean)
    throws SyntaxError
  {
    Object localObject1 = null;
    Object localObject2 = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    copyStart(paramType.position, localSourcePosition1);
    Object localObject3;
    if (this.currentToken.kind == 29)
    {
      accept();
      if (this.currentToken.kind == 34) {
        localObject3 = parseExpr();
      } else {
        localObject3 = new EmptyExpr(this.dummyPos);
      }
      match(30);
      finish(localSourcePosition1);
      paramType = new ArrayType(paramType, (Expr)localObject3, localSourcePosition1);
    }
    if (this.currentToken.kind == 17)
    {
      accept();
      localObject3 = parseInitialiser();
    }
    else
    {
      localObject3 = new EmptyExpr(this.dummyPos);
    }
    SourcePosition localSourcePosition2 = new SourcePosition();
    copyStart(paramIdent.position, localSourcePosition2);
    finish(localSourcePosition2);
    if (paramBoolean) {
      localObject2 = new GlobalVarDecl(paramType, paramIdent, (Expr)localObject3, localSourcePosition2);
    } else {
      localObject2 = new LocalVarDecl(paramType, paramIdent, (Expr)localObject3, localSourcePosition2);
    }
    SourcePosition localSourcePosition3 = new SourcePosition();
    copyStart(paramIdent.position, localSourcePosition3);
    if (this.currentToken.kind == 32)
    {
      accept();
      if ((paramType instanceof ArrayType)) {
        paramType = ((ArrayType)paramType).T;
      }
      localObject1 = parseInitDeclaratorList(paramType, paramBoolean);
      finish(localSourcePosition3);
      localObject1 = new DeclList((Decl)localObject2, (List)localObject1, localSourcePosition3);
    }
    else
    {
      finish(localSourcePosition3);
      localObject1 = new DeclList((Decl)localObject2, new EmptyDeclList(this.dummyPos), localSourcePosition3);
    }
    match(31);
    
    return (List)localObject1;
  }
  
  List parseVarDecl()
    throws SyntaxError
  {
    List localList = null;
    
    Type localType = parseType();
    localList = parseInitDeclaratorList(localType, false);
    match(31);
    
    return localList;
  }
  
  List parseInitDeclaratorList(Type paramType, boolean paramBoolean)
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    paramType = cloneType(paramType);
    
    Decl localDecl = parseInitDeclarator(paramType, paramBoolean);
    if (this.currentToken.kind == 32)
    {
      accept();
      localObject = parseInitDeclaratorList(paramType, paramBoolean);
      finish(localSourcePosition);
      localObject = new DeclList(localDecl, (List)localObject, localSourcePosition);
    }
    else
    {
      finish(localSourcePosition);
      localObject = new DeclList(localDecl, new EmptyDeclList(this.dummyPos), localSourcePosition);
    }
    return (List)localObject;
  }
  
  Decl parseInitDeclarator(Type paramType, boolean paramBoolean)
    throws SyntaxError
  {
    Object localObject1 = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    TypeAndIdent localTypeAndIdent = parseDeclarator(paramType);
    Object localObject2 = null;
    if (this.currentToken.kind == 17)
    {
      accept();
      localObject2 = parseInitialiser();
    }
    else
    {
      localObject2 = new EmptyExpr(this.dummyPos);
    }
    finish(localSourcePosition);
    if (paramBoolean) {
      localObject1 = new GlobalVarDecl(localTypeAndIdent.tAST, localTypeAndIdent.iAST, (Expr)localObject2, localSourcePosition);
    } else {
      localObject1 = new LocalVarDecl(localTypeAndIdent.tAST, localTypeAndIdent.iAST, (Expr)localObject2, localSourcePosition);
    }
    return (Decl)localObject1;
  }
  
  TypeAndIdent parseDeclarator(Type paramType)
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    copyStart(paramType.position, localSourcePosition1);
    
    Ident localIdent = parseIdent();
    if (this.currentToken.kind == 29)
    {
      accept();
      if (this.currentToken.kind == 34)
      {
        SourcePosition localSourcePosition2 = new SourcePosition();
        start(localSourcePosition2);
        IntLiteral localIntLiteral = parseIntLiteral();
        finish(localSourcePosition2);
        localObject = new IntExpr(localIntLiteral, localSourcePosition2);
      }
      else
      {
        localObject = new EmptyExpr(this.dummyPos);
      }
      match(30);
      finish(localSourcePosition1);
      paramType = new ArrayType(paramType, (Expr)localObject, localSourcePosition1);
    }
    return new TypeAndIdent(this, paramType, localIdent);
  }
  
  List parseInitExpr()
    throws SyntaxError
  {
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    Object localObject = null;
    
    Expr localExpr = parseExpr();
    if (this.currentToken.kind == 32)
    {
      accept();
      localObject = parseInitExpr();
      finish(localSourcePosition);
      localObject = new ExprList(localExpr, (List)localObject, localSourcePosition);
    }
    else
    {
      finish(localSourcePosition);
      localObject = new ExprList(localExpr, new EmptyExprList(this.dummyPos), localSourcePosition);
    }
    return (List)localObject;
  }
  
  Expr parseInitialiser()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    if (this.currentToken.kind == 25)
    {
      accept();
      List localList = parseInitExpr();
      match(26);
      finish(localSourcePosition);
      localObject = new InitExpr(localList, localSourcePosition);
    }
    else
    {
      localObject = parseExpr();
    }
    return (Expr)localObject;
  }
  
  Type parseType()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    if (this.currentToken.kind == 9)
    {
      accept();
      finish(localSourcePosition);
      localObject = new VoidType(localSourcePosition);
    }
    else if (this.currentToken.kind == 0)
    {
      accept();
      finish(localSourcePosition);
      localObject = new BooleanType(localSourcePosition);
    }
    else if (this.currentToken.kind == 7)
    {
      accept();
      finish(localSourcePosition);
      localObject = new IntType(localSourcePosition);
    }
    else if (this.currentToken.kind == 4)
    {
      accept();
      finish(localSourcePosition);
      localObject = new FloatType(localSourcePosition);
    }
    else
    {
      syntacticError("\"%\" illegal type (must be one of void, int, float and boolean)", this.currentToken.spelling);
    }
    return (Type)localObject;
  }
  
  Stmt parseCompoundStmt()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(25);
    List localList1 = parseDeclStmtList();
    List localList2 = parseStmtList();
    match(26);
    finish(localSourcePosition);
    if (((localList1 instanceof EmptyDeclList)) && ((localList2 instanceof EmptyStmtList)))
    {
      localObject = new EmptyCompStmt(localSourcePosition);
    }
    else
    {
      finish(localSourcePosition);
      localObject = new CompoundStmt(localList1, localList2, localSourcePosition);
    }
    return (Stmt)localObject;
  }
  
  List parseDeclStmtList()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    if ((this.currentToken.kind == 7) || (this.currentToken.kind == 4) || (this.currentToken.kind == 0) || (this.currentToken.kind == 9)) {
      localObject = parseVarDecl();
    }
    while ((this.currentToken.kind == 7) || (this.currentToken.kind == 4) || (this.currentToken.kind == 0) || (this.currentToken.kind == 9))
    {
      List localList = parseVarDecl();
      DeclList localDeclList = (DeclList)localObject;
      while (!(localDeclList.DL instanceof EmptyDeclList)) {
        localDeclList = (DeclList)localDeclList.DL;
      }
      localDeclList.DL = ((DeclList)localList);
    }
    if (localObject == null) {
      localObject = new EmptyDeclList(this.dummyPos);
    }
    return (List)localObject;
  }
  
  List parseStmtList()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    if (this.currentToken.kind != 26)
    {
      Stmt localStmt = parseStmt();
      if (this.currentToken.kind != 26)
      {
        localObject = parseStmtList();
        finish(localSourcePosition);
        localObject = new StmtList(localStmt, (List)localObject, localSourcePosition);
      }
      else
      {
        finish(localSourcePosition);
        localObject = new StmtList(localStmt, new EmptyStmtList(this.dummyPos), localSourcePosition);
      }
    }
    else
    {
      localObject = new EmptyStmtList(this.dummyPos);
    }
    return (List)localObject;
  }
  
  Stmt parseStmt()
    throws SyntaxError
  {
    Stmt localStmt = null;
    switch (this.currentToken.kind)
    {
    case 25: 
      localStmt = parseCompoundStmt();
      break;
    case 6: 
      localStmt = parseIfStmt();
      break;
    case 5: 
      localStmt = parseForStmt();
      break;
    case 10: 
      localStmt = parseWhileStmt();
      break;
    case 1: 
      localStmt = parseBreakStmt();
      break;
    case 2: 
      localStmt = parseContinueStmt();
      break;
    case 8: 
      localStmt = parseReturnStmt();
      break;
    case 3: 
    case 4: 
    case 7: 
    case 9: 
    case 11: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    default: 
      localStmt = parseExprStmt();
    }
    return localStmt;
  }
  
  Stmt parseIfStmt()
    throws SyntaxError
  {
    IfStmt localIfStmt = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(6);
    match(27);
    Expr localExpr = parseExpr();
    match(28);
    Stmt localStmt1 = parseStmt();
    if (this.currentToken.kind == 3)
    {
      accept();
      Stmt localStmt2 = parseStmt();
      finish(localSourcePosition);
      localIfStmt = new IfStmt(localExpr, localStmt1, localStmt2, localSourcePosition);
    }
    else
    {
      finish(localSourcePosition);
      localIfStmt = new IfStmt(localExpr, localStmt1, localSourcePosition);
    }
    return localIfStmt;
  }
  
  Stmt parseForStmt()
    throws SyntaxError
  {
    Object localObject1 = null;
    Object localObject2 = null;Object localObject3 = null;Object localObject4 = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(5);
    match(27);
    if (this.currentToken.kind != 31) {
      localObject2 = parseExpr();
    } else {
      localObject2 = new EmptyExpr(this.dummyPos);
    }
    match(31);
    if (this.currentToken.kind != 31) {
      localObject3 = parseExpr();
    } else {
      localObject3 = new EmptyExpr(this.dummyPos);
    }
    match(31);
    if (this.currentToken.kind != 28) {
      localObject4 = parseExpr();
    } else {
      localObject4 = new EmptyExpr(this.dummyPos);
    }
    match(28);
    localObject1 = parseStmt();
    finish(localSourcePosition);
    localObject1 = new ForStmt((Expr)localObject2, (Expr)localObject3, (Expr)localObject4, (Stmt)localObject1, localSourcePosition);
    return (Stmt)localObject1;
  }
  
  Stmt parseWhileStmt()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(10);
    match(27);
    Expr localExpr = parseExpr();
    match(28);
    localObject = parseStmt();
    finish(localSourcePosition);
    localObject = new WhileStmt(localExpr, (Stmt)localObject, localSourcePosition);
    return (Stmt)localObject;
  }
  
  Stmt parseBreakStmt()
    throws SyntaxError
  {
    BreakStmt localBreakStmt = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(1);
    match(31);
    finish(localSourcePosition);
    
    localBreakStmt = new BreakStmt(localSourcePosition);
    return localBreakStmt;
  }
  
  Stmt parseContinueStmt()
    throws SyntaxError
  {
    ContinueStmt localContinueStmt = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(2);
    match(31);
    finish(localSourcePosition);
    
    localContinueStmt = new ContinueStmt(localSourcePosition);
    return localContinueStmt;
  }
  
  Stmt parseReturnStmt()
    throws SyntaxError
  {
    ReturnStmt localReturnStmt = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(8);
    Object localObject;
    if (this.currentToken.kind != 31) {
      localObject = parseExpr();
    } else {
      localObject = new EmptyExpr(this.dummyPos);
    }
    match(31);
    finish(localSourcePosition);
    localReturnStmt = new ReturnStmt((Expr)localObject, localSourcePosition);
    return localReturnStmt;
  }
  
  Stmt parseExprStmt()
    throws SyntaxError
  {
    ExprStmt localExprStmt = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    if ((this.currentToken.kind == 33) || (this.currentToken.kind == 15) || (this.currentToken.kind == 11) || (this.currentToken.kind == 12) || (this.currentToken.kind == 15) || (this.currentToken.kind == 34) || (this.currentToken.kind == 35) || (this.currentToken.kind == 36) || (this.currentToken.kind == 37) || (this.currentToken.kind == 27))
    {
      Expr localExpr = parseExpr();
      match(31);
      finish(localSourcePosition);
      localExprStmt = new ExprStmt(localExpr, localSourcePosition);
    }
    else
    {
      match(31);
      finish(localSourcePosition);
      localExprStmt = new ExprStmt(new EmptyExpr(this.dummyPos), localSourcePosition);
    }
    return localExprStmt;
  }
  
  List parseParaList()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(27);
    if (this.currentToken.kind == 28)
    {
      accept();
      finish(localSourcePosition);
      localObject = new EmptyParaList(localSourcePosition);
    }
    else
    {
      localObject = parseProperParaList();
      match(28);
    }
    return (List)localObject;
  }
  
  List parseProperParaList()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    ParaDecl localParaDecl = parseParaDecl();
    if (this.currentToken.kind == 32)
    {
      accept();
      localObject = parseProperParaList();
      finish(localSourcePosition);
      localObject = new ParaList(localParaDecl, (List)localObject, localSourcePosition);
    }
    else
    {
      finish(localSourcePosition);
      localObject = new ParaList(localParaDecl, new EmptyParaList(this.dummyPos), localSourcePosition);
    }
    return (List)localObject;
  }
  
  ParaDecl parseParaDecl()
    throws SyntaxError
  {
    ParaDecl localParaDecl = null;
    
    Type localType = parseType();
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    TypeAndIdent localTypeAndIdent = parseDeclarator(localType);
    
    finish(localSourcePosition);
    localParaDecl = new ParaDecl(localTypeAndIdent.tAST, localTypeAndIdent.iAST, localSourcePosition);
    
    return localParaDecl;
  }
  
  List parseArgList()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    match(27);
    if (this.currentToken.kind == 28)
    {
      match(28);
      finish(localSourcePosition);
      localObject = new EmptyArgList(localSourcePosition);
    }
    else
    {
      localObject = parseProperArgList();
      match(28);
    }
    return (List)localObject;
  }
  
  List parseProperArgList()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    Arg localArg = parseArg();
    if (this.currentToken.kind == 32)
    {
      accept();
      localObject = parseProperArgList();
      finish(localSourcePosition);
      localObject = new ArgList(localArg, (List)localObject, localSourcePosition);
    }
    else
    {
      finish(localSourcePosition);
      localObject = new ArgList(localArg, new EmptyArgList(localSourcePosition), localSourcePosition);
    }
    return (List)localObject;
  }
  
  Arg parseArg()
    throws SyntaxError
  {
    Arg localArg = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    Expr localExpr = parseExpr();
    finish(localSourcePosition);
    localArg = new Arg(localExpr, localSourcePosition);
    return localArg;
  }
  
  Ident parseIdent()
    throws SyntaxError
  {
    Ident localIdent = null;
    if (this.currentToken.kind == 33)
    {
      String str = this.currentToken.spelling;
      accept();
      localIdent = new Ident(str, this.previousTokenPosition);
    }
    else
    {
      syntacticError("identifier expected here", "");
    }
    return localIdent;
  }
  
  Operator acceptOperator()
    throws SyntaxError
  {
    Operator localOperator = null;
    
    String str = this.currentToken.spelling;
    accept();
    localOperator = new Operator(str, this.previousTokenPosition);
    return localOperator;
  }
  
  public Expr parseExpr()
    throws SyntaxError
  {
    Expr localExpr = null;
    localExpr = parseAssignExpr();
    return localExpr;
  }
  
  public Expr parseAssignExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    
    localObject = parseCondOrExpr();
    if (this.currentToken.kind == 17)
    {
      acceptOperator();
      Expr localExpr = parseAssignExpr();
      finish(localSourcePosition);
      localObject = new AssignExpr((Expr)localObject, localExpr, localSourcePosition);
    }
    return (Expr)localObject;
  }
  
  Expr parseCondOrExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    start(localSourcePosition1);
    
    localObject = parseCondAndExpr();
    while (this.currentToken.kind == 24)
    {
      Operator localOperator = acceptOperator();
      Expr localExpr = parseCondAndExpr();
      SourcePosition localSourcePosition2 = new SourcePosition();
      copyStart(localSourcePosition1, localSourcePosition2);
      finish(localSourcePosition2);
      localObject = new BinaryExpr((Expr)localObject, localOperator, localExpr, localSourcePosition2);
    }
    return (Expr)localObject;
  }
  
  Expr parseCondAndExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    start(localSourcePosition1);
    
    localObject = parseEqualityExpr();
    while (this.currentToken.kind == 23)
    {
      Operator localOperator = acceptOperator();
      Expr localExpr = parseEqualityExpr();
      SourcePosition localSourcePosition2 = new SourcePosition();
      copyStart(localSourcePosition1, localSourcePosition2);
      finish(localSourcePosition2);
      localObject = new BinaryExpr((Expr)localObject, localOperator, localExpr, localSourcePosition2);
    }
    return (Expr)localObject;
  }
  
  Expr parseEqualityExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    start(localSourcePosition1);
    
    localObject = parseRelExpr();
    while ((this.currentToken.kind == 18) || (this.currentToken.kind == 16))
    {
      Operator localOperator = acceptOperator();
      Expr localExpr = parseRelExpr();
      SourcePosition localSourcePosition2 = new SourcePosition();
      copyStart(localSourcePosition1, localSourcePosition2);
      finish(localSourcePosition2);
      localObject = new BinaryExpr((Expr)localObject, localOperator, localExpr, localSourcePosition2);
    }
    return (Expr)localObject;
  }
  
  Expr parseRelExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    start(localSourcePosition1);
    
    localObject = parseAdditiveExpr();
    while ((this.currentToken.kind == 19) || (this.currentToken.kind == 20) || (this.currentToken.kind == 21) || (this.currentToken.kind == 22))
    {
      Operator localOperator = acceptOperator();
      Expr localExpr = parseAdditiveExpr();
      SourcePosition localSourcePosition2 = new SourcePosition();
      copyStart(localSourcePosition1, localSourcePosition2);
      finish(localSourcePosition2);
      localObject = new BinaryExpr((Expr)localObject, localOperator, localExpr, localSourcePosition2);
    }
    return (Expr)localObject;
  }
  
  Expr parseAdditiveExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    start(localSourcePosition1);
    
    localObject = parseMultiplicativeExpr();
    while ((this.currentToken.kind == 11) || (this.currentToken.kind == 12))
    {
      Operator localOperator = acceptOperator();
      Expr localExpr = parseMultiplicativeExpr();
      
      SourcePosition localSourcePosition2 = new SourcePosition();
      copyStart(localSourcePosition1, localSourcePosition2);
      finish(localSourcePosition2);
      localObject = new BinaryExpr((Expr)localObject, localOperator, localExpr, localSourcePosition2);
    }
    return (Expr)localObject;
  }
  
  Expr parseMultiplicativeExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition1 = new SourcePosition();
    start(localSourcePosition1);
    
    localObject = parseUnaryExpr();
    while ((this.currentToken.kind == 13) || (this.currentToken.kind == 14))
    {
      Operator localOperator = acceptOperator();
      Expr localExpr = parseUnaryExpr();
      SourcePosition localSourcePosition2 = new SourcePosition();
      copyStart(localSourcePosition1, localSourcePosition2);
      finish(localSourcePosition2);
      localObject = new BinaryExpr((Expr)localObject, localOperator, localExpr, localSourcePosition2);
    }
    return (Expr)localObject;
  }
  
  Expr parseUnaryExpr()
    throws SyntaxError
  {
    Object localObject = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    switch (this.currentToken.kind)
    {
    case 11: 
    case 12: 
    case 15: 
      Operator localOperator = acceptOperator();
      Expr localExpr = parseUnaryExpr();
      finish(localSourcePosition);
      localObject = new UnaryExpr(localOperator, localExpr, localSourcePosition);
      
      break;
    case 13: 
    case 14: 
    default: 
      localObject = parsePrimaryExpr();
    }
    return (Expr)localObject;
  }
  
  Expr parsePrimaryExpr()
    throws SyntaxError
  {
    Object localObject1 = null;
    
    SourcePosition localSourcePosition = new SourcePosition();
    start(localSourcePosition);
    Object localObject2;
    Object localObject3;
    switch (this.currentToken.kind)
    {
    case 33: 
      Ident localIdent = parseIdent();
      if (this.currentToken.kind == 27)
      {
        localObject2 = parseArgList();
        finish(localSourcePosition);
        localObject1 = new CallExpr(localIdent, (List)localObject2, localSourcePosition);
      }
      else if (this.currentToken.kind == 29)
      {
        finish(localSourcePosition);
        localObject2 = new SimpleVar(localIdent, localSourcePosition);
        accept();
        localObject3 = parseExpr();
        finish(localSourcePosition);
        localObject1 = new ArrayExpr((Var)localObject2, (Expr)localObject3, localSourcePosition);
        match(30);
      }
      else
      {
        finish(localSourcePosition);
        localObject2 = new SimpleVar(localIdent, localSourcePosition);
        localObject1 = new VarExpr((Var)localObject2, localSourcePosition);
      }
      break;
    case 27: 
      accept();
      localObject1 = parseExpr();
      match(28);
      
      break;
    case 34: 
      localObject2 = parseIntLiteral();
      finish(localSourcePosition);
      localObject1 = new IntExpr((IntLiteral)localObject2, localSourcePosition);
      break;
    case 35: 
      localObject3 = parseFloatLiteral();
      finish(localSourcePosition);
      localObject1 = new FloatExpr((FloatLiteral)localObject3, localSourcePosition);
      break;
    case 36: 
      BooleanLiteral localBooleanLiteral = parseBooleanLiteral();
      finish(localSourcePosition);
      localObject1 = new BooleanExpr(localBooleanLiteral, localSourcePosition);
      break;
    case 37: 
      StringLiteral localStringLiteral = parseStringLiteral();
      finish(localSourcePosition);
      localObject1 = new StringExpr(localStringLiteral, localSourcePosition);
      break;
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 32: 
    default: 
      syntacticError("illegal parimary expression", this.currentToken.spelling);
    }
    return (Expr)localObject1;
  }
  
  IntLiteral parseIntLiteral()
    throws SyntaxError
  {
    IntLiteral localIntLiteral = null;
    if (this.currentToken.kind == 34)
    {
      String str = this.currentToken.spelling;
      accept();
      localIntLiteral = new IntLiteral(str, this.previousTokenPosition);
    }
    else
    {
      syntacticError("integer literal expected here", "");
    }
    return localIntLiteral;
  }
  
  FloatLiteral parseFloatLiteral()
    throws SyntaxError
  {
    FloatLiteral localFloatLiteral = null;
    if (this.currentToken.kind == 35)
    {
      String str = this.currentToken.spelling;
      accept();
      localFloatLiteral = new FloatLiteral(str, this.previousTokenPosition);
    }
    else
    {
      syntacticError("float literal expected here", "");
    }
    return localFloatLiteral;
  }
  
  BooleanLiteral parseBooleanLiteral()
    throws SyntaxError
  {
    BooleanLiteral localBooleanLiteral = null;
    if (this.currentToken.kind == 36)
    {
      String str = this.currentToken.spelling;
      accept();
      localBooleanLiteral = new BooleanLiteral(str, this.previousTokenPosition);
    }
    else
    {
      syntacticError("string literal expected here", "");
    }
    return localBooleanLiteral;
  }
  
  StringLiteral parseStringLiteral()
    throws SyntaxError
  {
    StringLiteral localStringLiteral = null;
    if (this.currentToken.kind == 37)
    {
      this.previousTokenPosition = this.currentToken.position;
      String str = this.currentToken.spelling;
      localStringLiteral = new StringLiteral(str, this.previousTokenPosition);
      this.currentToken = this.scanner.getToken();
    }
    else
    {
      syntacticError("string literal expected here", "");
    }
    return localStringLiteral;
  }
}
