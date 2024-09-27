package jaubin.raycasting;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.ejml.simple.SimpleMatrix;

public class Engine {
	private Camera player;
	private WorldMap worldMap;
	private Renderer renderer;
	private ControlHandler controlHandler;

	private RenderThread renderThread;
	
	private TexturedWallRenderSlice[] columnBuffer;
	private ArrayList<ArrayList<SpriteRenderSlice>> spriteBuffer;
	private FloorCeilRenderSlice[] floorCeilBuffer;
	
	private double[] zBuffer;
	
	private double currFrameTimestamp;
	private double prevFrameTimestamp;
	private double elapsedFrameTime;
	private double currFPS;
	private double lastFPSUpdate;
	
	private boolean isRunning;
	
	public static final long NANOSECONDS_PER_SECOND = 1000000000;
	public static final int TARGET_FPS = 60;
	public static final double OPTIMAL_FRAME_TIME = (double)NANOSECONDS_PER_SECOND / (double)TARGET_FPS;
	
	public Engine() {
		this.player = new Camera(2, 2);
		this.worldMap = new WorldMap(30, 20);
		this.renderer = new Renderer();
		this.controlHandler = new ControlHandler(this.player);
		this.renderThread = new RenderThread(this);
		
		this.columnBuffer = new TexturedWallRenderSlice[Renderer.WIDTH];
		//this.spriteBuffer = new SpriteRenderSlice[Renderer.WIDTH];
		this.floorCeilBuffer = new FloorCeilRenderSlice[Renderer.HEIGHT];
		
		this.resetSpriteBuffer();
		
		this.zBuffer = new double[Renderer.WIDTH];
		
		this.currFrameTimestamp = 0.0;
		this.prevFrameTimestamp = 0.0;
		this.elapsedFrameTime = 0.0;
		this.currFPS = 0.0;
		this.lastFPSUpdate = 0.0;
		
		this.isRunning = true;
		
		this.renderer.getFrame().addKeyListener(this.controlHandler);
		//this.renderThread.start();
		//this.renderer.getFrame().requestFocus();
		//this.renderer.getFrame().setAlwaysOnTop(true);
	}
	
	public boolean gameIsRunning() {
		return this.isRunning;
	}
	
	public void resetSpriteBuffer() {
		this.spriteBuffer = new ArrayList<ArrayList<SpriteRenderSlice>>(Renderer.WIDTH);
		for (int i = 0; i < Renderer.WIDTH; i++) {
			this.spriteBuffer.add(new ArrayList<SpriteRenderSlice>());
		}
	}
	
	public void doRendering() {
		//this.castFloorsAndCeilings();
		this.castWalls();
		this.castEntities();
		this.renderer.drawFloorAndCeiling(new Color(0, 156, 104), new Color(96, 218, 230));
		this.renderer.drawWallTextures(this.columnBuffer);
		this.renderer.drawSprites(this.spriteBuffer);
		this.renderer.drawFPSInfo(this.currFPS, this.elapsedFrameTime / 1000000.0);
		this.renderer.update();
		this.renderer.clear();
		this.resetSpriteBuffer();
	}
	
	public void doGameLoop() {
		//frame timing
		this.prevFrameTimestamp = this.currFrameTimestamp;
		this.currFrameTimestamp = (double)System.nanoTime();
		this.elapsedFrameTime = this.currFrameTimestamp - this.prevFrameTimestamp;
		this.currFPS = NANOSECONDS_PER_SECOND / this.elapsedFrameTime;
		
		
		//update player speed
		double playerSpeedModifier = this.elapsedFrameTime / OPTIMAL_FRAME_TIME;
		//System.out.println(playerSpeedModifier);
		this.player.setMoveSpeed(playerSpeedModifier / 10.0);
		this.player.setTurnSpeed(playerSpeedModifier / 20.0);
				
		//read player input
		this.handlePlayerMovement();
				
		//render 
		this.doRendering();
		
		//update FPS display every ~1 second
		if (this.currFrameTimestamp - this.lastFPSUpdate > NANOSECONDS_PER_SECOND) {
			System.out.println(this.currFPS);
			this.lastFPSUpdate = this.currFrameTimestamp;
		}
		
		//limit FPS to target value
		if (this.elapsedFrameTime < OPTIMAL_FRAME_TIME) {
			try {
				long sleepTimeMs = (long)((OPTIMAL_FRAME_TIME - this.elapsedFrameTime) / 1000000);
				TimeUnit.MILLISECONDS.sleep(sleepTimeMs);
				//Thread.sleep(sleepTimeMs);
			} catch (InterruptedException e) {
				//System.out.println("sleep interrupt");
				e.printStackTrace();
			}
		}
	}
	
