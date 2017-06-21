local setPos, write, setCol, blit, rep, concat = term.setCursorPos, term.write, term.setBackgroundColour, term.blit, string.rep, table.concat

local maxn = table.maxn or function( tTable )
    local maxn = 0
    for n in pairs( tTable ) do
        if type( n ) == "number" and n > maxn then
            maxn = n
        end
    end
    return maxn
end

local tColourLookup = {}
local tColourReverseLookup = {}
for n=1,16 do
    tColourLookup[ string.byte( "0123456789abcdef",n,n ) ] = 2^(n-1)
    tColourReverseLookup[ 2^(n-1) ] = string.sub( "0123456789abcdef",n,n )
end

function loadImage( sPath )
    if type( sPath ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( sPath ) .. ")", 2 )
    end

    local tImage = {}
    if fs.exists( sPath ) then
        for sLine in io.lines(sPath) do
            local tLine = {}
            for x=1,#sLine do
                tLine[x] = tColourLookup[ string.byte( sLine,x ) ] or 0
            end
            table.insert( tImage, tLine )
        end
        return tImage
    end
    return nil
end

function saveImage( tImage, sPath )
    if type( tImage ) ~= "table" then error( "bad argument #1 (expected table, got " .. type( tImage ) .. ")", 2 ) end
    if type( sPath ) ~= "string" then error( "bad argument #2 (expected string, got " .. type( sPath ) .. ")", 2 ) end

    local file, lines, lastRow = fs.open(sPath, "w" ), {}, 0
    if not file then return false end
    
    for y=1,maxn( tImage ) do
        local tIn, tOut, lastCol = tImage[y], {}, 0
        
        if tIn then
            for x=1,maxn( tIn ) do
                local pixel = tColourReverseLookup[ tIn[x] ]
                if pixel then
                    tOut[x], lastCol = pixel, x
                else
                    tOut[x] = " "
                end
            end
        end

        if lastCol > 0 then
        	lines[y], lastRow = concat( tOut, "", 1, lastCol ), y
        else
        	lines[y] = ""
        end
    end
    
    for y=1,lastRow do
        file.writeLine(lines[y])
    end
    
    file.close()
    return true
end

function drawPixel( xPos, yPos, nColour )
    if type( xPos ) ~= "number" then error( "bad argument #1 (expected number, got " .. type( xPos ) .. ")", 2 ) end
    if type( yPos ) ~= "number" then error( "bad argument #2 (expected number, got " .. type( yPos ) .. ")", 2 ) end
    if nColour ~= nil and type( nColour ) ~= "number" then error( "bad argument #3 (expected number, got " .. type( nColour ) .. ")", 2 ) end

    if nColour then
        setCol( nColour )
    end
    setPos( xPos, yPos )
    write( " " )
end

