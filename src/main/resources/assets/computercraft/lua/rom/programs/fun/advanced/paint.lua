-- Paint created by nitrogenfingers (edited by dan200 & co)
-- http://www.youtube.com/user/NitrogenFingers

------------
-- Fields --
------------

-- The width and height of the terminal
local w,h = term.getSize()

-- The selected colours on the left and right mouse button, and the colour of the canvas
local leftColour, rightColour = colours.white, nil
local canvasColour = colours.black

-- The values stored in the canvas
local canvas = {}

-- The menu options
local mChoices = { "Save","Exit" }

-- The message displayed in the footer bar
local fMessage = "Press Ctrl to access menu"

-- Colour to character conversions
local tColourLookup = {}
for n=1,16 do
    tColourLookup[ 2^(n-1) ] = string.sub( "0123456789abcdef",n,n )
end

-----------------------
-- Pre-Flight Checks --
-----------------------

-- Determine if we can even run this
if not term.isColour() then
    print("Requires an Advanced Computer")
    return
end

-- Determines if the file exists, and can be edited on this computer
local sPath = ...
if not sPath then
    print("Usage: paint <path>")
    return
end
sPath = shell.resolve( sPath )
if fs.isDir( sPath ) then
    print("Cannot edit a directory.")
    return
end

-- Create .nfp files by default
if not fs.exists( sPath ) and not string.find( sPath, "%." ) then
    local sExtension = settings.get("paint.default_extension", "" )
    if sExtension ~= "" then
        sPath = sPath .. "." .. sExtension
    end
end

local bReadOnly = fs.isReadOnly(sPath)

---------------
-- Functions --
---------------

--[[  
    Draws colour picker sidebar, the palette and the footer
    returns: nil
]]
local function drawInterface()
    -- Footer
    term.setCursorPos(1, h)
    term.setBackgroundColour(colours.black)
    term.setTextColour(colours.yellow)
    term.clearLine()
    term.write(fMessage)
    
    -- Colour Picker
    for i=1,16 do
        term.setCursorPos(w-1, i)
        term.setBackgroundColour( 2^(i-1) )
        term.write("  ")
    end

    term.setCursorPos(w-1, 17)
    term.setBackgroundColour( canvasColour )
    term.setTextColour( colours.grey )
    term.write("\127\127")
            
    -- Left and Right Selected Colours
    term.setCursorPos(w-1, 18)
    if leftColour ~= nil then
        term.setBackgroundColour( leftColour )
        term.write(" ")
    else
        term.setBackgroundColour( canvasColour )
        term.setTextColour( colours.grey )
        term.write("\127")
    end
    if rightColour ~= nil then
        term.setBackgroundColour( rightColour )
        term.write(" ")
    else
        term.setBackgroundColour( canvasColour )
        term.setTextColour( colours.grey )
        term.write("\127")
    end

    -- Padding
    term.setBackgroundColour( canvasColour )
    for i=20,h-1 do
        term.setCursorPos(w-1, i)
        term.write("  ")
    end
end

--[[  
    Converts each colour in the canvas and draws it
    returns: nil
]]
local function drawCanvas()
    local TC = string.rep( "7", w-2 )
    for y = 1, h-1 do
        local T, BC = {}, {}
        for x = 1, w-2 do
            local pixel = canvas[y] and canvas[y][x]
            if pixel and pixel ~= 0 then
                T[x], BC[x] = " ", tColourLookup[pixel]
            else
                T[x], BC[x] = "\127", tColourLookup[canvasColour]
            end
        end
        term.setCursorPos( 1, y )
        term.blit( table.concat( T ), TC, table.concat( BC ) )
    end
end

--[[
    Draws menu options and handles input from within the menu.
    returns: true if the program is to be exited; false otherwise
]]
local function accessMenu()
    -- Selected menu option
    local selection = 1
    term.setTextColour(colours.white)
    term.setBackgroundColour(colours.black)
    while true do
        -- Draw the menu
        term.setCursorPos(1,h)
        term.clearLine()
        for k,v in pairs(mChoices) do
            if selection==k then 
                term.blit( "["..v.."]", "4"..string.rep(" ",#v).."4", string.rep("f",#v+2) )
            else
                term.write(" "..v.." ")
            end
        end
        
        -- Handle input in the menu
        local id,key = os.pullEvent("key")
        
        -- S and E are shortcuts
        if key == keys.s then
            selection = 1
            key = keys.enter
        elseif key == keys.e then
            selection = 2
            key = keys.enter
        end
        
        if key == keys.right then
            -- Move right
            selection = selection == #mChoices and 1 or (selection + 1)
        
        elseif key == keys.left then
            -- Move left
            selection = selection == 1 and #mChoices or (selection - 1)
        
        elseif key == keys.enter then
            -- Select an option
            if mChoices[selection]=="Save" then
                if bReadOnly then 
                    fMessage = "Access Denied"
                    return false
                end
                fMessage = (paintutils.saveImage( canvas, sPath ) and "Saved to " or "Error saving to ")..sPath
                return false
            elseif mChoices[selection]=="Exit" then 
                return true
            end
        elseif key == keys.leftCtrl or keys == keys.rightCtrl then
            -- Cancel the menu
            return false 
        end
    end
end

--[[  
    Runs the main thread of execution. Draws the canvas and interface, and handles
    mouse and key events.
    returns: nil
]]
local function handleEvents()
    while true do
        local id,p1,p2,p3 = os.pullEvent()
        if id=="mouse_click" or id=="mouse_drag" and p1 < 3 and p2 > 0 and p2 <= w and p3 > 0 and p3 < h then
            if p2 >= w-1 and p3 <= 17 then
                if id ~= "mouse_drag" then
                    -- Selecting an items in the colour picker
                    if p3 <= 16 then
                        if p1==1 then
                            leftColour = 2^(p3-1)
                        else
                            rightColour = 2^(p3-1)
                        end
                    else
                        if p1==1 then
                            leftColour = nil
                        else
                            rightColour = nil
                        end
                    end
                    drawInterface()
                end
            elseif p2 < w-1 then
                -- Clicking on the canvas
                local paintColour
                if p1==1 then
                    paintColour = leftColour
                else
                    paintColour = rightColour
                end
                if not canvas[p3] then
                    canvas[p3] = {}
                end
                canvas[p3][p2] = paintColour

                term.setCursorPos( p2, p3 )
                if paintColour then
                    term.blit( " ", " ", tColourLookup[paintColour] )
                else
                    term.blit( "\127", "7", tColourLookup[canvasColour] )
                end
            end
        elseif id=="key" then
            if p1==keys.leftCtrl or p1==keys.rightCtrl then
                if accessMenu() then return end
                drawInterface()
            end
        elseif id=="term_resize" then
            w,h = term.getSize()
            drawCanvas()
            drawInterface()
        end
    end
end

--------------------
-- Initialisation --
--------------------

if fs.exists( sPath ) then canvas = paintutils.loadImage( sPath ) end
drawCanvas()
drawInterface()

---------------
-- Main Loop --
---------------

handleEvents()

--------------
-- Shutdown --
--------------

term.setBackgroundColour(colours.black)
term.setTextColour(colours.white)
term.clear()
term.setCursorPos(1,1)
