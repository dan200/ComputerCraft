-- Colors
white = 1
orange = 2
magenta = 4
lightBlue = 8
yellow = 16
lime = 32
pink = 64
gray = 128
lightGray = 256
cyan = 512
purple = 1024
blue = 2048
brown = 4096
green = 8192
red = 16384
black = 32768

function combine( ... )
    local r = 0
    for n,c in ipairs( { ... } ) do
        r = bit32.bor(r,c)
    end
    return r
end

function subtract( colors, ... )
    local r = colors
    for n,c in ipairs( { ... } ) do
        r = bit32.band(r, bit32.bnot(c))
    end
    return r
end

function test( colors, color )
    return ((bit32.band(colors, color)) == color)
end

function rgb8( r, g, b )
    if type(r) == "number" and g == nil and b == nil then
        return bit32.band( bit32.rshift( r, 16 ), 0xFF ) / 255, bit32.band( bit32.rshift( r, 8 ), 0xFF ) / 255, bit32.band( r, 0xFF ) / 255
    elseif type(r) == "number" and type(g) == "number" and type(b) == "number" then
        return 
            bit32.lshift( bit32.band(r * 255, 0xFF), 16 ) +
            bit32.lshift( bit32.band(g * 255, 0xFF), 8 ) +
            bit32.band(b * 255, 0xFF)
    else
        error( "Expected 1 or 3 numbers", 2 )
    end
end
