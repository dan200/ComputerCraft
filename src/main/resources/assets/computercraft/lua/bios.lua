
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

--Boot

local strBoot
if fs.exists( "boot.lua" ) and not fs.isDir( "boot.lua" ) then
    strBoot = "boot.lua"
elseif fs.exists( "boot" ) and not fs.isDir( "boot" ) then
    strBoot = "boot"
else
    strBoot = "rom/boot.lua"
end
local bootfile = fs.open( strBoot , "r" )
local fnBoot, loaderr = load( bootfile.readAll(), "boot", "t", _G )
bootfile.close()
if fnBoot ~= nil then
    local success, err = pcall( fnBoot )
	if not success then
        term.write( "Error executing " .. bootfilename .. ":\n" .. err )
		coroutine.yield( "key" )
	end
else
    term.write( "Error loading " .. bootfilename .. ":\n" .. err )
	coroutine.yield( "key" )
end

-- End
os.shutdown()

