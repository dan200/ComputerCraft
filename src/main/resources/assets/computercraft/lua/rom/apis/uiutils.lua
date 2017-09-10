function read( _sReplaceChar, _tHistory, _fnComplete, _sDefault, _nLimit )
    if _sReplaceChar ~= nil and type( _sReplaceChar ) ~= "string" then
        error( "bad argument #1 (expected string, got " .. type( _sReplaceChar ) .. ")", 2 ) 
    end
    if _tHistory ~= nil and type( _tHistory ) ~= "table" then
        error( "bad argument #2 (expected table, got " .. type( _tHistory ) .. ")", 2 ) 
    end
    if _fnComplete ~= nil and type( _fnComplete ) ~= "function" then
        error( "bad argument #3 (expected function, got " .. type( _fnComplete ) .. ")", 2 ) 
    end
    if _sDefault ~= nil and type( _sDefault ) ~= "string" then
        error( "bad argument #4 (expected string, got " .. type( _sDefault ) .. ")", 2 ) 
    end
    if _nLimit ~= nil and type( _nLimit ) ~= "number" then
        error( "bad argument #5 (expected number, got " .. type( _nLimit ) .. ")", 2 ) 
    end
    term.setCursorBlink( true )

    local sLine
    if type( _sDefault ) == "string" then
        sLine = _sDefault
    else
        sLine = ""
    end
    local nHistoryPos
    local nPos = #sLine
    if _sReplaceChar then
        _sReplaceChar = string.sub( _sReplaceChar, 1, 1 )
    end

    local tCompletions
    local nCompletion
    local function recomplete()
        if _fnComplete and nPos == string.len(sLine) then
            tCompletions = _fnComplete( sLine )
            if tCompletions and #tCompletions > 0 then
                nCompletion = 1
            else
                nCompletion = nil
            end
        else
            tCompletions = nil
            nCompletion = nil
        end
    end

    local function uncomplete()
        tCompletions = nil
        nCompletion = nil
    end
    
    local w
    if not _nLimit then
        w = term.getSize()
    else
        w = _nLimit
    end
    
    local sx = term.getCursorPos()

    local function redraw( _bClear )
        local nScroll = 0
        if sx + nPos >= w then
            nScroll = (sx + nPos) - w
        end

        local cx,cy = term.getCursorPos()
        term.setCursorPos( sx, cy )
        local sReplace = (_bClear and " ") or _sReplaceChar
        if sReplace then
            term.write( string.sub( string.rep( sReplace, math.max( string.len(sLine) + nScroll, 0 ) ),  nScroll + 1, nScroll + w ) )
        else
            term.write( string.sub( sLine, nScroll + 1, nScroll + w ) )
        end

        if nCompletion then
            local sCompletion = tCompletions[ nCompletion ]
            local oldText, oldBg
            if not _bClear then
                oldText = term.getTextColor()
                oldBg = term.getBackgroundColor()
                term.setTextColor( colors.white )
                term.setBackgroundColor( colors.gray )
            end
            if sReplace then
                term.write( string.rep( sReplace, string.len( sCompletion ) ) )
            else
                term.write( sCompletion )
            end
            if not _bClear then
                term.setTextColor( oldText )
                term.setBackgroundColor( oldBg )
            end
        end

        term.setCursorPos( sx + nPos - nScroll, cy )
    end
    
    local function clear()
        redraw( true )
    end

    recomplete()
    redraw()

    local function acceptCompletion()
        if nCompletion then
            -- Clear
            clear()

            -- Find the common prefix of all the other suggestions which start with the same letter as the current one
            local sCompletion = tCompletions[ nCompletion ]
            sLine = sLine .. sCompletion
            nPos = string.len( sLine )

            -- Redraw
            recomplete()
            redraw()
        end
    end
    while true do
        local sEvent, param = os.pullEvent()
        if sEvent == "char" then
            -- Typed key
            clear()
            sLine = string.sub( sLine, 1, nPos ) .. param .. string.sub( sLine, nPos + 1 )
            nPos = nPos + 1
            recomplete()
            redraw()

        elseif sEvent == "paste" then
            -- Pasted text
            clear()
            sLine = string.sub( sLine, 1, nPos ) .. param .. string.sub( sLine, nPos + 1 )
            nPos = nPos + string.len( param )
            recomplete()
            redraw()

        elseif sEvent == "key" then
            if param == keys.enter then
                -- Enter
                if nCompletion then
                    clear()
                    uncomplete()
                    redraw()
                end
                break
                
            elseif param == keys.left then
                -- Left
                if nPos > 0 then
                    clear()
                    nPos = nPos - 1
                    recomplete()
                    redraw()
                end
                
            elseif param == keys.right then
                -- Right                
                if nPos < string.len(sLine) then
                    -- Move right
                    clear()
                    nPos = nPos + 1
                    recomplete()
                    redraw()
                else
                    -- Accept autocomplete
                    acceptCompletion()
                end

            elseif param == keys.up or param == keys.down then
                -- Up or down
                if nCompletion then
                    -- Cycle completions
                    clear()
                    if param == keys.up then
                        nCompletion = nCompletion - 1
                        if nCompletion < 1 then
                            nCompletion = #tCompletions
                        end
                    elseif param == keys.down then
                        nCompletion = nCompletion + 1
                        if nCompletion > #tCompletions then
                            nCompletion = 1
                        end
                    end
                    redraw()

                elseif _tHistory then
                    -- Cycle history
                    clear()
                    if param == keys.up then
                        -- Up
                        if nHistoryPos == nil then
                            if #_tHistory > 0 then
                                nHistoryPos = #_tHistory
                            end
                        elseif nHistoryPos > 1 then
                            nHistoryPos = nHistoryPos - 1
                        end
                    else
                        -- Down
                        if nHistoryPos == #_tHistory then
                            nHistoryPos = nil
                        elseif nHistoryPos ~= nil then
                            nHistoryPos = nHistoryPos + 1
                        end                        
                    end
                    if nHistoryPos then
                        sLine = _tHistory[nHistoryPos]
                        nPos = string.len( sLine ) 
                    else
                        sLine = ""
                        nPos = 0
                    end
                    uncomplete()
                    redraw()

                end

            elseif param == keys.backspace then
                -- Backspace
                if nPos > 0 then
                    clear()
                    sLine = string.sub( sLine, 1, nPos - 1 ) .. string.sub( sLine, nPos + 1 )
                    nPos = nPos - 1
                    recomplete()
                    redraw()
                end

            elseif param == keys.home then
                -- Home
                if nPos > 0 then
                    clear()
                    nPos = 0
                    recomplete()
                    redraw()
                end

            elseif param == keys.delete then
                -- Delete
                if nPos < string.len(sLine) then
                    clear()
                    sLine = string.sub( sLine, 1, nPos ) .. string.sub( sLine, nPos + 2 )                
                    recomplete()
                    redraw()
                end

            elseif param == keys["end"] then
                -- End
                if nPos < string.len(sLine ) then
                    clear()
                    nPos = string.len(sLine)
                    recomplete()
                    redraw()
                end

            elseif param == keys.tab then
                -- Tab (accept autocomplete)
                acceptCompletion()

            end

        elseif sEvent == "term_resize" then
            -- Terminal resized
            if not _nLimit then
                w = term.getSize()
            else
                w = _nLimit
            end
            redraw()

        end
    end

    local cx, cy = term.getCursorPos()
    term.setCursorBlink( false )
    term.setCursorPos( w + 1, cy )
    print()
    
    return sLine
end
