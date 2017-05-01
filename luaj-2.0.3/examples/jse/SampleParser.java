/** 
 * Sample luaj program that uses the LuaParser class for parsing, and intercepts the 
 * generated ParseExceptions and fills in the file, line and column information where 
 * the exception occurred.
 */
import java.io.*;

import org.luaj.vm2.ast.*;
import org.luaj.vm2.ast.Exp.AnonFuncDef;
import org.luaj.vm2.ast.Stat.FuncDef;
import org.luaj.vm2.ast.Stat.LocalFuncDef;
import org.luaj.vm2.parser.*;


public class SampleParser {
	
	static public void main(String[] args) {
		if (args.length == 0) {
			System.out.println("usage: SampleParser luafile");
			return;
		}
		try {
			final String file = args[0];
			
			// Create a LuaParser. This will fill in line and column number 
			// information for most exceptions.
			LuaParser parser = new LuaParser(new FileInputStream(file));
			
			// Perform the parsing.
			Chunk chunk = parser.Chunk();
			
			// Print out names found in this source file.
			chunk.accept( new Visitor() {
				public void visit(Exp.NameExp exp) {
					System.out.println("Name in use: "+exp.name.name);
				}
			} );
			
		} catch ( ParseException e ) {
			System.out.println("parse failed: " + e.getMessage() + "\n"
					+ "Token Image: '" + e.currentToken.image + "'\n"
					+ "Location: " + e.currentToken.beginLine + ":" + e.currentToken.beginColumn 
					        + "-" + e.currentToken.endLine + "," + e.currentToken.endColumn);
		} catch ( IOException e ) {
			System.out.println( "IOException occurred: "+e );
			e.printStackTrace();
		}
	}
}