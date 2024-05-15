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
package org.luaj.vm2;


import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

import org.luaj.vm2.lib.MathLib;
import org.luaj.vm2.lib.StringLib;

/**
 * Subclass of {@link LuaValue} for representing lua strings. 
 * <p>
 * Because lua string values are more nearly sequences of bytes than 
 * sequences of characters or unicode code points, the {@link LuaString}
 * implementation holds the string value in an internal byte array.  
 * <p>
 * {@link LuaString} values are generally not mutable once constructed, 
 * so multiple {@link LuaString} values can chare a single byte array.
 * <p>
 * Currently {@link LuaString}s are pooled via a centrally managed weak table.
 * To ensure that as many string values as possible take advantage of this, 
 * Constructors are not exposed directly.  As with number, booleans, and nil, 
 * instance construction should be via {@link LuaValue#valueOf(byte[])} or similar API.
 * <p>
 * When Java Strings are used to initialize {@link LuaString} data, the UTF8 encoding is assumed. 
 * The functions 
 * {@link LuaString#lengthAsUtf8(char[]),
 * {@link LuaString#encodeToUtf8(char[], byte[], int)}, and 
 * {@link LuaString#decodeAsUtf8(byte[], int, int) 
 * are used to convert back and forth between UTF8 byte arrays and character arrays.
 * 
 * @see LuaValue
 * @see LuaValue#valueOf(String)
 * @see LuaValue#valueOf(byte[])
 */
public class LuaString extends LuaValue {

	/** The singleton instance representing lua {@code true} */
	public static LuaValue s_metatable;

	/** The bytes for the string */
	public final byte[] m_bytes;
	
	/** The offset into the byte array, 0 means start at the first byte */
	public final int    m_offset;
	
	/** The number of bytes that comprise this string */
	public final int    m_length;

	private static final Hashtable index_java = new Hashtable();

	private final static LuaString index_get(Hashtable indextable, Object key) {
		WeakReference w = (WeakReference) indextable.get(key);
		return w!=null? (LuaString) w.get(): null;
	}
	
	private final static void index_set(Hashtable indextable, Object key, LuaString value) {
		indextable.put(key, new WeakReference(value));
	}

	/**
	 * Get a {@link LuaString} instance whose bytes match 
	 * the supplied Java String using the UTF8 encoding. 
	 * @param string Java String containing characters to encode as UTF8
	 * @return {@link LuaString} with UTF8 bytes corresponding to the supplied String
	 */
	public static LuaString valueOf(String string) {
		LuaString s = index_get( index_java, string );
		if ( s != null ) return s;
		char[] c = string.toCharArray();
        /* DAN200 START */
        /*
		byte[] b = new byte[lengthAsUtf8(c)];
		encodeToUtf8(c, b, 0);
		*/
        byte[] b = new byte[c.length];
        for( int i=0; i<b.length; ++i )
        {
            char ch = c[i];
            b[i] = (ch < 256) ? (byte)ch : (byte)'?';
        }
        /* DAN200 END */
		s = valueOf(b, 0, b.length);
		index_set( index_java, string, s );
		return s;
	}

	// TODO: should this be deprecated or made private?
	/** Construct a {@link LuaString} around a byte array without copying the contents.
	 * <p>
	 * The array is used directly after this is called, so clients must not change contents.
	 * <p>
	 * @param bytes byte buffer
	 * @param off offset into the byte buffer
	 * @param len length of the byte buffer
	 * @return {@link LuaString} wrapping the byte buffer
	 */
	public static LuaString valueOf(byte[] bytes, int off, int len) { 
		return new LuaString(bytes, off, len);
	}
	
