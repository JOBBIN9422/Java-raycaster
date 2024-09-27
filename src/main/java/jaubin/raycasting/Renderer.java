package jaubin.raycasting;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.ejml.simple.SimpleMatrix;

public class Renderer {
	private JFrame frame;
	private DrawingPanel panel;
	
	public static final int WIDTH = 1600;
	public static final int HEIGHT = 900;
	
	//public static boolean exit = false;
	
	public Renderer() {
		this.panel = new DrawingPanel(WIDTH, HEIGHT);
		//this.panel.setOpaque(false);
		
		this.frame = new JFrame("GayCaster");
		this.frame.setLayout(new BorderLayout());
		this.frame.add(this.panel);
		this.frame.pack();
		this.frame.setSize(WIDTH, HEIGHT);
		this.frame.setVisible(true);
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
	}
	
	public JFrame getFrame() {
		return this.frame;
	}
	
	public DrawingPanel getDrawingPanel() {
		return this.panel;
	}
	
	//draws the underlying image of the drawing panel to the frame (call this in event loop)
	public void update() {
		this.panel.paintComponent(this.frame.getGraphics());
		//this.clear();
	}
	
	public void drawPlayer2D(Camera player) {
		double headingX = player.getHeading().get(0, 0);
		double headingY = player.getHeading().get(1, 0);
		double headingVecScale = 15.0;
		int headingEndX = (int)(headingX * headingVecScale);
		int headingEndY = (int)(headingY * headingVecScale);
		
		SimpleMatrix camPlaneLeft = player.getHeading().minus(player.getCamPlane()).scale(headingVecScale);
		SimpleMatrix camPlaneRight = player.getHeading().plus(player.getCamPlane()).scale(headingVecScale);
		
		int planeStartX = (int)camPlaneLeft.get(0, 0);
		int planeStartY = (int)camPlaneLeft.get(1, 0);
		int planeEndX = (int)camPlaneRight.get(0, 0);
		int planeEndY = (int)camPlaneRight.get(1, 0);
		
		int xPos = (int)player.getPosition().get(0, 0);
		int yPos = (int)player.getPosition().get(1, 0);
		
		Graphics g = this.panel.getImage().getGraphics();
		g.setColor(new Color(0, 255, 0));
		g.drawOval(xPos, yPos, 6, 6);
		g.drawLine(xPos + 3, yPos + 3, xPos + 3 + headingEndX, yPos + 3 + headingEndY);
		g.drawLine(xPos + 3 + planeStartX, yPos + 3 + planeStartY, xPos + 3 + planeEndX, yPos + 3 + planeEndY);
		
		this.update();
	}
	
	public void drawFloorAndCeiling(Color floorColor, Color ceilColor) {
		//Graphics g = this.panel.getImage().getGraphics();
		Graphics2D g = (Graphics2D)this.panel.getImage().getGraphics();
		GradientPaint grad = new GradientPaint(WIDTH / 2,  0, Color.LIGHT_GRAY, WIDTH / 2, HEIGHT / 2, Color.DARK_GRAY);
		//g.setColor(ceilColor);
		g.setPaint(grad);
		g.fillRect(0,  0, WIDTH, HEIGHT / 2);
		
		grad = new GradientPaint(WIDTH / 2, HEIGHT / 2, Color.DARK_GRAY, WIDTH / 2, HEIGHT, Color.LIGHT_GRAY);
		
		g.setPaint(grad);
		g.fillRect(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
		//this.update();
	}
	
	public void drawWallTextures(TexturedWallRenderSlice[] texSlices) {
		Graphics2D g = (Graphics2D)this.panel.getImage().getGraphics();
		for (int x = 0; x < texSlices.length; x++) {
			//float brightness = texSlices[x].brightness;
			//RescaleOp darken = new RescaleOp(
			       // new float[]{brightness, brightness, brightness}, // scale factors for red, green, blue, alpha
			       // new float[]{0, 0, 0}, // offsets for red, green, blue, alpha
			       // null);
			//texSlices[x].textureImage = darken.filter(texSlices[x].textureImage, null);
			g.drawImage(texSlices[x].textureImage, texSlices[x].x, texSlices[x].startY, texSlices[x].x + 1, texSlices[x].endY, texSlices[x].textureX, 0, texSlices[x].textureX + 1, texSlices[x].textureImage.getHeight(), null);
		}
		//this.update();
	}
	
	public void drawSprites(ArrayList<ArrayList<SpriteRenderSlice>> spriteSlices) {
		Graphics2D g = (Graphics2D)this.panel.getImage().getGraphics();
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f);
		g.setComposite(ac);
		for (int x = 0; x < spriteSlices.size(); x++) {
			ArrayList<SpriteRenderSlice> currSlices = spriteSlices.get(x);
			for (int n = 0; n < currSlices.size(); n++) {
				g.drawImage(currSlices.get(n).textureImage, currSlices.get(n).x, currSlices.get(n).startY, currSlices.get(n).x + 1, currSlices.get(n).endY, currSlices.get(n).textureX, 0, currSlices.get(n).textureX + 1, currSlices.get(n).textureImage.getHeight(), null);
			}
		}
		//this.update();
	}
	
	public void drawSpriteSlice(SpriteRenderSlice slice) {
		Graphics2D g = (Graphics2D)this.panel.getImage().getGraphics();
		g.drawImage(slice.textureImage, slice.x, slice.startY, slice.x + 1, slice.endY, slice.textureX, 0, slice.textureX + 1, slice.textureImage.getHeight(), null);
	}
	
	public void drawVertLines(SolidWallRenderSlice[] lines) {
		Graphics g = this.panel.getImage().getGraphics();
		
		for (int x = 0; x < lines.length; x++) {
			g.setColor(lines[x].color);
			g.drawLine(x, lines[x].startY, x, lines[x].endY);
		}
		this.update();
	}
	
	public void drawFPSInfo(double currFPS, double elapsedFrameTime) {
		Graphics2D g = (Graphics2D)this.panel.getImage().getGraphics();
		g.drawString("FPS: " + Double.toString(currFPS), 100,  100);
		g.drawString("Frame time: " + Double.toString(elapsedFrameTime), 100, 130);
	}
	
	public void clear() {
		BufferedImage image = this.panel.getImage();
		Graphics g = image.getGraphics();
		g.clearRect(0, 0, image.getWidth(), image.getHeight());
	}
	
	public void testDraw() {
		Random rand = new Random();
		
		while (true) {
			BufferedImage image = this.panel.getImage();
			int width = image.getWidth();
			int height = image.getHeight();
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					image.setRGB(i, j, rand.nextInt());
				}
			}
			this.update();
			//this.panel.paintComponent(this.frame.getGraphics());
		}
	}
}
