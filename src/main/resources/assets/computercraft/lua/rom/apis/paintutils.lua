
local function drawPixelInternal( xPos, yPos )
    term.setCursorPos( xPos, yPos )
    term.write(" ")
end

local tColourLookup = {}
for n=1,16 do
    tColourLookup[ string.byte( "0123456789abcdef",n,n ) ] = 2^(n-1)
end

function loadImage( sPath )
    if type( sPath ) ~= "string" then
        error( "Expected path", 2 )
    end

    local tImage = {}
    if fs.exists( sPath ) then
        local file = io.open(sPath, "r" )
        local sLine = file:read()
        while sLine do
            local tLine = {}
            for x=1,sLine:len() do
                tLine[x] = tColourLookup[ string.byte(sLine,x,x) ] or 0
            end
            table.insert( tImage, tLine )
            sLine = file:read()
        end
        file:close()
        return tImage
    end
    return nil
end

function drawPixel( xPos, yPos, nColour )
    if type( xPos ) ~= "number" or type( yPos ) ~= "number" or (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected x, y, colour", 2 )
    end
    if nColour then
        term.setBackgroundColor( nColour )
    end
    drawPixelInternal( xPos, yPos )
end

function drawLine( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" or type( startX ) ~= "number" or
       type( endX ) ~= "number" or type( endY ) ~= "number" or
       (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected startX, startY, endX, endY, colour", 2 )
    end
    
    startX = math.floor(startX)
    startY = math.floor(startY)
    endX = math.floor(endX)
    endY = math.floor(endY)

    if nColour then
        term.setBackgroundColor( nColour )
    end
    if startX == endX and startY == endY then
        drawPixelInternal( startX, startY )
        return
    end
    
    local minX = math.min( startX, endX )
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
            
    if xDiff > math.abs(yDiff) then
        local y = minY
        local dy = yDiff / xDiff
        for x=minX,maxX do
            drawPixelInternal( x, math.floor( y + 0.5 ) )
            y = y + dy
        end
    else
        local x = minX
        local dx = xDiff / yDiff
        if maxY >= minY then
            for y=minY,maxY do
                drawPixelInternal( math.floor( x + 0.5 ), y )
                x = x + dx
            end
        else
            for y=minY,maxY,-1 do
                drawPixelInternal( math.floor( x + 0.5 ), y )
                x = x - dx
            end
        end
    end
end

function drawBox( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" or type( startX ) ~= "number" or
       type( endX ) ~= "number" or type( endY ) ~= "number" or
       (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected startX, startY, endX, endY, colour", 2 )
    end

    startX = math.floor(startX)
    startY = math.floor(startY)
    endX = math.floor(endX)
    endY = math.floor(endY)

    if nColour then
        term.setBackgroundColor( nColour )
    end
    if startX == endX and startY == endY then
        drawPixelInternal( startX, startY )
        return
    end

    local minX = math.min( startX, endX )
    if minX == startX then
        minY = startY
        maxX = endX
        maxY = endY
    else
        minY = endY
        maxX = startX
        maxY = startY
    end

    for x=minX,maxX do
        drawPixelInternal( x, minY )
        drawPixelInternal( x, maxY )
    end

    if (maxY - minY) >= 2 then
        for y=(minY+1),(maxY-1) do
            drawPixelInternal( minX, y )
            drawPixelInternal( maxX, y )
        end
    end
end

function drawFilledBox( startX, startY, endX, endY, nColour )
    if type( startX ) ~= "number" or type( startX ) ~= "number" or
       type( endX ) ~= "number" or type( endY ) ~= "number" or
       (nColour ~= nil and type( nColour ) ~= "number") then
        error( "Expected startX, startY, endX, endY, colour", 2 )
    end

    startX = math.floor(startX)
    startY = math.floor(startY)
    endX = math.floor(endX)
    endY = math.floor(endY)

    if nColour then
        term.setBackgroundColor( nColour )
    end
    if startX == endX and startY == endY then
        drawPixelInternal( startX, startY )
        return
    end

    local minX = math.min( startX, endX )
    if minX == startX then
        minY = startY
        maxX = endX
        maxY = endY
    else
        minY = endY
        maxX = startX
        maxY = startY
    end

    for x=minX,maxX do
        for y=minY,maxY do
            drawPixelInternal( x, y )
        end
    end
end

function drawImage( tImage, xPos, yPos )
    if type( tImage ) ~= "table" or type( xPos ) ~= "number" or type( yPos ) ~= "number" then
        error( "Expected image, x, y", 2 )
    end
    for y=1,#tImage do
        local tLine = tImage[y]
        for x=1,#tLine do
            if tLine[x] > 0 then
                term.setBackgroundColor( tLine[x] )
                drawPixelInternal( x + xPos - 1, y + yPos - 1 )
            end
        end
    end
end
