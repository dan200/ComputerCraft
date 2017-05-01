local tArgs = { ... }
if #tArgs < 1 then
	print( "Usage: mkdir <path>" )
	return
end

local sNewDir = shell.resolve( tArgs[1] )
fs.makeDir( sNewDir )

