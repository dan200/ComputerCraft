package dan200.computercraft.core.filesystem;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.shared.computer.core.IComputer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class WorldFSWrapper implements ILuaObject {

    // Chars to create the Key
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String m_channelname;
    private String m_channelkey;
    private String m_path;
    private FileSystem m_fs;
    private IComputerEnvironment m_computerEnvironment;

    public WorldFSWrapper(String label, String channel, FileSystem fs, IComputerEnvironment computerEnvironment) {
        m_channelname = channel;
        m_channelkey = nameToKey(channel);
        m_path = label;
        m_computerEnvironment = computerEnvironment;
        m_fs = fs;
    }

    public void mount() throws FileSystemException{
        m_fs.mountWritable(m_path, m_path, getWritableMount());
    }

    public void unmount() {
        m_fs.unmount(m_path);
    }

    public String getPath() {
        return m_path;
    }

    public IWritableMount getWritableMount() {
        return m_computerEnvironment.createSaveDirMount("computer/worldfs/"+m_channelkey, ComputerCraft.worldfsLimit);
    }

    private static String nameToKey(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] keyChars = CHARS.toCharArray();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes(StandardCharsets.UTF_8));

            for (byte b : hash) {
                stringBuilder.append(
                        keyChars[ Math.abs( b % keyChars.length )]
                );
            }
        } catch (NoSuchAlgorithmException no) {
            // Then we have some other problems...
            no.printStackTrace();
        }

        return stringBuilder.toString();
    }

    @Override
    public String[] getMethodNames() {
        return new String[]{
                "getChannel",
                "getPath",
                "getKey"
        };
    }

    @Override
    public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: // getChannel
                return new Object[]{m_channelname};
            case 1: // getPath
                return new Object[]{m_path};
            case 2: // getKey
                return new Object[]{m_channelkey};
        }
        return null;
    }
}
