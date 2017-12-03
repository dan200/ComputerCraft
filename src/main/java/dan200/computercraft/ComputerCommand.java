package dan200.computercraft;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.ArrayUtils;

public class ComputerCommand extends CommandBase {

    @Override
    public String getName() {
        return "computer";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "computer <id> <value1> [value2]...";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if( args.length < 2 ){
            throw new CommandException( "Usage: /computer <id> <value1> [value2]..." );
        }
        int id;
        try {
            id = Integer.valueOf(args[0]);
        }catch( NumberFormatException e ){
            throw new CommandException( "Invalid ID" );
        }
        boolean found_valid_computer = false;
        for( ServerComputer computer : ComputerCraft.serverComputerRegistry.getComputers() ){
            if( computer.getID() == id && computer.getFamily() == ComputerFamily.Command ){
                computer.queueEvent("computer_command", ArrayUtils.remove(args, 0));
                found_valid_computer = true;
            }
        }
        if( !found_valid_computer ){
            throw new CommandException( "Computer #" + args[0] + " is not a Command Computer" );
        }
    }

    @Override
    public int getRequiredPermissionLevel(){
        return 0;
    }
}
