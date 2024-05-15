/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.lua2java;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Block;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.ast.Exp;
import org.luaj.vm2.ast.FuncArgs;
import org.luaj.vm2.ast.FuncBody;
import org.luaj.vm2.ast.Name;
import org.luaj.vm2.ast.NameResolver;
import org.luaj.vm2.ast.Variable;
import org.luaj.vm2.ast.ParList;
import org.luaj.vm2.ast.Stat;
import org.luaj.vm2.ast.TableConstructor;
import org.luaj.vm2.ast.TableField;
import org.luaj.vm2.ast.Visitor;
import org.luaj.vm2.ast.Exp.AnonFuncDef;
import org.luaj.vm2.ast.Exp.BinopExp;
import org.luaj.vm2.ast.Exp.Constant;
import org.luaj.vm2.ast.Exp.FieldExp;
import org.luaj.vm2.ast.Exp.FuncCall;
import org.luaj.vm2.ast.Exp.IndexExp;
import org.luaj.vm2.ast.Exp.MethodCall;
import org.luaj.vm2.ast.Exp.NameExp;
import org.luaj.vm2.ast.Exp.ParensExp;
import org.luaj.vm2.ast.Exp.UnopExp;
import org.luaj.vm2.ast.Exp.VarExp;
import org.luaj.vm2.ast.Exp.VarargsExp;
import org.luaj.vm2.ast.Stat.Assign;
import org.luaj.vm2.ast.Stat.Break;
import org.luaj.vm2.ast.Stat.FuncCallStat;
import org.luaj.vm2.ast.Stat.FuncDef;
import org.luaj.vm2.ast.Stat.GenericFor;
import org.luaj.vm2.ast.Stat.IfThenElse;
import org.luaj.vm2.ast.Stat.LocalAssign;
import org.luaj.vm2.ast.Stat.LocalFuncDef;
import org.luaj.vm2.ast.Stat.NumericFor;
import org.luaj.vm2.ast.Stat.RepeatUntil;
import org.luaj.vm2.ast.Stat.Return;
import org.luaj.vm2.ast.Stat.WhileDo;

public class JavaCodeGen {

	final Chunk chunk;
	final String packagename;
	final String classname;
	Writer writer;

	public JavaCodeGen( Chunk chunk, Writer writer,String packagename, String classname) {
		this.chunk = chunk;
		this.writer = writer;
		this.packagename = packagename;
		this.classname = classname;
		chunk.accept( new NameResolver() );
		chunk.accept( new JavaClassWriterVisitor() );
	}

	class JavaClassWriterVisitor extends Visitor {

		JavaScope javascope = null;
		List<String> constantDeclarations = new ArrayList<String>();
		Map<LuaString,String> stringConstants = new HashMap<LuaString,String>();
		Map<Double,String> numberConstants = new HashMap<Double,String>();
		
		
		String indent = "";
		void addindent() {
			indent+="   ";
		}
		void subindent() {
			indent = indent.substring(3);
		}
		void out(String s) {
			try { 
				writer.write(s); 
			} catch (IOException e) { 
				throw new RuntimeException("write failed: "+e, e); 
			} 
		}
		void outi(String s) {
			out( indent );
			out( s );
		}
		void outl(String s) {
			outi( s );
			out( "\n" );
		}
		void outr(String s) {
			out( s );
			out( "\n" );
		}
		void outb(String s) {
			outl( s );
			addindent();
		}
		void oute(String s) {
			subindent();
			outl( s );
		}
		
		public void visit(Chunk chunk) {
			if ( packagename != null )
				outl("package "+packagename+";");
			outl("import org.luaj.vm2.*;");
			outl("import org.luaj.vm2.lib.*;");
			outb("public class "+classname+" extends VarArgFunction {");
			outl("public Varargs onInvoke(Varargs $arg) {");
			addindent();
			javascope = JavaScope.newJavaScope( chunk );
			writeBodyBlock(chunk.block);
			oute("}");
			for ( int i=0, n=constantDeclarations.size(); i<n; i++ )
				outl( (String) constantDeclarations.get(i) );
			subindent();
			outi("}");
		}

		void writeBodyBlock(Block block) {
			if ( javascope.needsbinoptmp )
				outl( "LuaValue $b;" );
			super.visit(block);
			if ( ! endsInReturn(block) )
				outl( "return NONE;" );
		}
		
		public void visit(Block block) {
			outb("{");
			super.visit(block);
			oute("}");
		}

		private boolean endsInReturn(Block block) {
			int n = block.stats.size();
			if ( n<=0 ) return false;
			Stat s = (Stat) block.stats.get(n-1);
			if ( s instanceof Return || s instanceof Break ) 
				return true;
			else if ( isInfiniteLoop( s ) )
				return true;
			else if ( s instanceof IfThenElse ) {
				IfThenElse ite = (IfThenElse) s;
				if ( ite.elseblock == null || ! endsInReturn(ite.ifblock) || ! endsInReturn(ite.elseblock) )
					return false;
				if ( ite.elseifblocks != null )
					for ( int i=0, nb=ite.elseifblocks.size(); i<nb; i++ )
						if ( ! endsInReturn((Block) ite.elseifblocks.get(i)) )
							return false;
				return true;
			}
			return false;
		}
		
