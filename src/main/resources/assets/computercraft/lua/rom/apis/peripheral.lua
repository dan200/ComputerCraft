local native = peripheral
local internalcou = 1
local perita = {}

function getNames()
	local tResults = {}
	for n,sSide in ipairs( rs.getSides() ) do
		if native.isPresent( sSide ) then
			table.insert( tResults, sSide )
			if native.getType( sSide ) == "modem" and not native.call( sSide, "isWireless" ) then
				local tRemote = native.call( sSide, "getNamesRemote" )
				for n,sName in ipairs( tRemote ) do
					table.insert( tResults, sName )
				end
			end
		end
	end
    for k,v in pairs(perita) do
        table.insert( tResults, k )
    end
	return tResults
end

function isPresent( _sSide )
    if type( _sSide ) ~= "string" then
        error( "Expected string", 2 )
    end
	if native.isPresent( _sSide ) then
		return true
	end
    if type(perita[_sSide]) == "table" then
        return true
    end
	for n,sSide in ipairs( rs.getSides() ) do
		if native.getType( sSide ) == "modem" and not native.call( sSide, "isWireless" ) then
			if native.call( sSide, "isPresentRemote", _sSide )  then
				return true
			end
		end
	end
	return false
end

function getType( _sSide )
    if type( _sSide ) ~= "string" then
        error( "Expected string", 2 )
    end
    if type(perita[_sSide]) == "table" then
        return perita[_sSide]["type"]
    end
	if native.isPresent( _sSide ) then
		return native.getType( _sSide )
	end
	for n,sSide in ipairs( rs.getSides() ) do
		if native.getType( sSide ) == "modem" and not native.call( sSide, "isWireless" ) then
			if native.call( sSide, "isPresentRemote", _sSide )  then
				return native.call( sSide, "getTypeRemote", _sSide ) 
			end
		end
	end
	return nil
end

function getMethods( _sSide )
    if type( _sSide ) ~= "string" then
        error( "Expected string", 2 )
    end
    if type(perita[_sSide]) == "table" then
        return perita[_sSide]["env"]
    end
	if native.isPresent( _sSide ) then
		return native.getMethods( _sSide )
	end
	for n,sSide in ipairs( rs.getSides() ) do
		if native.getType( sSide ) == "modem" and not native.call( sSide, "isWireless" ) then
			if native.call( sSide, "isPresentRemote", _sSide )  then
				return native.call( sSide, "getMethodsRemote", _sSide ) 
			end
		end
	end
	return nil
end

function call( _sSide, _sMethod, ... )
    if type( _sSide ) ~= "string" or type( _sMethod ) ~= "string" then
        error( "Expected string, string", 2 )
    end
    if type(perita[_sSide]) == "table" then
        return perita[_sSide]["env"][_sMethod](...)
    end
	if native.isPresent( _sSide ) then
		return native.call( _sSide, _sMethod, ... )
	end
	for n,sSide in ipairs( rs.getSides() ) do
		if native.getType( sSide ) == "modem" and not native.call( sSide, "isWireless" ) then
			if native.call( sSide, "isPresentRemote", _sSide )  then
				return native.call( sSide, "callRemote", _sSide, _sMethod, ... ) 
			end
		end
	end
	return nil
end

function wrap( _sSide )
    if type( _sSide ) ~= "string"  then
        error( "Expected string", 2 )
    end
    if type(perita[_sSide]) == "table" then
        return perita[_sSide]["env"]
    end
	if peripheral.isPresent( _sSide ) then
		local tMethods = peripheral.getMethods( _sSide )
		local tResult = {}
		for n,sMethod in ipairs( tMethods ) do
			tResult[sMethod] = function( ... )
				return peripheral.call( _sSide, sMethod, ... )
			end
		end
		return tResult
	end
	return nil
end

function find( sType, fnFilter )
    if type( sType ) ~= "string" or (fnFilter ~= nil and type( fnFilter ) ~= "function") then
        error( "Expected string, [function]", 2 )
    end
	local tResults = {}
    for k,v in pairs(perita) do
        if v.type == sType then
            table.insert( tResults, v.env )
        end
    end
	for n,sName in ipairs( peripheral.getNames() ) do
		if peripheral.getType( sName ) == sType then
			local wrapped = peripheral.wrap( sName )
			if fnFilter == nil or fnFilter( sName, wrapped ) then
				table.insert( tResults, wrapped )
			end
		end
	end
	return table.unpack( tResults )
end

function create( name, env, side )
    if type(name) ~= "string" then
        error( "Expected string", 2 )
        return false
    end
    if type(env) ~= "table" then
        error( "Expected table", 2 )
        return false
    end
    if side == nil then
        side = "internal"..internalcou
        internalcou = internalcou + 1
    end
    perita[side] = {}
    perita[side]["type"] = name
    perita[side]["env"] = env
    os.queueEvent("peripheral",side)
    return true,side
end

function remove( side )
    if type(perita[side]) == "table" then
        perita[side] = nil
        os.queueEvent("peripheral_detach",side)
        return true
    else
        error( "Not a virtual peripheral", 2 )
        return false
    end
end
