/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import java.io.IOException;

public interface IMountedFileBinary extends IMountedFile {
    public int read() throws IOException;
    public void write(int i) throws IOException;
    public void close() throws IOException;
    public void flush() throws IOException;
}
