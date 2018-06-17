local nativeSocket = socket
local function invokeAsyncFunc(func, ...)
  local ok, id = pcall(func, ...)
  if not ok then
	error(id, 2)
  end
  local e = {}
  while not (e[2] == id) do
    e = {os.pullEvent("async_socket")}
  end
  if not e[4] then
    error(e[5], 3)
  end
  for i = 1, 4 do
    table.remove(e, 1)
  end
  return table.unpack(e)
end
checkHostAsync = function(URL)
  if not (type(URL) == "string") then
    error("Bad argument #1 (expected string, got "..type(URL)..")", 2)
  end
  return nativeSocket.checkHost(URL)
end
checkHost = function(URL)
  if not (type(URL) == "string") then
    error("Bad argument #1 (expected string, got "..type(URL)..")", 2)
  end
  return invokeAsyncFunc(nativeSocket.checkHost, URL)
end
lookupAsync = function(URL)
  if not (type(URL) == "string") then
    error("Bad argument #1 (expected string, got "..type(URL)..")", 2)
  end
  return nativeSocket.lookup(URL)
end
lookup = function(URL)
  if not (type(URL) == "string") then
    error("Bad argument #1 (expected string, got "..type(URL)..")", 2)
  end
  return invokeAsyncFunc(nativeSocket.lookup, URL)
end
openAsync = function(host, port, isSSL)
  if not (type(host) == "string") then
    error("Bad argument #1 (expected string, got "..type(host)..")", 2)
  end
  if type(port) ~= "number" then
    error("Bad argument #2 (expected number, got "..type(port)..")", 2)
  end
  if isSSL ~= nil and type(isSSL) ~= "boolean" then
    error("Bad argument #3 (expected boolean, got "..type(isSSL)..")", 2)
  end
  return nativeSocket.open(host, port, isSSL)
end
open = function(host, port, isSSL)
  if not (type(host) == "string") then
    error("Bad argument #1 (expected string, got "..type(host)..")", 2)
  end
  if type(port) ~= "number" then
    error("Bad argument #2 (expected number, got "..type(port)..")", 2)
  end
  if isSSL ~= nil and type(isSSL) ~= "boolean" then
    error("Bad argument #3 (expected boolean, got "..type(isSSL)..")", 2)
  end
  local nativeSock = invokeAsyncFunc(nativeSocket.open, host, port, isSSL)
  local newSock = {}
  for k, v in pairs(nativeSock) do
    newSock[k] = function(...)
	  return invokeAsyncFunc(v, ...)
	end
  end
  return newSock
end