		private boolean isInfiniteLoop(Stat s) {
			if ( s instanceof WhileDo && "true".equals(evalBoolean(((WhileDo)s).exp)) )
				return true;
			if ( s instanceof RepeatUntil && "false".equals(evalBoolean(((RepeatUntil)s).exp)) )
				return true;
			return false;
		}
		
		
		public void visit(Stat.Return s) {
			int n = s.nreturns();
			switch ( n ) {
			case 0: 
				outl( "return NONE;" ); 
				break;
			case 1: 
				outl( "return "+evalLuaValue((Exp) s.values.get(0))+";" ); 
				break;
			default: 
				if ( s.values.size()==1 && ((Exp) s.values.get(0)).isfunccall() )
					tailCall( (Exp) s.values.get(0) );
				else
					outl( "return "+evalListAsVarargs(s.values)+";" ); 
				break;
			}
		}

		public void visit(AnonFuncDef def) {
			super.visit(def);
		}

		public void visit(Assign stat) {
			multiAssign(stat.vars, stat.exps);
		}
		
		public void visit(LocalAssign stat) {
			List<Name> names = stat.names;
			List<Exp> values = stat.values;
			int n = names.size();
			int m = values != null? values.size(): 0;
			boolean isvarlist = m>0 && m<n && ((Exp) values.get(m-1)).isvarargexp();
			for ( int i=0; i<n && i<(isvarlist? m-1: m); i++ )
				if ( ! ((Name) names.get(i)).variable.isConstant() )
					singleLocalDeclareAssign((Name) names.get(i), evalLuaValue((Exp) values.get(i)));
			if ( isvarlist ) {
				String t = javascope.getJavaName(tmpJavaVar("t").variable);
				outl( "final Varargs "+t+" = "+evalVarargs((Exp) values.get(m-1))+";" );
				for ( int i=m-1; i<n; i++ )
					singleLocalDeclareAssign((Name) names.get(i), t+(i==m-1? ".arg1()": ".arg("+(i-m+2)+")"));
				
			} else {
				for ( int i=m; i<n; i++ ) {
					if ( ! ((Name) names.get(i)).variable.isConstant() )
						singleLocalDeclareAssign((Name) names.get(i), "NIL");
				}
			}
			for ( int i=n; i<m; i++ ) {
				String t = javascope.getJavaName(tmpJavaVar("t").variable);
				outl( "final Varargs "+t+" = "+evalVarargs((Exp) values.get(i)) );
			}
		}
		
		private void multiAssign(final List varsOrNames, List<Exp> exps) {
			final boolean[] needsTmpvarsMultiAssign = { false };
			if ( exps.size() > 1 ) {
				new Visitor() {
					public void visit(FuncBody body) {}
					public void visit(FieldExp exp) { needsTmpvarsMultiAssign[0] = true; }
					public void visit(FuncCall exp) { needsTmpvarsMultiAssign[0] = true; }
					public void visit(IndexExp exp) { needsTmpvarsMultiAssign[0] = true; }
					public void visit(MethodCall exp) { needsTmpvarsMultiAssign[0] = true; }
					public void visit(NameExp exp) { needsTmpvarsMultiAssign[0] = true; }
				}.visitExps(exps);
			}
			if ( needsTmpvarsMultiAssign[0] )
				tmpvarsMultiAssign( varsOrNames, exps );
			else
				directMultiAssign( varsOrNames, exps );
		}
	
		private void directMultiAssign(List varsOrNames, List<Exp> values) {
			int n = varsOrNames.size();
			int m = values != null? values.size(): 0;
			boolean isvarlist = m>0 && m<n && ((Exp) values.get(m-1)).isvarargexp();
			for ( int i=0; i<n && i<(isvarlist? m-1: m); i++ )
					singleVarOrNameAssign(varsOrNames.get(i), evalLuaValue((Exp) values.get(i)));
			if ( isvarlist ) {
				String vname = javascope.getJavaName(tmpJavaVar("v").variable);
				outl( "final Varargs "+vname+" = "+evalVarargs((Exp) values.get(m-1))+";" );
				for ( int i=m-1; i<n; i++ )
					singleVarOrNameAssign(varsOrNames.get(i), vname+(i==m-1? ".arg1()": ".arg("+(i-m+2)+")"));
				
			} else
				for ( int i=m; i<n; i++ )
					singleVarOrNameAssign(varsOrNames.get(i), "NIL");
			for ( int i=n; i<m; i++ ) {
				String tmp = javascope.getJavaName(tmpJavaVar("tmp").variable);
				outl( "final Varargs "+tmp+" = "+evalVarargs((Exp) values.get(i)) );
			}
		}
			
