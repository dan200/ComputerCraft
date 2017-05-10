/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import java.io.IOException;

public interface IMountedFileNormal extends IMountedFile {
    String readLine() throws IOException;
    void write( String s, int off, int len, boolean newLine ) throws IOException;
    void close() throws IOException;
    void flush() throws IOException;
}
