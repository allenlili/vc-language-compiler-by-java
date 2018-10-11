#!/bin/bash
# The result of ASS2-TEST1 is in ASS2-RESULTS.
# The result of ASS2-TEST0 is in itself.

make clean

make ../vc.class

cmd1=$1
cmd2=$2

if [ $cmd1 = "-s" ]; then
	echo "#$i generate ${cmd2}${i}.vc -> ${cmd2}${i}.t"
	CLASSPATH=../ java VC.Parser.vc Parser/ASS3-TEST0/${cmd2}.vc
	mv Parser/ASS3-TEST0/${cmd2}.vct Parser/Test
	mv Parser/ASS3-TEST0/${cmd2}.vcu Parser/Unparse
	diff Parser/Unparse/${cmd2}.vcu Parser/ASS3-TEST0/${cmd2}.sol > Parser/Diff/${cmd2}.dff
	cat Parser/Diff/${cmd2}${i}.dff	
else
	for file in t; 
	do
		if [ $file = "t" ]; then
			for (( i=1; i<=47; i++ ))
			do
				echo "#$i generate ${file}${i}.vc -> ${file}${i}.t"
				CLASSPATH=../ java VC.Parser.vc Parser/ASS3-TEST0/${file}${i}.vc
				mv Parser/ASS3-TEST0/${file}${i}.vct Parser/Test
				mv Parser/ASS3-TEST0/${file}${i}.vcu Parser/Unparse
				diff Parser/Unparse/${file}${i}.vcu Parser/ASS3-TEST0/${file}${i}.sol > Parser/Diff/${file}${i}.dff
				cat Parser/Diff/${file}${i}.dff
			done
			continue 1
		fi			
	done
fi
