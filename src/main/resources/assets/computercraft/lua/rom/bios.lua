
local nativegetfenv = getfenv
if _VERSION == "Lua 5.1" then
    -- If we're on Lua 5.1, install parts of the Lua 5.2/5.3 API so that programs can be written against it
    local nativeload = load
    local nativeloadstring = loadstring
    local nativesetfenv = setfenv
    function load( x, name, mode, env )
        if mode ~= nil and mode ~= "t" then
            error( "Binary chunk loading prohibited", 2 )
        end
        local ok, p1, p2 = pcall( function()        
            if type(x) == "string" then
                local result, err = nativeloadstring( x, name )
                if result then
                    if env then
                        env._ENV = env
                        nativesetfenv( result, env )
                    end
                    return result
                else
                    return nil, err
                end
            else
                local result, err = nativeload( x, name )
                if result then
                    if env then
                        env._ENV = env
                        nativesetfenv( result, env )
                    end
                    return result
                else
                    return nil, err
                end
            end
        end )
        if ok then
            return p1, p2
        else
            error( p1, 2 )
        end        
    end
    table.unpack = unpack
    table.pack = function( ... ) return { n = select( "#", ... ), ... } end

    -- Install the bit32 api
    local nativebit = bit
    bit32 = {}
    bit32.arshift = nativebit.brshift
    bit32.band = nativebit.band
    bit32.bnot = nativebit.bnot
    bit32.bor = nativebit.bor
    bit32.btest = function( a, b ) return nativebit.band(a,b) ~= 0 end
    bit32.bxor = nativebit.bxor
    bit32.lshift = nativebit.blshift
    bit32.rshift = nativebit.blogic_rshift

    if _CC_DISABLE_LUA51_FEATURES then
        -- Remove the Lua 5.1 features that will be removed when we update to Lua 5.2, for compatibility testing.
        -- See "disable_lua51_functions" in ComputerCraft.cfg
        setfenv = nil
        getfenv = nil
        loadstring = nil
        unpack = nil
        math.log10 = nil
        table.maxn = nil
        bit = nil
    end
end

if _VERSION == "Lua 5.3" then
    -- If we're on Lua 5.3, install the bit32 api from Lua 5.2
    -- (Loaded from a string so this file will still parse on <5.3 lua)
    load( [[
        bit32 = {}

        function bit32.arshift( n, bits )
            if type(n) ~= "number" or type(bits) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return n >> bits
        end

        function bit32.band( m, n )
            if type(m) ~= "number" or type(n) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return m & n
        end

        function bit32.bnot( n )
            if type(n) ~= "number" then
                error( "Expected number", 2 )
            end
            return ~n
        end

        function bit32.bor( m, n )
            if type(m) ~= "number" or type(n) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return m | n
        end

        function bit32.btest( m, n )
            if type(m) ~= "number" or type(n) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return (m & n) ~= 0
        end

        function bit32.bxor( m, n )
            if type(m) ~= "number" or type(n) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return m ~ n
        end

        function bit32.lshift( n, bits )
            if type(n) ~= "number" or type(bits) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return n << bits
        end

        function bit32.rshift( n, bits )
            if type(n) ~= "number" or type(bits) ~= "number" then
                error( "Expected number, number", 2 )
            end
            return n >> bits
        end
    ]] )()
end

if string.find( _HOST, "ComputerCraft" ) == 1 then
    -- Prevent access to metatables or environments of strings, as these are global between all computers
    local nativegetmetatable = getmetatable
    local nativeerror = error
    local nativetype = type
    local string_metatable = nativegetmetatable("")
    function getmetatable( t )
        local mt = nativegetmetatable( t )
        if mt == string_metatable then
            nativeerror( "Attempt to access string metatable", 2 )
        else
            return mt
        end
    end
    if _VERSION == "Lua 5.1" and not _CC_DISABLE_LUA51_FEATURES then
        local string_env = nativegetfenv(("").gsub)
        function getfenv( env )
            if env == nil then
                env = 2
            elseif nativetype( env ) == "number" and env > 0 then
                env = env + 1
            end
            local fenv = nativegetfenv(env)
            if fenv == string_env then
                --nativeerror( "Attempt to access string metatable", 2 )
                return nativegetfenv( 0 )
            else
                return fenv
            end
        end
    end
