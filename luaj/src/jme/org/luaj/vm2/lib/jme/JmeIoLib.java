/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.lib.jme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.IoLib;
import org.luaj.vm2.lib.LibFunction;

/** 
 * Subclass of {@link IoLib} and therefore {@link LibFunction} which implements the lua standard {@code io} 
 * library for the JSE platform. 
 * <p> 
 * The implementation of the is based on CLDC 1.0 and StreamConnection.
 * However, seek is not supported. 
 * <p>
 * Typically, this library is included as part of a call to 
 * {@link JmePlatform#standardGlobals()}
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * LuaTable _G = new LuaTable();
 * LuaThread.setGlobals(_G);
 * _G.load(new BaseLib());
 * _G.load(new PackageLib());
 * _G.load(new JmeIoLib());
 * _G.get("io").get("write").call(LuaValue.valueOf("hello, world\n"));
 * } </pre>
 * Doing so will ensure the library is properly initialized 
 * and loaded into the globals table. 
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * @see LibFunction
 * @see JsePlatform
 * @see JmePlatform
 * @see IoLib
 * @see JseIoLib
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.6">http://www.lua.org/manual/5.1/manual.html#5.6</a>
 */
public class JmeIoLib extends IoLib {
	
	public JmeIoLib() {
		super();
	}

	protected File wrapStdin() throws IOException {
		return new FileImpl(BaseLib.instance.STDIN);
	}
	
	protected File wrapStdout() throws IOException {
		return new FileImpl(BaseLib.instance.STDOUT);
	}
	
	protected File openFile( String filename, boolean readMode, boolean appendMode, boolean updateMode, boolean binaryMode ) throws IOException {
		String url = "file:///" + filename;
		int mode  = readMode? Connector.READ: Connector.READ_WRITE;
		StreamConnection conn = (StreamConnection) Connector.open( url, mode );
		File f = readMode? 
				new FileImpl(conn, conn.openInputStream(), null):
				new FileImpl(conn, conn.openInputStream(), conn.openOutputStream());
		/*
		if ( appendMode ) {
			f.seek("end",0);
		} else {
			if ( ! readMode )
				conn.truncate(0);
		}
		*/
		return f;
	}
	
	private static void notimplemented() throws IOException {
		throw new IOException("not implemented");
	}
	
	protected File openProgram(String prog, String mode) throws IOException {
		notimplemented();
		return null;
	}

	protected File tmpFile() throws IOException {
		notimplemented();
		return null;
	}
	
	private final class FileImpl extends File {
		private final StreamConnection conn;
		private final InputStream is;
		private final OutputStream os;
		private boolean closed = false;
		private boolean nobuffer = false;
		private int lookahead = -1;
		private FileImpl( StreamConnection conn, InputStream is, OutputStream os ) {
			this.conn = conn;
			this.is = is;
			this.os = os;
		}
		private FileImpl( InputStream i ) {
			this( null, i, null );
		}
		private FileImpl( OutputStream o ) {
			this( null, null, o );
		}
		public String tojstring() {
			return "file ("+this.hashCode()+")";
		}
		public boolean isstdfile() {
			return conn == null;
		}
		public void close() throws IOException  {
			closed = true;
			if ( conn != null ) {
				conn.close();
			}
		}
		public void flush() throws IOException {
			if ( os != null )
				os.flush();
		}
		public void write(LuaString s) throws IOException {
			if ( os != null )
				os.write( s.m_bytes, s.m_offset, s.m_length );
			else
				notimplemented();
			if ( nobuffer )
				flush();
		}
		public boolean isclosed() {
			return closed;
		}
		public int seek(String option, int pos) throws IOException {
			/*
			if ( conn != null ) {
				if ( "set".equals(option) ) {
					conn.seek(pos);
					return (int) conn.getFilePointer();
				} else if ( "end".equals(option) ) {
					conn.seek(conn.length()+1+pos);
					return (int) conn.length()+1;
				} else {
					conn.seek(conn.getFilePointer()+pos);
					return (int) conn.getFilePointer();
				}
			}
			*/
			notimplemented();
			return 0;
		}
		public void setvbuf(String mode, int size) {
			nobuffer = "no".equals(mode);
		}

		// get length remaining to read
		public int remaining() throws IOException {
			return -1;
		}
		
		// peek ahead one character
		public int peek() throws IOException {
			if ( lookahead < 0 )
				lookahead = is.read();
			return lookahead;
		}		
		
		// return char if read, -1 if eof, throw IOException on other exception 
		public int read() throws IOException {
			if ( lookahead >= 0 ) {
				int c = lookahead;
				lookahead = -1;
				return c;
			}
			if ( is != null ) 
				return is.read();
			notimplemented();
			return 0;
		}

		// return number of bytes read if positive, -1 if eof, throws IOException
		public int read(byte[] bytes, int offset, int length) throws IOException {
			int n,i=0;
			if (is!=null) {
				if ( length > 0 && lookahead >= 0 ) {
					bytes[offset] = (byte) lookahead;
					lookahead = -1;
					i += 1;
				}
				for ( ; i<length; ) {
					n = is.read(bytes, offset+i, length-i);
					if ( n < 0 )
						return ( i > 0 ? i : -1 );
					i += n;
				}
			} else {
				notimplemented();
			}
			return length;
		}
	}
}
