Arg = {...}

if not Arg[1] then
  print("Usage: webrun <url> <args>")
  return
end

if not http then
print("Please enable http in the config")
return
end

local test,text = http.checkURL(Arg[1])
if test == false then
  print("[Error]"..text)
  return
end

local data = http.get(Arg[1])
local program = data.readAll()
data.close()
local run = load(program)
table.remove(Arg,1)
run(table.unpack(Arg))
