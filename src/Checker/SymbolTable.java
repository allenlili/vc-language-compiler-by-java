package VC.Checker;

import VC.ASTs.Decl;

public final class SymbolTable
{
  private int level;
  private IdEntry latest;
  
  public SymbolTable()
  {
    this.level = 1;
    this.latest = null;
  }
  
  public void openScope()
  {
    this.level += 1;
  }
  
  public void closeScope()
  {
    IdEntry localIdEntry = this.latest;
    while (localIdEntry.level == this.level) {
      localIdEntry = localIdEntry.previousEntry;
    }
    this.level -= 1;
    this.latest = localIdEntry;
  }
  
  public void insert(String paramString, Decl paramDecl)
  {
    IdEntry localIdEntry = new IdEntry(paramString, paramDecl, this.level, this.latest);
    this.latest = localIdEntry;
  }
  
  public Decl retrieve(String paramString)
  {
    Decl localDecl = null;
    int i = 0;int j = 1;
    
    IdEntry localIdEntry = this.latest;
    while (j != 0) {
      if (localIdEntry == null)
      {
        j = 0;
      }
      else if (localIdEntry.id.equals(paramString))
      {
        i = 1;
        j = 0;
        localDecl = localIdEntry.attr;
      }
      else
      {
        localIdEntry = localIdEntry.previousEntry;
      }
    }
    return localDecl;
  }
  
  public IdEntry retrieveOneLevel(String paramString)
  {
    IdEntry localIdEntry = this.latest;
    while (localIdEntry != null)
    {
      if (localIdEntry.level != this.level) {
        return null;
      }
      if (localIdEntry.id.equals(paramString)) {
        break;
      }
      localIdEntry = localIdEntry.previousEntry;
    }
    return localIdEntry;
  }
}
