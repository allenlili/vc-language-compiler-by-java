package VC;

import VC.ASTs.FuncDecl;
import VC.ASTs.Type;

public final class StdEnvironment
{
  public static Type booleanType;
  public static Type intType;
  public static Type floatType;
  public static Type stringType;
  public static Type voidType;
  public static Type errorType;
  public static FuncDecl putBoolDecl;
  public static FuncDecl putBoolLnDecl;
  public static FuncDecl getIntDecl;
  public static FuncDecl putIntDecl;
  public static FuncDecl putIntLnDecl;
  public static FuncDecl getFloatDecl;
  public static FuncDecl putFloatDecl;
  public static FuncDecl putFloatLnDecl;
  public static FuncDecl putStringDecl;
  public static FuncDecl putStringLnDecl;
  public static FuncDecl putLnDecl;
}
