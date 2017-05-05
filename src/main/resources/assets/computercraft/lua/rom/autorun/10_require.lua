-- -*- coding: utf8 -*-
-- Copyright (c) 2014 Odd Straaboe <oddstr13 at openshell dot no>
-- License: MIT - http://opensource.org/licenses/MIT
-- Filename: require.lua

_G.package = {}

_G.package.cpath = ""
_G.package.loaded = {}
_G.package.loadlib = function() return nil, "not implemented: package.loadlib" end
_G.package.path = table.concat({
  "?",
  "?.lua",
  "?/init.lua",
  "/lib/?",
  "/lib/?.lua",
  "/lib/?/init.lua",
  "/rom/apis/?",
  "/rom/apis/?.lua",
  "/rom/apis/?/init.lua",
  "/rom/apis/turtle/?",
  "/rom/apis/turtle/?.lua",
  "/rom/apis/turtle/?/init.lua",
  "/rom/apis/command/?",
  "/rom/apis/command/?.lua",
  "/rom/apis/command/?/init.lua",
}, ";")
_G.package.preload = {}
_G.package.seeall = function(module)
  if type(module) ~= "table" then
    error("bad argument #1 to 'require' (table expected, got " .. type(module) .. ")", 2)
  end

  local meta = getmetatable(module)
  if not meta then
    meta = {}
    setmetatable(module, meta)
  end

  meta.__index = _G
end
_G.module = function(m) error("not implemented: module") end

local _table_blacklist = { "_ENV", "_G" }
do
  local t = {}
  for _, v in pairs(_table_blacklist) do
    t[v] = true
  end
  _table_blacklist = t
end

local function _package_path_loader(fpath)
  return function(name)
    local apienv = {}
    setmetatable(apienv, { __index = _G })

    local apifunc, err = loadfile(fpath, apienv) -- dan why

    if apifunc then
      err = apifunc()
    else
      error("error loading module '" .. name .. "' from file '" .. fpath .. "'\n\t" .. err)
    end

    local api = {}
    if err ~= nil then
      api = err
    else
      for k, v in pairs(apienv) do
        if not _table_blacklist[k] then
          api[k] = v
        end
      end
    end

    return api
  end
end

_G.package.loaders = {
  function(name)
    if package.preload[name] then
      return package.preload[name]
    else
      return "\tno field package.preload['" .. name .. "']"
    end
  end,

  function(name)
    local _errors = {}

    local fname = name:gsub("%.", "/")

    for pattern in package.path:gmatch("[^;]+") do

      local fpath = pattern:gsub("%?", fname)
      if fs.exists(fpath) and not fs.isDir(fpath) then
        return _package_path_loader(fpath)
      else
        table.insert(_errors, "\tno file '" .. fpath .. "'")
      end
    end

    return table.concat(_errors, "\n")
  end
}

local sentinel = {} -- just a magic value to avoid circular dependencies
_G.require = function(name)
  if type(name) ~= "string" then
    error("bad argument #1 to 'require' (string expected, got " .. type(name) .. ")", 2)
  end

  local previous = package.loaded[name]
  if previous == sentinel then
    error("loop or previous error loading module ' " .. name .. "'", 2)
  elseif previous ~= nil then
    return previous
  end

  local _errors = {}

  for _, searcher in ipairs(package.loaders) do
    local loader = searcher(name)
    if type(loader) == "function" then
      package.loaded[name] = sentinel
      local res = loader(name)
      if res ~= nil then
        package.loaded[name] = res
      end

      if package.loaded[name] == nil then
        package.loaded[name] = true
      end

      return package.loaded[name]
    elseif type(loader) == "string" then
      table.insert(_errors, loader)
    end
  end

  error("module '" .. name .. "' not found:\n" .. table.concat(_errors, "\n"))
end
