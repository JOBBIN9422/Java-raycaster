package jaubin.raycasting;

import java.awt.Color;

public class SolidWallRenderSlice extends WallRenderSlice {
	public Color color;
	
	public SolidWallRenderSlice(int x, int startY, int endY, Color color) {
		super(x, startY, endY);
		this.color = color;
	}
	
}
