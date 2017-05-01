/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import dan200.computercraft.core.filesystem.IMountedFileBinary;
import dan200.computercraft.core.filesystem.IMountedFileNormal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FSAPI implements ILuaAPI
{
    private IAPIEnvironment m_env;
    private FileSystem m_fileSystem;
    
    public FSAPI( IAPIEnvironment _env )
    {
        m_env = _env;
        m_fileSystem = null;
    }
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "fs"
        };
    }

    @Override
    public void startup( )
    {
        m_fileSystem = m_env.getFileSystem();
    }

    @Override
    public void advance( double _dt )
    {
    }
    
    @Override
    public void shutdown( )
    {
        m_fileSystem = null;
    }

    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "list",
            "combine",
            "getName",
            "getSize",
            "exists",
            "isDir",
            "isReadOnly",
            "makeDir",
            "move",
            "copy",
            "delete",
            "open",
            "getDrive",
            "getFreeSpace",
            "find",
            "getDir",
        };
    }

    @Override
    public Object[] callMethod( ILuaContext context, int method, Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // list
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    String[] results = m_fileSystem.list( path );
                    Map<Object,Object> table = new HashMap<Object,Object>();
                    for(int i=0; i<results.length; ++i ) {
                        table.put( i+1, results[i] );
                    }
                    return new Object[] { table };
                }
                catch( FileSystemException e )
                {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 1:
            {
                // combine
                if( args.length != 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof String) )
                {
                    throw new LuaException( "Expected string, string" );
                }
                String pathA = (String)args[0];
                String pathB = (String)args[1];
                return new Object[] { m_fileSystem.combine( pathA, pathB ) };
            }
            case 2:
            {
                // getName
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                return new Object[]{ m_fileSystem.getName( path ) };
            }
            case 3:
            {
                // getSize
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try
                {
                    return new Object[]{ m_fileSystem.getSize( path ) };
                }
                catch( FileSystemException e )
                {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 4:
            {
                // exists
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    return new Object[]{ m_fileSystem.exists( path ) };
                } catch( FileSystemException e ) {
                    return new Object[]{ false };
                }
            }
            case 5:
            {
                // isDir
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    return new Object[]{ m_fileSystem.isDir( path ) };
                } catch( FileSystemException e ) {
                    return new Object[]{ false };
                }
            }
            case 6:
            {
                // isReadOnly
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    return new Object[]{ m_fileSystem.isReadOnly( path ) };
                } catch( FileSystemException e ) {
                    return new Object[]{ false };
                }
            }
            case 7:
            {
                // makeDir
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    m_fileSystem.makeDir( path );
                    return null;
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 8:
            {
                // move
                if( args.length != 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof String) )
                {
                    throw new LuaException( "Expected string, string" );
                }
                String path = (String)args[0];
                String dest = (String)args[1];
                try {
                    m_fileSystem.move( path, dest );
                    return null;
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 9:
            {
                // copy
                if( args.length != 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof String) )
                {
                    throw new LuaException( "Expected string, string" );
                }
                String path = (String)args[0];
                String dest = (String)args[1];
                try {
                    m_fileSystem.copy( path, dest );
                    return null;
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 10:
            {
                // delete
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    m_fileSystem.delete( path );
                    return null;
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 11:
            {
                // open
                if( args.length < 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof String) )
                {
                    throw new LuaException( "Expected string, string" );
                }
                String path = (String)args[0];
                String mode = (String)args[1];
                try {
                    if( mode.equals( "r" ) ) {
                        // Open the file for reading, then create a wrapper around the reader
                        IMountedFileNormal reader = m_fileSystem.openForRead( path );
                        return wrapBufferedReader( reader );
                        
                    } else if( mode.equals( "w" ) ) {
                        // Open the file for writing, then create a wrapper around the writer
                        IMountedFileNormal writer = m_fileSystem.openForWrite( path, false );
                        return wrapBufferedWriter( writer );
                    
                    } else if( mode.equals( "a" ) ) {
                        // Open the file for appending, then create a wrapper around the writer
                        IMountedFileNormal writer = m_fileSystem.openForWrite( path, true );
                        return wrapBufferedWriter( writer );
                                            
                    } else if( mode.equals( "rb" ) ) {
                        // Open the file for binary reading, then create a wrapper around the reader
                        IMountedFileBinary reader = m_fileSystem.openForBinaryRead( path );
                        return wrapInputStream( reader );
                        
                    } else if( mode.equals( "wb" ) ) {
                        // Open the file for binary writing, then create a wrapper around the writer
                        IMountedFileBinary writer = m_fileSystem.openForBinaryWrite( path, false );
                        return wrapOutputStream( writer );
                    
                    } else if( mode.equals( "ab" ) ) {
                        // Open the file for binary appending, then create a wrapper around the reader
                        IMountedFileBinary writer = m_fileSystem.openForBinaryWrite( path, true );
                        return wrapOutputStream( writer );
                        
                    } else {
                        throw new LuaException( "Unsupported mode" );
                        
                    }
                } catch( FileSystemException e ) {
                    return null;
                }
            }
            case 12:
            {
                // getDrive
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    if( !m_fileSystem.exists( path ) )
                    {
                        return null;
                    }
                    return new Object[]{ m_fileSystem.getMountLabel( path ) };
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 13:
            {
                // getFreeSpace
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    long freeSpace = m_fileSystem.getFreeSpace( path );
                    if( freeSpace >= 0 )
                    {
                        return new Object[]{ freeSpace };
                    }
                    return new Object[]{ "unlimited" };
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 14:
            {
                // find
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                try {
                    String[] results = m_fileSystem.find( path );
                    Map<Object,Object> table = new HashMap<Object,Object>();
                    for(int i=0; i<results.length; ++i ) {
                        table.put( i+1, results[i] );
                    }
                    return new Object[] { table };
                } catch( FileSystemException e ) {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 15:
            {
                // getDir
                if( args.length != 1 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String path = (String)args[0];
                return new Object[]{ m_fileSystem.getDirectory( path ) };
            }
            default:
            {
                assert( false );
                return null;
            }
        }
    }
    
    private static Object[] wrapBufferedReader( final IMountedFileNormal reader )
    {
        return new Object[] { new ILuaObject() {
            @Override
            public String[] getMethodNames()
            {
                return new String[] {
                    "readLine",
                    "readAll",
                    "close"
                };
            }
            
            @Override
            public Object[] callMethod( ILuaContext context, int method, Object[] args ) throws LuaException
            {
                switch( method )
                {
                    case 0:
                    {
                        // readLine
                        try {
                            String line = reader.readLine();
                            if( line != null ) {
                                return new Object[] { line };
                            } else {
                                return null;
                            }
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 1:
                    {
                        // readAll
                        try {
                            StringBuilder result = new StringBuilder( "" );
                            String line = reader.readLine();
                            while( line != null ) {
                                result.append( line );
                                line = reader.readLine();
                                if( line != null ) {
                                    result.append( "\n" );
                                }
                            }
                            return new Object[] { result.toString() };
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 2:
                    {
                        // close
                        try {
                            reader.close();
                            return null;
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    default:
                    {
                        return null;
                    }
                }
            }
        } };
    }

    private static Object[] wrapBufferedWriter( final IMountedFileNormal writer )
    {
        return new Object[] { new ILuaObject() {
            @Override
            public String[] getMethodNames()
            {
                return new String[] {
                    "write",
                    "writeLine",
                    "close",
                    "flush"
                };
            }
            
            @Override
            public Object[] callMethod( ILuaContext context, int method, Object[] args ) throws LuaException
            {
                switch( method )
                {
                    case 0:
                    {
                        // write
                        String text;
                        if( args.length > 0 && args[0] != null ) {
                            text = args[0].toString();
                        } else {
                            text = "";
                        }
                        try {
                            writer.write( text, 0, text.length(), false );
                            return null;
                        } catch( IOException e ) {
                            throw new LuaException( e.getMessage() );
                        }
                    }
                    case 1:
                    {
                        // writeLine
                        String text;
                        if( args.length > 0 && args[0] != null ) {
                            text = args[0].toString();
                        } else {
                            text = "";
                        }
                        try {
                            writer.write( text, 0, text.length(), true );
                            return null;
                        } catch( IOException e ) {
                            throw new LuaException( e.getMessage() );
                        }
                    }
                    case 2:
                    {
                        // close
                        try {
                            writer.close();
                            return null;
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 3:
                    {
                        try {
                            writer.flush();
                            return null;
                        } catch ( IOException e ) {
                            return null;
                        }
                    }
                    default:
                    {
                        assert( false );
                        return null;
                    }
                }
            }
        } };
    }
    
    private static Object[] wrapInputStream( final IMountedFileBinary reader )
    {
        
        return new Object[] { new ILuaObject() {

            @Override
            public String[] getMethodNames() {
                return new String[] {
                        "read",
                        "close"
                    };
            }

            @Override
            public Object[] callMethod( ILuaContext context, int method, Object[] args) throws LuaException {
                switch( method )
                {
                    case 0:
                    {
                        // read
                        try {
                            int b = reader.read();
                            if( b != -1 ) {
                                return new Object[] { b };
                            } else {
                                return null;
                            }
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 1:
                    {
                        //close
                        try {
                            reader.close();
                            return null;
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    default:
                    {
                        assert( false );
                        return null;
                    }
                }
            }
        }};
    }

    private static Object[] wrapOutputStream( final IMountedFileBinary writer )
    {        
        
        return new Object[] { new ILuaObject() {

            @Override
            public String[] getMethodNames() {
                return new String[] {
                        "write",
                        "close",
                        "flush"
                    };
            }

            @Override
            public Object[] callMethod( ILuaContext context, int method, Object[] args) throws LuaException {
                switch( method )
                {
                    case 0:
                    {
                        // write
                        try {
                            if( args.length > 0 && args[0] instanceof Number )
                            {
                                int number = ((Number)args[0]).intValue();
                                writer.write( number );
                            }
                            return null;
                        } catch( IOException e ) {
                            throw new LuaException(e.getMessage());
                        }
                    }
                    case 1:
                    {
                        //close
                        try {
                            writer.close();
                            return null;
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 2:
                    {
                        try {
                            writer.flush();
                            return null;
                        } catch ( IOException e ) {
                            return null;
                        }
                    }
                    default:
                    {
                        assert( false );
                        return null;
                    }
                }
            }
        }};
    }    
}
