# A simple Makefile for UNSW cs3161/cs9102 programming languages and compilers
# Written by Chris Hall
#
# Place this Makefile inside your VC/ folder, so your directory layout
# should look like:
#
#   VC/
#     ErrorReporter.java
#     vc.java
#     Makefile
#     Scanner/
#       <a bunch of .vc files>
#       <a bunch of .sol files>
#       <a bunch of .java files>
#
# if you change directory to VC/
#   $ cd VC/
#
# you can then use the below make commands
#   $ make run

.POSIX:

# `make` is the same as `make run`
all: run

# This is the rule for actually doing the compile of vc.java into vc.class
../vc.class:
#	CLASSPATH=../ javac Scanner/vc.java
#	CLASSPATH=../ javac Recogniser/vc.java
#	CLASSPATH=../ javac Parser/vc.java
#	CLASSPATH=../ javac Checker/vc.java
	CLASSPATH=../ javac CodeGen/vc.java

# `make clean` will remove "tokens.out" and any .class files (compiled java)
clean:
	find . -iname '*.class' -delete
# scanner 
#	rm -rf Scanner/Test/*.out Scanner/Diff/*.dif

# recogniser
#	rm -rf Recogniser/Test/*.out Recogniser/Diff/*.dif

# parser
#	rm -rf Parser/ASS3-TEST0/*.vct Parser/ASS3-TEST0/*.vcu
#	rm -rf Parser/Test/*.vct Parser/Unparse/*.vcu Parser/Diff/*.dff

# checker
#	rm -rf Checker/Test/*.out Checker/Diff/*.dif

# codegen
	rm -f CodeGen/Test/*.j CodeGen/Test/*.class CodeGen/Diff/*.dif

# `make run` will compile and run your scanner
run: clean ../vc.class
	CLASSPATH=../ java Scanner/VC.vc Scanner/comment1.vc

# `make tokens.out` will compile and run your scanner, writing all output to
# "tokens.out"
tokens.out: clean ../vc.class
	CLASSPATH=../ java Scanner/VC.vc Scanner/comment1.vc > tokens.out

.PHONY: all clean run