end

-- Install lua parts of the os api
function os.version()
    return "CraftOS 1.8"
end

function os.pullEventRaw( sFilter )
    return coroutine.yield( sFilter )
end

function os.pullEvent( sFilter )
    local eventData = table.pack( os.pullEventRaw( sFilter ) )
    if eventData[1] == "terminate" then
        error( "Terminated", 0 )
    end
    return table.unpack( eventData, 1, eventData.n )
end

-- Install globals
function sleep( nTime )
    if nTime ~= nil and type( nTime ) ~= "number" then
        error( "bad argument #1 (expected number, got " .. type( nTime ) .. ")", 2 ) 
    end
    local timer = os.startTimer( nTime or 0 )
    repeat
        local sEvent, param = os.pullEvent( "timer" )
    until param == timer
end

function write( sText )
    if sText ~= nil and type( sText ) ~= "string" and type( sText ) ~= "number" then
        error( "bad argument #1 (expected string, got " .. type( sText ) .. ")", 2 ) 
    end

    local w,h = term.getSize()        
    local x,y = term.getCursorPos()
    
    local nLinesPrinted = 0
    local function newLine()
        if y + 1 <= h then
            term.setCursorPos(1, y + 1)
        else
            term.setCursorPos(1, h)
            term.scroll(1)
        end
        x, y = term.getCursorPos()
        nLinesPrinted = nLinesPrinted + 1
    end
    
    -- Print the line with proper word wrapping
    while string.len(sText) > 0 do
        local whitespace = string.match( sText, "^[ \t]+" )
        if whitespace then
            -- Print whitespace
            term.write( whitespace )
            x,y = term.getCursorPos()
            sText = string.sub( sText, string.len(whitespace) + 1 )
        end
        
        local newline = string.match( sText, "^\n" )
        if newline then
            -- Print newlines
            newLine()
            sText = string.sub( sText, 2 )
        end
        
        local text = string.match( sText, "^[^ \t\n]+" )
        if text then
            sText = string.sub( sText, string.len(text) + 1 )
            if string.len(text) > w then
                -- Print a multiline word                
                while string.len( text ) > 0 do
                    if x > w then
                        newLine()
                    end
                    term.write( text )
                    text = string.sub( text, (w-x) + 2 )
                    x,y = term.getCursorPos()
                end
            else
                -- Print a word normally
                if x + string.len(text) - 1 > w then
                    newLine()
                end
                term.write( text )
                x,y = term.getCursorPos()
            end
        end
    end
    
    return nLinesPrinted
end

function print( ... )
    local nLinesPrinted = 0
    local nLimit = select("#", ... )
    for n = 1, nLimit do
        local s = tostring( select( n, ... ) )
        if n < nLimit then
            s = s .. "\t"
        end
        nLinesPrinted = nLinesPrinted + write( s )
    end
    nLinesPrinted = nLinesPrinted + write( "\n" )
    return nLinesPrinted
end

function printError( ... )
    local oldColour
    if term.isColour() then
        oldColour = term.getTextColour()
        term.setTextColour( colors.red )
    end
    print( ... )
    if term.isColour() then
        term.setTextColour( oldColour )
    end
end

