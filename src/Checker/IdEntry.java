package VC.Checker;

import VC.ASTs.Decl;

public class IdEntry
{
  protected String id;
  protected Decl attr;
  protected int level;
  protected IdEntry previousEntry;
  
  IdEntry(String paramString, Decl paramDecl, int paramInt, IdEntry paramIdEntry)
  {
    this.id = paramString;
    this.attr = paramDecl;
    this.level = paramInt;
    this.previousEntry = paramIdEntry;
  }
}
