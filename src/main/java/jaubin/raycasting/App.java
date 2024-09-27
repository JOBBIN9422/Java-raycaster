package jaubin.raycasting;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException
    {
    	System.setProperty("sun.java2d.opengl", "true");
    	Engine engine = new Engine();
    	while (engine.gameIsRunning()) {
    		engine.doGameLoop();
    		//Thread.sleep(1);
    	}
    }
}