	private void handlePlayerMovement() {
		SimpleMatrix requestedMove = new SimpleMatrix(2, 1);
		if (player.isMovingForwards()) {
			requestedMove = this.player.GetNextMovePos(Direction.FORWARDS);
			if (!worldMap.isObstacle((int)requestedMove.get(0, 0), (int)requestedMove.get(1, 0))) {
				this.player.move(Direction.FORWARDS);
			}
		}
		if (this.player.isMovingBackwards()) {
			requestedMove = this.player.GetNextMovePos(Direction.BACKWARDS);
			if (!worldMap.isObstacle((int)requestedMove.get(0, 0), (int)requestedMove.get(1, 0))) {
				this.player.move(Direction.BACKWARDS);
			}
		}
		if (this.player.isStrafingLeft()) {
			requestedMove = this.player.GetNextMovePos(Direction.LEFT);
			if (!worldMap.isObstacle((int)requestedMove.get(0, 0), (int)requestedMove.get(1, 0))) {
				this.player.strafe(Direction.LEFT);
			}
		}
		if (this.player.isStrafingRight()) {
			requestedMove = this.player.GetNextMovePos(Direction.RIGHT);
			if (!worldMap.isObstacle((int)requestedMove.get(0, 0), (int)requestedMove.get(1, 0))) {
				this.player.strafe(Direction.RIGHT);
			}
		}
		if (this.player.isTurningLeft()) {
			this.player.turn(Direction.LEFT);
		}
		if (this.player.isTurningRight()) {
			this.player.turn(Direction.RIGHT);
		}
	}
	
	private void castFloorsAndCeilings() {
		//define boundaries for drawing floor/ceiling slices
		SimpleMatrix leftMostRay  = this.player.getHeading().minus(this.player.getCamPlane());
		SimpleMatrix rightMostRay = this.player.getHeading().plus(this.player.getCamPlane());
		
		//cast floors/ceilings horizontally across the viewport
		for (int y = Renderer.HEIGHT / 2 + 1; y < Renderer.HEIGHT; y++) {
			int vertDistFromCam = y - Renderer.HEIGHT / 2;
			double camHeight = Renderer.HEIGHT / 2;
			
			//horizontal distance from the cam to the floor intersection for the current screen row
			double rowDistHorizontal = camHeight / vertDistFromCam;
			
			//determine vector increment (amount/dir) to step along the current row in game-world space 
			SimpleMatrix rowStepVec = rightMostRay.minus(leftMostRay).scale(rowDistHorizontal).scale(1 / (double)Renderer.WIDTH);
			
			//real-world coordinates of the left edge of the current screen row 
			//SimpleMatrix floorPosVec = leftMostRay.scale(rowDistHorizontal).plus(this.player.getPosition());
			SimpleMatrix floorPosVec = this.player.getPosition().plus(leftMostRay.scale(rowDistHorizontal));

			//load and scale the texture for the current row
			for (int x = 0; x < Renderer.WIDTH; x++) {
				//the coordinates of the current map cell
				int currCellX = (int)floorPosVec.get(0, 0);
				int currCellY = (int)floorPosVec.get(1, 0);
				//Tile currTile = this.worldMap.getMapData().get(currCellY).get(currCellX);
				
				BufferedImage floorTexture = null;
				BufferedImage ceilTexture = null;
				floorTexture = this.worldMap.getTextureData().get(Paths.get("textures", "wood.png").toString());
				ceilTexture = this.worldMap.getTextureData().get(Paths.get("textures", "colorstone.png").toString());
				//BufferedImage ceilTexture = this.worldMap.getTextureData().get(currTile.getCeilingTextureFilename());
				
				//determine what row of the texture to draw
				int floorTextureY = (int)(floorTexture.getHeight() * floorPosVec.get(1, 0) - currCellY) & (floorTexture.getHeight() - 1);
				int ceilTextureY = (int)(ceilTexture.getHeight() * floorPosVec.get(1, 0) - currCellY) & (ceilTexture.getHeight() - 1);
				
				//step along the current floor/ceiling row 
				floorPosVec = floorPosVec.plus(rowStepVec);
				
				//add floor texture to buffer
				this.floorCeilBuffer[y] = new FloorCeilRenderSlice(y, 0, Renderer.WIDTH, floorTextureY, floorTexture);
				
				//add ceiling texture to buffer
				this.floorCeilBuffer[Renderer.HEIGHT - y - 1] = new FloorCeilRenderSlice(y, 0, Renderer.WIDTH, ceilTextureY, ceilTexture);

			}
		}
		int fuck = 0;
	}
	
