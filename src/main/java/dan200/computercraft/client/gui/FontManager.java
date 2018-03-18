package dan200.computercraft.client.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class FontManager {
	
	private Map<String, FontDefinition> fonts = new HashMap<>();
	
	public static final FontDefinition LEGACY = new FontDefinition(
			"LEGACY", new ResourceLocation( "computercraft", "textures/gui/term_font.png" ),
			9, 6, 256, 16, 256.0, 256.0);
	
	public FontManager()
	{
		this.loadFonts();
	}
	
	public List<FontDefinition> getFonts()
	{
		return new ArrayList<>(fonts.values());
	}
	
	public FontDefinition get(String name)
	{
		return fonts.get(name);
	}
	
	public FontDefinition getLegacy()
	{
		return LEGACY;
	}
	
	private void loadFonts(IMount mount, String name)
	{
		final List<String> list = new ArrayList<>();
		try
		{
			mount.list("/", list);
			list.stream().filter(f -> f.endsWith(".properties")).forEach(f -> {
				final String fname = f.substring(0, f.length()-".properties".length());
				final String png = fname + ".png";
				final Properties props = new Properties();
				try (final InputStream is = mount.openForRead(f))
				{
					props.load(is);
					fonts.put(fname, new FontDefinition(
							fname, new ResourceLocation( "computercraft", "textures/gui/fonts/" + png ),
							Integer.parseInt(props.getProperty("fontHeight")),
							Integer.parseInt(props.getProperty("fontWidth")),
							Integer.parseInt(props.getProperty("maxChars")),
							Integer.parseInt(props.getProperty("charsPerLine")),
							Integer.parseInt(props.getProperty("texWidth")),
							Integer.parseInt(props.getProperty("texHeight"))
							));
				}
				catch (IOException | NullPointerException | NumberFormatException ex)
				{
					ComputerCraft.log.error("Error loading font " + fname + " from " + name, ex);
				}
			});
		}
		catch (IOException ex)
		{
			ComputerCraft.log.error("Error loading fonts from " + name, ex);
		}
	}
	
	private void loadFonts()
	{
		File codeDir = ComputerCraft.getDebugCodeDir( getClass() );
        if( codeDir != null )
        {
            File subResource = new File( codeDir, "assets/computercraft/textures/gui/fonts" );
            if( subResource.exists() )
            {
                IMount resourcePackMount = new FileMount( subResource, 0 );
                loadFonts(resourcePackMount, "dir:"+codeDir);
            }
        }
        
		final File jar = ComputerCraft.getContainingJar(getClass());
		if (jar != null)
		{
			try
			{
				final JarMount jarMount = new JarMount( jar, "assets/computercraft/textures/gui/fonts" );
				loadFonts(jarMount, "jar:"+jar);
			}
			catch (IOException ex)
			{
				ComputerCraft.log.error("Error loading fonts from jar:"+jar, ex);
			}
		}
		
        final File resourcePackDir = new File(Minecraft.getMinecraft().mcDataDir, "resourcepacks");
        if( resourcePackDir.exists() && resourcePackDir.isDirectory() )
        {
            String[] resourcePacks = resourcePackDir.list();
            for( String resourcePack1 : resourcePacks )
            {
                try
                {
                    File resourcePack = new File( resourcePackDir, resourcePack1 );
                    if( !resourcePack.isDirectory() )
                    {
                        // Mount a resource pack from a jar
                        IMount resourcePackMount = new JarMount( resourcePack, "assets/computercraft/textures/gui/fonts" );
                        loadFonts(resourcePackMount, "resourcePack:"+resourcePack);
                    }
                    else
                    {
                        // Mount a resource pack from a folder
                        File subResource = new File( resourcePack, "assets/computercraft/textures/gui/fonts" );
                        if( subResource.exists() )
                        {
                            IMount resourcePackMount = new FileMount( subResource, 0 );
                            loadFonts(resourcePackMount, "resourcePack:"+resourcePack);
                        }
                    }
                }
                catch( IOException e )
                {
                    // Ignore
                }
            }
        }
	}

}
