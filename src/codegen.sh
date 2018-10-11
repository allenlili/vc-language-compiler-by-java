#!/bin/bash
# The result of ASS2-TEST1 is in ASS2-RESULTS.
# The result of ASS2-TEST0 is in itself.

make clean

make ../vc.class

for file in gcd max; 
do
#	echo "!!generate ${file}.vc -> ${file}.class"
	javac ./lang/System.java
	cp ./lang/System.class ./CodeGen/Test/VC/lang
	cd ./CodeGen/Test/
	CLASSPATH=../../../ java VC.CodeGen.vc ${file}.vc
	diff ${file}.j ../ASST5-TEST0/${file}.j > ../Diff/${file}_diff.dif
	java jasmin.Main ${file}.j
	java ${file} < ../input1.txt >> ../Diff/${file}_diff.dif
	java ${file} < ../input2.txt >> ../Diff/${file}_diff.dif
	diff ${file}_diff.out ../Diff/${file}_diff.dif > ../Diff/${file}_diff2.dif
	cat ../Diff/${file}_diff2.dif
	cd -
done