	// do all of the necessary raycasting - give the renderer a list of pixel cols to draw?
	private void castWalls() {
		//SolidWallRenderSlice[] lineBuffer = new SolidWallRenderSlice[Renderer.WIDTH];
		//wall casting - cast a ray for each vertical column of the display
		for (int x = 0; x < Renderer.WIDTH; x++) {
			//map the current display column to a spot on the camera plane (between -1 and 1 from left to right)
			double camPlaneX = 2 * x / (double)Renderer.WIDTH - 1;
			
			//calculate the direction of the next ray to be cast
			SimpleMatrix rayDirVec = this.player.getCamPlane().scale(camPlaneX).plus(this.player.getHeading());
			
			//determine starting map grid and exact starting position of the ray to be cast
			int mapGridX = (int)this.player.getPosition().get(0, 0);
			int mapGridY = (int)this.player.getPosition().get(1, 0);
			SimpleMatrix rayPosVec = this.player.getPosition();
			
			//determine ray step distance to next x and y grid border
			double deltaDistX = Math.abs(1.0 / rayDirVec.get(0, 0));
			double deltaDistY = Math.abs(1.0 / rayDirVec.get(1, 0));
			
			//detmine ray step distance to next x or y side
			double sideDistX;
			double sideDistY;
			
			//determine what direction to step in x/y direction (+1 or -1)
			int stepX;
			int stepY;
			
			//determine step direction and initial side distance
			if (rayDirVec.get(0, 0) < 0) {
				stepX = -1;
				sideDistX = (rayPosVec.get(0, 0) - mapGridX) * deltaDistX;
			}
			else {
				stepX = 1;
				sideDistX = (mapGridX + 1.0 - rayPosVec.get(0, 0)) * deltaDistX;
			}
			if (rayDirVec.get(1, 0) < 0) {
				stepY = -1;
				sideDistY = (rayPosVec.get(1, 0) - mapGridY) * deltaDistY;
			}
			else {
				stepY = 1;
				sideDistY = (mapGridY + 1.0 - rayPosVec.get(1, 0)) * deltaDistY;
			}
			
			boolean hitWall = false;
			boolean hitYSide = false;
			Tile currTile = this.worldMap.getMapData().get(mapGridY).get(mapGridX);
			
			//begin casting the current ray
			while (!hitWall) {
				if (sideDistX < sideDistY) {
					sideDistX += deltaDistX;
					mapGridX += stepX;
					hitYSide = false;
				}
				else {
					sideDistY += deltaDistY;
					mapGridY += stepY;
					hitYSide = true;
				}
				
				//check if we've hit a wall
				currTile = this.worldMap.getMapData().get(mapGridY).get(mapGridX);
				if (currTile.getTileType() == TileType.WALL) {
					hitWall = true;
				}
			}
			
			//calculate euclidean and perpendicular distance of the ray
			double perpendicularDist;
			if (!hitYSide) {
				perpendicularDist = (mapGridX - rayPosVec.get(0, 0) + (1 - stepX) / 2) / rayDirVec.get(0, 0);
			}
			else {
				perpendicularDist = (mapGridY - rayPosVec.get(1, 0) + (1 - stepY) / 2) / rayDirVec.get(1, 0);
			}
			//set the current col of the z-buffer for use in sprite casting
			this.zBuffer[x] = perpendicularDist;
			
			//calculate height of line on screen 
			int lineHeight = (int)Math.round(Renderer.HEIGHT / perpendicularDist);
			int lineStart  = -lineHeight / 2 + Renderer.HEIGHT / 2;
			int lineEnd    =  lineHeight / 2 + Renderer.HEIGHT / 2;
			
			//set line color and add to column buffer
			//Color color = new Color(200, 200, 200);
			//if (hitYSide) {
				//color = new Color(127, 127, 127);
			//}
			//lineBuffer[x] = new SolidWallRenderSlice(x, lineStart, lineEnd, color);
			
			//texture indexing
			perpendicularDist *= Math.cos(camPlaneX); //i don't remember why i had to do this last time, but it fixes texture rendering issues
			double wallXCoord = 0.0;
			if (!hitYSide) {
				wallXCoord = rayPosVec.get(1, 0) + perpendicularDist * rayDirVec.get(1, 0) / Math.cos(camPlaneX);
			}
			else {
				wallXCoord = rayPosVec.get(0, 0) + perpendicularDist * rayDirVec.get(0, 0) / Math.cos(camPlaneX);
			}
			wallXCoord -= Math.floor(wallXCoord);
			
			//get the texture of the intersected wall along with its dimensions
			BufferedImage wallTexture = this.worldMap.getTextureData().get(currTile.getWallTextureFilename());
			int wallTexWidth = wallTexture.getWidth();
			int wallTexHeight = wallTexture.getHeight();
			
			//map the wall coordinate to the x coordinate to be drawn of the texture
			int texXCoord = (int)(wallXCoord * (double)wallTexWidth);
			TexturedWallRenderSlice texSlice = new TexturedWallRenderSlice(x, lineStart, lineEnd, texXCoord, wallTexture);
			//texSlice.brightness = (float)lineHeight / Renderer.HEIGHT;
			this.columnBuffer[x] = texSlice;
		}
		
		//return this.columnBuffer;
		//return lineBuffer;
	}
	