	/** Construct a {@link LuaString} using the supplied characters as byte values.
	 * <p>
	 * Only th elow-order 8-bits of each character are used, the remainder is ignored. 
	 * <p>
	 * This is most useful for constructing byte sequences that do not conform to UTF8. 
	 * @param bytes array of char, whose values are truncated at 8-bits each and put into a byte array. 
	 * @return {@link LuaString} wrapping a copy of the byte buffer 
	 */
	public static LuaString valueOf(char[] bytes) {
		int n = bytes.length;
		byte[] b = new byte[n];
		for ( int i=0; i<n; i++ )
			b[i] = (byte) bytes[i];
		return valueOf(b, 0, n);
	}
	
	
	/** Construct a {@link LuaString} around a byte array without copying the contents.
	 * <p>
	 * The array is used directly after this is called, so clients must not change contents.
	 * <p>
	 * @param bytes byte buffer
	 * @return {@link LuaString} wrapping the byte buffer
	 */
	public static LuaString valueOf(byte[] bytes) {
		return valueOf(bytes, 0, bytes.length);
	}
	
	/** Construct a {@link LuaString} around a byte array without copying the contents.
	 * <p>
	 * The array is used directly after this is called, so clients must not change contents.
	 * <p>
	 * @param bytes byte buffer
	 * @param offset offset into the byte buffer
	 * @param length length of the byte buffer
	 * @return {@link LuaString} wrapping the byte buffer
	 */
	private LuaString(byte[] bytes, int offset, int length) {
		this.m_bytes = bytes;
		this.m_offset = offset;
		this.m_length = length;
	}

	public boolean isstring() {
		return true; 
	}
		
	public LuaValue getmetatable() { 
		return s_metatable; 
	}
	
	public int type() {
		return LuaValue.TSTRING;
	}

	public String typename() {
		return "string";
	}
	
	public String tojstring() {
        /* DAN200 START */
		/*
		return decodeAsUtf8(m_bytes, m_offset, m_length);
		*/
        char[] chars = new char[ m_length ];
        for( int i=0; i<chars.length; ++i )
        {
            chars[i] = (char)(m_bytes[ m_offset + i ] & 255);
        }
        return new String( chars );
        /* DAN200 END */
	}

	// get is delegated to the string library
	public LuaValue get(LuaValue key) {
		return s_metatable!=null? gettable(this,key): StringLib.instance.get(key);
	}

	// unary operators
	public LuaValue neg() { double d = scannumber(10); return Double.isNaN(d)? super.neg(): valueOf(-d); }

	// basic binary arithmetic
	public LuaValue   add( LuaValue rhs )      { double d = scannumber(10); return Double.isNaN(d)? arithmt(ADD,rhs): rhs.add(d); }
	public LuaValue   add( double rhs )        { return valueOf( checkarith() + rhs ); }
	public LuaValue   add( int rhs )           { return valueOf( checkarith() + rhs ); }
	public LuaValue   sub( LuaValue rhs )      { double d = scannumber(10); return Double.isNaN(d)? arithmt(SUB,rhs): rhs.subFrom(d); }
	public LuaValue   sub( double rhs )        { return valueOf( checkarith() - rhs ); }
	public LuaValue   sub( int rhs )           { return valueOf( checkarith() - rhs ); }
	public LuaValue   subFrom( double lhs )    { return valueOf( lhs - checkarith() ); }
	public LuaValue   mul( LuaValue rhs )      { double d = scannumber(10); return Double.isNaN(d)? arithmt(MUL,rhs): rhs.mul(d); }
	public LuaValue   mul( double rhs )        { return valueOf( checkarith() * rhs ); }
	public LuaValue   mul( int rhs )           { return valueOf( checkarith() * rhs ); }
	public LuaValue   pow( LuaValue rhs )      { double d = scannumber(10); return Double.isNaN(d)? arithmt(POW,rhs): rhs.powWith(d); }
	public LuaValue   pow( double rhs )        { return MathLib.dpow(checkarith(),rhs); }
	public LuaValue   pow( int rhs )           { return MathLib.dpow(checkarith(),rhs); }
	public LuaValue   powWith( double lhs )    { return MathLib.dpow(lhs, checkarith()); }
	public LuaValue   powWith( int lhs )       { return MathLib.dpow(lhs, checkarith()); }
	public LuaValue   div( LuaValue rhs )      { double d = scannumber(10); return Double.isNaN(d)? arithmt(DIV,rhs): rhs.divInto(d); }
	public LuaValue   div( double rhs )        { return LuaDouble.ddiv(checkarith(),rhs); }
	public LuaValue   div( int rhs )           { return LuaDouble.ddiv(checkarith(),rhs); }
	public LuaValue   divInto( double lhs )    { return LuaDouble.ddiv(lhs, checkarith()); }
	public LuaValue   mod( LuaValue rhs )      { double d = scannumber(10); return Double.isNaN(d)? arithmt(MOD,rhs): rhs.modFrom(d); }
	public LuaValue   mod( double rhs )        { return LuaDouble.dmod(checkarith(), rhs); }
	public LuaValue   mod( int rhs )           { return LuaDouble.dmod(checkarith(), rhs); }
	public LuaValue   modFrom( double lhs )    { return LuaDouble.dmod(lhs, checkarith()); }
	