function read( _sReplaceChar, _tHistory, _fnComplete, _sDefault )
    if _sReplaceChar ~= nil and type( _sReplaceChar ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( _sReplaceChar ) .. ")", 2 ) 
    end
    if _tHistory ~= nil and type( _tHistory ) ~= "table" then
        error( "bad argument #2 (expected table, got " .. type( _tHistory ) .. ")", 2 ) 
    end
    if _fnComplete ~= nil and type( _fnComplete ) ~= "function" then
        error( "bad argument #3 (expected function, got " .. type( _fnComplete ) .. ")", 2 ) 
    end
    if _sDefault ~= nil and type( _sDefault ) ~= "string" then
        error( "bad argument #4 (expected string, got " .. type( _sDefault ) .. ")", 2 ) 
    end
    term.setCursorBlink( true )

    local sLine
    if type( _sDefault ) == "string" then
        sLine = _sDefault
    else
        sLine = ""
    end
    local nHistoryPos
    local nPos = #sLine
    if _sReplaceChar then
        _sReplaceChar = string.sub( _sReplaceChar, 1, 1 )
    end

    local tCompletions
    local nCompletion
    local function recomplete()
        if _fnComplete and nPos == string.len(sLine) then
            tCompletions = _fnComplete( sLine )
            if tCompletions and #tCompletions > 0 then
                nCompletion = 1
            else
                nCompletion = nil
            end
        else
            tCompletions = nil
            nCompletion = nil
        end
    end

    local function uncomplete()
        tCompletions = nil
        nCompletion = nil
    end

    local w = term.getSize()
    local sx = term.getCursorPos()

    local function redraw( _bClear )
        local nScroll = 0
        if sx + nPos >= w then
            nScroll = (sx + nPos) - w
        end

        local cx,cy = term.getCursorPos()
        term.setCursorPos( sx, cy )
        local sReplace = (_bClear and " ") or _sReplaceChar
        if sReplace then
            term.write( string.rep( sReplace, math.max( string.len(sLine) - nScroll, 0 ) ) )
        else
            term.write( string.sub( sLine, nScroll + 1 ) )
        end

        if nCompletion then
            local sCompletion = tCompletions[ nCompletion ]
            local oldText, oldBg
            if not _bClear then
                oldText = term.getTextColor()
                oldBg = term.getBackgroundColor()
                term.setTextColor( colors.white )
                term.setBackgroundColor( colors.gray )
            end
            if sReplace then
                term.write( string.rep( sReplace, string.len( sCompletion ) ) )
            else
                term.write( sCompletion )
            end
            if not _bClear then
                term.setTextColor( oldText )
                term.setBackgroundColor( oldBg )
            end
        end

        term.setCursorPos( sx + nPos - nScroll, cy )
    end
    
    local function clear()
        redraw( true )
    end

    recomplete()
    redraw()

    local function acceptCompletion()
        if nCompletion then
            -- Clear
            clear()

            -- Find the common prefix of all the other suggestions which start with the same letter as the current one
            local sCompletion = tCompletions[ nCompletion ]
            sLine = sLine .. sCompletion
            nPos = string.len( sLine )

            -- Redraw
            recomplete()
            redraw()
        end
    end
    while true do
        local sEvent, param = os.pullEvent()
        if sEvent == "char" then
            -- Typed key
            clear()
            sLine = string.sub( sLine, 1, nPos ) .. param .. string.sub( sLine, nPos + 1 )
            nPos = nPos + 1
            recomplete()
            redraw()

        elseif sEvent == "paste" then
            -- Pasted text
            clear()
            sLine = string.sub( sLine, 1, nPos ) .. param .. string.sub( sLine, nPos + 1 )
            nPos = nPos + string.len( param )
            recomplete()
            redraw()

        elseif sEvent == "key" then
            if param == keys.enter then
                -- Enter
                if nCompletion then
                    clear()
                    uncomplete()
                    redraw()
                end
                break
                
            elseif param == keys.left then
                -- Left
                if nPos > 0 then
                    clear()
                    nPos = nPos - 1
                    recomplete()
                    redraw()
                end
                
            elseif param == keys.right then
                -- Right                
                if nPos < string.len(sLine) then
                    -- Move right
                    clear()
                    nPos = nPos + 1
                    recomplete()
                    redraw()
                else
                    -- Accept autocomplete
                    acceptCompletion()
                end

            elseif param == keys.up or param == keys.down then
                -- Up or down
                if nCompletion then
                    -- Cycle completions
                    clear()
                    if param == keys.up then
                        nCompletion = nCompletion - 1
                        if nCompletion < 1 then
                            nCompletion = #tCompletions
                        end
                    elseif param == keys.down then
                        nCompletion = nCompletion + 1
                        if nCompletion > #tCompletions then
                            nCompletion = 1
                        end
                    end
                    redraw()

                elseif _tHistory then
                    -- Cycle history
                    clear()
                    if param == keys.up then
                        -- Up
                        if nHistoryPos == nil then
                            if #_tHistory > 0 then
                                nHistoryPos = #_tHistory
                            end
                        elseif nHistoryPos > 1 then
                            nHistoryPos = nHistoryPos - 1
                        end
                    else
                        -- Down
                        if nHistoryPos == #_tHistory then
                            nHistoryPos = nil
                        elseif nHistoryPos ~= nil then
                            nHistoryPos = nHistoryPos + 1
                        end                        
                    end
                    if nHistoryPos then
                        sLine = _tHistory[nHistoryPos]
                        nPos = string.len( sLine ) 
                    else
                        sLine = ""
                        nPos = 0
                    end
                    uncomplete()
                    redraw()

                end

            elseif param == keys.backspace then
                -- Backspace
                if nPos > 0 then
                    clear()
                    sLine = string.sub( sLine, 1, nPos - 1 ) .. string.sub( sLine, nPos + 1 )
                    nPos = nPos - 1
                    recomplete()
                    redraw()
                end

            elseif param == keys.home then
                -- Home
                if nPos > 0 then
                    clear()
                    nPos = 0
                    recomplete()
                    redraw()
                end

            elseif param == keys.delete then
                -- Delete
                if nPos < string.len(sLine) then
                    clear()
                    sLine = string.sub( sLine, 1, nPos ) .. string.sub( sLine, nPos + 2 )                
                    recomplete()
                    redraw()
                end

            elseif param == keys["end"] then
                -- End
                if nPos < string.len(sLine ) then
                    clear()
                    nPos = string.len(sLine)
                    recomplete()
                    redraw()
                end

            elseif param == keys.tab then
                -- Tab (accept autocomplete)
                acceptCompletion()

            end

        elseif sEvent == "term_resize" then
            -- Terminal resized
            w = term.getSize()
            redraw()

        end
    end

    local cx, cy = term.getCursorPos()
    term.setCursorBlink( false )
    term.setCursorPos( w + 1, cy )
    print()
    
    return sLine
