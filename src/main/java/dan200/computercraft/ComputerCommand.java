package dan200.computercraft;

import dan200.computercraft.core.computer.Computer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComputerCommand implements ICommand {
    private final ArrayList<String> aliases;

    ComputerCommand() {
        aliases = new ArrayList<String>();
        aliases.add( "computer" );
    }

    @Override
    public String getCommandName() {
        return "computer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "computer <id> <value>";
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        for( Computer computer : Computer.computers ){
            if( computer.getID() == Integer.valueOf( args[0] ) && computer.isOn() ){
                computer.queueEvent( "computer_command", ArrayUtils.remove( args, 0 ) );
                sender.addChatMessage( new TextComponentString( "Success" ) );
                return;
            }
        }
        sender.addChatMessage( new TextComponentString( "Could not find computer #" + args[0] ) );
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return new ArrayList<String>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
