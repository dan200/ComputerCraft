package dan200.computercraft.shared.computer.core;

import javax.annotation.Nullable;

/**
 * An instance of {@link net.minecraft.inventory.Container} which provides a computer. You should implement this
 * if you provide custom computers/GUIs to interact with them.
 */
public interface IContainerComputer
{
    /**
     * Get the computer you are interacting with.
     *
     * This will only be called on the server.
     *
     * @return The computer you are interacting with.
     */
    @Nullable
    IComputer getComputer();
}