function drawLine( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" then error( "bad argument #1 (expected number, got " .. type( startX ) .. ")", 2 ) end
    if type( startY ) ~= "number" then error( "bad argument #2 (expected number, got " .. type( startY ) .. ")", 2 ) end
    if type( endX ) ~= "number" then error( "bad argument #3 (expected number, got " .. type( endX ) .. ")", 2 ) end
    if type( endY ) ~= "number" then error( "bad argument #4 (expected number, got " .. type( endY ) .. ")", 2 ) end
    if nColour ~= nil and type( nColour ) ~= "number" then error( "bad argument #5 (expected number, got " .. type( nColour ) .. ")", 2 ) end
    
    startX = math.floor(startX)
    startY = math.floor(startY)
    endX = math.floor(endX)
    endY = math.floor(endY)

    if nColour then
        setCol( nColour )
    end
    if startX == endX and startY == endY then
        setPos( startX, startY )
        write(" ")
        return
    end
    
    local minX, minY, maxX, maxY = math.min( startX, endX )
    if minX == startX then
        minY = startY
        maxX = endX
        maxY = endY
    else
        minY = endY
        maxX = startX
        maxY = startY
    end

    -- TODO: clip to screen rectangle?
        
    local xDiff = maxX - minX
    local yDiff = maxY - minY
    
    if minY == maxY then
        setPos( minX, minY )
        write( rep( " ", xDiff + 1 ) )
        return
    end
            
    if xDiff > math.abs(yDiff) then
        local y = minY
        local dy = yDiff / xDiff
        for x=minX,maxX do
            setPos( x, math.floor( y + 0.5 ) )
            write( " " )
            y = y + dy
        end
    else
        local x, mul = minX, maxY >= minY and 1 or -1
        local dx = xDiff / yDiff * mul
        for y=minY,maxY,mul do
            setPos( math.floor( x + 0.5 ), y )
            write( " " )
            x = x + dx
        end
    end
end

function drawBox( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" then error( "bad argument #1 (expected number, got " .. type( startX ) .. ")", 2 ) end
    if type( startY ) ~= "number" then error( "bad argument #2 (expected number, got " .. type( startY ) .. ")", 2 ) end
    if type( endX ) ~= "number" then error( "bad argument #3 (expected number, got " .. type( endX ) .. ")", 2 ) end
    if type( endY ) ~= "number" then error( "bad argument #4 (expected number, got " .. type( endY ) .. ")", 2 ) end
    if nColour ~= nil and type( nColour ) ~= "number" then error( "bad argument #5 (expected number, got " .. type( nColour ) .. ")", 2 ) end

    startX = math.floor(startX)
    startY = math.floor(startY)
    endX = math.floor(endX)
    endY = math.floor(endY)

    if nColour then
        setCol( nColour )
    end
    if startX == endX and startY == endY then
        setPos( startX, startY )
        write( " " )
        return
    end
    
    local minX, minY, maxX, maxY
    if startX < endX then minX, maxX = startX, endX else minX, maxX = endX, startX end
    if startY < endY then minY, maxY = startY, endY else minY, maxY = endY, startY end

    local sStr = rep( " ", maxX - minX + 1 )
    setPos( minX, minY )
    write( sStr )
    setPos( minX, maxY )
    write( sStr )
    
    for y=(minY+1),(maxY-1) do
        setPos( minX, y )
        write( " " )
        setPos( maxX, y )
        write( " " )
    end
end

function drawFilledBox( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" then error( "bad argument #1 (expected number, got " .. type( startX ) .. ")", 2 ) end
    if type( startY ) ~= "number" then error( "bad argument #2 (expected number, got " .. type( startY ) .. ")", 2 ) end
    if type( endX ) ~= "number" then error( "bad argument #3 (expected number, got " .. type( endX ) .. ")", 2 ) end
    if type( endY ) ~= "number" then error( "bad argument #4 (expected number, got " .. type( endY ) .. ")", 2 ) end
    if nColour ~= nil and type( nColour ) ~= "number" then error( "bad argument #5 (expected number, got " .. type( nColour ) .. ")", 2 ) end

    startX = math.floor(startX)
    startY = math.floor(startY)
    endX = math.floor(endX)
    endY = math.floor(endY)

    if nColour then
        setCol( nColour )
    end
    if startX == endX and startY == endY then
        setPos( startX, startY )
        write( " " )
        return
    end

    local minX, minY, maxX, maxY
    if startX < endX then minX, maxX = startX, endX else minX, maxX = endX, startX end
    if startY < endY then minY, maxY = startY, endY else minY, maxY = endY, startY end

    local sStr = rep( " ", maxX - minX + 1 )
    for y=minY,maxY do
        setPos( minX, y )
        write( sStr )
    end
end

function drawImage( tImage, xPos, yPos )
    if type( tImage ) ~= "table" then error( "bad argument #1 (expected table, got " .. type( tImage ) .. ")", 2 ) end
    if type( xPos ) ~= "number" then error( "bad argument #2 (expected number, got " .. type( xPos ) .. ")", 2 ) end
    if type( yPos ) ~= "number" then error( "bad argument #3 (expected number, got " .. type( yPos ) .. ")", 2 ) end
    for y=1,#tImage do
        local tLine, sBG, counter = tImage[y], {}, 0
        if tLine then for x=1,#tLine+1 do
            local px = tLine[x] or 0
            if px > 0 then
                counter = counter + 1
                sBG[counter] = tColourReverseLookup[ px ]
            elseif counter > 0 then
                setPos( x + xPos - 1 - counter, y + yPos - 1 )	
                local sT = rep( " ", counter )
                blit( sT, sT, concat( sBG ) )
                sBG, counter = {}, 0
            end
        end end
    end
end
