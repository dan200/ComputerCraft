local Arg = {...}

if Arg[1] == nil then
  print("Usage: size <path>")
  return 1
end

local filename = shell.resolve(Arg[1])

if fs.exists(filename) == true then
  if fs.isDir(filename) then
    os.loadAPI("/usr/apis/wilmaapi")
    local fileta = wilmaapi.listAllFiles(filename)
    local size = 0
    for _,sizefile in ipairs(fileta) do
      size = size + fs.getSize(sizefile)
    end
    print("Size: "..size.." Bytes")
  else
  print("Size: "..fs.getSize(filename).." Bytes")
  end
else
  print("File does not exists")
  return 2
end

return 0