		private void tmpvarsMultiAssign(List varsOrNames, List<Exp> exps) {
			int n = varsOrNames.size();
			int m = exps != null? exps.size(): 0;
			boolean isvarlist = m>0 && m<n && ((Exp) exps.get(m-1)).isvarargexp();
			List<String> tmpnames = new ArrayList<String>();
			for ( int i=0; i<m; i++ ) {
				tmpnames.add( javascope.getJavaName(tmpJavaVar("t").variable) );
				if ( isvarlist && (i==m-1) )
					outl( "final Varargs "+tmpnames.get(i)+" = "+evalVarargs((Exp) exps.get(i))+";" );
				else
					outl( "final LuaValue "+tmpnames.get(i)+" = "+evalLuaValue((Exp) exps.get(i))+";" );
			}
			for ( int i=0; i<n; i++ ) {
				if ( i < (isvarlist? m-1: m) )
					singleVarOrNameAssign( varsOrNames.get(i), (String) tmpnames.get(i) );
				else if ( isvarlist ) 
					singleVarOrNameAssign( varsOrNames.get(i), tmpnames.get(m-1)+(i==m-1? ".arg1()": ".arg("+(i-m+2)+")") );
				else 
					singleVarOrNameAssign( varsOrNames.get(i), "NIL" );
			}
		}
		
		private void singleVarOrNameAssign(final Object varOrName, final String valu) {
			Visitor v = new Visitor() {
				public void visit(FieldExp exp) {
					outl(evalLuaValue(exp.lhs)+".set("+evalStringConstant(exp.name.name)+","+valu+");");
				}
				public void visit(IndexExp exp) {
					outl(evalLuaValue(exp.lhs)+".set("+evalLuaValue(exp.exp)+","+valu+");");
				}
				public void visit(NameExp exp) {
					singleAssign( exp.name, valu );
				}
			};
			if ( varOrName instanceof VarExp )
				((VarExp)varOrName).accept(v);
			else if ( varOrName instanceof Name )
				singleAssign((Name) varOrName, valu);
			else
				throw new IllegalStateException("can't assign to "+varOrName.getClass());
		}
		
		private void singleAssign(Name name, String valu) {
			if ( name.variable.isLocal() ) {
				if ( name.variable.isConstant() )
					return;
				outi( "" );
				singleReference( name );
				outr( " = "+valu+";" );
			} else
				outl( "env.set("+evalStringConstant(name.name)+","+valu+");");
		}
		
		private void singleReference(Name name) {
			if ( name.variable.isLocal() ) {
				if ( name.variable.isConstant() ) {
					out( evalConstant(name.variable.initialValue) );
					return;
				}
				out( javascope.getJavaName(name.variable) );
				if ( name.variable.isupvalue && name.variable.hasassignments )
					out( "[0]" );
			} else {
				out( "env.get("+evalStringConstant(name.name)+")");
			}
		}
		
		private void singleLocalDeclareAssign(Name name, String value) {
			singleLocalDeclareAssign( name.variable, value );
		}
		
		private void singleLocalDeclareAssign(Variable variable, String value) {
			String javaname = javascope.getJavaName(variable);
			if ( variable.isConstant() )
				return;
			else if ( variable.isupvalue && variable.hasassignments )
				outl( "final LuaValue[] "+javaname+" = {"+value+"};" );
			else if ( variable.isupvalue )
				outl( "final LuaValue "+javaname+(value!=null? " = "+value: "")+";" );
			else
				outl( (variable.hasassignments? "LuaValue ": "final LuaValue ")+javaname+(value!=null? " = "+value: "")+";" );
		}
		
		public void visit(Break breakstat) {
			// TODO: wrap in do {} while(false), or add label as nec
			outl( "break;" );
		}

		private Writer pushWriter() {
			Writer x = writer;
			writer = new CharArrayWriter();
			return x;
		}
		
		private String popWriter(Writer x) {
			Writer c = writer;
			writer = x;
			return c.toString();
		}
		
		public String evalListAsVarargs(List<Exp> values) {
			int n = values!=null? values.size(): 0;
			switch ( n ) {
			case 0: return "NONE";
			case 1: return evalVarargs((Exp) values.get(0));
			default: 
			case 2: case 3:
				Writer x = pushWriter();
				out( n>3? "varargsOf(new LuaValue[] {":"varargsOf(" );
				for ( int i=1; i<n; i++ )
					out( evalLuaValue((Exp) values.get(i-1))+"," );
				if ( n>3 )
					out( "}," );
				out( evalVarargs((Exp) values.get(n-1))+")" );
				return popWriter(x);
			}
		}
		
		Map<Exp,Integer> callerExpects = new HashMap<Exp,Integer>();
		
		public String evalLuaValue(Exp exp) {
			Writer x = pushWriter();
			callerExpects.put(exp,Integer.valueOf(1));
			exp.accept(this);
			return popWriter(x);
		}
		
		public String evalVarargs(Exp exp) {
			Writer x = pushWriter();
			callerExpects.put(exp,Integer.valueOf(-1));
			exp.accept(this);
			return popWriter(x);
		}
		
