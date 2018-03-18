package dan200.computercraft.client.gui;

import net.minecraft.util.ResourceLocation;

public class FontDefinition {

	private final ResourceLocation font;
    private final int fontHeight;
    private final int fontWidth;
    private final double texHeight;
    private final double texWidth;
    private final int maxChars;
    private final int charsPerLine;
	private final String name;
    
    public FontDefinition(String name, ResourceLocation font, int fontHeight, int fontWidth, int maxChars, int charsPerLine, double texWidth, double texHeight) {
		this.name = name;
    	this.font = font;
		this.fontHeight = fontHeight;
		this.fontWidth = fontWidth;
		this.maxChars = maxChars;
		this.charsPerLine = charsPerLine;
		this.texWidth = texWidth;
		this.texHeight = texHeight;
	}
    
    public String name()
    {
    	return this.name;
    }

	public ResourceLocation font()
    {
    	return this.font;
    }
    
	public int fontHeight()
    {
    	return this.fontHeight;
    }
    
	public int fontWidth()
    {
    	return this.fontWidth;
    }
    
	public int maxChars()
    {
    	return this.maxChars;
    }
    
	public int charsPerLine()
    {
    	return this.charsPerLine;
    }
    
	public double texWidth()
    {
    	return texWidth;
    }
    
	public double texHeight()
    {
    	return texHeight;
    }

}
