package jaubin.raycasting;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ControlHandler implements KeyListener {
	private Camera targetPlayer;

	
	public ControlHandler(Camera player) {
		this.targetPlayer = player;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			targetPlayer.setMovementState(Direction.FORWARDS);
			break;
			
		case KeyEvent.VK_S:
			targetPlayer.setMovementState(Direction.BACKWARDS);
			break;
			
		case KeyEvent.VK_A:
			targetPlayer.setMovementState(Direction.LEFT);
			break;
			
		case KeyEvent.VK_D:
			targetPlayer.setMovementState(Direction.RIGHT);
			break;
			
		case KeyEvent.VK_RIGHT:
			targetPlayer.setTurnState(Direction.RIGHT);
			break;
		
		case KeyEvent.VK_LEFT:
			targetPlayer.setTurnState(Direction.LEFT);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			targetPlayer.resetMovementState(Direction.FORWARDS);
			break;
			
		case KeyEvent.VK_S:
			targetPlayer.resetMovementState(Direction.BACKWARDS);
			break;
			
		case KeyEvent.VK_A:
			targetPlayer.resetMovementState(Direction.LEFT);
			break;
			
		case KeyEvent.VK_D:
			targetPlayer.resetMovementState(Direction.RIGHT);
			break;
			
		case KeyEvent.VK_RIGHT:
			targetPlayer.resetTurnState(Direction.RIGHT);
			break;
		
		case KeyEvent.VK_LEFT:
			targetPlayer.resetTurnState(Direction.LEFT);
			break;
		}
	}
}
