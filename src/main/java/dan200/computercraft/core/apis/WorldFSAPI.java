package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.filesystem.FileSystemException;
import dan200.computercraft.core.filesystem.WorldFSWrapper;
import static dan200.computercraft.core.apis.ArgumentHelper.getString;

import java.util.HashMap;
import java.util.Map;

public class WorldFSAPI implements ILuaAPI {

    private Map<String, WorldFSWrapper> mounted_wfs;
    private IAPIEnvironment m_api_enviroment;

    public WorldFSAPI(IAPIEnvironment iapiEnvironment) {
        m_api_enviroment = iapiEnvironment;
        mounted_wfs = new HashMap<>();
    }

    private void mount(String label, String channel) throws LuaException{
        if (!mounted_wfs.containsKey(channel)) {
            WorldFSWrapper worldFSWrapper = new WorldFSWrapper(label, channel, m_api_enviroment.getFileSystem(), m_api_enviroment.getComputerEnvironment());
            mounted_wfs.put(channel, worldFSWrapper);
            try {
                worldFSWrapper.mount();
            } catch (FileSystemException fse) {
                throw new LuaException("Error on mounting: "+fse.getMessage());
            }
        }
        else {
            throw new LuaException(
                    String.format("Error worldfs %s is already mounted under %s", channel, mounted_wfs.get(channel).getPath())
            );
        }
    }

    private void unmount(String channel) throws LuaException {
        if (mounted_wfs.containsKey(channel)) {
            mounted_wfs.get(channel).unmount();
            mounted_wfs.remove(channel);
        }
        else {
            throw new LuaException(
                    String.format("%s is not mounted", channel)
            );
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{
                "worldfs"
        };
    }

    @Override
    public void startup() {
        mounted_wfs.clear();
    }

    @Override
    public void advance(double _dt) {

    }

    @Override
    public void shutdown() {
        for (WorldFSWrapper mount : mounted_wfs.values()) {
            mount.unmount();
        }
        mounted_wfs.clear();
    }

    @Override
    public String[] getMethodNames() {
        return new String[]{
                "mount",
                "unmount",
                "list"
        };
    }

    @Override
    public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: // worldfs.mount(string, string)
                String channel = getString(arguments, 0);
                String label = getString(arguments, 1);

                mount(label, channel);
                break;
            case 1: // worldfs.unmount(string)
                String channel2 = getString(arguments, 0);

                unmount(channel2);
                break;
            case 2: // worldfs.list()
                Map<Object,Object> list = new HashMap<>();
                int i=1;
                for (WorldFSWrapper wrapper : mounted_wfs.values()) {
                    list.put(i++, wrapper);
                }
                return new Object[]{list};
        }

        return null;
    }
}