	private void castEntities() {
		ArrayList<Entity> entities = this.worldMap.getEntityData();
		//update player distance to each entity
		for (Entity entity : entities) {
			entity.setPlayerDistance(this.player.calcDistance(entity));
		}
		
		//sort the entity list
		entities.sort(new EntityDistSorter());
		
		//start projecting sprites
		for (int i = 0; i < entities.size(); i++) {
			Entity currEntity = entities.get(i);

			//get sprite pos relative to the player
			SimpleMatrix entPosRelative = currEntity.getPosition().minus(this.player.getPosition());
			double invDet = 1.0 / (this.player.getCamMatrix().get(0, 0) * this.player.getCamMatrix().get(1, 1) - this.player.getCamMatrix().get(0, 1) * this.player.getCamMatrix().get(1, 0));
			double transformX = invDet * (this.player.getCamMatrix().get(1, 1) * entPosRelative.get(0, 0) - this.player.getCamMatrix().get(0, 1) * entPosRelative.get(1, 0));
			double transformY = invDet * (-1 * this.player.getCamMatrix().get(1, 0) * entPosRelative.get(0, 0) + this.player.getCamMatrix().get(0, 0) * entPosRelative.get(1, 0));
			
			SimpleMatrix transformMatrix = new SimpleMatrix(2, 1);
			transformMatrix.set(0, 0, transformX);
			transformMatrix.set(1, 0, transformY);
			//SimpleMatrix transformMatrix = this.player.getCamMatrix().invert();
			//transformMatrix.set(0, 0, transformMatrix.get(0, 0) * entPosRelative.get(0, 0));
			//transformMatrix.set(1, 0, transformMatrix.get(1, 0) * entPosRelative.get(0, 0));
			//transformMatrix.set(0, 1, transformMatrix.get(0, 1) * entPosRelative.get(1, 0));
			//transformMatrix.set(1, 1, transformMatrix.get(1, 1) * entPosRelative.get(1, 0));

			
			int spriteScreenX = (int)((Renderer.WIDTH / 2) * (1 + transformMatrix.get(0, 0) / transformMatrix.get(1, 0)));
			
			//calculate sprite's screen height
			int spriteHeight = Math.abs((int)(Renderer.HEIGHT / transformMatrix.get(1, 0)));
			
			//calculate start and end screen Y of the current stripe 
			int drawStartY = -spriteHeight / 2 + Renderer.HEIGHT / 2;
			if (drawStartY < 0) {
				drawStartY = 0;
			}
			int drawEndY = spriteHeight / 2 + Renderer.HEIGHT / 2;
			if (drawEndY >= Renderer.HEIGHT) {
				drawEndY = Renderer.HEIGHT - 1;
			}
			
			//calculate sprite screen width 
			int spriteWidth = Math.abs((int)(Renderer.HEIGHT / transformMatrix.get(1, 0)));
			
			//calculate start and end screen X of the sprite
			int drawStartX = -spriteWidth / 2 + spriteScreenX;
			if (drawStartX < 0) {
				drawStartX = 0;
			}
			int drawEndX = spriteWidth / 2 + spriteScreenX;
			if (drawEndX >= Renderer.WIDTH) {
				drawEndX = Renderer.WIDTH - 1;
			}
			
			//loop over each vertical stripe of the entity's sprite on screen
			if (transformY >= 1) {
				for (int strip = drawStartX; strip < drawEndX; strip++) {
					int texX = (int)(256 * (strip - (-spriteWidth / 2 + spriteScreenX)) * currEntity.getCurrSprite().getWidth() / spriteWidth) / 256;
				
					//only draw the strip if it's visible
					if (transformMatrix.get(1, 0) > 0 && strip > 0 && strip < Renderer.WIDTH && transformMatrix.get(1, 0) < this.zBuffer[strip]) {
						SpriteRenderSlice spriteStrip = new SpriteRenderSlice(strip, drawStartY, drawEndY, texX, currEntity.getCurrSprite(), true);
						this.spriteBuffer.get(strip).add(spriteStrip);
						//System.out.println(strip);
					}
				}
			}
		}
	}
}