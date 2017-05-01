#!/bin/bash
#
# unpack the luaj test archive, compile and run it locally, and repack the results
#
if [ ! -x "$LUA51" -o ! -x "$LUA51C" ]; then
	echo "LUA51 and LUA51C must be set to version 5.1 lua and luac"
	exit -1
fi

# unzip existing archive
ZIP="luaj2.0-tests.zip"
unzip -n ${ZIP}
rm *.lc *.out */*.lc */*.out

# compile tests for compiler and save binary files
for DIR in "lua5.1-tests" "regressions"; do
	cd ${DIR}
	FILES=`ls -1 *.lua | awk 'BEGIN { FS="." } ; { print $1 }'`
	for FILE in $FILES ; do
		echo 'compiling' `pwd` $FILE
   		${LUA51C} ${FILE}.lua
		mv luac.out ${FILE}.lc
	done
	cd ..
done

# run test lua scripts and save output
for DIR in "errors" "perf" "."; do
	cd ${DIR}
	FILES=`ls -1 *.lua | awk 'BEGIN { FS="." } ; { print $1 }'`
	for FILE in $FILES ; do
		echo 'executing' `pwd` $FILE
   		${LUA51} ${FILE}.lua JSE > ${FILE}.out
	done
	cd ..
done
cd lua

# create new zipfile
rm -f ${ZIP}
zip ${ZIP} *.lua *.lc *.out */*.lua */*.lc */*.out

# cleanup
rm *.out */*.lc */*.out *.txt */*.txt
rm -r lua5.1-tests
rm -r regressions
