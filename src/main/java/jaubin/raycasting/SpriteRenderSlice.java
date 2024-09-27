package jaubin.raycasting;

import java.awt.image.BufferedImage;

public class SpriteRenderSlice extends TexturedWallRenderSlice {
	public boolean isVisible;
	public SpriteRenderSlice(int x, int startY, int endY, int textureX, BufferedImage texImg, boolean isVisible) {
		super(x, startY, endY, textureX, texImg);
		this.isVisible = isVisible;
	}

}
