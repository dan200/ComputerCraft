/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import java.io.IOException;

public interface IMountedFileBinary extends IMountedFile {
    int read() throws IOException;
    void write( int i ) throws IOException;
    void close() throws IOException;
    void flush() throws IOException;
}
