package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.environment.model.state.*;
import java.util.*;

public class AstarAgent {

	private int xExtent, yExtent;

	public AstarAgent(int xExtent, int yExtent) {
		this.xExtent = xExtent;
		this.yExtent = yExtent;
	}

	class MapLocation {
		public int x, y;
		public MapLocation parent;
		public int gCost;
		public float hCost;
		public float fCost;

		public MapLocation(int x, int y, MapLocation parent, float hCost) {
			this.x = x;
			this.y = y;
			this.parent = parent;
			this.hCost = hCost;
		}

		public MapLocation(int x, int y, MapLocation parent, float hCost, int gCost) {
			this.x = x;
			this.y = y;
			this.parent = parent;
			this.hCost = hCost;
			this.gCost = gCost;
		}

		public boolean equals(Object obj) {
			if (obj != null && obj instanceof MapLocation) {
				MapLocation mapLocationObj = (MapLocation) obj;
				if (this.x == mapLocationObj.x && this.y == mapLocationObj.y) {
					return true;
				}
			}
			return false;
		}
	}

	public Stack<MapLocation> findPath(List<ResourceNode.ResourceView> blockedResources, PlayerData player, PlayerData enemy) {

		MapLocation startLoc = new MapLocation(player.xPosition, player.yPosition, null, 0);
		MapLocation goalLoc = new MapLocation(enemy.xPosition, enemy.yPosition, null, 0);

		Set<MapLocation> blockedResourceLocations = new HashSet<MapLocation>();
		for (ResourceNode.ResourceView resource : blockedResources) {
			blockedResourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
		}

		return AstarSearch(startLoc, goalLoc, xExtent, yExtent, null, blockedResourceLocations);
	}

	/**
	 * This is the method you will implement for the assignment. Your implementation
	 * will use the A* algorithm to compute the optimum path from the start position
	 * to a position adjacent to the goal position.
	 *
	 * Therefore your you need to find some possible adjacent steps which are in
	 * range and are not trees or the enemy footman. Hint: Set<MapLocation>
	 * resourceLocations contains the locations of trees
	 *
	 * You will return a Stack of positions with the top of the stack being the
	 * first space to move to and the bottom of the stack being the last space to
	 * move to. If there is no path to the townhall then return null from the method
	 * and the agent will print a message and do nothing. The code to execute the
	 * plan is provided for you in the middleStep method.
	 *
	 * As an example consider the following simple map
	 *
	 * F - - - - x x x - x H - - - -
	 *
	 * F is the footman H is the townhall x's are occupied spaces
	 *
	 * xExtent would be 5 for this map with valid X coordinates in the range of [0,
	 * 4] x=0 is the left most column and x=4 is the right most column
	 *
	 * yExtent would be 3 for this map with valid Y coordinates in the range of [0,
	 * 2] y=0 is the top most row and y=2 is the bottom most row
	 *
	 * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
	 *
	 * The path would be
	 *
	 * (1,0) (2,0) (3,1) (2,2) (1,2)
	 *
	 * Notice how the initial footman position and the townhall position are not
	 * included in the path stack
	 *
	 * @param start
	 *            Starting position of the footman
	 * @param goal
	 *            MapLocation of the townhall
	 * @param xExtent
	 *            Width of the map
	 * @param yExtent
	 *            Height of the map
	 * @param resourceLocations
	 *            Set of positions occupied by resources
	 * @return Stack of positions with top of stack being first move in plan
	 */

	private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent,
			MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations) {

		MapLocation startNode = new MapLocation(start.x, start.y, null, 0);
		startNode.gCost = 0;
		startNode.hCost = getHeuristic(startNode, goal);
		startNode.fCost = startNode.gCost + startNode.hCost;

		List<MapLocation> openList = new ArrayList<>();
		List<MapLocation> closedList = new ArrayList<>();
		boolean[][] resource = new boolean[xExtent][yExtent];
		for (MapLocation mapLocation : resourceLocations) {
			resource[mapLocation.x][mapLocation.y] = true;
		}

		openList.add(startNode);
		while (!openList.isEmpty()) {
			MapLocation currentNode = openList.get(0);

			int listIndex, counter;
			listIndex = counter = 0;
			for (MapLocation i : openList) {
				if (i.fCost < currentNode.fCost) {
					currentNode = i;
					listIndex = counter;
				}
				counter++;
			}
			openList.remove(listIndex);
			closedList.add(currentNode);
			if (currentNode.equals(goal)) {
				Stack<MapLocation> finalPath = new Stack<>();
				MapLocation node = null;
				if (currentNode != null) {
					node = currentNode.parent;
				}

				while (node != null) {
					finalPath.push(node);
					node = node.parent;
				}

				finalPath.pop();
				return finalPath;
			}

			List<MapLocation> childernsList = new ArrayList<>();

			MapLocation[] actionsArray = new MapLocation[4];
			actionsArray[0] = new MapLocation(currentNode.x, currentNode.y + 1, currentNode, 0);
			actionsArray[1] = new MapLocation(currentNode.x - 1, currentNode.y, currentNode, 0);
			actionsArray[2] = new MapLocation(currentNode.x + 1, currentNode.y, currentNode, 0);
			actionsArray[3] = new MapLocation(currentNode.x, currentNode.y - 1, currentNode, 0);

			for (MapLocation action : actionsArray) {
				if (!isNodeValid(action, xExtent, yExtent, resource)) {
					continue;
				}
				MapLocation node = new MapLocation(action.x, action.y, currentNode, 0);
				childernsList.add(node);
			}

			for (MapLocation child : childernsList) {
				boolean b = false;
				for (MapLocation closedNode : closedList) {
					if (child.equals(closedNode)) {
						b = true;
						break;
					}
				}

				if (b) {
					continue;
				}

				child.gCost = currentNode.gCost + 1;
				child.hCost = getHeuristic(child, goal);
				child.fCost = child.gCost + child.hCost;

				for (MapLocation node : openList) {
					if (child.equals(node) && child.gCost > node.gCost) {
						b = true;
						break;
					}
				}
				if (b) {
					continue;
				}
				openList.add(child);
			}

		}

		return null;

	}

	private boolean isNodeValid(MapLocation location, int xExtent, int yExtent, boolean[][] resource) {
		if ((location.x >= 0 && location.x < xExtent) && (location.y >= 0 && location.y < yExtent)) {
			if (!resource[location.x][location.y]) {
				return true;
			}
		}
		return false;
	}

	private float getHeuristic(MapLocation currentPos, MapLocation goal) {
		if (currentPos != null && goal != null) {
			return Math.abs(goal.x - currentPos.x) + Math.abs(goal.y - currentPos.y) - 2;
		}
		return Float.MAX_VALUE;
	}

}
