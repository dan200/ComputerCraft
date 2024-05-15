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

import java.lang.ref.WeakReference;

import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Subclass of {@link LuaTable} that provides weak key and weak value semantics. 
 * <p> 
 * Normally these are not created directly, but indirectly when changing the mode 
 * of a {@link LuaTable} as lua script executes.  
 * <p>
 * However, calling the constructors directly when weak tables are required from 
 * Java will reduce overhead.  
 */
public class WeakTable extends LuaTable {
	private boolean weakkeys,weakvalues;
	
	/** 
	 * Construct a table with weak keys, weak values, or both
	 * @param weakkeys true to let the table have weak keys
	 * @param weakvalues true to let the table have weak values
	 */
	public WeakTable(boolean weakkeys, boolean weakvalues) {
		this(weakkeys, weakvalues, 0, 0);
	}

	/** 
	 * Construct a table with weak keys, weak values, or both, and an initial capacity
	 * @param weakkeys true to let the table have weak keys
	 * @param weakvalues true to let the table have weak values
	 * @param narray capacity of array part
	 * @param nhash capacity of hash part
	 */
	protected WeakTable(boolean weakkeys, boolean weakvalues, int narray, int nhash) {
		super(narray, nhash);
		this.weakkeys = weakkeys;
		this.weakvalues = weakvalues;
	}

	/** 
	 * Construct a table with weak keys, weak values, or both, and a source of initial data
	 * @param weakkeys true to let the table have weak keys
	 * @param weakvalues true to let the table have weak values
	 * @param source {@link LuaTable} containing the initial elements
	 */
	protected WeakTable(boolean weakkeys, boolean weakvalues, LuaTable source) {
		this(weakkeys, weakvalues, source.getArrayLength(), source.getHashLength());
		Varargs n;
		LuaValue k = NIL;
		while ( !(k = ((n = source.next(k)).arg1())).isnil() )
			rawset(k, n.arg(2));
		m_metatable = source.m_metatable;
	}
	
	public void presize( int narray ) {
		super.presize(narray);
	}

	/** 
	 * Presize capacity of both array and hash parts.
	 * @param narray capacity of array part
	 * @param nhash capacity of hash part
	 */
	public void presize(int narray, int nhash) {
		super.presize(narray, nhash);
	}
	
	protected int getArrayLength() {
		return super.getArrayLength();
	}

	protected int getHashLength() {
		return super.getHashLength();
	}
	
	protected LuaTable changemode(boolean weakkeys, boolean weakvalues) {
		this.weakkeys = weakkeys;
		this.weakvalues = weakvalues;
		return this;
	}
	
	/**
	 * Self-sent message to convert a value to its weak counterpart
	 * @param value value to convert
	 * @return {@link LuaValue} that is a strong or weak reference, depending on type of {@code value}
	 */
	LuaValue weaken( LuaValue value ) {
		switch ( value.type() ) {
			case LuaValue.TFUNCTION:
			case LuaValue.TTHREAD:
			case LuaValue.TTABLE:
				return new WeakValue(value);
			case LuaValue.TUSERDATA:
				return new WeakUserdata(value);
			default:
				return value;
		}
	}
	
	public void rawset( int key, LuaValue value ) {
		if ( weakvalues )
			value = weaken( value );
		super.rawset(key, value);
	}
	
	public void rawset( LuaValue key, LuaValue value ) {
		if ( weakvalues )
			value = weaken( value );
		if ( weakkeys ) {
			switch ( key.type() ) {
				case LuaValue.TFUNCTION:
				case LuaValue.TTHREAD:
				case LuaValue.TTABLE:
				case LuaValue.TUSERDATA:
					key = value = new WeakEntry(this, key, value);
					break;
				default:
					break;
			}
		}
		super.rawset(key, value);
	}
	

	public LuaValue rawget( int key ) {
		return super.rawget(key).strongvalue();
	}
	
	public LuaValue rawget( LuaValue key ) {
		return super.rawget(key).strongvalue();
	}

