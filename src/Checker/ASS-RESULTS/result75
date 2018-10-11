======= The VC compiler =======

Pass 1: Lexical and syntactic Analysis
Pass 2: Semantic Analysis
ERROR: 7(10)..7(25): *15: invalid initialiser: scalar initialiser for array: i
Compilation was unsuccessful.
int f() { return 1; }
int main() {
  {
     boolean b[] = {true};
  }
  {
     int i[2] =   f() + 1;
  }
  return 0;
}
