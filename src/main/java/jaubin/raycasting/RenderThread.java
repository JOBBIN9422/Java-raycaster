package jaubin.raycasting;

public class RenderThread extends Thread {
	private Engine engine;
	
	public RenderThread(Engine engine) {
		this.engine = engine;
	}
	
	public void run() {
		while (this.engine.gameIsRunning()) {
			this.engine.doRendering();
		}
	}
}
