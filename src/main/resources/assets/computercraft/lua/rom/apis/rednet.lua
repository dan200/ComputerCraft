local rednet
if shell then
    rednet = {}
else
    rednet = _ENV
end

rednet.CHANNEL_BROADCAST = 65535
rednet.CHANNEL_REPEAT = 65533

local tReceivedMessages = {}
local tReceivedMessageTimeouts = {}
local tHostnames = {}

function rednet.open( sModem )
    if type( sModem ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sModem ) .. ")", 2 )
    end
    if peripheral.getType( sModem ) ~= "modem" then 
        error( "No such modem: "..sModem, 2 )
    end
    peripheral.call( sModem, "open", os.getComputerID() )
    peripheral.call( sModem, "open", rednet.CHANNEL_BROADCAST )
end

function rednet.close( sModem )
    if sModem then
        -- Close a specific modem
        if type( sModem ) ~= "string" then
            error( "bad argument #1 (expected string, got " .. type( sModem ) .. ")", 2 )
        end
        if peripheral.getType( sModem ) ~= "modem" then
            error( "No such modem: "..sModem, 2 )
        end
        peripheral.call( sModem, "close", os.getComputerID() )
        peripheral.call( sModem, "close", rednet.CHANNEL_BROADCAST )
    else
        -- Close all modems
        for n,sModem in ipairs( peripheral.getNames() ) do
            if rednet.isOpen( sModem ) then
                rednet.close( sModem )
            end
        end
    end
end

function rednet.isOpen( sModem )
    if sModem then
        -- Check if a specific modem is open
        if type( sModem ) ~= "string" then
            error( "bad argument #1 (expected string, got " .. type( sModem ) .. ")", 2 )
        end
        if peripheral.getType( sModem ) == "modem" then
            return peripheral.call( sModem, "isOpen", os.getComputerID() ) and peripheral.call( sModem, "isOpen", rednet.CHANNEL_BROADCAST )
        end
    else
        -- Check if any modem is open
        for n,sModem in ipairs( peripheral.getNames() ) do
            if rednet.isOpen( sModem ) then
                return true
            end
        end
    end
    return false
end

function rednet.send( nRecipient, message, sProtocol )
    if type( nRecipient ) ~= "number" then
        error( "bad argument #1 (expected number, got " .. type( nRecipient ) .. ")", 2 )
    end
    if sProtocol ~= nil and type( sProtocol ) ~= "string" then
        error( "bad argument #3 (expected string, got " .. type( sProtocol ) .. ")", 2 )
    end
    -- Generate a (probably) unique message ID
    -- We could do other things to guarantee uniqueness, but we really don't need to
    -- Store it to ensure we don't get our own messages back
    local nMessageID = math.random( 1, 2147483647 )
    tReceivedMessages[ nMessageID ] = true
    tReceivedMessageTimeouts[ os.startTimer( 30 ) ] = nMessageID

    -- Create the message
    local nReplyChannel = os.getComputerID()
    local tMessage = {
        nMessageID = nMessageID,
        nRecipient = nRecipient,
        message = message,
        sProtocol = sProtocol,
    }

    if nRecipient == os.getComputerID() then
        -- Loopback to ourselves
        os.queueEvent( "rednet_message", nReplyChannel, message, sProtocol )

    else
        -- Send on all open modems, to the target and to repeaters
        local sent = false
        for n,sModem in ipairs( peripheral.getNames() ) do
            if rednet.isOpen( sModem ) then
                peripheral.call( sModem, "transmit", nRecipient, nReplyChannel, tMessage );
                peripheral.call( sModem, "transmit", rednet.CHANNEL_REPEAT, nReplyChannel, tMessage );
                sent = true
            end
        end
    end
end

function rednet.broadcast( message, sProtocol )
    if sProtocol ~= nil and type( sProtocol ) ~= "string" then
        error( "bad argument #2 (expected string, got " .. type( sProtocol ) .. ")", 2 )
    end
    rednet.send( rednet.CHANNEL_BROADCAST, message, sProtocol )
end

function rednet.receive( sProtocolFilter, nTimeout )
    -- The parameters used to be ( nTimeout ), detect this case for backwards compatibility
    if type(sProtocolFilter) == "number" and nTimeout == nil then
        sProtocolFilter, nTimeout = nil, sProtocolFilter
    end
    if sProtocolFilter ~= nil and type( sProtocolFilter ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sProtocolFilter ) .. ")", 2 )
    end
    if nTimeout ~= nil and type( nTimeout ) ~= "number" then
        error( "bad argument #2 (expected number, got " .. type( nTimeout ) .. ")", 2 )
    end

    -- Start the timer
    local timer = nil
    local sFilter = nil
    if nTimeout then
        timer = os.startTimer( nTimeout )
        sFilter = nil
    else
        sFilter = "rednet_message"
    end

    -- Wait for events
    while true do
        local sEvent, p1, p2, p3 = os.pullEvent( sFilter )
        if sEvent == "rednet_message" then
            -- Return the first matching rednet_message
            local nSenderID, message, sProtocol = p1, p2, p3
            if sProtocolFilter == nil or sProtocol == sProtocolFilter then
                return nSenderID, message, sProtocol
            end
        elseif sEvent == "timer" then
            -- Return nil if we timeout
            if p1 == timer then
                return nil
            end
        end
    end
