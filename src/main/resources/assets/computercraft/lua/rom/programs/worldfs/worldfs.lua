local function printUsage()
    print("Usage:")
    print("worldfs list")
    print("worldfs mount <channel> <label>")
    print("worldfs unmount <channel>")
end

if not worldfs then
    printError( "worldfs requires worldfs API" )
    printError( "Set worldfs_enable to true in ComputerCraft.cfg" )
    return
end

local targs = {...}
if #targs < 1 then
    printUsage()
    return
end

if targs[1] == "list" then
    local tlist = worldfs.list()
    if #tlist == 0 then
        print("No worldfs drives mounted")
        return
    end
    local twrap = {
        colors.yellow,
        {"Channel", "Path", "Key"},
        colors.white
    }
    for k,value in ipairs(tlist) do
        local key = string.sub(value.getKey(), 1, 10) .. "..."
        table.insert(twrap, {
            value.getChannel(),
            value.getPath(),
            key
        })
    end
    textutils.tabulate( unpack(twrap) )
elseif targs[1] == "mount" and #targs >= 3 then
    worldfs.mount(targs[2], targs[3])
    print("mounted worldfs " .. targs[2] .. " under /"..targs[3])
elseif targs[1] == "unmount" and #targs >= 2 then
    worldfs.unmount(targs[2])
    print("unmounted worldfs "..targs[2])
else
    printUsage()
end