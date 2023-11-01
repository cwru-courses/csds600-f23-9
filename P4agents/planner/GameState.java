package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.ArrayList;
import java.util.List;

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
	public int[][] goldMap, woodMap;

	public List<UnitView> allUnits;
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
		
		this.xExtent = state.getXExtent();// Get xExtent Map Size
		this.yExtent = state.getYExtent();// Get yExtent Map Size
		this.allUnits = state.getAllUnits();// Get list of all units
		for (UnitView unitView : allUnits) {
			if (unitView.getTemplateView().getPlayer() == playerNum) {
				if (unitView.getTemplateView().getName().equalsIgnoreCase("townhall")) {
					this.townHall = unitView;
				} else if (unitView.getTemplateView().getName().equalsIgnoreCase("peasant")) {
					playerUnits.add(unitView);
				} 
			}
		}

		this.map = new boolean[xExtent][yExtent]; 
		this.goldMap = new int[xExtent][yExtent]; // for gold resource location in map
		this.woodMap = new int[xExtent][yExtent]; // for wood resource location in map
		for (int x = 0; x < xExtent; x++) {
			for (int y = 0; y < yExtent; y++) {
				map[x][y] = false;
				woodMap[x][y] = 0;
				goldMap[x][y] = 0;
			}
		}
		map[townHall.getXPosition()][townHall.getYPosition()] = true; // mark town hall position as true in the map

		this.resourceNodes = state.getAllResourceNodes();// Gives all resources locations on map like gold, wood
		for (ResourceView resourceView : resourceNodes) {
			map[resourceView.getXPosition()][resourceView.getYPosition()] = true;// Marks all the resource locations as true in the map.
			if (resourceView.getType() == ResourceNode.Type.TREE) {
				woodMap[resourceView.getXPosition()][resourceView.getYPosition()] = resourceView.getAmountRemaining();
			}else if (resourceView.getType() == ResourceNode.Type.GOLD_MINE) {
				goldMap[resourceView.getXPosition()][resourceView.getYPosition()] = resourceView.getAmountRemaining();
			}
		}

		heuristic();
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        // TODO: Implement me!
        return false;
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
        // TODO: Implement me!
        return 0.0;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return 0.0;
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
        return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // TODO: Implement me!
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
        // TODO: Implement me!
        return 0;
    }
}