		public String evalBoolean(Exp exp) {
			Writer x = pushWriter();
			exp.accept(new Visitor() {
				public void visit(UnopExp exp) {
					switch ( exp.op ) {
					case Lua.OP_NOT: 
						String rhs = evalBoolean( exp.rhs );
						out( "true".equals(rhs)? "false":
							"false".equals(rhs)? "true":
							"(!"+rhs+")"); 
						break;
					default: out(evalLuaValue(exp)+".toboolean()"); break;
					}
				}
				public void visit(BinopExp exp) {
					switch ( exp.op ) {
					case Lua.OP_AND: out("("+evalBoolean(exp.lhs)+"&&"+evalBoolean(exp.rhs)+")"); return;
					case Lua.OP_OR: out("("+evalBoolean(exp.lhs)+"||"+evalBoolean(exp.rhs)+")"); return;
					case Lua.OP_GT: out(evalLuaValue(exp.lhs)+".gt_b("+evalLuaValue(exp.rhs)+")"); return;
					case Lua.OP_GE: out(evalLuaValue(exp.lhs)+".gteq_b("+evalLuaValue(exp.rhs)+")"); return;
					case Lua.OP_LT: out(evalLuaValue(exp.lhs)+".lt_b("+evalLuaValue(exp.rhs)+")"); return;
					case Lua.OP_LE: out(evalLuaValue(exp.lhs)+".lteq_b("+evalLuaValue(exp.rhs)+")"); return;
					case Lua.OP_EQ: out(evalLuaValue(exp.lhs)+".eq_b("+evalLuaValue(exp.rhs)+")"); return;
					case Lua.OP_NEQ: out(evalLuaValue(exp.lhs)+".neq_b("+evalLuaValue(exp.rhs)+")"); return;
					default: out(evalLuaValue(exp)+".toboolean()"); return;
					}
				}
				public void visit(Constant exp) {
					switch ( exp.value.type() ) {
					case LuaValue.TBOOLEAN:
						out(exp.value.toboolean()? "true": "false");
						break;
					default:
						out(evalLuaValue(exp)+".toboolean()");
						break;
					}
				}
				public void visit(ParensExp exp) {
					out(evalBoolean(exp.exp));
				}
				public void visit(VarargsExp exp) {
					out(evalLuaValue(exp)+".toboolean()");
				}
				public void visit(FieldExp exp) {
					out(evalLuaValue(exp)+".toboolean()");
				}
				public void visit(IndexExp exp) {
					out(evalLuaValue(exp)+".toboolean()");
				}
				public void visit(NameExp exp) {
					if ( exp.name.variable.isConstant() ) {
						out ( exp.name.variable.initialValue.toboolean()? "true": "false");
						return;
					}
					out(evalLuaValue(exp)+".toboolean()");
				}
				public void visit(FuncCall exp) {
					out(evalLuaValue(exp)+".toboolean()");
				}
				public void visit(MethodCall exp) {
					out(evalLuaValue(exp)+".toboolean()");
				}
				public void visit(TableConstructor exp) {
					out(evalLuaValue(exp)+".toboolean()");
				}
			});
			return popWriter(x);
		}
		
		public String evalNumber(Exp exp) {
			Writer x = pushWriter();
			exp.accept(new Visitor() {
				public void visit(UnopExp exp) {
					switch ( exp.op ) {
					case Lua.OP_LEN: out(evalLuaValue(exp.rhs)+".length()"); break;
					case Lua.OP_UNM: out("(-"+evalNumber(exp.rhs)+")"); break;
					default: out(evalLuaValue(exp)+".checkdouble()"); break;
					}
				}
				public void visit(BinopExp exp) {
					String op;
					switch ( exp.op ) {
					case Lua.OP_ADD:
					case Lua.OP_SUB:
					case Lua.OP_MUL:
						op = (exp.op==Lua.OP_ADD? "+": exp.op==Lua.OP_SUB? "-": "*"); 
						out("("+evalNumber(exp.lhs)+op+evalNumber(exp.rhs)+")");
						break;
					case Lua.OP_POW: out("MathLib.dpow_d("+evalNumber(exp.lhs)+","+evalNumber(exp.rhs)+")"); break;
					case Lua.OP_DIV: out("LuaDouble.ddiv_d("+evalNumber(exp.lhs)+","+evalNumber(exp.rhs)+")"); break;
					case Lua.OP_MOD: out("LuaDouble.dmod_d("+evalNumber(exp.lhs)+","+evalNumber(exp.rhs)+")"); break;
					default: out(evalLuaValue(exp)+".checkdouble()"); break;
					}
				}
				public void visit(Constant exp) {
					switch ( exp.value.type() ) {
					case LuaValue.TNUMBER:
						out( evalNumberLiteral(exp.value.checkdouble()) );
						break;
					default:
						out(evalLuaValue(exp)+".checkdouble()");
						break;
					}
				}
				public void visit(ParensExp exp) {
					out(evalNumber(exp.exp));
				}
				public void visit(VarargsExp exp) {
					out(evalLuaValue(exp)+".checkdouble()");
				}
				public void visit(FieldExp exp) {
					out(evalLuaValue(exp)+".checkdouble()");
				}
				public void visit(IndexExp exp) {
					out(evalLuaValue(exp)+".checkdouble()");
				}
				public void visit(NameExp exp) {
					if ( exp.name.variable.isConstant() ) {
						if ( exp.name.variable.initialValue.isnumber() ) {
							out( evalNumberLiteral(exp.name.variable.initialValue.checkdouble()) );
							return;
						}
					}
					out(evalLuaValue(exp)+".checkdouble()");
				}
				public void visit(FuncCall exp) {
					out(evalLuaValue(exp)+".checkdouble()");
				}
				public void visit(MethodCall exp) {
					out(evalLuaValue(exp)+".checkdouble()");
				}
				public void visit(TableConstructor exp) {
					out(evalLuaValue(exp)+".checkdouble()");
				}
			});
			return popWriter(x);
		}
		