	// relational operators, these only work with other strings
	public LuaValue   lt( LuaValue rhs )         { return rhs.strcmp(this)>0? LuaValue.TRUE: FALSE; }
	public boolean lt_b( LuaValue rhs )       { return rhs.strcmp(this)>0; }
	public boolean lt_b( int rhs )         { typerror("attempt to compare string with number"); return false; }
	public boolean lt_b( double rhs )      { typerror("attempt to compare string with number"); return false; }
	public LuaValue   lteq( LuaValue rhs )       { return rhs.strcmp(this)>=0? LuaValue.TRUE: FALSE; }
	public boolean lteq_b( LuaValue rhs )     { return rhs.strcmp(this)>=0; }
	public boolean lteq_b( int rhs )       { typerror("attempt to compare string with number"); return false; }
	public boolean lteq_b( double rhs )    { typerror("attempt to compare string with number"); return false; }
	public LuaValue   gt( LuaValue rhs )         { return rhs.strcmp(this)<0? LuaValue.TRUE: FALSE; }
	public boolean gt_b( LuaValue rhs )       { return rhs.strcmp(this)<0; }
	public boolean gt_b( int rhs )         { typerror("attempt to compare string with number"); return false; }
	public boolean gt_b( double rhs )      { typerror("attempt to compare string with number"); return false; }
	public LuaValue   gteq( LuaValue rhs )       { return rhs.strcmp(this)<=0? LuaValue.TRUE: FALSE; }
	public boolean gteq_b( LuaValue rhs )     { return rhs.strcmp(this)<=0; }
	public boolean gteq_b( int rhs )       { typerror("attempt to compare string with number"); return false; }
	public boolean gteq_b( double rhs )    { typerror("attempt to compare string with number"); return false; }

	// concatenation
	public LuaValue concat(LuaValue rhs)      { return rhs.concatTo(this); }
	public Buffer   concat(Buffer rhs)        { return rhs.concatTo(this); }
	public LuaValue concatTo(LuaNumber lhs)   { return concatTo(lhs.strvalue()); }
	public LuaValue concatTo(LuaString lhs)   { 
		byte[] b = new byte[lhs.m_length+this.m_length];
		System.arraycopy(lhs.m_bytes, lhs.m_offset, b, 0, lhs.m_length);
		System.arraycopy(this.m_bytes, this.m_offset, b, lhs.m_length, this.m_length);
		return new LuaString(b, 0, b.length);
	}

	// string comparison 
	public int strcmp(LuaValue lhs)           { return -lhs.strcmp(this); }
	public int strcmp(LuaString rhs) {
		for ( int i=0, j=0; i<m_length && j<rhs.m_length; ++i, ++j ) {
			if ( m_bytes[m_offset+i] != rhs.m_bytes[rhs.m_offset+j] ) {
				return ((int)m_bytes[m_offset+i]) - ((int) rhs.m_bytes[rhs.m_offset+j]);
			}
		}
		return m_length - rhs.m_length;
	}
	