end

loadfile = function( _sFile, _tEnv )
    if type( _sFile ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( _sFile ) .. ")", 2 ) 
    end
    if _tEnv ~= nil and type( _tEnv ) ~= "table" then
        error( "bad argument #2 (expected table, got " .. type( _tEnv ) .. ")", 2 ) 
    end
    local file = fs.open( _sFile, "r" )
    if file then
        local func, err = load( file.readAll(), fs.getName( _sFile ), "t", _tEnv )
        file.close()
        return func, err
    end
    return nil, "File not found"
end

dofile = function( _sFile )
    if type( _sFile ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( _sFile ) .. ")", 2 ) 
    end
    local fnFile, e = loadfile( _sFile, _G )
    if fnFile then
        return fnFile()
    else
        error( e, 2 )
    end
end

-- Install the rest of the OS api
function os.run( _tEnv, _sPath, ... )
    if type( _tEnv ) ~= "table" then
        error( "bad argument #1 (expected table, got " .. type( _tEnv ) .. ")", 2 ) 
    end
    if type( _sPath ) ~= "string" then
        error( "bad argument #2 (expected string, got " .. type( _sPath ) .. ")", 2 ) 
    end
    local tArgs = table.pack( ... )
    local tEnv = _tEnv
    setmetatable( tEnv, { __index = _G } )
    local fnFile, err = loadfile( _sPath, tEnv )
    if fnFile then
        local ok, err = pcall( function()
            fnFile( table.unpack( tArgs, 1, tArgs.n ) )
        end )
        if not ok then
            if err and err ~= "" then
                printError( err )
            end
            return false
        end
        return true
    end
    if err and err ~= "" then
        printError( err )
    end
    return false
