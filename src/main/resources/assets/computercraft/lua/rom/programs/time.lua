local tArgs = { ... }
local sSource
if tArgs[1] == nil then
    sSource = "ingame"
else
    if tArgs[1] == "ingame" or tArgs[1] == "local" or tArgs[1] == "utc" then
        sSource = tArgs[1]
    else
        printError("Not a valid source (ingame/local/utc)")
        return
    end
end
local nTime = os.time( sSource )
local nDay = os.day( sSource )
print( "The time is "..textutils.formatTime( nTime, false ).." on Day "..nDay )
