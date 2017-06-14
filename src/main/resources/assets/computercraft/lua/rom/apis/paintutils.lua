local setPos, write, setCol, blit = term.setCursorPos, term.write, term.setBackgroundColour, term.blit

local maxn = table.maxn or function( tTable )
    local maxn = 0
    for n in pairs( tTable ) do
        if type( n ) == "number" and n > max then
            maxn = n
        end
    end
    return maxn
end

local tColourLookup = {}
for n=1,16 do
    tColourLookup[ string.sub( "0123456789abcdef",n,n ) ] = 2^(n-1)
    tColourLookup[ 2^(n-1) ] = string.sub( "0123456789abcdef",n,n )
end

function loadImage( sPath )
    if type( sPath ) ~= "string" then
        error( "Expected path", 2 )
    end

    local tImage = {}
    if fs.exists( sPath ) then
        for sLine in io.lines(sPath) do
            local tLine = {}
            for x=1,#sLine do
                tLine[x] = tColourLookup[ string.sub(sLine,x,x) ] or 0
            end
            table.insert( tImage, tLine )
        end
        return tImage
    end
    return nil
end

function saveImage( tImage, sPath )
    if type( tImage ) ~= "table" then
        error( "Expected paintutils image", 2 )
    elseif type( sPath ) ~= "string" then
        error( "Expected path", 2 )
    end

    local file = fs.open(sPath, "w" )
    if not file then return false end
    
    for y=1,maxn( tImage ) do
        local tOld, tNew, last = tImage[y], {}, 0
        if tOld then for x=1,maxn( tOld ) do
            local thisCol = tColourLookup[ tOld[x] ]
            if thisCol then
                tNew[x], last = thisCol, x
            else
                tNew[x] = " "
            end
        end end
        file.writeLine( table.concat( tNew, "", 1, last ) )
    end
    file.close()
    return true
end

function drawPixel( xPos, yPos, nColour )
    if type( xPos ) ~= "number" or type( yPos ) ~= "number" or (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected x, y [, colour]", 2 )
    end
    if nColour then
        setCol( nColour )
    end
    setPos( xPos, yPos )
    write( " " )
end

function drawLine( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" or type( startX ) ~= "number" or
       type( endX ) ~= "number" or type( endY ) ~= "number" or
       (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected startX, startY, endX, endY [, colour]", 2 )
    end
    
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
        write( string.rep( " ", xDiff + 1 ) )
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
    if type( startX ) ~= "number" or type( startX ) ~= "number" or
       type( endX ) ~= "number" or type( endY ) ~= "number" or
       (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected startX, startY, endX, endY [, colour]", 2 )
    end

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

    local sStr = string.rep( " ", maxX - minX + 1 )
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
    if type( startX ) ~= "number" or type( startX ) ~= "number" or
       type( endX ) ~= "number" or type( endY ) ~= "number" or
       (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected startX, startY, endX, endY [, colour]", 2 )
    end

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

    local sStr = string.rep( " ", maxX - minX + 1 )
    for y=minY,maxY do
        setPos( minX, y )
        write( sStr )
    end
end

function drawImage( tImage, xPos, yPos )
    if type( tImage ) ~= "table" or type( xPos ) ~= "number" or type( yPos ) ~= "number" then
        error( "Expected image, x, y", 2 )
    end
    for y=1,maxn( tImage ) do
        local tLine, sBG, counter = tImage[y], {}, 0
        if tLine then for x=1,maxn( tLine )+1 do
            local px = tLine[x] or 0
            if px > 0 then
                counter = counter + 1
                sBG[counter] = tColourLookup[ px ]
            elseif counter > 0 then
                setPos( x + xPos - 1 - counter, y + yPos - 1 )	
                local sT = string.rep( " ", counter )
                blit( sT, sT, table.concat( sBG ) )
                sBG, counter = {}, 0
            end
        end end
    end
end
