#!/bin/bash
# The result of ASS2-TEST1 is in ASS2-RESULTS.
# The result of ASS2-TEST0 is in itself.

make clean

make ../vc.class

for file in t mytest "test"; 
do
	if [ $file = t ]; then
		for (( i=1; i<=31; i++ ))
		do
			echo "!!generate ${file}${i}.vc -> ${file}${i}.out"
			CLASSPATH=../ java VC.Recogniser.vc Recogniser/ASS2-TESTS0/${file}${i}.vc > Recogniser/Test/${file}${i}.out
			diff Recogniser/Test/${file}${i}.out Recogniser/ASS2-TESTS0/${file}${i}.sol > Recogniser/Diff/${file}${i}_diff.dif
			cat Recogniser/Diff/${file}${i}_diff.dif
		done
		continue 1
	fi
	
	if [ $file = mytest ]; then
		for (( i=1; i<=5; i++ ))
		do
			echo "!!generate ${file}${i}.vc -> ${file}${i}.out"
			CLASSPATH=../ java VC.Recogniser.vc Recogniser/ASS2-TESTS0/${file}${i}.vc > Recogniser/Test/${file}${i}.out
#			diff Recogniser/Test/${file}${i}.out Recogniser/ASS2-TESTS0/${file}${i}.sol > Recogniser/Diff/${file}${i}_diff.dif
#			cat Recogniser/Diff/${file}${i}_diff.dif
			cat Recogniser/Test/${file}${i}.out
		done
		continue 1
	fi
	
	if [ $file = "test" ]; then
		for (( i=1; i<=76; i++ ))
		do
			echo "!!generate ${file}${i}.vc -> ${file}${i}.out"
			CLASSPATH=../ java VC.Recogniser.vc Recogniser/ASS2-TESTS1/${file}${i} > Recogniser/Test/${file}${i}.out
			diff Recogniser/Test/${file}${i}.out Recogniser/ASS2-RESULTS/result${i} > Recogniser/Diff/${file}${i}_diff.dif
			cat Recogniser/Diff/${file}${i}_diff.dif
		done
		continue 1
	fi			
done

