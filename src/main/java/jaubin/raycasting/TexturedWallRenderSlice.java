package jaubin.raycasting;

import java.awt.image.BufferedImage;

public class TexturedWallRenderSlice extends WallRenderSlice {
	public BufferedImage textureImage;
	public int textureX;
	public float brightness;
	
	public TexturedWallRenderSlice(int x, int startY, int endY, int textureX, BufferedImage texImg) {
		super(x, startY, endY);
		this.textureX = textureX;
		this.textureImage = texImg;
	}

}
