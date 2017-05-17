local tArgs = { ... }
if #tArgs < 2 then
	print( "Usage: rename <source> <destination>" )
	return
end

local sSource = shell.resolve( tArgs[1] )
local sDest = shell.resolve( tArgs[2] )
fs.move( sSource, sDest )
