#!/bin/sh
cd luaj-2.0.3
echo "Building LuaJ..."
ant clean
ant

echo "Copying output to libs..."
rm ../libs/luaj-jse-2.0.3.jar
cp luaj-jse-2.0.3.jar ../libs

echo "Done."
cd ..