		public void visit(FuncCallStat stat) {
			outi("");
			stat.funccall.accept(this);
			outr(";");
		}
		
		public void visit(BinopExp exp) {
			switch ( exp.op ) {
			case Lua.OP_AND:
			case Lua.OP_OR:
				String not = (exp.op==Lua.OP_AND? "!": ""); 
				out("("+not+"($b="+evalLuaValue(exp.lhs)+").toboolean()?$b:"+evalLuaValue(exp.rhs)+")");
				return;				
			}
			switch ( exp.op ) {
			case Lua.OP_ADD: out("valueOf("+evalNumber(exp.lhs)+"+"+evalNumber(exp.rhs)+")"); return;
			case Lua.OP_SUB: out("valueOf("+evalNumber(exp.lhs)+"-"+evalNumber(exp.rhs)+")"); return;
			case Lua.OP_MUL: out("valueOf("+evalNumber(exp.lhs)+"*"+evalNumber(exp.rhs)+")"); return;
			case Lua.OP_POW: out("MathLib.dpow("+evalNumber(exp.lhs)+","+evalNumber(exp.rhs)+")"); return;
			case Lua.OP_DIV: out("LuaDouble.ddiv("+evalNumber(exp.lhs)+","+evalNumber(exp.rhs)+")"); return;
			case Lua.OP_MOD: out("LuaDouble.dmod("+evalNumber(exp.lhs)+","+evalNumber(exp.rhs)+")"); return;
			case Lua.OP_GT: out(evalLuaValue(exp.lhs)+".gt("+evalLuaValue(exp.rhs)+")"); return;
			case Lua.OP_GE: out(evalLuaValue(exp.lhs)+".gteq("+evalLuaValue(exp.rhs)+")"); return;
			case Lua.OP_LT: out(evalLuaValue(exp.lhs)+".lt("+evalLuaValue(exp.rhs)+")"); return;
			case Lua.OP_LE: out(evalLuaValue(exp.lhs)+".lteq("+evalLuaValue(exp.rhs)+")"); return;
			case Lua.OP_EQ: out(evalLuaValue(exp.lhs)+".eq("+evalLuaValue(exp.rhs)+")"); return;
			case Lua.OP_NEQ: out(evalLuaValue(exp.lhs)+".neq("+evalLuaValue(exp.rhs)+")"); return;
			case Lua.OP_CONCAT: 
				if ( isConcatExp(exp.rhs) ) {
					out( evalLuaValue(exp.lhs) );
					Exp e = exp.rhs;
					String close = "";
					for ( ; isConcatExp(e); e=((BinopExp)e).rhs ) { 
						out( ".concat("+evalLuaValue(((BinopExp)e).lhs) );
						close += ')';
					}
					out( ".concat("+evalLuaValue(e)+".buffer())" );
					out( close );
					out( ".value()" );
				} else {
					out(evalLuaValue(exp.lhs)+".concat("+evalLuaValue(exp.rhs)+")");
				}
				return;
			default: throw new IllegalStateException("unknown bin op:"+exp.op);
			}
		}

		private boolean isConcatExp(Exp e) {
			return (e instanceof BinopExp) && (((BinopExp)e).op == Lua.OP_CONCAT);
		}
		
		public void visit(UnopExp exp) {
			exp.rhs.accept(this);
			switch ( exp.op ) {
			case Lua.OP_NOT: out(".not()"); break;
			case Lua.OP_LEN: out(".len()"); break;
			case Lua.OP_UNM: out(".neg()"); break;
			}
		}

		public void visit(Constant exp) {
			out( evalConstant(exp.value) );
		}

		protected String evalConstant(LuaValue value) {
			switch ( value.type() ) {
			case LuaValue.TSTRING:
				return evalLuaStringConstant(value.checkstring());
			case LuaValue.TNIL:
				return "NIL";
			case LuaValue.TBOOLEAN:
				return value.toboolean()? "TRUE": "FALSE";
			case LuaValue.TNUMBER:
				return evalNumberConstant(value.todouble());
			default:
				throw new IllegalStateException("unknown constant type: "+value.typename());
			}
		}

		private String evalStringConstant(String str) {
			return evalLuaStringConstant( LuaValue.valueOf(str) );
		}
		
		private String evalLuaStringConstant(LuaString str) {
			if ( stringConstants.containsKey(str) )
				return (String) stringConstants.get(str);
			String declvalue = quotedStringInitializer(str);
			String javaname = javascope.createConstantName(str.tojstring());
			constantDeclarations.add( "static final LuaValue "+javaname+" = valueOf("+declvalue+");" );
			stringConstants.put(str,javaname);
			return javaname;
		}
		
		private String evalNumberConstant(double value) {
			if ( value == 0 ) return "ZERO";
			if ( value == -1 ) return "MINUSONE";
			if ( value == 1 ) return "ONE";
			if ( numberConstants.containsKey(Double.valueOf(value)) )
				return (String) numberConstants.get(Double.valueOf(value));
			String declvalue = evalNumberLiteral(value);
			String javaname = javascope.createConstantName(declvalue);
			constantDeclarations.add( "static final LuaValue "+javaname+" = valueOf("+declvalue+");" );
			numberConstants.put(Double.valueOf(value),javaname);
			return javaname;
		}
		
