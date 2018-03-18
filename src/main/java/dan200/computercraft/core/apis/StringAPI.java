/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.VarArgFunction;

// Contributed by mepeisen
// specialized version for utf handling if needed

public class StringAPI extends OneArgFunction {
	
	protected IAPIEnvironment m_env;
	
	public StringAPI( IAPIEnvironment _environment )
    {
		this.m_env = _environment;
    }
	
	public LuaValue call(LuaValue arg) {
		LuaTable t = new LuaTable();
		t.set("dump", new Dump(t));
		t.set("len", new Len(t));
		t.set("lower", new Lower(t));
		t.set("reverse", new Reverse(t));
		t.set("upper", new Upper(t));
		t.set("byte", new Byte(t));
		t.set("char", new Char(t));
		t.set("find", new Find(t));
		t.set("format", new Format(t));
		t.set("gmatch", new Gmatch(t));
		t.set("gsub", new Gsub(t));
		t.set("match", new Match(t));
		t.set("rep", new Rep(t));
		t.set("sub", new Sub(t));
		return t;
	}
	
	private class Dump extends OneArgFunction
	{

		private LuaValue m_orig;

		public Dump(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("dump").checkfunction();
		}
		
		public LuaValue call(LuaValue arg) {
			// no utf handling needed, pass to orig
			return m_orig.call(arg);
		}
		
	}
	
	private class Len extends OneArgFunction
	{

		private LuaValue m_orig;

		public Len(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("len").checkfunction();
		}
		
		public LuaValue call(LuaValue arg) {
			if (m_env.isUtf()) {
				return LuaInteger.valueOf(arg.checkjstring().length());
			}
			return m_orig.call(arg);
		}
		
	}
	
	private class Lower extends OneArgFunction
	{

		private LuaValue m_orig;

		public Lower(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("lower").checkfunction();
		}
		
		public LuaValue call(LuaValue arg) {
			// utf variant is not needed because StringLib already does Java-String-toLowerCase
			return m_orig.call(arg);
		}
		
	}
	
	private class Reverse extends OneArgFunction
	{

		private LuaValue m_orig;

		public Reverse(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("reverse").checkfunction();
		}
		
		public LuaValue call(LuaValue arg) {
			if (m_env.isUtf()) {
				return LuaString.valueOf(new StringBuilder(arg.checkjstring()).reverse().toString());
			}
			return m_orig.call(arg);
		}
		
	}
	
	private class Upper extends OneArgFunction
	{

		private LuaValue m_orig;

		public Upper(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("upper").checkfunction();
		}
		
		public LuaValue call(LuaValue arg) {
			// utf variant is not needed because StringLib already does Java-String-toUpperCase
			return m_orig.call(arg);
		}
		
	}
	
	private class Byte extends VarArgFunction
	{

		private LuaValue m_orig;

