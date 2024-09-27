package jaubin.raycasting;

public class HorizontalRenderSlice {
	public int startX;
	public int endX;
	public int y;
	
	public HorizontalRenderSlice(int y, int startX, int endX) {
		this.y = y;
		this.startX = startX;
		this.endX = endX;
		//this.color = color;
	}
}
