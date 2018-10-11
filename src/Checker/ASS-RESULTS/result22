======= The VC compiler =======

Pass 1: Lexical and syntactic Analysis
Pass 2: Semantic Analysis
ERROR: 4(5)..4(5): *27: wrong type for actual parameter: i
Compilation was unsuccessful.
int f(int i[], float f[], float j[20]) { return 1; }
int main() {
  float g[10];
  f(g, g, g);
  return 0;
}
