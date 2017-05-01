package dan200.computercraft.shared.media.common;

import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.media.IMediaProvider;
import dan200.computercraft.shared.media.items.RecordMedia;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;

public class DefaultMediaProvider implements IMediaProvider
{
    public DefaultMediaProvider()
    {
    }

    @Override
    public IMedia getMedia( ItemStack stack )
    {
        Item item = stack.getItem();
        if( item instanceof IMedia )
        {
            return (IMedia)item;
        }
        else if( item instanceof ItemRecord )
        {
            return new RecordMedia();
        }
        return null;
    }
}
