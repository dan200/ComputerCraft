
local tArgs = { ... }
if #tArgs < 2 then
	print( "Usage: cp <source> <destination>" )
	return
end

local sSource = shell.resolve( tArgs[1] )
local sDest = shell.resolve( tArgs[2] )
local tFiles = fs.find( sSource )
if #tFiles > 0 then
    for n,sFile in ipairs( tFiles ) do
        if fs.isDir( sDest ) then
            fs.copy( sFile, fs.combine( sDest, fs.getName(sFile) ) )
        elseif #tFiles == 1 then
            fs.copy( sFile, sDest )
        else
            printError( "Cannot overwrite file multiple times" )
            return
        end
    end
else
    printError( "No matching files" )
end