end

local tAPIsLoading = {}
function os.loadAPI( _sPath )
    if type( _sPath ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( _sPath ) .. ")", 2 ) 
    end
    local sName = fs.getName( _sPath )
    if sName:sub(-4) == ".lua" then
        sName = sName:sub(1,-5)
    end
    if tAPIsLoading[sName] == true then
        printError( "API "..sName.." is already being loaded" )
        return false
    end
    tAPIsLoading[sName] = true

    local tEnv = {}
    setmetatable( tEnv, { __index = _G } )
    local fnAPI, err = loadfile( _sPath, tEnv )
    if fnAPI then
        local ok, err = pcall( fnAPI )
        if not ok then
            printError( err )
            tAPIsLoading[sName] = nil
            return false
        end
    else
        printError( err )
        tAPIsLoading[sName] = nil
        return false
    end
    
    local tAPI = {}
    for k,v in pairs( tEnv ) do
        if k ~= "_ENV" then
            tAPI[k] =  v
        end
    end

    _G[sName] = tAPI    
    tAPIsLoading[sName] = nil
    return true
end

function os.unloadAPI( _sName )
    if type( _sName ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( _sName ) .. ")", 2 ) 
    end
    if _sName ~= "_G" and type(_G[_sName]) == "table" then
        _G[_sName] = nil
    end
end

function os.sleep( nTime )
    sleep( nTime )
end

local nativeShutdown = os.shutdown
function os.shutdown()
    nativeShutdown()
    while true do
        coroutine.yield()
    end
end

local nativeReboot = os.reboot
function os.reboot()
    nativeReboot()
    while true do
        coroutine.yield()
    end
end

-- Install the lua part of the HTTP api (if enabled)
if http then
    local nativeHTTPRequest = http.request

    local function wrapRequest( _url, _post, _headers, _binary )
        local ok, err = nativeHTTPRequest( _url, _post, _headers, _binary )
        if ok then
            while true do
                local event, param1, param2, param3 = os.pullEvent()
                if event == "http_success" and param1 == _url then
                    return param2
                elseif event == "http_failure" and param1 == _url then
                    return nil, param2, param3
                end
            end
        end
        return nil, err
    end
    
    http.get = function( _url, _headers, _binary)
        if type( _url ) ~= "string" then
            error( "bad argument #1 (expected string, got " .. type( _url ) .. ")", 2 ) 
        end
        if _headers ~= nil and type( _headers ) ~= "table" then
            error( "bad argument #2 (expected table, got " .. type( _headers ) .. ")", 2 ) 
        end
        return wrapRequest( _url, nil, _headers, _binary)
    end

    http.post = function( _url, _post, _headers, _binary)
        if type( _url ) ~= "string" then
            error( "bad argument #1 (expected string, got " .. type( _url ) .. ")", 2 ) 
        end
        if type( _post ) ~= "string" then
            error( "bad argument #2 (expected string, got " .. type( _post ) .. ")", 2 ) 
        end
        if _headers ~= nil and type( _headers ) ~= "table" then
            error( "bad argument #3 (expected table, got " .. type( _headers ) .. ")", 2 ) 
        end
        return wrapRequest( _url, _post or "", _headers, _binary)
    end

    http.request = function( _url, _post, _headers, _binary )
        if type( _url ) ~= "string" then
            error( "bad argument #1 (expected string, got " .. type( _url ) .. ")", 2 ) 
        end
        if _post ~= nil and type( _post ) ~= "string" then
            error( "bad argument #2 (expected string, got " .. type( _post ) .. ")", 2 ) 
        end
        if _headers ~= nil and type( _headers ) ~= "table" then
            error( "bad argument #3 (expected table, got " .. type( _headers ) .. ")", 2 ) 
        end
        local ok, err = nativeHTTPRequest( _url, _post, _headers, _binary )
        if not ok then
            os.queueEvent( "http_failure", _url, err )
        end
        return ok, err
    end
    
    local nativeCheckURL = http.checkURL
    http.checkURLAsync = nativeCheckURL
    http.checkURL = function( _url )
        local ok, err = nativeCheckURL( _url )
        if not ok then return ok, err end
    
        while true do
            local event, url, ok, err = os.pullEvent( "http_check" )
            if url == _url then return ok, err end
        end
    end
