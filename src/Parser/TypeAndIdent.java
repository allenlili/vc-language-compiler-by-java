package VC.Parser;

import VC.ASTs.Ident;
import VC.ASTs.Type;

public class TypeAndIdent
{
  Type tAST;
  Ident iAST;
  
  public TypeAndIdent(Parser paramParser, Type paramType, Ident paramIdent)
  {
    this.tAST = paramType;
    this.iAST = paramIdent;
  }
}
