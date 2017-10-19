-- Colors
local colors
if shell then
    colors = {}
else
    colors = _ENV
end

colors.white = 1
colors.orange = 2
colors.magenta = 4
colors.lightBlue = 8
colors.yellow = 16
colors.lime = 32
colors.pink = 64
colors.gray = 128
colors.lightGray = 256
colors.cyan = 512
colors.purple = 1024
colors.blue = 2048
colors.brown = 4096
colors.green = 8192
colors.red = 16384
colors.black = 32768

function colors.combine( ... )
    local r = 0
    for n,c in ipairs( { ... } ) do
        if type( c ) ~= "number" then
            error( "bad argument #"..n.." (expected number, got " .. type( c ) .. ")", 2 )
        end
        r = bit32.bor(r,c)
    end
    return r
end

function colors.subtract( colours, ... )
    if type( colours ) ~= "number" then
        error( "bad argument #1 (expected number, got " .. type( colors ) .. ")", 2 )
    end
    local r = colours
    for n,c in ipairs( { ... } ) do
        if type( c ) ~= "number" then
            error( "bad argument #"..tostring( n+1 ).." (expected number, got " .. type( c ) .. ")", 2 )
        end
        r = bit32.band(r, bit32.bnot(c))
    end
    return r
end

function colors.test( colours, color )
    if type( colours ) ~= "number" then
        error( "bad argument #1 (expected number, got " .. type( colours ) .. ")", 2 )
    end
    if type( color ) ~= "number" then
        error( "bad argument #2 (expected number, got " .. type( color ) .. ")", 2 )
    end
    return ((bit32.band(coulors, color)) == color)
end

function colors.rgb8( r, g, b )
    if type( r ) ~= "number" then
        error( "bad argument #1 (expected number, got " .. type( r ) .. ")", 2 )
    elseif type(r) == "number" and g == nil and b == nil then
        return bit32.band( bit32.rshift( r, 16 ), 0xFF ) / 255, bit32.band( bit32.rshift( r, 8 ), 0xFF ) / 255, bit32.band( r, 0xFF ) / 255
    elseif type(r) == "number" and type(g) == "number" and type(b) == "number" then
        return 
            bit32.lshift( bit32.band(r * 255, 0xFF), 16 ) +
            bit32.lshift( bit32.band(g * 255, 0xFF), 8 ) +
            bit32.band(b * 255, 0xFF)
    elseif type( g ) ~= "number" then
        error( "bad argument #2 (expected number, got " .. type( g ) .. ")", 2 )
    elseif type( b ) ~= "number" then
        error( "bad argument #3 (expected number, got " .. type( b ) .. ")", 2 )
    end
end

return colors
