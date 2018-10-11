#!/bin/bash
# The result of ASS2-TEST1 is in ASS2-RESULTS.
# The result of ASS2-TEST0 is in itself.

make clean

make ../vc.class

for file in t 'test'; 
do
	if [ $file = t ]; then
		for (( i=1; i<=2; i++ ))
		do
			echo "!!generate ${file}${i}.vc -> ${file}${i}.out"
			CLASSPATH=../ java VC.Checker.vc Checker/ASS4-TEST0/${file}${i}.vc > Checker/Test/${file}${i}.out
			diff Checker/Test/${file}${i}.out Checker/ASS4-TEST0/${file}${i}.soll > Checker/Diff/${file}${i}_diff.dif
			cat Checker/Diff/${file}${i}_diff.dif
		done
		continue 1
	fi
	
#	if [ $file = 'test' ]; then
#		for (( i=1; i<=1; i++ ))
#		do
#			echo "!!generate ${file}${i}.vc -> ${file}${i}.out"
#			CLASSPATH=../ java VC.Checker.vc Checker/ASS-TESTS/${file}${i} > Checker/Test/${file}${i}.out
#			diff Checker/Test/${file}${i}.out Checker/ASS-RESULTS/result${i} > Checker/Diff/${file}${i}_diff.dif
#			cat Checker/Diff/${file}${i}_diff.dif
#		done
#		continue 1
#	fi
done

