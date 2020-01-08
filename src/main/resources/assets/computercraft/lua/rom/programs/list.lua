
local tArgs = { ... }

-- Get all the files in the directory
local sDir = shell.dir()
local bShowHiddenOption

if tArgs[1] == "-a" then
    bShowHiddenOption = true
    if tArgs[2] ~= nil then
        sDir = shell.resolve( tArgs[2] )
    end
elseif tArgs[1] ~= nil then
    sDir = shell.resolve( tArgs[1] )
end

if not fs.isDir( sDir ) then
    printError( "Not a directory" )
    return
end

-- Sort into dirs/files, and calculate column count
local tAll = fs.list( sDir )
local tFiles = {}
local tDirs = {}

local bShowHiddenSetting = settings.get( "list.show_hidden" )
for n, sItem in pairs( tAll ) do
    if bShowHiddenSetting or string.sub( sItem, 1, 1 ) ~= "." or bShowHiddenOption then
        local sPath = fs.combine( sDir, sItem )
        if fs.isDir( sPath ) then
            table.insert( tDirs, sItem )
        else
            table.insert( tFiles, sItem )
        end
    end
end
table.sort( tDirs )
table.sort( tFiles )

if term.isColour() then
    textutils.pagedTabulate( colors.green, tDirs, colors.white, tFiles )
else
    textutils.pagedTabulate( tDirs, tFiles )
end