	/** Check for number in arithmetic, or throw aritherror */
	private double checkarith() { 
		double d = scannumber(10);
		if ( Double.isNaN(d) ) 
			aritherror();
		return d;
	}
	
	public int checkint() {
		return (int) (long) checkdouble();
	}
	public LuaInteger checkinteger() {
		return valueOf(checkint());
	}
	public long checklong() {
		return (long) checkdouble();
	}
	public double checkdouble() {
		double d = scannumber(10);
		if ( Double.isNaN(d) )
			argerror("number");
		return d;
	}
	public LuaNumber checknumber() {
		return valueOf(checkdouble());
	}
	public LuaNumber checknumber(String msg) {
		double d = scannumber(10);
		if ( Double.isNaN(d) )
			error(msg);
		return valueOf(d);
	}
	public LuaValue tonumber() {
		return tonumber(10);
	}

	public boolean isnumber() {
		double d = scannumber(10);
		return ! Double.isNaN(d);
	}
	
	public boolean isint() {
		double d = scannumber(10);
		if ( Double.isNaN(d) ) 
			return false;
		int i = (int) d;
		return i == d;
	}

	public boolean islong() { 
		double d = scannumber(10);
		if ( Double.isNaN(d) ) 
			return false;
		long l = (long) d;
		return l == d;
	}
	
	public byte    tobyte()        { return (byte) toint(); }
	public char    tochar()        { return (char) toint(); }
	public double  todouble()      { double d=scannumber(10); return Double.isNaN(d)? 0: d; }
	public float   tofloat()       { return (float) todouble(); }
	public int     toint()         { return (int) tolong(); }
	public long    tolong()        { return (long) todouble(); }
	public short   toshort()       { return (short) toint(); }

	public double optdouble(double defval) {
		return checknumber().checkdouble();
	}
	
	public int optint(int defval) {
		return checknumber().checkint();
	}
	
	public LuaInteger optinteger(LuaInteger defval) { 
		return checknumber().checkinteger();
	}
	
	public long optlong(long defval) {
		return checknumber().checklong();
	}
	
	public LuaNumber optnumber(LuaNumber defval) {
		return checknumber().checknumber();
	}
	
	public LuaString optstring(LuaString defval) {
		return this; 
	}
	
	public LuaValue tostring() {
		return this;
	}

	public String optjstring(String defval) { 
		return tojstring(); 
	}
	
	public LuaString strvalue() {
		return this;
	}
	
	public LuaString substring( int beginIndex, int endIndex ) {
		return new LuaString( m_bytes, m_offset + beginIndex, endIndex - beginIndex );
	}
	
	public int hashCode() {
		int h = m_length;  /* seed */
		int step = (m_length>>5)+1;  /* if string is too long, don't hash all its chars */
		for (int l1=m_length; l1>=step; l1-=step)  /* compute hash */
		    h = h ^ ((h<<5)+(h>>2)+(((int) m_bytes[m_offset+l1-1] ) & 0x0FF ));
		return h;
	}
	
	// object comparison, used in key comparison
	public boolean equals( Object o ) {
		if ( o instanceof LuaString ) {
			return raweq( (LuaString) o );
		}
		return false;
	}

	// equality w/ metatable processing
	public LuaValue eq( LuaValue val )    { return val.raweq(this)? TRUE: FALSE; }
	public boolean eq_b( LuaValue val )   { return val.raweq(this); }
	
	// equality w/o metatable processing
	public boolean raweq( LuaValue val ) {
		return val.raweq(this);
	}
	
	public boolean raweq( LuaString s ) { 
		if ( this == s )
			return true;
		if ( s.m_length != m_length )
			return false;
		if ( s.m_bytes == m_bytes && s.m_offset == m_offset )
			return true;
		if ( s.hashCode() != hashCode() )
			return false;
		for ( int i=0; i<m_length; i++ )
			if ( s.m_bytes[s.m_offset+i] != m_bytes[m_offset+i] )
				return false;
		return true;
	}

