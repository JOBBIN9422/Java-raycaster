package jaubin.raycasting;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import org.ejml.simple.SimpleMatrix;

public class Entity {
	protected SimpleMatrix positionVec;
	protected HashMap<String, BufferedImage> sprites;
	protected BufferedImage currSprite;
	protected double playerDistance;
	
	public Entity(double x, double y, String spritesPath) {
		this.positionVec = new SimpleMatrix(2, 1);
		this.positionVec.set(0, 0, x);
		this.positionVec.set(1, 0, y);
		
		this.playerDistance = 0.0;
		
		this.sprites = new HashMap<String, BufferedImage>();
		
		//iterate all of the files in the given sprite folder
		File spritesDir = new File(spritesPath);
		File[] spritesDirListing = spritesDir.listFiles();
		
		if (spritesDirListing != null) {
			for (File spriteFile : spritesDirListing) {
				//load the current image and add it to the sprite map
				if (!this.sprites.containsKey(spriteFile.getName())) {
					BufferedImage spriteImg = null;
					try {
						spriteImg = ImageIO.read(spriteFile);
						BufferedImage spriteImgARGB = new BufferedImage(spriteImg.getWidth(), spriteImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = spriteImgARGB.createGraphics();
						g.drawImage(spriteImg, 0, 0, spriteImg.getWidth(), spriteImg.getHeight(), null);
						g.dispose();
						this.sprites.put(spriteFile.getName(), spriteImgARGB);
					}
					catch (IOException e) {
						
					}
				}
			}
		}
		
		//set the current sprite to the "first" element in the sprite map
		if (this.sprites.size() > 0) {
			this.currSprite = this.sprites.get(this.sprites.keySet().stream().findFirst().get());
		}
	}
	
	public SimpleMatrix getPosition() {
		return this.positionVec;
	}
	
	public BufferedImage getCurrSprite() {
		return this.currSprite;
	}
	
	public double getPlayerDistance() {
		return this.playerDistance;
	}
	
	public void setPlayerDistance(double dist) {
		this.playerDistance = dist;
	}
}
