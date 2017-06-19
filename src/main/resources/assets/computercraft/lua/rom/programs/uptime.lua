local function formatUptime( nTime )
    local nHour = math.floor( nTime/60/60 )
    local nMinute = math.floor( (nTime/60)  ) - nHour*60
    local nSecond = math.floor( nTime ) - nMinute*60 - nHour*60*60
    return nHour..":"..nMinute..":"..nSecond
end

print( "The computer has been running for "..formatUptime( os.clock() ) .." hours")
