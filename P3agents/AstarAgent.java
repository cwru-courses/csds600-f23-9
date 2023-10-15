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
	 *  Gives back the Stack of mapLocations that can be used to reach goal node from current
	 * @param start
	 * @param goal
	 * @param xExtent
	 * @param yExtent
	 * @param enemyFootmanLoc
	 * @param resourceLocations
	 * @return
	 */
	private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent,
			MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations) {

		MapLocation startNode = new MapLocation(start.x, start.y, null, 0);
		startNode.gCost = 0;
		startNode.hCost = getHeuristic(startNode, goal);
		startNode.fCost = startNode.gCost + startNode.hCost;

		//Stores the MapLocation Obj node which are being opened
		List<MapLocation> openList = new ArrayList<>();
		List<MapLocation> closedList = new ArrayList<>();
		//Store the locations of blocked resouces if blocked then will mark that position as true by using 2*2 matrix array
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
				// stores the final path to reach the enemy
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
			// Expand Down Node
			actionsArray[0] = new MapLocation(currentNode.x, currentNode.y + 1, currentNode, 0);
			//Expand Left Node
			actionsArray[1] = new MapLocation(currentNode.x - 1, currentNode.y, currentNode, 0);
			// Expand Right Node
			actionsArray[2] = new MapLocation(currentNode.x + 1, currentNode.y, currentNode, 0);
			// Expand Top Node
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

	/**
	 * Checking if the node is valid or not, will determine if the node is a blocked resource or not
	 * @param location
	 * @param xExtent
	 * @param yExtent
	 * @param resource
	 * @return
	 */
	private boolean isNodeValid(MapLocation location, int xExtent, int yExtent, boolean[][] resource) {
		if ((location.x >= 0 && location.x < xExtent) && (location.y >= 0 && location.y < yExtent)) {
			if (!resource[location.x][location.y]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the optimal heuristic value between goal node to current
	 * @param currentPos
	 * @param goal
	 * @return
	 */
	private float getHeuristic(MapLocation currentPos, MapLocation goal) {
		if (currentPos != null && goal != null) {
			return Math.abs(goal.x - currentPos.x) + Math.abs(goal.y - currentPos.y) - 2;
		}
		return Float.MAX_VALUE;
	}

}
