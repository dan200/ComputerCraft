local tArgs = { ... }
if #tArgs > 0 then
	sTopic = tArgs[1]
else
	sTopic = "intro"
end

if sTopic == "index" then
	print( "Help topics availiable:" )
	local tTopics = help.topics()
	textutils.pagedTabulate( tTopics )
	return
end
	
local w,h = term.getSize()
local sFile = help.lookup( sTopic )
local file = ((sFile ~= nil) and io.open( sFile )) or nil
local nLinesPrinted = 0
if file then
	local sLine = file:read()
	local nLines = 0
	while sLine do
		nLines = nLines + textutils.pagedPrint( sLine, (h-3) - nLines )
    	sLine = file:read()
    end
	file:close()
else
	print( "No help available" )
end
