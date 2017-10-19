
if not turtle then
	error( "Cannot load turtle API on computer", 2 )
end
native = turtle.native or turtle

local function addCraftMethod( object )
    if peripheral.getType( "left" ) == "workbench" then
        object.craft = function( ... )
            return peripheral.call( "left", "craft", ... )
        end
    elseif peripheral.getType( "right" ) == "workbench" then
        object.craft = function( ... )
            return peripheral.call( "right", "craft", ... )
        end
    else
        object.craft = nil
    end
end

-- Put commands into environment table
local env
if shell then
    env = {}
else
    env = _ENV
end

for k,v in pairs( native ) do
    if k == "equipLeft" or k == "equipRight" then
        env[k] = function( ... )
            local result, err = v( ... )
            addCraftMethod( turtle )
            return result, err
        end
    else
    	env[k] = v
    end
end
addCraftMethod( env )

return env
