
-- Sandbox things and such
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

-- Implement core Lua functions
loadfile = function( _sFile, _tEnv )
    local file = fs.open( _sFile, "r" )
    if file then
        local func, err = load( file.readAll(), fs.getName( _sFile ), "t", _tEnv )
        file.close()
        return func, err
    end
    return nil, "File not found"
end

dofile = function( _sFile )
    local fnFile, e = loadfile( _sFile, _G )
    if fnFile then
        return fnFile()
    else
        error( e, 2 )
    end
end





-- The actual custom OS loader bit

-- A modified copy of os.loadAPI so that some of this can be sane
-- Returns the API as a table
local function apiBootStrap(_sPath)
    local tEnv = {}
    setmetatable( tEnv, { __index = _G } )
    local fnAPI, err = loadfile( _sPath, tEnv )
    if fnAPI then
        local ok, err = pcall( fnAPI )
        if not ok then
            return false
        end
    else
        return false
    end
    
    local tAPI = {}
    for k,v in pairs( tEnv ) do
        if k ~= "_ENV" then
            tAPI[k] =  v
        end
    end

    return true, tAPI
end


-- Look at settings for OS path

local sOSPath
-- If there is a path in the setting then load that


-- Check that the path is valid
if type(sOSPath) == "string" and fs.exists(sOSPath) then
  -- Attempt to run it
  local ok, err = pcall(sOSPath)
  
end

-- If it was not valid or crashes then load CraftOS

local ok, err = pcall(sOSFile)
-- If the shell errored, let the user read it.
term.redirect( term.native() )
if not ok then
    printError( err ) -- Make local Copy or move this and prerequisits?
    pcall( function()
        term.setCursorBlink( false )
        print( "Press any key to continue" )
        coroutine.yield( "key" ) -- No OS API so no event pulling
    end )
end
