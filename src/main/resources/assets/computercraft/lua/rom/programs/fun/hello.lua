if term.isColour() then
	term.setTextColour( 2^math.random(0,15) )
end
textutils.slowPrint( "Hello World!" )
term.setTextColour( colours.white )