		private String evalNumberLiteral(double value) {
			int ivalue = (int) value;
			String svalue = value==ivalue? String.valueOf(ivalue): String.valueOf(value);
			return (value < 0? "("+svalue+")": svalue);
		}
		
		public void visit(FieldExp exp) {
			exp.lhs.accept(this);
			out(".get("+evalStringConstant(exp.name.name)+")");
		}

		public void visit(IndexExp exp) {
			exp.lhs.accept(this);
			out(".get(");
			exp.exp.accept(this);
			out(")");
		}

		public void visit(NameExp exp) {
			singleReference( exp.name );
		}

		public void visit(ParensExp exp) {
			if ( exp.exp.isvarargexp() )
				out( evalLuaValue(exp.exp) );
			else
				exp.exp.accept(this);
		}

		public void visit(VarargsExp exp) {
			int c = callerExpects.containsKey(exp)? ((Integer)callerExpects.get(exp)).intValue(): 0;
			out( c==1? "$arg.arg1()": "$arg" );
		}

		public void visit(MethodCall exp) {
			List<Exp> e = exp.args.exps;
			int n = e != null? e.size(): 0;
			int c = callerExpects.containsKey(exp)? ((Integer)callerExpects.get(exp)).intValue(): 0;
			if ( c == -1 )
				n = -1;
			out( evalLuaValue(exp.lhs) );
			switch ( n ) {
			case 0: 
				out(".method("+evalStringConstant(exp.name)+")"); 
				break;
			case 1: case 2: 
				out(".method("+evalStringConstant(exp.name)+",");
				exp.args.accept(this);
				out(")");
				break;
			default:			
				out(".invokemethod("+evalStringConstant(exp.name)
						+((e==null||e.size()==0)? "": ","+evalListAsVarargs(exp.args.exps))+")");
				if ( c == 1 )
					out(".arg1()");
				break;
			}
		}
		
		public void visit(FuncCall exp) {
			List<Exp> e = exp.args.exps;
			int n = e != null? e.size(): 0;
			if ( n > 0 && ((Exp)e.get(n-1)).isvarargexp() )
				n = -1;
			int c = callerExpects.containsKey(exp)? ((Integer)callerExpects.get(exp)).intValue(): 0;
			if ( c == -1 )
				n = -1;
			out( evalLuaValue(exp.lhs) );
			switch ( n ) {
			case 0: case 1: case 2: case 3: 
				out(".call(");
				exp.args.accept(this);
				out(")");
				break;
			default:
				out(".invoke("+((e==null||e.size()==0)? "": evalListAsVarargs(e))+")");
				if ( c == 1 )
					out(".arg1()");
				break;
			}
		}

		public void tailCall( Exp e ) {
			if ( e instanceof MethodCall ) {
				MethodCall mc = (MethodCall) e;
				outl("return new TailcallVarargs("+evalLuaValue(mc.lhs)+","+evalStringConstant(mc.name)+","+evalListAsVarargs(mc.args.exps)+");");
			} else if ( e instanceof FuncCall ) {
				FuncCall fc = (FuncCall) e;
				outl("return new TailcallVarargs("+evalLuaValue(fc.lhs)+","+evalListAsVarargs(fc.args.exps)+");");
			} else {
				throw new IllegalArgumentException("can't tail call "+e);
			}
		}
		
		public void visit(FuncArgs args) {
			if ( args.exps != null ) {
				int n = args.exps.size();
				if ( n > 0 ) {
					for ( int i=1; i<n; i++ )
						out( evalLuaValue( (Exp) args.exps.get(i-1) )+"," );
					out( evalVarargs( (Exp) args.exps.get(n-1) ) );
				}
			}
		}

		public void visit(FuncBody body) {
			javascope = javascope.pushJavaScope(body);
			int n = javascope.nreturns;
			int m = body.parlist.names!=null? body.parlist.names.size(): 0;
			if ( n>=0 && n<=1 && m<=3 && ! body.parlist.isvararg ) {
				switch ( m ) {
				case 0: 
					outr("new ZeroArgFunction(env) {");
					addindent();
					outb("public LuaValue call() {");
					break;
				case 1: 
					outr("new OneArgFunction(env) {");
					addindent();
					outb("public LuaValue call("
							+declareArg((Name) body.parlist.names.get(0))+") {");
					assignArg((Name) body.parlist.names.get(0));
					break;
				case 2: 
					outr("new TwoArgFunction(env) {");
					addindent();
					outb("public LuaValue call("
							+declareArg((Name) body.parlist.names.get(0))+","
							+declareArg((Name) body.parlist.names.get(1))+") {");
					assignArg((Name) body.parlist.names.get(0));
					assignArg((Name) body.parlist.names.get(1));
					break;
				case 3: 
					outr("new ThreeArgFunction(env) {");
					addindent();
					outb("public LuaValue call("
							+declareArg((Name) body.parlist.names.get(0))+","
							+declareArg((Name) body.parlist.names.get(1))+","
							+declareArg((Name) body.parlist.names.get(2))+") {");
					assignArg((Name) body.parlist.names.get(0));
					assignArg((Name) body.parlist.names.get(1));
					assignArg((Name) body.parlist.names.get(2));
					break;
				}
			} else {
				outr("new VarArgFunction(env) {");
				addindent();
				outb("public Varargs invoke(Varargs $arg) {");
				for ( int i=0; i<m; i++ ) {
					Name name = (Name) body.parlist.names.get(i);
					String value = i>0? "$arg.arg("+(i+1)+")": "$arg.arg1()";
					singleLocalDeclareAssign( name, value );
				}
				if ( body.parlist.isvararg ) {
					Variable arg = body.scope.find("arg");
					javascope.setJavaName(arg,"arg");
					if ( m > 0 ) 
						outl( "$arg = $arg.subargs("+(m+1)+");" );
					String value = (javascope.usesvarargs? "NIL": "LuaValue.tableOf($arg,1)");
					singleLocalDeclareAssign( arg, value );
				}
			}
			writeBodyBlock(body.block);
			oute("}");
			subindent();
			outi("}");
			javascope = javascope.popJavaScope();
		}

