
local function printUsage()
    print( "Usage:" )
    print( "wget <url> [filename]" )
end
 
local tArgs = { ... }
if #tArgs < 1 then
    printUsage()
    return
end
 
if not http then
    printError( "wget requires http API" )
    printError( "Set http_enable to true in ComputerCraft.cfg" )
    return
end

local function getFilename( sUrl )
    while sUrl:sub(#sUrl) == "/" do
        sUrl = sUrl:sub(1,#sUrl-1) --Remove any trailing slashes
    end
    local pos = sUrl:find("/")
    while sUrl:find("/", pos + 1) do
        pos = sUrl:find("/", pos + 1) --Find the last /
    end
    return sUrl:sub(pos+1)
end

local function get( sUrl )
    write( "Connecting to " .. sUrl .. "... " )

    local ok, err = http.checkURL( sUrl )
    if not ok then
        print( "Failed." )
        if err then
            printError( err )
        end
        return nil
    end

    local response = http.get( sUrl , nil , true )
    if not response then
        print( "Failed." )
        return nil
    end

    print( "Success." )

    local sResponse = response.readAll()
    response.close()
    return sResponse
end
 
-- Determine file to download
local sUrl = tArgs[1]
local sFile = tArgs[2] or getFilename(sUrl)
local sPath = shell.resolve( sFile )
if fs.exists( sPath ) then
    print( "File already exists" )
    return
end

-- Do the get
local res = get( sUrl )
if res then
    local file = fs.open( sPath, "wb" )
    file.write( res )
    file.close()

    print( "Downloaded as "..sFile )
end
