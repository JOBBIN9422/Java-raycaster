package jaubin.raycasting;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

public class WorldMap {
	private ArrayList<ArrayList<Tile>> mapData;
	private HashMap<String, BufferedImage> textureData;
	private Tile moveTile;
	private Random rand;
	private ArrayList<Entity> entityData;
	
	public WorldMap(int width, int height) {
		//build a map of the specified dimensions (initially filled w/ empty tiles)
		this.textureData = new HashMap<String, BufferedImage>();
		this.mapData = new ArrayList<ArrayList<Tile>>();
		this.entityData = new ArrayList<Entity>();
		
		for (int y = 0; y < height; y++) {
			this.mapData.add(new ArrayList<Tile>());
			
			for (int x = 0; x < width; x++) {
				Tile currTile = new Tile(TileType.EMPTY);
				currTile.setCeilingTextureFilename(Paths.get("textures", "colorstone.png").toString());
				currTile.setFloorTextureFilename(Paths.get("textures", "wood.png").toString());
				this.mapData.get(y).add(currTile);
			}
		}
		
		//add top and bottom perimeter walls
		for (int x = 0; x < width; x++) {
			this.mapData.get(0).get(x).setTileType(TileType.WALL);
			this.mapData.get(0).get(x).setWallTextureFilename(Paths.get("textures", "bluestone.png").toString());
			this.mapData.get(height - 1).get(x).setTileType(TileType.WALL);
			this.mapData.get(height - 1).get(x).setWallTextureFilename(Paths.get("textures", "redbrick.png").toString());
		}
		
		//add left and right perimeter walls
		for (int y = 0; y < height; y++) {
			this.mapData.get(y).get(0).setTileType(TileType.WALL);
			this.mapData.get(y).get(0).setWallTextureFilename(Paths.get("textures", "wood.png").toString());
			this.mapData.get(y).get(width - 1).setTileType(TileType.WALL);
			this.mapData.get(y).get(width - 1).setWallTextureFilename(Paths.get("textures", "colorstone.png").toString());
		}
		
		//add center obstacle
		this.mapData.get(height / 2).get(width / 2).setTileType(TileType.WALL);
		this.mapData.get(height / 2).get(width / 2).setWallTextureFilename(Paths.get("textures", "eagle.png").toString());
		
		 this.rand = new Random();
		int randX = this.rand.nextInt(width);
		int randY = this.rand.nextInt(height);
		this.moveTile = this.mapData.get(randY).get(randX);
		this.mapData.get(randY).get(randX).setTileType(TileType.WALL);
		this.mapData.get(randY).get(randX).setWallTextureFilename(Paths.get("textures", "ricardo.jpg").toString());
		
		this.loadTextures();
		this.loadEntities();
	}
	
	public void doTestMove() {
		this.moveTile.setTileType(TileType.EMPTY);
		this.moveTile.setWallTextureFilename("");
		
		int randX = this.rand.nextInt(this.getMapWidth());
		int randY = this.rand.nextInt(this.getMapHeight());
		this.moveTile = this.mapData.get(randY).get(randX);
		this.mapData.get(randY).get(randX).setTileType(TileType.WALL);
		this.mapData.get(randY).get(randX).setWallTextureFilename(Paths.get("textures", "ricardo.jpg").toString());
	}
	
	private void loadTextures() {
		//Path currentRelativePath = Paths.get("");
		//String s = currentRelativePath.toAbsolutePath().toString();
		//System.out.println("Current relative path is: " + s);
		for (int y = 0; y < this.mapData.size(); y++) {
			for (int x = 0; x < this.mapData.get(y).size(); x++) {
				Tile currTile = this.mapData.get(y).get(x);
				this.tryLoadTexture(currTile.getWallTextureFilename());
				this.tryLoadTexture(currTile.getFloorTextureFilename());
				this.tryLoadTexture(currTile.getCeilingTextureFilename());
			}
		}
		
		//int fuckYou = 0;
	}
	
	private void loadEntities() {
		File[] spriteDirs = new File(Paths.get("sprites").toString()).listFiles();
		for (File spriteDir : spriteDirs) {
			if (spriteDir.isDirectory()) {
				int randX = this.rand.nextInt(this.getMapWidth() - 1);
				int randY = this.rand.nextInt(this.getMapHeight() - 1);
				
				for (int i = 0; i < 5; i++) {
					this.entityData.add(new Entity(randX, randY, spriteDir.getAbsolutePath()));
					randX = this.rand.nextInt(this.getMapWidth() - 1);
					randY = this.rand.nextInt(this.getMapHeight() - 1);
				}
			}
		}
	}
	
	private void tryLoadTexture(String filename) {
		if (filename != null && !filename.isEmpty()) {
			//if the current texture file hasn't been loaded, load it 
			if (!this.textureData.containsKey(filename)) {
				BufferedImage texImage = null;
				try {
					texImage = ImageIO.read(new File(filename));
					this.textureData.put(filename, texImage);
				}
				catch (IOException e) {
					
				}
			}
		}
	}
	
	public ArrayList<ArrayList<Tile>> getMapData() {
		return this.mapData;
	}
	
	public HashMap<String, BufferedImage> getTextureData() {
		return this.textureData;
	}
	public ArrayList<Entity> getEntityData() {
		return this.entityData;
	}
	
	public boolean isObstacle(int x, int y) {
		TileType type = this.mapData.get(y).get(x).getTileType();
		return type == TileType.WALL || type == TileType.DOOR;
	}
	
	public int getMapHeight() {
		return mapData.size();
	}
	public int getMapWidth() {
		return mapData.get(0).size();
	}
}
