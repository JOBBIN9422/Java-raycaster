package jaubin.raycasting;

import java.awt.Color;

public class WallRenderSlice {
	public int startY;
	public int endY;
	public int x;
	//public Color color;
	
	public WallRenderSlice(int x, int startY, int endY) {
		this.x = x;
		this.startY = startY;
		this.endY = endY;
		//this.color = color;
	}
}