end

function rednet.host( sProtocol, sHostname )
    if type( sProtocol ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sProtocol ) .. ")", 2 )
    end
    if type( sHostname ) ~= "string" then
        error( "bad argument #2 (expected string, got " .. type( sHostname ) .. ")", 2 )
    end
    if sHostname == "localhost" then
        error( "Reserved hostname", 2 )
    end
    if tHostnames[ sProtocol ] ~= sHostname then
        if rednet.lookup( sProtocol, sHostname ) ~= nil then
            error( "Hostname in use", 2 )
        end
        tHostnames[ sProtocol ] = sHostname
    end
end

function unhost( sProtocol )
    if type( sProtocol ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sProtocol ) .. ")", 2 )
    end
    tHostnames[ sProtocol ] = nil
end

function rednet.lookup( sProtocol, sHostname )
    if type( sProtocol ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sProtocol ) .. ")", 2 )
    end
    if sHostname ~= nil and type( sHostname ) ~= "string" then
        error( "bad argument #2 (expected string, got " .. type( sHostname ) .. ")", 2 )
    end

    -- Build list of host IDs
    local tResults = nil
    if sHostname == nil then
        tResults = {}
    end

    -- Check localhost first
    if tHostnames[ sProtocol ] then
        if sHostname == nil then
            table.insert( tResults, os.getComputerID() )
        elseif sHostname == "localhost" or sHostname == tHostnames[ sProtocol ] then
            return os.getComputerID()
        end
    end

    if not isOpen() then
        if tResults then
            return table.unpack( tResults )
        end
        return nil
    end

    -- Broadcast a lookup packet
    broadcast( {
        sType = "lookup",
        sProtocol = sProtocol,
        sHostname = sHostname,
    }, "dns" )

    -- Start a timer
    local timer = os.startTimer( 2 )

    -- Wait for events
    while true do
        local event, p1, p2, p3 = os.pullEvent()
        if event == "rednet_message" then
            -- Got a rednet message, check if it's the response to our request
            local nSenderID, tMessage, sMessageProtocol = p1, p2, p3
            if sMessageProtocol == "dns" and type(tMessage) == "table" and tMessage.sType == "lookup response" then
                if tMessage.sProtocol == sProtocol then
                    if sHostname == nil then
                        table.insert( tResults, nSenderID )
                    elseif tMessage.sHostname == sHostname then
                        return nSenderID
                    end
                end
            end
        else
            -- Got a timer event, check it's the end of our timeout
            if p1 == timer then
                break
            end
        end
    end
    if tResults then
        return table.unpack( tResults )
    end
    return nil
end

local bRunning = false
function rednet.run()
    if bRunning then
        error( "rednet is already running", 2 )
    end
    bRunning = true
    
    while bRunning do
        local sEvent, p1, p2, p3, p4 = os.pullEventRaw()
        if sEvent == "modem_message" then
            -- Got a modem message, process it and add it to the rednet event queue
            local sModem, nChannel, nReplyChannel, tMessage = p1, p2, p3, p4
            if isOpen( sModem ) and ( nChannel == os.getComputerID() or nChannel == rednet.CHANNEL_BROADCAST ) then
                if type( tMessage ) == "table" and tMessage.nMessageID then
                    if not tReceivedMessages[ tMessage.nMessageID ] then
                        tReceivedMessages[ tMessage.nMessageID ] = true
                        tReceivedMessageTimeouts[ os.startTimer( 30 ) ] = tMessage.nMessageID
                        os.queueEvent( "rednet_message", nReplyChannel, tMessage.message, tMessage.sProtocol )
                    end
                end
            end

        elseif sEvent == "rednet_message" then
            -- Got a rednet message (queued from above), respond to dns lookup
            local nSenderID, tMessage, sProtocol = p1, p2, p3
            if sProtocol == "dns" and type(tMessage) == "table" and tMessage.sType == "lookup" then
                local sHostname = tHostnames[ tMessage.sProtocol ]
                if sHostname ~= nil and (tMessage.sHostname == nil or tMessage.sHostname == sHostname) then
                    rednet.send( nSenderID, {
                        sType = "lookup response",
                        sHostname = sHostname,
                        sProtocol = tMessage.sProtocol,
                    }, "dns" )
                end
            end

        elseif sEvent == "timer" then
            -- Got a timer event, use it to clear the event queue
            local nTimer = p1
            local nMessage = tReceivedMessageTimeouts[ nTimer ]
            if nMessage then
                tReceivedMessageTimeouts[ nTimer ] = nil
                tReceivedMessages[ nMessage ] = nil
            end
        end
    end
end

return rednet
