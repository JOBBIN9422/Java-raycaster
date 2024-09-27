package jaubin.raycasting;

import java.util.Comparator;

public class EntityDistSorter implements Comparator<Entity> {
	@Override
	public int compare(Entity o1, Entity o2) {
		double dist1 = o1.getPlayerDistance();
		double dist2 = o2.getPlayerDistance();
		
		return Double.compare(dist1, dist2) * -1;
	}
}
