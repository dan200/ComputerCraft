/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */


package dan200.computercraft.client.gui.widgets;

import dan200.computercraft.client.gui.widgets.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;

public class WidgetContainer extends Widget
{
	private ArrayList<Widget> m_widgets;
	private Widget m_modalWidget;

	public WidgetContainer( int x, int y, int w, int h )
	{
		super( x, y, w, h );
		m_widgets = new ArrayList<>();
		m_modalWidget = null;
	}

	public void addWidget( Widget widget )
	{
		m_widgets.add( widget );
		widget.setParent( this );
	}

	public void setModalWidget( Widget widget )
	{
		m_modalWidget = widget;
		if( widget != null )
		{
			widget.setParent( this );
		}
	}

	public Widget getModalWidget()
	{
		return m_modalWidget;
	}

	@Override
	public void update()
	{
        for( Widget m_widget : m_widgets )
        {
            m_widget.update();
        }
		if( m_modalWidget != null )
		{
			m_modalWidget.update();
		}
	}

	@Override
	public void draw( Minecraft mc, int xOrigin, int yOrigin, int mouseX, int mouseY )
	{
        for( Widget widget : m_widgets )
        {
            if( widget.isVisible() )
            {
                widget.draw(
                    mc,
                    xOrigin + getXPosition(),
                    yOrigin + getYPosition(),
                    (m_modalWidget == null) ? (mouseX - getXPosition()) : -99,
                    (m_modalWidget == null) ? (mouseY - getYPosition()) : -99
                );
            }
        }
		if( m_modalWidget != null )
		{
			if( m_modalWidget.isVisible() )
			{
				GlStateManager.pushMatrix();
				try
				{
					GlStateManager.translate( 0.0f, 0.0f, 200.0f );
					m_modalWidget.draw(
							mc,
							xOrigin + getXPosition(),
							yOrigin + getYPosition(),
							mouseX - getXPosition(),
							mouseY - getYPosition()
					);
				}
				finally
				{
					GlStateManager.popMatrix();
				}
			}
		}
	}

	@Override
	public void drawForeground( Minecraft mc, int xOrigin, int yOrigin, int mouseX, int mouseY )
	{
        for( Widget widget : m_widgets )
        {
            if( widget.isVisible() )
            {
                widget.drawForeground(
                    mc,
                    xOrigin + getXPosition(),
                    yOrigin + getYPosition(),
                    (m_modalWidget == null) ? (mouseX - getXPosition()) : -99,
                    (m_modalWidget == null) ? (mouseY - getYPosition()) : -99
                );
            }
        }

		if( m_modalWidget != null )
		{
			if( m_modalWidget.isVisible() )
			{
				GlStateManager.pushMatrix();
				try
				{
					GlStateManager.translate( 0.0f, 0.0f, 200.0f );
					m_modalWidget.drawForeground(
							mc,
							xOrigin + getXPosition(),
							yOrigin + getYPosition(),
							mouseX - getXPosition(),
							mouseY - getYPosition()
					);
				}
				finally
				{
					GlStateManager.popMatrix();
				}
			}
		}
	}

	@Override
	public void modifyMousePosition( MousePos pos )
	{
		pos.x -= getXPosition();
		pos.y -= getYPosition();
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    widget.modifyMousePosition( pos );
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() )
			{
				m_modalWidget.modifyMousePosition( pos );
			}
		}
		pos.x += getXPosition();
		pos.y += getYPosition();
	}

	@Override
	public boolean suppressItemTooltips( Minecraft mc, int xOrigin, int yOrigin, int mouseX, int mouseY )
	{
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    if( widget.suppressItemTooltips(
                        mc,
                        xOrigin + getXPosition(),
                        yOrigin + getYPosition(),
                        mouseX - getXPosition(),
                        mouseY - getYPosition()
                    ) )
                    {
                        return true;
                    }
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() && m_modalWidget.suppressItemTooltips(
					mc,
					xOrigin + getXPosition(),
					yOrigin + getYPosition(),
					mouseX - getXPosition(),
					mouseY - getYPosition()
			) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
    public boolean suppressKeyPress( char c, int k )
	{
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    if( widget.suppressKeyPress( c, k ) )
                    {
                        return true;
                    }
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() )
			{
				if( m_modalWidget.suppressKeyPress( c, k ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void handleMouseInput( int mouseX, int mouseY )
	{
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    widget.handleMouseInput(
                        mouseX - getXPosition(),
                        mouseY - getYPosition()
                    );
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() )
			{
				m_modalWidget.handleMouseInput(
						mouseX - getXPosition(),
						mouseY - getYPosition()
				);
			}
		}
	}

	@Override
	public void handleKeyboardInput()
	{
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    widget.handleKeyboardInput();
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() )
			{
				m_modalWidget.handleKeyboardInput();
			}
		}
	}

	@Override
	public void mouseClicked( int mouseX, int mouseY, int mouseButton )
	{
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    widget.mouseClicked(
                        mouseX - getXPosition(),
                        mouseY - getYPosition(),
                        mouseButton
                    );
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() )
			{
				m_modalWidget.mouseClicked(
						mouseX - getXPosition(),
						mouseY - getYPosition(),
						mouseButton
				);
			}
		}
	}

	@Override
	public void keyTyped( char c, int k )
	{
		if( m_modalWidget == null )
		{
            for( Widget widget : m_widgets )
            {
                if( widget.isVisible() )
                {
                    widget.keyTyped( c, k );
                }
            }
		}
		else
		{
			if( m_modalWidget.isVisible() )
			{
				m_modalWidget.keyTyped( c, k );
			}
		}
	}
}
