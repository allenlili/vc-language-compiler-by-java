#!/bin/bash


make clean

make ../vc.class

for file in fib comment error escape longestmatch string tab tokens fib gcd
do
	if [ $file = comment ]; then
		for (( i=1; i<5; i++ ))
		do
			echo "generate ${file}${i}.vc -> ${file}${i}.out"
			CLASSPATH=../ java VC.Scanner.vc Scanner/ASSN1-TEST0/${file}${i}.vc > Scanner/Test/${file}${i}.out
			diff Scanner/Test/${file}${i}.out Scanner/ASSN1-TEST0/${file}${i}.sol > Scanner/Diff/${file}${i}_diff.dif
			cat Scanner/Diff/${file}${i}_diff.dif
		done
		continue 1
	fi
	if [ $file = error ]; then
		for (( i=1; i<5; i++ ))
		do
			echo "generate ${file}${i}.vc -> ${file}${i}.out"
			CLASSPATH=../ java VC.Scanner.vc Scanner/ASSN1-TEST0/${file}${i}.vc > Scanner/Test/${file}${i}.out
			diff Scanner/Test/${file}${i}.out Scanner/ASSN1-TEST0/${file}${i}.sol > Scanner/Diff/${file}${i}_diff.dif
			cat Scanner/Diff/${file}${i}_diff.dif
		done
		continue 1
	fi
	
	echo "generate ${file}.vc -> ${file}.out"
	CLASSPATH=../ java VC.Scanner.vc Scanner/ASSN1-TEST0/${file}.vc > Scanner/Test/${file}.out
	diff Scanner/Test/${file}.out Scanner/ASSN1-TEST0/${file}.sol > Scanner/Diff/${file}_diff.dif
	cat Scanner/Diff/${file}_diff.dif			
done


# assn1 mark test, modify toString() in Token.java
#file="test"
#echo "assn1:-------------------------------------------"
#for (( i=1; i<60; i++ ))
#do
#	echo "generate ${file}${i} -> ${file}${i}.out"
#	CLASSPATH=../ java VC.Scanner.vc Scanner/ASSN1-TEST1/${file}${i} > Scanner/Test/${file}${i}.out
#	diff Scanner/Test/${file}${i}.out Scanner/ASSN1-RESULTS/result${i} > Scanner/Diff/${file}${i}_diff.dif
#	cat Scanner/Diff/${file}${i}_diff.dif
#done

