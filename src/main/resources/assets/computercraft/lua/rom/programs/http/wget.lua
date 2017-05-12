
local function printUsage()
    print( "Usage:" )
    print( "wget <url> <filename>" )
end
 
local tArgs = { ... }
if #tArgs < 2 then
    printUsage()
    return
end
 
if not http then
    printError( "wget requires http API" )
    printError( "Set http_enable to true in ComputerCraft.cfg" )
    return
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

    local response = http.get( sUrl )
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
local sFile = tArgs[2]
local sPath = shell.resolve( sFile )
if fs.exists( sPath ) then
    print( "File already exists" )
    return
end

-- Do the get
local res = get( sUrl )
if res then
    local file = fs.open( sPath, "w" )
    file.write( res )
    file.close()

    print( "Downloaded as "..sFile )
end
