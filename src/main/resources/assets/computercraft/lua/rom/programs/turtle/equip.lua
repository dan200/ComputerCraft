
local tArgs = { ... }
local function printUsage()
	print( "Usage: equip <slot> <side>" )
end

if #tArgs ~= 2 then
    printUsage()
	return
end

local function equip( nSlot, fnEquipFunction )
    turtle.select( nSlot )
    local nOldCount = turtle.getItemCount( nSlot )
    if nOldCount == 0 then
        print( "Nothing to equip" )
    elseif fnEquipFunction() then
        local nNewCount = turtle.getItemCount( nSlot )
        if nNewCount > 0 then
            print( "Items swapped" )
        else
            print( "Item equipped" )
        end
    else
        print( "Item not equippable" )
    end
end

local nSlot = tonumber( tArgs[1] )
local sSide = tArgs[2]
if sSide == "left" then
    equip( nSlot, turtle.equipLeft )
elseif sSide == "right" then
    equip( nSlot, turtle.equipRight )
else
    printUsage()
    return
end
