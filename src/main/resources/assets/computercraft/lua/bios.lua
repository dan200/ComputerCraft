-- Look at settings for OS path

local sOSPath
-- If there is a path in the setting then load that


-- Check that the path is valid
if type(sOSPath) == "string" and fs.exists(sOSPath) then
  -- Attempt to run it
  local ok, err = pcall(sOSPath)
  
end

-- If it was not valid or crashes then load CraftOS

local ok, err = pcall(sOSPath)
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