	public static boolean equals( LuaString a, int i, LuaString b, int j, int n ) {
		return equals( a.m_bytes, a.m_offset + i, b.m_bytes, b.m_offset + j, n );
	}
	
	public static boolean equals( byte[] a, int i, byte[] b, int j, int n ) {
		if ( a.length < i + n || b.length < j + n )
			return false;
		while ( --n>=0 ) 
			if ( a[i++]!=b[j++] )
				return false;
		return true;
	}

	public void write(DataOutputStream writer, int i, int len) throws IOException {
		writer.write(m_bytes,m_offset+i,len);
	}
	
	public LuaValue len() {
		return LuaInteger.valueOf(m_length);
	}

	public int length() {
		return m_length;
	}

	public int luaByte(int index) {
		return m_bytes[m_offset + index] & 0x0FF;
	}

	public int charAt( int index ) {
		if ( index < 0 || index >= m_length )
			throw new IndexOutOfBoundsException();
		return luaByte( index );
	}
	
	public String checkjstring() { 
		return tojstring(); 
	}

	public LuaString checkstring() {
		return this;
	}
	
	/** Convert value to an input stream.
	 * 
	 * @return {@link InputStream} whose data matches the bytes in this {@link LuaString}
	 */
	public InputStream toInputStream() {
		return new ByteArrayInputStream(m_bytes, m_offset, m_length);
	}
	
	/**
	 * Copy the bytes of the string into the given byte array. 
	 * @param strOffset offset from which to copy
	 * @param bytes destination byte array
	 * @param arrayOffset offset in destination
	 * @param len number of bytes to copy
	 */
	public void copyInto( int strOffset, byte[] bytes, int arrayOffset, int len ) {
		System.arraycopy( m_bytes, m_offset+strOffset, bytes, arrayOffset, len );
	}
	
