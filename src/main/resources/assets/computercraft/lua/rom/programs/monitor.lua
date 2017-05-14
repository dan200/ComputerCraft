
local function printUsage()
	print( "Usage: monitor <name> <program> <arguments>" )
	return
end

local tArgs = { ... }
if #tArgs < 2 then
	printUsage()
	return
end

local sName = tArgs[1]
if peripheral.getType( sName ) ~= "monitor" then
	print( "No monitor named ".. sName )
	return
end

local sProgram = tArgs[2]
local sPath = shell.resolveProgram( sProgram )
if sPath == nil then
	print( "No such program: "..sProgram )
	return
end

print( "Running "..sProgram.." on monitor "..sName )

local monitor = peripheral.wrap( sName )
local previousTerm = term.redirect( monitor )

local co = coroutine.create( function()
    shell.run( sProgram, table.unpack( tArgs, 3 ) )
end )

local function resume( ... )
    local ok, param = coroutine.resume( co, ... )
    if not ok then
    	printError( param )
    end
    return param
end

local ok, param = pcall( function()
	local sFilter = resume()
	while coroutine.status( co ) ~= "dead" do
		local tEvent = table.pack( os.pullEventRaw() )
		if sFilter == nil or tEvent[1] == sFilter or tEvent[1] == "terminate" then
			sFilter = resume( table.unpack( tEvent, 1, tEvent.n ) )
		end
		if coroutine.status( co ) ~= "dead" and (sFilter == nil or sFilter == "mouse_click") then
			if tEvent[1] == "monitor_touch" and tEvent[2] == sName then
				sFilter = resume( "mouse_click", 1, table.unpack( tEvent, 3, tEvent.n ) )
			end
		end
		if coroutine.status( co ) ~= "dead" and (sFilter == nil or sFilter == "term_resize") then
			if tEvent[1] == "monitor_resize" and tEvent[2] == sName then
				sFilter = resume( "term_resize" )
			end
		end
	end
end )

term.redirect( previousTerm )
if not ok then
	printError( param )
end
	
