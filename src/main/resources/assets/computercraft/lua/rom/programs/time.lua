local tArgs = { ... }
local source
if tArgs[1] == nil then
    source = "ingame"
else
    if tArgs[1] == "ingame" or tArgs[1] == "local" or tArgs[1] == "utc" then
        source = tArgs[1]
    else
        printError("Not a valid source")
        return
    end
end
local nTime = os.time( source )
local nDay = os.day( source )
print( "The time is "..textutils.formatTime( nTime, false ).." on Day "..nDay )
