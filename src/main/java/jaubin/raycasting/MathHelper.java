package jaubin.raycasting;

import org.ejml.simple.SimpleMatrix;

public class MathHelper {
	public static SimpleMatrix GetRotationMatrix(double theta) {
		double[][] rotationData = new double[2][2];
		rotationData[0][0] = Math.cos(theta);
		rotationData[1][0] = Math.sin(theta);
		rotationData[0][1] = -1 * Math.sin(theta);
		rotationData[1][1] = Math.cos(theta);
		
		return new SimpleMatrix(rotationData);
	}
}