end

-- Install the lua part of the FS api
local tEmpty = {}
function fs.complete( sPath, sLocation, bIncludeFiles, bIncludeDirs )
    if type( sPath ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sPath ) .. ")", 2 ) 
    end
    if type( sLocation ) ~= "string" then
        error( "bad argument #2 (expected string, got " .. type( sLocation ) .. ")", 2 ) 
    end
    if bIncludeFiles ~= nil and type( bIncludeFiles ) ~= "boolean" then
        error( "bad argument #3 (expected boolean, got " .. type( bIncludeFiles ) .. ")", 2 ) 
    end
    if bIncludeDirs ~= nil and type( bIncludeDirs ) ~= "boolean" then
        error( "bad argument #4 (expected boolean, got " .. type( bIncludeDirs ) .. ")", 2 ) 
    end
    bIncludeFiles = (bIncludeFiles ~= false)
    bIncludeDirs = (bIncludeDirs ~= false)
    local sDir = sLocation
    local nStart = 1
    local nSlash = string.find( sPath, "[/\\]", nStart )
    if nSlash == 1 then
        sDir = ""
        nStart = 2
    end
    local sName
    while not sName do
        local nSlash = string.find( sPath, "[/\\]", nStart )
        if nSlash then
            local sPart = string.sub( sPath, nStart, nSlash - 1 )
            sDir = fs.combine( sDir, sPart )
            nStart = nSlash + 1
        else
            sName = string.sub( sPath, nStart )
        end
    end

    if fs.isDir( sDir ) then
        local tResults = {}
        if bIncludeDirs and sPath == "" then
            table.insert( tResults, "." )
        end
        if sDir ~= "" then
            if sPath == "" then
                table.insert( tResults, (bIncludeDirs and "..") or "../" )
            elseif sPath == "." then
                table.insert( tResults, (bIncludeDirs and ".") or "./" )
            end
        end
        local tFiles = fs.list( sDir )
        for n=1,#tFiles do
            local sFile = tFiles[n]
            if #sFile >= #sName and string.sub( sFile, 1, #sName ) == sName then
                local bIsDir = fs.isDir( fs.combine( sDir, sFile ) )
                local sResult = string.sub( sFile, #sName + 1 )
                if bIsDir then
                    table.insert( tResults, sResult .. "/" )
                    if bIncludeDirs and #sResult > 0 then
                        table.insert( tResults, sResult )
                    end
                else
                    if bIncludeFiles and #sResult > 0 then
                        table.insert( tResults, sResult )
                    end
                end
            end
        end
        return tResults
    end
    return tEmpty
end

-- Load APIs
local bAPIError = false
local tApis = fs.list( "rom/apis" )
for n,sFile in ipairs( tApis ) do
    if string.sub( sFile, 1, 1 ) ~= "." then
        local sPath = fs.combine( "rom/apis", sFile )
        if not fs.isDir( sPath ) then
            if not os.loadAPI( sPath ) then
                bAPIError = true
            end
        end
    end
end

if turtle and fs.isDir( "rom/apis/turtle" ) then
    -- Load turtle APIs
    local tApis = fs.list( "rom/apis/turtle" )
    for n,sFile in ipairs( tApis ) do
        if string.sub( sFile, 1, 1 ) ~= "." then
            local sPath = fs.combine( "rom/apis/turtle", sFile )
            if not fs.isDir( sPath ) then
                if not os.loadAPI( sPath ) then
                    bAPIError = true
                end
            end
        end
    end
end

if pocket and fs.isDir( "rom/apis/pocket" ) then
    -- Load pocket APIs
    local tApis = fs.list( "rom/apis/pocket" )
    for n,sFile in ipairs( tApis ) do
        if string.sub( sFile, 1, 1 ) ~= "." then
            local sPath = fs.combine( "rom/apis/pocket", sFile )
            if not fs.isDir( sPath ) then
                if not os.loadAPI( sPath ) then
                    bAPIError = true
                end
            end
        end
    end
end

if commands and fs.isDir( "rom/apis/command" ) then
    -- Load command APIs
    if os.loadAPI( "rom/apis/command/commands.lua" ) then
        -- Add a special case-insensitive metatable to the commands api
        local tCaseInsensitiveMetatable = {
            __index = function( table, key )
                local value = rawget( table, key )
                if value ~= nil then
                    return value
                end
                if type(key) == "string" then
                    local value = rawget( table, string.lower(key) )
                    if value ~= nil then
                        return value
                    end
                end
                return nil
            end
        }
        setmetatable( commands, tCaseInsensitiveMetatable )
        setmetatable( commands.async, tCaseInsensitiveMetatable )

        -- Add global "exec" function
        exec = commands.exec
    else
        bAPIError = true
    end
end

if bAPIError then
    print( "Press any key to continue" )
    os.pullEvent( "key" )
    term.clear()
    term.setCursorPos( 1,1 )
end

-- Set default settings
settings.set( "shell.allow_startup", true )
settings.set( "shell.allow_disk_startup", (commands == nil) )
settings.set( "shell.autocomplete", true )
settings.set( "edit.autocomplete", true ) 
settings.set( "edit.default_extension", "lua" )
settings.set( "paint.default_extension", "nfp" )
settings.set( "lua.autocomplete", true )
settings.set( "list.show_hidden", false )
if term.isColour() then
    settings.set( "bios.use_multishell", true )
end
if _CC_DEFAULT_SETTINGS then
    for sPair in string.gmatch( _CC_DEFAULT_SETTINGS, "[^,]+" ) do
        local sName, sValue = string.match( sPair, "([^=]*)=(.*)" )
        if sName and sValue then
            local value
            if sValue == "true" then
                value = true
            elseif sValue == "false" then
                value = false
            elseif sValue == "nil" then
                value = nil
            elseif tonumber(sValue) then
                value = tonumber(sValue)
            else
                value = sValue
            end
            if value ~= nil then
                settings.set( sName, value )
            else
                settings.unset( sName )
            end
        end
    end
end

-- Load user settings
if fs.exists( ".settings" ) then
    settings.load( ".settings" )
end

-- Run the shell
local ok, err = pcall( function()
    parallel.waitForAny( 
        function()
            local sShell
            if term.isColour() and settings.get( "bios.use_multishell" ) then
                sShell = "rom/programs/advanced/multishell.lua"
            else
                sShell = "rom/programs/shell.lua"
            end
            os.run( {}, sShell )
            os.run( {}, "rom/programs/shutdown.lua" )
        end,
        function()
            rednet.run()
        end )
end )

-- If the shell errored, let the user read it.
term.redirect( term.native() )
if not ok then
    printError( err )
    pcall( function()
        term.setCursorBlink( false )
        print( "Press any key to continue" )
        os.pullEvent( "key" )
    end )
end

-- End
os.shutdown()
