-- Colours (for lovers of british spelling)
local colours
if shell then
    colours = {}
else
    colours = _ENV
end

for k,v in pairs(colors) do
	colours[k] = v
end

colours.grey = colors.gray
colours.gray = nil

colours.lightGrey = colors.lightGray
colours.lightGray = nil

return colours
