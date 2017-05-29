#!/bin/sh
echo "Java code:"
cat `find src | grep \\.java$` | wc

echo "Lua code:"
cat `find src/main/resources/assets/computercraft/lua | grep \\.lua$` | wc

echo "JSON:"
cat `find src/main/resources/assets/computercraft | grep \\.json$` | wc

