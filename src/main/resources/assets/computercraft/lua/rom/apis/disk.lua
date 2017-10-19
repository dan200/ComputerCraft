local disk
if shell then
    disk = {}
else
    disk = _ENV
end

local function isDrive( name )
    if type( name ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( name ) .. ")", 3 ) 
    end
	return peripheral.getType( name ) == "drive"
end

function disk.isPresent( name )
	if isDrive( name ) then
		return peripheral.call( name, "isDiskPresent" )
	end
	return false
end

function disk.getLabel( name )
	if isDrive( name ) then
		return peripheral.call( name, "getDiskLabel" )
	end
	return nil
end

function disk.setLabel( name, label )
	if isDrive( name ) then
		peripheral.call( name, "setDiskLabel", label )
	end
end

function disk.hasData( name )
	if isDrive( name ) then
		return peripheral.call( name, "hasData" )
	end
	return false
end

function disk.getMountPath( name )
	if isDrive( name ) then
		return peripheral.call( name, "getMountPath" )
	end
	return nil
end

function disk.hasAudio( name )
	if isDrive( name ) then
		return peripheral.call( name, "hasAudio" )
	end
	return false
end

function disk.getAudioTitle( name )
	if isDrive( name ) then
		return peripheral.call( name, "getAudioTitle" )
	end
	return nil
end

function disk.playAudio( name )
	if isDrive( name ) then
		peripheral.call( name, "playAudio" )
	end
end

function disk.stopAudio( name )
	if not name then
		for n,sName in ipairs( peripheral.getNames() ) do
			stopAudio( sName )
		end
	else
		if isDrive( name ) then
			peripheral.call( name, "stopAudio" )
		end
	end
end

function disk.eject( name )
	if isDrive( name ) then
		peripheral.call( name, "ejectDisk" )
	end
end

function disk.getID( name )
	if isDrive( name ) then
		return peripheral.call( name, "getDiskID" )
	end
	return nil
end

return disk
