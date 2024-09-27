package jaubin.raycasting;

import org.ejml.simple.SimpleMatrix;

public class Camera {
	//protected double xPos;
	//protected double yPos;
	
	protected double moveSpeed;
	protected double turnSpeed;
	
	//current movement states (set via key listener)
	protected boolean movingForwards;
	protected boolean movingBackwards;
	protected boolean strafingLeft;
	protected boolean strafingRight;
	protected boolean turningLeft;
	protected boolean turningRight;
	
	protected SimpleMatrix positionVec;
	protected SimpleMatrix headingVec;
	protected SimpleMatrix camPlane;
	
	public Camera(double startX, double startY) {
		//xPos = startX;
		//yPos = startY;
		
		this.moveSpeed = 0.05;
		this.turnSpeed = 0.03;
		
		this.movingForwards  = false;
		this.movingBackwards = false;
		this.strafingLeft    = false;
		this.strafingRight   = false;
		this.turningLeft     = false;
		this.turningRight    = false;
		
		positionVec = new SimpleMatrix(2, 1);
		headingVec = new SimpleMatrix(2, 1);
		camPlane = new SimpleMatrix(2, 1);
		
		positionVec.set(0, 0, startX);
		positionVec.set(1, 0, startY);
		
		headingVec.set(0, 0, 1);
		headingVec.set(1, 0, 0);
		
		camPlane.set(0, 0, 0);
		camPlane.set(1, 0, 1);
	}
	
	public boolean isMovingForwards() {
		return this.movingForwards;
	}
	public boolean isMovingBackwards() {
		return this.movingBackwards;
	}
	public boolean isStrafingLeft() {
		return this.strafingLeft;
	}
	public boolean isStrafingRight() {
		return this.strafingRight;
	}
	public boolean isTurningLeft() {
		return this.turningLeft;
	}
	public boolean isTurningRight() {
		return this.turningRight;
	}
	
	public SimpleMatrix getHeading() {
		return this.headingVec;
	}
	public SimpleMatrix getCamPlane() {
		return this.camPlane;
	}
	public SimpleMatrix getPosition() {
		return this.positionVec;
	}
	public SimpleMatrix getCamMatrix() {
		SimpleMatrix camMatrix = new SimpleMatrix(2, 2);
		
		camMatrix.set(0, 0, this.camPlane.get(0, 0));
		camMatrix.set(1, 0, this.camPlane.get(1, 0));
		camMatrix.set(0, 1, this.headingVec.get(0, 0));
		camMatrix.set(1, 1, this.headingVec.get(1, 0));
		
		return camMatrix;
	}
	
	public double getMoveSpeed() {
		return this.moveSpeed;
	}
	public double getTurnSpeed() {
		return this.turnSpeed;
	}
	public double calcDistance(Entity e) {
		SimpleMatrix ePos = e.getPosition();
		double dx = this.positionVec.get(0, 0) - ePos.get(0, 0);
		double dy = this.positionVec.get(1, 0) - ePos.get(1, 0);
		
		return Math.abs(dx * dx - dy * dy);
	}
	
	public void setMoveSpeed(double speed) {
		this.moveSpeed = speed;
	}
	public void setTurnSpeed(double speed) {
		this.turnSpeed = speed;
	}
	
	public void turn(Direction turnDir) {
 		double dirModifier = 0;
		switch (turnDir) {
		case LEFT:
			dirModifier = -1.0;
			break;
			
		case RIGHT:
			dirModifier = 1.0;
			break;
			
		default:
			break;
		}
		
		headingVec = MathHelper.GetRotationMatrix(dirModifier * this.turnSpeed).mult(this.headingVec);
		camPlane = MathHelper.GetRotationMatrix(dirModifier * this.turnSpeed).mult(this.camPlane);
	}
	