	/** Get the hash value for a key 
	 * key the key to look up 
	 * */
	protected LuaValue hashget(LuaValue key) {
		if ( hashEntries > 0 ) {
			int i = hashFindSlot(key);
			if ( hashEntries == 0 )
				return NIL;
			LuaValue v = hashValues[i];
			return v!=null? v: NIL;
		}
		return NIL;
	}

	
	// override to remove values for weak keys as we search
	public int hashFindSlot(LuaValue key) {		
		int i = ( key.hashCode() & 0x7FFFFFFF ) % hashKeys.length;
		LuaValue k;
		while ( ( k = hashKeys[i] ) != null ) {
			if ( k.isweaknil() ) {
				hashClearSlot(i);
				if ( hashEntries == 0 )
					return 0;
			}
			else {
				if ( k.raweq(key.strongkey()) )
					return i;
				i = ( i + 1 ) % hashKeys.length;
			}
		}
		return i;
	}
	
	public int maxn() {
		return super.maxn();
	}


	/**
	 * Get the next element after a particular key in the table 
	 * @return key,value or nil
	 */
	public Varargs next( LuaValue key ) {
		while ( true ) {
			Varargs n = super.next(key);
			LuaValue k = n.arg1();
			if ( k.isnil() )
				return NIL;
			LuaValue ks = k.strongkey();
			LuaValue vs = n.arg(2).strongvalue();
			if ( ks.isnil() || vs.isnil() ) {
				super.rawset(k, NIL);
			} else {
				return varargsOf(ks,vs);
			}
		}
	}
	
	// ----------------- sort support -----------------------------
	public void sort(final LuaValue comparator) {
		super.sort( new TwoArgFunction() {
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return comparator.call( arg1.strongvalue(), arg2.strongvalue() );
			}
		} );
	}

	/** Internal class to implement weak values. 
	 * @see WeakTable
	 */
	static class WeakValue extends LuaValue {
		final WeakReference ref;

		protected WeakValue(LuaValue value) {
			ref = new WeakReference(value);
		}

		public int type() {
			illegal("type","weak value");
			return 0;
		}

		public String typename() {
			illegal("typename","weak value");
			return null;
		}
		
		public String toString() {
			return "weak<"+ref.get()+">";
		}
		
		public LuaValue strongvalue() {
			Object o = ref.get();
			return o!=null? (LuaValue)o: NIL;
		}
		
		public boolean raweq(LuaValue rhs) {
			Object o = ref.get();
			return o!=null && rhs.raweq((LuaValue)o);
		}
		
		public boolean isweaknil() {
			return ref.get() == null;
		}
	}
	
	/** Internal class to implement weak userdata values. 
	 * @see WeakTable
	 */
	static final class WeakUserdata extends WeakValue {
		private final WeakReference ob;
		private final LuaValue mt;

		private WeakUserdata(LuaValue value) {
			super(value);
			ob = new WeakReference(value.touserdata());
			mt = value.getmetatable();
		}
		
		public LuaValue strongvalue() {
			Object u = ref.get();
			if ( u != null )
				return (LuaValue) u;
			Object o = ob.get();
			return o!=null? userdataOf(o,mt): NIL;
		}
		
		public boolean raweq(LuaValue rhs) {
			if ( ! rhs.isuserdata() )
				return false;
			LuaValue v = (LuaValue) ref.get();
			if ( v != null && v.raweq(rhs) )
				return true;
			return rhs.touserdata() == ob.get();
		}
		
		public boolean isweaknil() {
			return ob.get() == null || ref.get() == null;
		}
	}

	/** Internal class to implement weak table entries. 
	 * @see WeakTable
	 */
	static final class WeakEntry extends LuaValue {
		final LuaValue weakkey;
		LuaValue weakvalue;
		final int keyhash;

		private WeakEntry(WeakTable table, LuaValue key, LuaValue weakvalue) {
			this.weakkey = table.weaken(key);
			this.keyhash = key.hashCode();
			this.weakvalue = weakvalue;
		}

		public LuaValue strongkey() {
			return weakkey.strongvalue();
		}

		// when looking up the value, look in the keys metatable
		public LuaValue strongvalue() {
			LuaValue key = weakkey.strongvalue();
			if ( key.isnil() )
				return weakvalue = NIL;
			return weakvalue.strongvalue();
		}

		public int type() {
			return TNONE;
		}

		public String typename() {
			illegal("typename","weak entry");
			return null;
		}
		
		public String toString() {
			return "weak<"+weakkey.strongvalue()+","+strongvalue()+">";
		}
		
		public int hashCode() {
			return keyhash;
		}
		
		public boolean raweq(LuaValue rhs) {
			//return rhs.raweq(weakkey.strongvalue());
			return weakkey.raweq(rhs);
		}
		
		public boolean isweaknil() {
			return weakkey.isweaknil() || weakvalue.isweaknil();
		}
	}
}
