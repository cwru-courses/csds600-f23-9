package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
  * Note that SEPIA saves the townhall as a unit. Therefore when you create a GameState instance,
 * you must be able to distinguish the townhall from a peasant. This can be done by getting
 * the name of the unit type from that unit's TemplateView:
 * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
 * 
 * You will also need to distinguish between gold mines and trees.
 * state.getResourceNode(id).getType(): returns the type of the given resource
 * 
 * You can compare these types to values in the ResourceNode.Type enum:
 * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
 * 
 * You can check how much of a resource is remaining with the following:
 * state.getResourceNode(id).getAmountRemaining()
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
	
	State.StateView state;
	public int playerNum,requiredGold, requiredWood, currentGold, currentWood,xExtent, yExtent;
	public boolean buildPeasants;

	public List<ResourceView> resourceNodes;
	public boolean[][] map;
	public int[][] goldMapArray, woodmapArray;

	public List<UnitView> units;
	public List<UnitView> playerUnits = new ArrayList<>();
	public UnitView townHall;

	public double cost;
	public List<StripsAction> plan;
	public GameState parent;

    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
    	this.state=state;
		this.playerNum = playernum;
		this.requiredGold = requiredGold;// Set req Gold
		this.requiredWood = requiredWood;// Set req Wood
		this.currentGold = state.getResourceAmount(playernum, ResourceType.GOLD); // Set current Gold
		this.currentWood = state.getResourceAmount(playernum, ResourceType.WOOD); // Set current Wood
		this.plan = new ArrayList<>();
		this.buildPeasants = buildPeasants;
		this.cost = getCost();
		this.parent = null;
		heuristic();
		
		this.xExtent = state.getXExtent();// Get xExtent Map Size
		this.yExtent = state.getYExtent();// Get yExtent Map Size
		this.units = state.getAllUnits();// Get list of all units
		for (UnitView unitView : units) {
			if (unitView.getTemplateView().getPlayer() == playerNum) {
				if (unitView.getTemplateView().getName().equalsIgnoreCase("townhall")) {
					this.townHall = unitView;
				} else if (unitView.getTemplateView().getName().equalsIgnoreCase("peasant")) {
					playerUnits.add(unitView);
				} 
			}
		}

		this.map = new boolean[xExtent][yExtent]; 
		this.goldMapArray = new int[xExtent][yExtent]; // for gold resource location in map
		this.woodmapArray = new int[xExtent][yExtent]; // for wood resource location in map
		for (int x = 0; x < xExtent; x++) {
			for (int y = 0; y < yExtent; y++) {
				map[x][y] = false;
				woodmapArray[x][y] = 0;
				goldMapArray[x][y] = 0;
			}
		}
		map[townHall.getXPosition()][townHall.getYPosition()] = true; // mark town hall position as true in the map

		this.resourceNodes = state.getAllResourceNodes();// Gives all resources locations on map like gold, wood
		for (ResourceView resourceView : resourceNodes) {
			map[resourceView.getXPosition()][resourceView.getYPosition()] = true;// Marks all the resource locations as true in the map.
			if (resourceView.getType() == ResourceNode.Type.TREE) {
				woodmapArray[resourceView.getXPosition()][resourceView.getYPosition()] = resourceView.getAmountRemaining();
			}else if (resourceView.getType() == ResourceNode.Type.GOLD_MINE) {
				goldMapArray[resourceView.getXPosition()][resourceView.getYPosition()] = resourceView.getAmountRemaining();
			}
		}

		
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
    	return (this.currentWood >= this.requiredWood && this.currentGold >= this.requiredGold);
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
        return null;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
    	UnitView peasant = playerUnits.get(0); // Get the available peasant
		Position unitPosition = new Position(peasant.getXPosition(), peasant.getYPosition()); // creating a position const using peasant x,y pos

    	int goldDiffernence = requiredGold - currentGold;
    	int woodDifference = requiredWood - currentWood;
        double value = goldDiffernence + woodDifference;

    	if (peasant.getCargoAmount() > 0) {
    		Position position = new Position(townHall.getXPosition(), townHall.getYPosition());// Creating a position const using townHall x,Y Pos
    		double distance =position.chebyshevDistance(unitPosition);
			value = value - peasant.getCargoAmount() * 0.5;
			value = value + distance;
    	}
    	else {
    		Position pos1 = getBestPosOfResource(unitPosition, goldDiffernence, woodDifference);
			value = value + pos1.chebyshevDistance(unitPosition) * 0.5;
    		double harvestAmount = 0;
    		for(Direction direction : Direction.values()) {
    			int yPos = peasant.getYPosition() + direction.yComponent();
    			int xPos = peasant.getXPosition() + direction.xComponent();
    			int woodAvailable = woodmapArray[xPos][yPos];
    			int goldAvailable = goldMapArray[xPos][yPos];
    			
    			if (goldAvailable > 0) {
    				if (goldDiffernence > 0) {
    					if (goldAvailable > 100) {
    						harvestAmount = 100;
    					}
    					else {
    						harvestAmount = goldMapArray[xPos][yPos];
    					}
    				}
    			}
    			if (woodAvailable > 0) {
    				if (woodDifference > 0) {
    					if (woodAvailable > 100) {
    						harvestAmount = 100;
    					}
    					else {
    						harvestAmount = woodmapArray[xPos][yPos];
    					}
    				}
    			}
    		}
			value = value - harvestAmount * 0.3;
    	}

        return value;
    }
    
	private Position getBestPosOfResource(Position currentPosition, int goldDiff, int woodDiff) {
		Position best = null;
		int resource = 0;
		int dist = 0;
		int requiredAmount,currentAmount;
		int[][] map;
		if(goldDiff < woodDiff) {
			requiredAmount = requiredGold;
			currentAmount = currentGold;
			map = goldMapArray;
		}
		else {
			requiredAmount = requiredWood;
			currentAmount = currentWood;
			map = woodmapArray;
		}

		for(int i = 0; i < map.length; i ++) {// Iterating the map to find the nearest position of resource
			for(int j = 0; j < map[i].length; j ++) {
				int currentBest = map[i][j];
				if(currentBest > 0) {
					int distance = currentPosition.chebyshevDistance(new Position(i, j));;
					if(best == null) {
						best = new Position(i, j);
						dist = distance;
						resource = currentBest;
					}
					else {
						if(distance <= dist) {
							if(currentBest >= resource || currentBest >= requiredAmount - currentAmount) {
								best = new Position(i, j);
								dist = distance;
								resource = currentBest;
							}
						}
						else {
							if(resource >= 100) {
								if(currentBest >= resource && currentBest >= 100 && resource < requiredAmount - currentAmount) {
									best = new Position(i, j);
									dist = distance;
									resource = currentBest;
								}
							}
						}
					}
				}
			}
		}
		return best;
	}

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return this.cost;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Implement me!
    	double cost1 = o.heuristic() + o.getCost();
    	double cost2 = this.heuristic()+ this.getCost();
    	double returnValue= cost2-cost1;
    	return (int) returnValue;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
    	if(o instanceof GameState) {
    		GameState gState = (GameState) o;
    		if(gState.currentGold==this.currentGold 
    				&& gState.currentWood== this.currentWood && gState.units==this.units) {
    			return true;
    		}
    	}
        return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
	public int hashCode() {
		return Objects.hash(currentGold, currentWood, units);
	}
}