	public void strafe(Direction strafeDir) {
		double dirModifier = 0;
		switch (strafeDir) {
		case LEFT:
			dirModifier = -1.0;
			break;
			
		case RIGHT:
			dirModifier = 1.0;
			break;
			
		default:
			break;
		}
		
		//rotate the current heading by 90 degrees CCW 
		SimpleMatrix strafeDirVec = MathHelper.GetRotationMatrix(dirModifier * Math.PI / 2).mult(this.headingVec);
		
		//may have to scale this later (multiply in another constant - play around with it)
		double dx = strafeDirVec.get(0, 0) * this.moveSpeed;
		double dy = strafeDirVec.get(1, 0) * this.moveSpeed;
		
		//update the camera position 
		this.positionVec.set(0, 0, this.positionVec.get(0, 0) + dx);
		this.positionVec.set(1, 0, this.positionVec.get(1, 0) + dy);
	}
	
	public void move(Direction moveDir) {
		double dirModifier = 0;
		switch (moveDir) {
		case FORWARDS:
			dirModifier = 0;
			break;
			
		case BACKWARDS:
			dirModifier = 1.0;
			break;
			
		default:
			break;
		}
		
		SimpleMatrix strafeDirVec = MathHelper.GetRotationMatrix(dirModifier * Math.PI).mult(this.headingVec);
		
		//may have to scale this later (multiply in another constant - play around with it)
		double dx = strafeDirVec.get(0, 0) * this.moveSpeed;
		double dy = strafeDirVec.get(1, 0) * this.moveSpeed;
		
		//update the camera position 
		this.positionVec.set(0, 0, this.positionVec.get(0, 0) + dx);
		this.positionVec.set(1, 0, this.positionVec.get(1, 0) + dy);
	}
	
	public SimpleMatrix GetNextMovePos(Direction moveDir) {
		SimpleMatrix nextPos = new SimpleMatrix(2, 1);
		SimpleMatrix moveDirVec = new SimpleMatrix(2, 1);
		double dx, dy, x, y = 0.0;
		
		//determine where the player would end up if they moved 1 frame in the given direction
		switch (moveDir) {
		case FORWARDS:
			moveDirVec = this.headingVec;
			break;
			
		case BACKWARDS:
			moveDirVec = MathHelper.GetRotationMatrix(Math.PI).mult(this.headingVec);
			break;
		
		case LEFT:
			moveDirVec = MathHelper.GetRotationMatrix(-1 * Math.PI / 2).mult(this.headingVec);
			break;
			
		case RIGHT:
			moveDirVec = MathHelper.GetRotationMatrix(Math.PI / 2).mult(this.headingVec);
			break;
		}
		
		dx = this.moveSpeed * moveDirVec.get(0, 0);
		dy = this.moveSpeed * moveDirVec.get(1, 0);
		
		x = this.positionVec.get(0, 0) + dx;
		y = this.positionVec.get(1, 0) + dy;
		nextPos.set(0, 0, x);
		nextPos.set(1, 0, y);
		
		return nextPos;
	}
	
	public void setMovementState(Direction moveDir) {
		switch (moveDir) {
		case FORWARDS:
			this.movingForwards = true;
			break;
			
		case BACKWARDS:
			this.movingBackwards = true;
			break;
			
		//strafe left
		case LEFT:
			this.strafingLeft = true;
			break;
			
		//strafe right
		case RIGHT:
			this.strafingRight = true;
			break;
			
		default:
			break;
		}
	}
	
	public void setTurnState(Direction turnDir) {
		switch (turnDir) {
		//turn left
		case LEFT:
			this.turningLeft = true;
			break;
			
		//turn right
		case RIGHT:
			this.turningRight = true;
			break;
			
		default:
			break;
		}
	}
	
	public void resetMovementState(Direction moveDir) {
		switch (moveDir) {
		case FORWARDS:
			this.movingForwards = false;
			break;
			
		case BACKWARDS:
			this.movingBackwards = false;
			break;
			
		//strafe left
		case LEFT:
			this.strafingLeft = false;
			break;
			
		//strafe right
		case RIGHT:
			this.strafingRight = false;
			break;
			
		default:
			break;
		}
	}
	
	public void resetTurnState(Direction turnDir) {
		switch (turnDir) {
		//turn left
		case LEFT:
			this.turningLeft = false;
			break;
			
		//turn right
		case RIGHT:
			this.turningRight = false;
			break;
			
		default:
			break;
		}
	}
}