		public Byte(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("byte").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				// mostly taken from original StringLib
				String s = args.checkjstring(1);
				int l = s.length();
				int posi = posrelat( args.optint(2,1), l );
				int pose = posrelat( args.optint(3,posi), l );
				int n,i;
				if (posi <= 0) posi = 1;
				if (pose > l) pose = l;
				if (posi > pose) return NONE;  /* empty interval; return no values */
				n = (int)(pose -  posi + 1);
				if (posi + n <= pose)  /* overflow? */
				    error("string slice too long");
				LuaValue[] v = new LuaValue[n];
				for (i=0; i<n; i++)
					v[i] = valueOf((int) s.charAt(posi+i-1));
				return varargsOf(v);
			}
			return m_orig.invoke(args);
		}
		
	}
	
	private class Char extends VarArgFunction
	{

		private LuaValue m_orig;

		public Char(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("char").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				// mostly taken from original StringLib
				int n = args.narg();
				char[] bytes = new char[n];
				for ( int i=0, a=1; i<n; i++, a++ ) {
					int c = args.checkint(a);
					if (c<0 || c>=65536) argerror(a, "invalid value");
					bytes[i] = (char) c;
				}
				return LuaString.valueOf(new String(bytes));
			}
			return m_orig.invoke(args);
		}
		
	}
	
	private class Find extends VarArgFunction
	{

		private LuaValue m_orig;

		public Find(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("find").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				return str_find_aux( args, true );
			}
			return m_orig.invoke(args);
		}
		
	}
	
	private class Format extends VarArgFunction
	{

		private LuaValue m_orig;

		public Format(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("format").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				// mostly taken from original StringLib but works on java strings
				String fmt = args.checkjstring( 1 );
				final int n = fmt.length();
				StringBuffer result = new StringBuffer(n);
				int arg = 1;
				char c;
				
				for ( int i = 0; i < n; ) {
					switch ( c = fmt.charAt( i++ ) ) {
					case '\n':
						result.append( "\n" );
						break;
					default:
						result.append( c );
						break;
					case L_ESC:
						if ( i < n ) {
							if ( ( c = fmt.charAt( i ) ) == L_ESC ) {
								++i;
								result.append( L_ESC );
							} else {
								arg++;
								FormatDesc fdsc = new FormatDesc(args, fmt, i );
								i += fdsc.length;
								switch ( fdsc.conversion ) {
								case 'c':
									fdsc.format( result, (char)args.checkint( arg ) );
									break;
								case 'i':
								case 'd':
									fdsc.format( result, args.checkint( arg ) );
									break;
								case 'o':
								case 'u':
								case 'x':
								case 'X':
									fdsc.format( result, args.checklong( arg ) );
									break;
								case 'e':
								case 'E':
								case 'f':
								case 'g':
								case 'G':
									fdsc.format( result, args.checkdouble( arg ) );
									break;
								case 'q':
									addquoted( result, args.checkjstring( arg ) );
									break;
								case 's': {
									String s = args.checkjstring( arg );
									if ( fdsc.precision == -1 && s.length() >= 100 ) {
										result.append( s );
									} else {
										fdsc.format( result, s );
									}
								}	break;
								default:
									error("invalid option '%"+(char)fdsc.conversion+"' to 'format'");
									break;
								}
							}
						}
					}
				}
				
				return LuaString.valueOf(result.toString());
			}
			return m_orig.invoke(args);
		}
		
	}
	
	private class Match extends VarArgFunction
	{

		private LuaValue m_orig;

		public Match(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("match").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				return str_find_aux( args, false);
			}
			return m_orig.invoke(args);
		}
		
	}
	
	private class Rep extends VarArgFunction
	{

		private LuaValue m_orig;

		public Rep(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("rep").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			// utf variant is not needed because StringLib duplicates underlying byte arrays which is really ok for utf
			return m_orig.invoke(args);
		}
		
	}
	
	private class Sub extends VarArgFunction
	{

		private LuaValue m_orig;

		public Sub(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("sub").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				final String s = args.checkjstring( 1 );
				final int l = s.length();
				
				int start = posrelat( args.checkint( 2 ), l );
				int end = posrelat( args.optint( 3, -1 ), l );
				
				if ( start < 1 )
					start = 1;
				if ( end > l )
					end = l;
				
				if ( start <= end ) {
					return LuaString.valueOf(s.substring( start-1 , end ));
				} else {
					return EMPTYSTRING;
				}
			}
			return m_orig.invoke(args);
		}
		
	}
	
	// utilities

	
	/**
	 * This utility method implements both string.find and string.match.
	 * mostly taken for original string lib
	 */
	static Varargs str_find_aux( Varargs args, boolean find ) {
		String s = args.checkjstring( 1 );
		String pat = args.checkjstring( 2 );
		int init = args.optint( 3, 1 );
		
		if ( init > 0 ) {
			init = Math.min( init - 1, s.length() );
		} else if ( init < 0 ) {
			init = Math.max( 0, s.length() + init );
		}
		
		boolean fastMatch = find && ( args.arg(4).toboolean() || indexOfAny(pat, SPECIALS ) == -1 );
		
		if ( fastMatch ) {
			int result = s.indexOf( pat, init );
			if ( result != -1 ) {
				return varargsOf( valueOf(result+1), valueOf(result+pat.length()) );
			}
		} else {
			MatchState ms = new MatchState( args, s, pat );
			
			boolean anchor = false;
			int poff = 0;
			if ( pat.charAt( 0 ) == '^' ) {
				anchor = true;
				poff = 1;
			}
			
			int soff = init;
			do {
				int res;
				ms.reset();
				if ( ( res = ms.match( soff, poff ) ) != -1 ) {
					if ( find ) {
						return varargsOf( valueOf(soff+1), valueOf(res), ms.push_captures( false, soff, res ));
					} else {
						return ms.push_captures( true, soff, res );
					}
				}
			} while ( soff++ < s.length() && !anchor );
		}
		return NIL;
	}
	
	// taken from original StringLib
	private static final String SPECIALS = "^$*+?.([%-";
	private static final int MAX_CAPTURES = 32;
	private static final char L_ESC = '%';
	private static final int CAP_UNFINISHED = -1;
	private static final int CAP_POSITION = -2;
	private static final byte MASK_ALPHA		= 0x01;
	private static final byte MASK_LOWERCASE	= 0x02;
	private static final byte MASK_UPPERCASE	= 0x04;
	private static final byte MASK_DIGIT		= 0x08;
	private static final byte MASK_PUNCT		= 0x10;
	private static final byte MASK_SPACE		= 0x20;
	private static final byte MASK_CONTROL		= 0x40;
	private static final byte MASK_HEXDIGIT		= (byte)0x80;
	private static final byte[] CHAR_TABLE;
	
	// taken from original StringLib
	static {
		CHAR_TABLE = new byte[256];
		
		for ( int i = 0; i < 256; ++i ) {
			final char c = (char) i;
			CHAR_TABLE[i] = (byte)( ( Character.isDigit( c ) ? MASK_DIGIT : 0 ) |
							( Character.isLowerCase( c ) ? MASK_LOWERCASE : 0 ) |
							( Character.isUpperCase( c ) ? MASK_UPPERCASE : 0 ) |
							( ( c < ' ' || c == 0x7F ) ? MASK_CONTROL : 0 ) );
			if ( ( c >= 'a' && c <= 'f' ) || ( c >= 'A' && c <= 'F' ) || ( c >= '0' && c <= '9' ) ) {
				CHAR_TABLE[i] |= MASK_HEXDIGIT;
			}
			if ( ( c >= '!' && c <= '/' ) || ( c >= ':' && c <= '@' ) ) {
				CHAR_TABLE[i] |= MASK_PUNCT;
			}
			if ( ( CHAR_TABLE[i] & ( MASK_LOWERCASE | MASK_UPPERCASE ) ) != 0 ) {
				CHAR_TABLE[i] |= MASK_ALPHA;
			}
		}
		
		CHAR_TABLE[' '] = MASK_SPACE;
		CHAR_TABLE['\r'] |= MASK_SPACE;
		CHAR_TABLE['\n'] |= MASK_SPACE;
		CHAR_TABLE['\t'] |= MASK_SPACE;
		/* DAN200 START */
		//CHAR_TABLE[0x0C /* '\v' */ ] |= MASK_SPACE;
		CHAR_TABLE[0x0B /* '\v' */ ] |= MASK_SPACE;
		/* DAN200 END */
		CHAR_TABLE['\f'] |= MASK_SPACE;
	};
	
	// similar to LuaString
	private static final int indexOfAny(String src, String pattern)
	{
		final char[] srcarr = src.toCharArray();
		final char[] patarr = pattern.toCharArray();
		for (int i = 0, n = srcarr.length; i < n; i++)
		{
			for (int j = 0, n2 = patarr.length; j < n2; j++)
			{
				if (srcarr[i] == patarr[j]) return i;
			}
		}
		return -1;
	}
	
	// mostly taken from StringLib but works on java strings for utf support
	static class MatchState {
		final String s;
		final String p;
		final Varargs args;
		int level;
		int[] cinit;
		int[] clen;
		
		MatchState( Varargs args, String s, String pattern ) {
			this.s = s;
			this.p = pattern;
			this.args = args;
			this.level = 0;
			this.cinit = new int[ MAX_CAPTURES ];
			this.clen = new int[ MAX_CAPTURES ];
		}
		
		void reset() {
			level = 0;
		}
		
		private void add_s( StringBuffer lbuf, String news, int soff, int e ) {
			int l = news.length();
			for ( int i = 0; i < l; ++i ) {
				char b = news.charAt( i );
				if ( b != L_ESC ) {
					lbuf.append( b );
				} else {
					++i; // skip ESC
					b = news.charAt( i );
					if ( !isAsciiDigit( b ) ) {
						lbuf.append( b );
					} else if ( b == '0' ) {
						lbuf.append( s.substring( soff, e ) );
					} else {
						lbuf.append( push_onecapture( b - '1', soff, e ).strvalue().tojstring() );
					}
				}
			}
		}
		
		public void add_value( StringBuffer lbuf, int soffset, int end, LuaValue repl ) {
			switch ( repl.type() ) {
			case LuaValue.TSTRING:
			case LuaValue.TNUMBER:
				add_s( lbuf, repl.strvalue().tojstring(), soffset, end );
				return;
				
			case LuaValue.TFUNCTION:
				repl = repl.invoke( push_captures( true, soffset, end ) ).arg1();
				break;
				
			case LuaValue.TTABLE:
				// Need to call push_onecapture here for the error checking
				repl = repl.get( push_onecapture( 0, soffset, end ) );
				break;
				
			default:
				error( "bad argument: string/function/table expected" );
				return;
			}
			
			if ( !repl.toboolean() ) {
				lbuf.append( s.substring( soffset, end ) );
			}
			else if ( ! repl.isstring() ) {
				error( "invalid replacement value (a "+repl.typename()+")" );
			}
			else {
				lbuf.append( repl.strvalue().tojstring() );
			}
		}
		
		Varargs push_captures( boolean wholeMatch, int soff, int end ) {
			int nlevels = ( this.level == 0 && wholeMatch ) ? 1 : this.level;
			switch ( nlevels ) {
			case 0: return NONE;
			case 1: return push_onecapture( 0, soff, end );
			}
			LuaValue[] v = new LuaValue[nlevels];
			for ( int i = 0; i < nlevels; ++i )
				v[i] = push_onecapture( i, soff, end );
			return varargsOf(v);
		}
		
		private LuaValue push_onecapture( int i, int soff, int end ) {
			if ( i >= this.level ) {
				if ( i == 0 ) {
					return LuaString.valueOf(s.substring( soff, end ));
				} else {
					return error( "invalid capture index" );
				}
			} else {
				int l = clen[i];
				if ( l == CAP_UNFINISHED ) {
					return error( "unfinished capture" );
				}
				if ( l == CAP_POSITION ) {
					return valueOf( cinit[i] + 1 );
				} else {
					int begin = cinit[i];
					return LuaString.valueOf(s.substring( begin, begin + l ));
				}
			}
		}
		
		private int check_capture( int l ) {
			l -= '1';
			if ( l < 0 || l >= level || this.clen[l] == CAP_UNFINISHED ) {
				error("invalid capture index");
			}
			return l;
		}
		
		private int capture_to_close() {
			int level = this.level;
			for ( level--; level >= 0; level-- )
				if ( clen[level] == CAP_UNFINISHED )
					return level;
			error("invalid pattern capture");
			return 0;
		}
		
		int classend( int poffset ) {
			switch ( p.charAt( poffset++ ) ) {
			case L_ESC:
				if ( poffset == p.length() ) {
					error( "malformed pattern (ends with %)" );
				}
				return poffset + 1;
				
			case '[':
				if ( p.charAt( poffset ) == '^' ) poffset++;
				do {
					if ( poffset == p.length() ) {
						error( "malformed pattern (missing ])" );
					}
					if ( p.charAt( poffset++ ) == L_ESC && poffset != p.length() )
						poffset++;
				} while ( p.charAt( poffset ) != ']' );
				return poffset + 1;
			default:
				return poffset;
			}
		}
		
		static boolean match_class( int c, char cl ) {
			final char lcl = Character.toLowerCase( cl );
			
			int cdata;
			if (c < CHAR_TABLE.length)
			{
				cdata = CHAR_TABLE[c];
			}
			else
			{
				final char cc = (char) c;
				cdata = 0;
				if (Character.isDigit(cc)) cdata |= MASK_DIGIT;
				if (Character.isLowerCase(cc)) cdata |= MASK_LOWERCASE | MASK_ALPHA;
				if (Character.isUpperCase(cc)) cdata |= MASK_UPPERCASE | MASK_ALPHA;
				if (Character.isWhitespace(cc)) cdata |= MASK_SPACE;
			}
			
			boolean res;
			switch ( lcl ) {
			case 'a': res = ( cdata & MASK_ALPHA ) != 0; break;
			case 'd': res = ( cdata & MASK_DIGIT ) != 0; break;
			case 'l': res = ( cdata & MASK_LOWERCASE ) != 0; break;
			case 'u': res = ( cdata & MASK_UPPERCASE ) != 0; break;
			case 'c': res = ( cdata & MASK_CONTROL ) != 0; break;
			case 'p': res = ( cdata & MASK_PUNCT ) != 0; break;
			case 's': res = ( cdata & MASK_SPACE ) != 0; break;
			case 'w': res = ( cdata & ( MASK_ALPHA | MASK_DIGIT ) ) != 0; break;
			case 'x': res = ( cdata & MASK_HEXDIGIT ) != 0; break;
			case 'z': res = ( c == 0 ); break;
			default: return cl == c;
			}
			return ( lcl == cl ) ? res : !res;
		}
		
		boolean matchbracketclass( int c, int poff, int ec ) {
			boolean sig = true;
			if ( p.charAt( poff + 1 ) == '^' ) {
				sig = false;
				poff++;
			}
			while ( ++poff < ec ) {
				if ( p.charAt( poff ) == L_ESC ) {
					poff++;
					if ( match_class( c, p.charAt( poff ) ) )
						return sig;
				}
				else if ( ( p.charAt( poff + 1 ) == '-' ) && ( poff + 2 < ec ) ) {
					poff += 2;
					if ( p.charAt( poff - 2 ) <= c && c <= p.charAt( poff ) )
						return sig;
				}
				else if ( p.charAt( poff ) == c ) return sig;
			}
			return !sig;
		}
		
		boolean singlematch( char c, int poff, int ep ) {
			switch ( p.charAt( poff ) ) {
			case '.': return true;
			case L_ESC: return match_class( c, p.charAt( poff + 1 ) );
			case '[': return matchbracketclass( c, poff, ep - 1 );
			default: return p.charAt( poff ) == c;
			}
		}
		
		/**
		 * Perform pattern matching. If there is a match, returns offset into s
		 * where match ends, otherwise returns -1.
		 */
		int match( int soffset, int poffset ) {
			while ( true ) {
				// Check if we are at the end of the pattern - 
				// equivalent to the '\0' case in the C version, but our pattern
				// string is not NUL-terminated.
				if ( poffset == p.length() )
					return soffset;
				switch ( p.charAt( poffset ) ) {
				case '(':
					if ( ++poffset < p.length() && p.charAt( poffset ) == ')' )
						return start_capture( soffset, poffset + 1, CAP_POSITION );
					else
						return start_capture( soffset, poffset, CAP_UNFINISHED );
				case ')':
					return end_capture( soffset, poffset + 1 );
				case L_ESC:
					if ( poffset + 1 == p.length() )
						error("malformed pattern (ends with '%')");
					switch ( p.charAt( poffset + 1 ) ) {
					case 'b':
						soffset = matchbalance( soffset, poffset + 2 );
						if ( soffset == -1 ) return -1;
						poffset += 4;
						continue;
					case 'f': {
						poffset += 2;
						if ( p.charAt( poffset ) != '[' ) {
							error("Missing [ after %f in pattern");
						}
						int ep = classend( poffset );
						int previous = ( soffset == 0 ) ? -1 : s.charAt( soffset - 1 );
						if ( matchbracketclass( previous, poffset, ep - 1 ) ||
							 matchbracketclass( s.charAt( soffset ), poffset, ep - 1 ) )
							return -1;
						poffset = ep;
						continue;
					}
					default: {
						int c = p.charAt( poffset + 1 );
						if ( Character.isDigit( (char) c ) ) {
							soffset = match_capture( soffset, c );
							if ( soffset == -1 )
								return -1;
							return match( soffset, poffset + 2 );
						}
					}
					}
				case '$':
					if ( poffset + 1 == p.length() )
						return ( soffset == s.length() ) ? soffset : -1;
				}
				int ep = classend( poffset );
				boolean m = soffset < s.length() && singlematch( s.charAt( soffset ), poffset, ep );
				int pc = ( ep < p.length() ) ? p.charAt( ep ) : '\0';
				
				switch ( pc ) {
				case '?':
					int res;
					if ( m && ( ( res = match( soffset + 1, ep + 1 ) ) != -1 ) )
						return res;
					poffset = ep + 1;
					continue;
				case '*':
					return max_expand( soffset, poffset, ep );
				case '+':
					return ( m ? max_expand( soffset + 1, poffset, ep ) : -1 );
				case '-':
					return min_expand( soffset, poffset, ep );
				default:
					if ( !m )
						return -1;
					soffset++;
					poffset = ep;
					continue;
				}
			}
		}
		
		int max_expand( int soff, int poff, int ep ) {
			int i = 0;
			while ( soff + i < s.length() &&
					singlematch( s.charAt( soff + i ), poff, ep ) )
				i++;
			while ( i >= 0 ) {
				int res = match( soff + i, ep + 1 );
				if ( res != -1 )
					return res;
				i--;
			}
			return -1;
		}
		
		int min_expand( int soff, int poff, int ep ) {
			for ( ;; ) {
				int res = match( soff, ep + 1 );
				if ( res != -1 )
					return res;
				else if ( soff < s.length() && singlematch( s.charAt( soff ), poff, ep ) )
					soff++;
				else return -1;
			}
		}
		
		int start_capture( int soff, int poff, int what ) {
			int res;
			int level = this.level;
			if ( level >= MAX_CAPTURES ) {
				error( "too many captures" );
			}
			cinit[ level ] = soff;
			clen[ level ] = what;
			this.level = level + 1;
			if ( ( res = match( soff, poff ) ) == -1 )
				this.level--;
			return res;
		}
		
		int end_capture( int soff, int poff ) {
			int l = capture_to_close();
			int res;
			clen[l] = soff - cinit[l];
			if ( ( res = match( soff, poff ) ) == -1 )
				clen[l] = CAP_UNFINISHED;
			return res;
		}
		
		int match_capture( int soff, int l ) {
			l = check_capture( l );
			int len = clen[ l ];
			if ( ( s.length() - soff ) >= len &&
				 LuaString.equals( LuaString.valueOf(s), cinit[l], LuaString.valueOf(s), soff, len ) )
				return soff + len;
			else
				return -1;
		}
		
		int matchbalance( int soff, int poff ) {
			final int plen = p.length();
			if ( poff == plen || poff + 1 == plen ) {
				error( "unbalanced pattern" );
			}
			/* DAN200 START */
			if ( soff >= s.length() )
				return -1;
			/* DAN200 END */
			if ( s.charAt( soff ) != p.charAt( poff ) )
				return -1;
			else {
				char b = p.charAt( poff );
				char e = p.charAt( poff + 1 );
				int cont = 1;
				while ( ++soff < s.length() ) {
					if ( s.charAt( soff ) == e ) {
						if ( --cont == 0 ) return soff + 1;
					}
					else if ( s.charAt( soff ) == b ) cont++;
				}
			}
			return -1;
		}
	}
	
	// taken from original StringLib
	private static int posrelat( int pos, int len ) {
		return ( pos >= 0 ) ? pos : len + pos + 1;
	}
	
	// taken from original StringLib but works on java strings
	static class FormatDesc {
		
		private boolean leftAdjust;
		private boolean zeroPad;
		private boolean explicitPlus;
		private boolean space;
		private boolean alternateForm;
		private static final int MAX_FLAGS = 5;
		
		private int width;
		private int precision;
		
		public final int conversion;
		public final int length;
		
		public FormatDesc(Varargs args, String strfrmt, final int start) {
			int p = start, n = strfrmt.length();
			char c = 0;
			
			boolean moreFlags = true;
			while ( moreFlags ) {
				switch ( c = ( (p < n) ? strfrmt.charAt( p++ ) : 0 ) ) {
				case '-': leftAdjust = true; break;
				case '+': explicitPlus = true; break;
				case ' ': space = true; break;
				case '#': alternateForm = true; break;
				case '0': zeroPad = true; break;
				default: moreFlags = false; break;
				}
			}
			if ( p - start > MAX_FLAGS )
				error("invalid format (repeated flags)");
			
			width = -1;
			if ( isAsciiDigit( c ) ) { 
				width = c - '0';
				c = ( (p < n) ? strfrmt.charAt( p++ ) : 0 );
				if ( isAsciiDigit( c ) ) {
					width = width * 10 + (c - '0');
					c = ( (p < n) ? strfrmt.charAt( p++ ) : 0 );
				}
			}
			
			precision = -1;
			if ( c == '.' ) {
				c = ( (p < n) ? strfrmt.charAt( p++ ) : 0 );
				if ( isAsciiDigit( c ) ) {
					precision = c - '0';
					c = ( (p < n) ? strfrmt.charAt( p++ ) : 0 );
					if ( isAsciiDigit( c ) ) {
						precision = precision * 10 + (c - '0');
						c = ( (p < n) ? strfrmt.charAt( p++ ) : 0 );
					}
				}
			}
			
			if ( isAsciiDigit( c ) )
				error("invalid format (width or precision too long)");
			
			zeroPad &= !leftAdjust; // '-' overrides '0'
			conversion = c;
			length = p - start;
		}
		
		public void format(StringBuffer buf, char c) {
			// TODO original StringLib does not handle this... Should be upgraded as soon as we use some other lua version
			buf.append(c);
		}
		
		public void format(StringBuffer buf, long number) {
			String digits;
			
			if ( number == 0 && precision == 0 ) {
				digits = "";
			} else {
				int radix;
				switch ( conversion ) {
				case 'x':
				case 'X':
					radix = 16;
					break;
				case 'o':
					radix = 8;
					break;
				default:
					radix = 10;
					break;
				}
				digits = Long.toString( number, radix );
				if ( conversion == 'X' )
					digits = digits.toUpperCase();
			}
			
			int minwidth = digits.length();
			int ndigits = minwidth;
			int nzeros;
			
			if ( number < 0 ) {
				ndigits--;
			} else if ( explicitPlus || space ) {
				minwidth++;
			}
			
			if ( precision > ndigits )
				nzeros = precision - ndigits;
			else if ( precision == -1 && zeroPad && width > minwidth )
				nzeros = width - minwidth;
			else
				nzeros = 0;
			
			minwidth += nzeros;
			int nspaces = width > minwidth ? width - minwidth : 0;
			
			if ( !leftAdjust )
				pad( buf, ' ', nspaces );
			
			if ( number < 0 ) {
				if ( nzeros > 0 ) {
					buf.append( '-' );
					digits = digits.substring( 1 );
				}
			} else if ( explicitPlus ) {
				buf.append( '+' );
			} else if ( space ) {
				buf.append( ' ' );
			}
			
			if ( nzeros > 0 )
				pad( buf, '0', nzeros );
			
			buf.append( digits );
			
			if ( leftAdjust )
				pad( buf, ' ', nspaces );
		}
		
		public void format(StringBuffer buf, double x) {
			// TODO original StringLib does not handle this... Should be upgraded as soon as we use some other lua version
			buf.append( String.valueOf( x ) );
		}
		
		public void format(StringBuffer buf, String s) {
			int nullindex = s.indexOf( '\0', 0 );
			if ( nullindex != -1 )
				s = s.substring( 0, nullindex );
			buf.append(s);
		}
		
		public static final void pad(StringBuffer buf, char c, int n) {
			while ( n-- > 0 )
				buf.append(c);
		}
	}
	
	// the original invokations of Character.isDigit will return true for eastern arabic etc. However this may break the logic on how it works the "lua way".
	// maybe we could rework the methods to support eastern arabic and other digits.
	protected static boolean isAsciiDigit(char c)
	{
		return c >= '0' && c <= '9';
	}

	// taken from original StringLib but works on java strings
	static class GMatchAux extends VarArgFunction {
		private final int srclen;
		private final MatchState ms;
		private int soffset;
		public GMatchAux(Varargs args, String src, String pat) {
			this.srclen = src.length();
			this.ms = new MatchState(args, src, pat);
			this.soffset = 0;
		}
		public Varargs invoke(Varargs args) {
			for ( ; soffset<srclen; soffset++ ) {
				ms.reset();
				int res = ms.match(soffset, 0);
				if ( res >=0 ) {
					int soff = soffset;
					soffset = res;
					/* DAN200 START */
					if (res == soff) soffset++;
					/* DAN200 END */
					return ms.push_captures( true, soff, res );
				}
			}
			return NIL;
		}
	}
	
	// taken from StringLib but works on java strings
	private static void addquoted(StringBuffer buf, String s) {
		char c;
		buf.append( '"' );
		for ( int i = 0, n = s.length(); i < n; i++ ) {
			switch ( c = s.charAt( i ) ) {
			case '"': case '\\':  case '\n':
				buf.append( '\\' );
				buf.append( c );
				break;
			case '\r':
				buf.append( "\\r" );
				break;
			case '\0':
				buf.append( "\\000" );
				break;
			default:
				/* DAN200 START */
				//buf.append( (byte) c );
				if( (c >= 32 && c <= 126) || c >= 160 ) {
					buf.append( c );
				} else {
					String str = Integer.toString((int) c);
					while( str.length() < 3 ) {
					    str = "0" + str;
					}					
					buf.append( "\\" + str );
				}
				/* DAN200 END */
			break;
			}
		}
		buf.append( (byte) '"' );
	}
	
	private class Gmatch extends VarArgFunction
	{

		private LuaValue m_orig;

		public Gmatch(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("gmatch").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				String src = args.checkjstring( 1 );
				String pat = args.checkjstring( 2 );
				return new GMatchAux(args, src, pat);
			}
			return m_orig.invoke(args);
		}
		
	}
	
	private class Gsub extends VarArgFunction
	{

		private LuaValue m_orig;

		public Gsub(LuaTable t) {
			name = "dump";
			env = t;
			this.m_orig = StringLib.instance.get("gsub").checkfunction();
		}
		
		public Varargs invoke(Varargs args) {
			if (m_env.isUtf()) {
				// mostly taken from original StringLib
				String src = args.checkjstring( 1 );
				final int srclen = src.length();
				String p = args.checkjstring( 2 );
				LuaValue repl = args.arg( 3 );
				int max_s = args.optint( 4, srclen + 1 );
				final boolean anchor = p.length() > 0 && p.charAt( 0 ) == '^';
				
				StringBuffer lbuf = new StringBuffer( srclen );
				MatchState ms = new MatchState( args, src, p );
				
				int soffset = 0;
				int n = 0;
				while ( n < max_s ) {
					ms.reset();
					int res = ms.match( soffset, anchor ? 1 : 0 );
					if ( res != -1 ) {
						n++;
						ms.add_value( lbuf, soffset, res, repl );
					}
					if ( res != -1 && res > soffset )
						soffset = res;
					else if ( soffset < srclen )
						lbuf.append( src.charAt( soffset++ ) );
					else
						break;
					if ( anchor )
						break;
				}
				lbuf.append( src.substring( soffset, srclen ) );
				return varargsOf(LuaString.valueOf(lbuf.toString()), valueOf(n));
			}
			return m_orig.invoke(args);
		}
		
	}

}
