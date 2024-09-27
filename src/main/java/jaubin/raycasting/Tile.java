package jaubin.raycasting;

import java.awt.Color;

public class Tile {
	private TileType tileType;
	private String wallTextureFilename;
	private String floorTextureFilename;
	private String ceilTextureFilename;
	
	public static final Color WALL_COLOR = new Color(128, 128, 128);
	
	public Tile(TileType type) {
		this.tileType = type;
		this.wallTextureFilename = "";
		this.floorTextureFilename = "";
		this.ceilTextureFilename = "";
	}
	public Tile(TileType type, String wallTextureFilename) {
		this(type);
		this.wallTextureFilename = wallTextureFilename;
		this.floorTextureFilename = "";
		this.ceilTextureFilename = "";
	}
	public Tile(TileType type, String wallTextureFilename, String floorTextureFilename, String ceilTextureFilename) {
		this(type, wallTextureFilename);
		this.floorTextureFilename = floorTextureFilename;
		this.ceilTextureFilename = ceilTextureFilename;
	}
	
	public TileType getTileType() {
		return this.tileType;
	}
	
	public String getWallTextureFilename() {
		return this.wallTextureFilename;
	}
	public String getFloorTextureFilename() {
		return this.floorTextureFilename;
	}
	public String getCeilingTextureFilename() {
		return this.ceilTextureFilename;
	}
	
	public void setTileType(TileType type) {
		this.tileType = type;
	}
	
	public void setWallTextureFilename(String filename) {
		this.wallTextureFilename = filename;
	}
	public void setFloorTextureFilename(String filename) {
		this.floorTextureFilename = filename;
	}
	public void setCeilingTextureFilename(String filename) {
		this.ceilTextureFilename = filename;
	}
}
