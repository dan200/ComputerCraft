
local tApis = {}
for k,v in pairs( _G ) do
	if type(k) == "string" and type(v) == "table" and k ~= "_G" then
		table.insert( tApis, k )
	end
end
table.insert( tApis, "shell" )
table.sort( tApis )

textutils.pagedTabulate( tApis )