		private String declareArg(Name name) {
			String argname = javascope.getJavaName(name.variable);
			return "LuaValue "+argname+(name.variable.isupvalue? "$0": "");
		}
		
		private void assignArg(Name name) {
			if ( name.variable.isupvalue ) {
				String argname = javascope.getJavaName(name.variable);
				singleLocalDeclareAssign(name, argname+"$0");
			}
		}
		
		public void visit(FuncDef stat) {
			Writer x = pushWriter();
			stat.body.accept(this);
			String value = popWriter(x);
			int n = stat.name.dots!=null? stat.name.dots.size(): 0;
			boolean m = stat.name.method != null;
			if ( n>0 && !m && stat.name.name.variable.isLocal() ) 
				singleAssign( stat.name.name, value );
			else if ( n==0 && !m ) {
				singleAssign( stat.name.name, value );
			} else {
				singleReference( stat.name.name );
				for ( int i=0; i<n-1 || (m&&i<n); i++ )
					out( ".get("+evalStringConstant((String) stat.name.dots.get(i))+")" );
				outr( ".set("+evalStringConstant(m? (String)stat.name.method: (String)stat.name.dots.get(n))+", "+value+");" );
			}
		}

		// functions that use themselves as upvalues require special treatment
		public void visit(LocalFuncDef stat) {
			final Name funcname = stat.name;
			final boolean[] isrecursive = { false };
			stat.body.accept( new Visitor() {
				public void visit(Name name) {
					if ( name.variable == funcname.variable ) {
						isrecursive[0] = true;
						name.variable.hasassignments = true;
					}
				}				
			} );
			
			// write body
			Writer x = pushWriter();
			super.visit(stat);
			String value = popWriter(x);

			// write declaration
			if ( isrecursive[0] ) {
				String javaname = javascope.getJavaName(funcname.variable);
				outl("final LuaValue[] "+javaname+" = new LuaValue[1];");
				outl(javaname+"[0] = "+value+";");
			} else {
				singleLocalDeclareAssign( funcname, value );
			}
		}

		public void visit(NumericFor stat) {
			String j = javascope.getJavaName(stat.name.variable);
			String i = j+"$0";
			outi("for ( double "
					+i+"="+evalLuaValue(stat.initial)+".checkdouble(), "
					+j+"$limit="+evalLuaValue(stat.limit)+".checkdouble()");
			if ( stat.step == null )
				outr( "; "+i+"<="+j+"$limit; ++"+i+" ) {" );
			else {
				out( ", "+j+"$step="+evalLuaValue(stat.step)+".checkdouble()");
				out( "; "+j+"$step>0? ("+i+"<="+j+"$limit): ("+i+">="+j+"$limit);" );
				outr( " "+i+"+="+j+"$step ) {" );
			}
			addindent();
			singleLocalDeclareAssign(stat.name, "valueOf("+i+")");
			super.visit(stat.block);
			oute( "}" );
		}

		private Name tmpJavaVar(String s) {
			Name n = new Name(s);
			n.variable = javascope.define(s);
			return n;
		}
		
		public void visit(GenericFor stat) {
			Name f = tmpJavaVar("f");
			Name s = tmpJavaVar("s");
			Name var = tmpJavaVar("var");
			Name v = tmpJavaVar("v");
			String javaf = javascope.getJavaName(f.variable);
			String javas = javascope.getJavaName(s.variable);
			String javavar = javascope.getJavaName(var.variable);
			String javav = javascope.getJavaName(v.variable);
			outl("LuaValue "+javaf+","+javas+","+javavar+";");
			outl("Varargs "+javav+";");
			List<Name> fsvar = new ArrayList<Name>();
			fsvar.add(f);
			fsvar.add(s);
			fsvar.add(var);
			multiAssign(fsvar, stat.exps);
			
			outb("while (true) {");
			outl( javav+" = "+javaf+".invoke(varargsOf("+javas+","+javavar+"));");
			outl( "if (("+javavar+"="+javav+".arg1()).isnil()) break;");
			singleLocalDeclareAssign((Name) stat.names.get(0),javavar);
			for ( int i=1, n=stat.names.size(); i<n; i++ )
				singleLocalDeclareAssign((Name) stat.names.get(i),javav+".arg("+(i+1)+")");
			super.visit(stat.block);
			oute("}");
		}