	/** Java version of strpbrk - find index of any byte that in an accept string.
	 * @param accept {@link LuaString} containing characters to look for.
	 * @return index of first match in the {@code accept} string, or -1 if not found.
	 */
	public int indexOfAny( LuaString accept ) {
		final int ilimit = m_offset + m_length;
		final int jlimit = accept.m_offset + accept.m_length;
		for ( int i = m_offset; i < ilimit; ++i ) {
			for ( int j = accept.m_offset; j < jlimit; ++j ) {
				if ( m_bytes[i] == accept.m_bytes[j] ) {
					return i - m_offset;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Find the index of a byte starting at a point in this string
	 * @param b the byte to look for
	 * @param start the first index in the string
	 * @return index of first match found, or -1 if not found.
	 */
	public int indexOf( byte b, int start ) {
		for ( int i=0, j=m_offset+start; i < m_length; ++i ) {
			if ( m_bytes[j++] == b )
				return i;
		}
		return -1;
	}
	
	/**
	 * Find the index of a string starting at a point in this string
	 * @param s the string to search for
	 * @param start the first index in the string
	 * @return index of first match found, or -1 if not found.
	 */
	public int indexOf( LuaString s, int start ) {
		final int slen = s.length();
		final int limit = m_offset + m_length - slen;
		for ( int i = m_offset + start; i <= limit; ++i ) {
			if ( equals( m_bytes, i, s.m_bytes, s.m_offset, slen ) ) {
				/* DAN200 START */
				//return i;
				return i - m_offset;
				/* DAN200 END */
			}
		}
		return -1;
	}
	
	/**
	 * Find the last index of a string in this string
	 * @param s the string to search for
	 * @return index of last match found, or -1 if not found.
	 */
	public int lastIndexOf( LuaString s ) {
		final int slen = s.length();
		final int limit = m_offset + m_length - slen;
		for ( int i = limit; i >= m_offset; --i ) {
			if ( equals( m_bytes, i, s.m_bytes, s.m_offset, slen ) ) {
				/* DAN200 START */
				//return i;
				return i - m_offset;
				/* DAN200 END */
			}
		}
		return -1;
	}


	/**
	 * Convert to Java String interpreting as utf8 characters. 
	 * 
	 * @param bytes byte array in UTF8 encoding to convert
	 * @param offset starting index in byte array
	 * @param length number of bytes to convert
	 * @return Java String corresponding to the value of bytes interpreted using UTF8 
	 * @see #lengthAsUtf8(char[])
	 * @see #encodeToUtf8(char[], byte[], int)
	 * @see #isValidUtf8()
	 */
	/* DAN200 START */
	//public static String decodeAsUtf8(byte[] bytes, int offset, int length) {
	private static String decodeAsUtf8(byte[] bytes, int offset, int length) {
	/* DAN200 END */
		int i,j,n,b;
		for ( i=offset,j=offset+length,n=0; i<j; ++n ) {
			switch ( 0xE0 & bytes[i++] ) {
			case 0xE0: ++i;
			case 0xC0: ++i;
			}
		}
		char[] chars=new char[n];
		for ( i=offset,j=offset+length,n=0; i<j; ) {
			chars[n++] = (char) (
				((b=bytes[i++])>=0||i>=j)? b:
				(b<-32||i+1>=j)? (((b&0x3f) << 6) | (bytes[i++]&0x3f)):
					(((b&0xf) << 12) | ((bytes[i++]&0x3f)<<6) | (bytes[i++]&0x3f)));
		}
		return new String(chars);
	}
	
	/**
	 * Count the number of bytes required to encode the string as UTF-8.
	 * @param chars Array of unicode characters to be encoded as UTF-8
	 * @return count of bytes needed to encode using UTF-8
	 * @see #encodeToUtf8(char[], byte[], int)
	 * @see #decodeAsUtf8(byte[], int, int)
	 * @see #isValidUtf8()
	 */
	/* DAN200 START */
	//public static int lengthAsUtf8(char[] chars) {		
	private static int lengthAsUtf8(char[] chars) {		
	/* DAN200 END */
		int i,b;
		char c;
		for ( i=b=chars.length; --i>=0; )
			if ( (c=chars[i]) >=0x80 )
				b += (c>=0x800)? 2: 1;
		return b;
	}
	
	/**
	 * Encode the given Java string as UTF-8 bytes, writing the result to bytes
	 * starting at offset. 
	 * <p>
	 * The string should be measured first with lengthAsUtf8
	 * to make sure the given byte array is large enough.
	 * @param chars Array of unicode characters to be encoded as UTF-8
	 * @param bytes byte array to hold the result
	 * @param off offset into the byte array to start writing
	 * @see #lengthAsUtf8(char[])
	 * @see #decodeAsUtf8(byte[], int, int)
	 * @see #isValidUtf8()
	 */
	/* DAN200 START */
	//public static void encodeToUtf8(char[] chars, byte[] bytes, int off) {
	private static void encodeToUtf8(char[] chars, byte[] bytes, int off) {
	/* DAN200 END */
		final int n = chars.length;
		char c;
		for ( int i=0, j=off; i<n; i++ ) {
			if ( (c = chars[i]) < 0x80 ) {
				bytes[j++] = (byte) c;
			} else if ( c < 0x800 ) {
				bytes[j++] = (byte) (0xC0 | ((c>>6)  & 0x1f));
				bytes[j++] = (byte) (0x80 | ( c      & 0x3f));				
			} else {
				bytes[j++] = (byte) (0xE0 | ((c>>12) & 0x0f));
				bytes[j++] = (byte) (0x80 | ((c>>6)  & 0x3f));
				bytes[j++] = (byte) (0x80 | ( c      & 0x3f));				
			}
		}
	}

	/** Check that a byte sequence is valid UTF-8
	 * @return true if it is valid UTF-8, otherwise false
	 * @see #lengthAsUtf8(char[])
	 * @see #encodeToUtf8(char[], byte[], int)
	 * @see #decodeAsUtf8(byte[], int, int)
	 */
	public boolean isValidUtf8() {
		int i,j,n,b,e=0;
		for ( i=m_offset,j=m_offset+m_length,n=0; i<j; ++n ) {
			int c = m_bytes[i++];
			if ( c >= 0 ) continue;
			if ( ((c & 0xE0) == 0xC0) 
					&& i<j 
					&& (m_bytes[i++] & 0xC0) == 0x80) continue;
			if ( ((c & 0xF0) == 0xE0) 
					&& i+1<j 
					&& (m_bytes[i++] & 0xC0) == 0x80 
					&& (m_bytes[i++] & 0xC0) == 0x80) continue;
			return false;
		}
		return true;
	}
	
	// --------------------- number conversion -----------------------
	
	/** 
	 * convert to a number using a supplied base, or NIL if it can't be converted
	 * @param base the base to use, such as 10
	 * @return IntValue, DoubleValue, or NIL depending on the content of the string.
	 * @see LuaValue#tonumber() 
	 */
	public LuaValue tonumber( int base ) {
		double d = scannumber( base );
		return Double.isNaN(d)? NIL: valueOf(d);
	}
	
	/** 
	 * Convert to a number in a base, or return Double.NaN if not a number.
	 * @param base the base to use, such as 10
	 * @return double value if conversion is valid, or Double.NaN if not 
	 */
	public double scannumber( int base ) {
		if ( base >= 2 && base <= 36 ) {
			int i=m_offset,j=m_offset+m_length;
			while ( i<j && m_bytes[i]==' ' ) ++i;
			while ( i<j && m_bytes[j-1]==' ' ) --j;
			if ( i>=j )
				return Double.NaN;
			if ( ( base == 10 || base == 16 ) && ( m_bytes[i]=='0' && i+1<j && (m_bytes[i+1]=='x'||m_bytes[i+1]=='X') ) ) {
				base = 16;
				i+=2;
			}
			double l = scanlong( base, i, j );
			return Double.isNaN(l) && base==10? scandouble(i,j): l;
		}
		
		return Double.NaN;
	}
	
	/**
	 * Scan and convert a long value, or return Double.NaN if not found.
	 * @param base the base to use, such as 10
	 * @param start the index to start searching from
	 * @param end the first index beyond the search range
	 * @return double value if conversion is valid, 
	 * or Double.NaN if not
	 */
	private double scanlong( int base, int start, int end ) {
		long x = 0;
		boolean neg = (m_bytes[start] == '-');
		for ( int i=(neg?start+1:start); i<end; i++ ) {
			int digit = m_bytes[i] - (base<=10||(m_bytes[i]>='0'&&m_bytes[i]<='9')? '0':
					m_bytes[i]>='A'&&m_bytes[i]<='Z'? ('A'-10): ('a'-10));
			if ( digit < 0 || digit >= base )
				return Double.NaN;		
			x = x * base + digit;
		}
		return neg? -x: x;
	}
	
	/**
	 * Scan and convert a double value, or return Double.NaN if not a double.
	 * @param start the index to start searching from
	 * @param end the first index beyond the search range
	 * @return double value if conversion is valid, 
	 * or Double.NaN if not
	 */
	private double scandouble(int start, int end) {
		if ( end>start+64 ) end=start+64;
		for ( int i=start; i<end; i++ ) {
			switch ( m_bytes[i] ) {
			case '-':
			case '+':
			case '.':
			case 'e': case 'E':
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				break;
			default:
				return Double.NaN;
			}
		}
		char [] c = new char[end-start];
		for ( int i=start; i<end; i++ )
			c[i-start] = (char) m_bytes[i];
		try {
			return Double.parseDouble(new String(c));
		} catch ( Exception e ) {			
			return Double.NaN;
		}
	}

}
