/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import java.io.IOException;

public interface IMountedFileNormal extends IMountedFile {
	public String readLine() throws IOException;
	public void write(String s, int off, int len, boolean newLine) throws IOException;
	public void close() throws IOException;
	public void flush() throws IOException;
}
