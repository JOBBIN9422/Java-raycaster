package jaubin.raycasting;

import java.awt.image.BufferedImage;

public class FloorCeilRenderSlice extends HorizontalRenderSlice {
	
	public BufferedImage textureImage;
	public int textureY;
	
	public FloorCeilRenderSlice(int y, int startX, int endX, int textureY, BufferedImage texImg) {
		super(y, startX, endX);
		this.textureY = textureY;
		this.textureImage = texImg;
	}
}