		public void visit(ParList pars) {
			super.visit(pars);
		}

		public void visit(IfThenElse stat) {
			outb( "if ( "+evalBoolean(stat.ifexp)+" ) {");
			super.visit(stat.ifblock);
			if ( stat.elseifblocks != null ) 
				for ( int i=0, n=stat.elseifblocks.size(); i<n; i++ ) {
					subindent();
					outl( "} else if ( "+evalBoolean((Exp) stat.elseifexps.get(i))+" ) {");
					addindent();
					super.visit((Block) stat.elseifblocks.get(i));
				}
			if ( stat.elseblock != null ) {
				subindent();
				outl( "} else {");
				addindent();
				super.visit( stat.elseblock );
			}
			oute( "}" );
		}

		public void visit(RepeatUntil stat) {
			outb( "do {");
			super.visit(stat.block);
			oute( "} while (!"+evalBoolean(stat.exp)+");" );
		}
		
		public void visit(TableConstructor table) {
			int n = table.fields!=null? table.fields.size(): 0;
			List<TableField> keyed = new ArrayList<TableField>();
			List<TableField> list = new ArrayList<TableField>();
			for ( int i=0; i<n; i++ ) {
				TableField f = (TableField) table.fields.get(i);
				(( f.name != null || f.index != null )? keyed: list).add(f);
			}
			int nk = keyed.size();
			int nl = list.size();
			out( (nk==0 && nl!=0)? "LuaValue.listOf(": "LuaValue.tableOf(" );
			
			// named elements
			if ( nk != 0 ) {
				out( "new LuaValue[]{");
				for ( int i=0, nf=keyed.size(); i<nf; i++ ) {
					TableField f = (TableField) keyed.get(i);
					if ( f.name != null )
						out( evalStringConstant(f.name)+"," );
					else
						out( evalLuaValue(f.index)+"," );
					out( evalLuaValue(f.rhs)+"," );
				}
				out( "}" );
			}
			
			// unnamed elements
			if ( nl != 0 ) {
				out( (nk!=0? ",": "") + "new LuaValue[]{" );
				Exp last = ((TableField)list.get(nl-1)).rhs;
				boolean vlist = last.isvarargexp();
				for ( int i=0, limit=vlist? nl-1: nl; i<limit ; i++ )
					out( evalLuaValue( ((TableField)list.get(i)).rhs )+"," );
				out( vlist? "}, "+evalVarargs(last): "}");
			}
			out( ")" );
		}

		public void visit(WhileDo stat) {
			outb( "while ("+evalBoolean(stat.exp)+") {");
			super.visit(stat.block);
			oute( "}" );
		}

		public void visitExps(List<Exp> exps) {
			super.visitExps(exps);
		}

		public void visitNames(List<Name> names) {
			super.visitNames(names);
		}

		public void visitVars(List<VarExp> vars) {
			super.visitVars(vars);
		}		
	}

	private static String quotedStringInitializer(LuaString s) {
		byte[] bytes = s.m_bytes;
		int o = s.m_offset;
		int n = s.m_length;
		StringBuffer sb = new StringBuffer(n+2);		
		
		// check for bytes not encodable as utf8
		if ( ! s.isValidUtf8() ) {
			sb.append( "new byte[]{" );
			for ( int j=0; j<n; j++ ) {
				if ( j>0 ) sb.append(",");
				byte b = bytes[o+j];
				switch ( b ) {
					case '\n': sb.append( "'\\n'" ); break; 
					case '\r': sb.append( "'\\r'" ); break; 
					case '\t': sb.append( "'\\t'" ); break; 
					case '\\': sb.append( "'\\\\'" ); break;
					default:
						if ( b >= ' ' ) {
							sb.append( '\'');
							sb.append( (char) b );
							sb.append( '\'');
						} else {
							sb.append( String.valueOf((int)b) );
						}
					break;
				}					
			}
			sb.append( "}" );
			return sb.toString();
		}

		sb.append('"');
		for ( int i=0; i<n; i++ ) {
			byte b = bytes[o+i];
			switch ( b ) {
				case '\b': sb.append( "\\b" ); break; 
				case '\f': sb.append( "\\f" ); break; 
				case '\n': sb.append( "\\n" ); break; 
				case '\r': sb.append( "\\r" ); break; 
				case '\t': sb.append( "\\t" ); break;
				case '"':  sb.append( "\\\"" ); break;
				case '\\': sb.append( "\\\\" ); break;
				default:
					if ( b >= ' ' ) {
						sb.append( (char) b ); break;
					} else {
						// convert from UTF-8
						int u = 0xff & (int) b;
						if ( u>=0xc0 && i+1<n ) {
							if ( u>=0xe0 && i+2<n ) {
								u = ((u & 0xf) << 12) | ((0x3f & bytes[i+1]) << 6) | (0x3f & bytes[i+2]);
								i+= 2;
							} else {
								u = ((u & 0x1f) << 6) | (0x3f & bytes[++i]);
							}
						}
						sb.append( "\\u" );
						sb.append( Integer.toHexString(0x10000+u).substring(1) );
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}
	
}
