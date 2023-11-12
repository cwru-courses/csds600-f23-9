package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.*;

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
	
	public State.StateView state;
	public int playerNum,requiredGold, requiredWood, currentGold, currentWood,xExtent, yExtent,currentFood;
	public boolean buildPeasants;

	public List<ResourceView> resourceNodes;
	public List<edu.cwru.sepia.agent.planner.GameState.Peasant> peasantUnits = new ArrayList<>();
	public boolean[][] map;
	public int[][] goldMapArray, woodmapArray;

	public List<UnitView> units= new ArrayList<>();
	public List<UnitView> playerUnits = new ArrayList<>();
	public UnitView townHall;

	public double cost;
	public List<StripsAction> plan;
	public GameState parent;
	public double heuristic;


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
        this.currentFood = state.getSupplyAmount(playernum);
		this.plan = new ArrayList<>();
		this.buildPeasants = buildPeasants;
		this.cost = getCost();
		this.parent = null;
		this.xExtent = state.getXExtent();// Get xExtent Map Size
		this.yExtent = state.getYExtent();// Get yExtent Map Size
		this.units = state.getAllUnits();// Get list of all units
		
        for (UnitView unitView : units) {
			if (unitView.getTemplateView().getPlayer() == playerNum) {
				if (unitView.getTemplateView().getName().equalsIgnoreCase("peasant")) {
					playerUnits.add(unitView);
                    Position pos = new Position(townHall.getXPosition(), townHall.getYPosition());
                    Peasant p = new Peasant(unitView.getID(), unitView.getXPosition(), unitView.getYPosition(),false,false,unitView.getCargoAmount(),pos);
					if (unitView.getCargoType() == ResourceType.GOLD) {
						p.containsGold = true;
                        p.containsWood = false;
					} else if (unitView.getCargoType() == ResourceType.WOOD) {
                        p.containsGold = false;
						p.containsWood = true;
					}
					this.peasantUnits.add(p);
				} else if (unitView.getTemplateView().getName().equalsIgnoreCase("townhall")) {
					this.townHall = unitView;
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
		this.heuristic = heuristic();
	}
	
	public GameState() {
		super();
	}

	public GameState(GameState state) {
		this.state = state.state;
		this.playerNum = state.playerNum;
        this.xExtent = state.xExtent;
		this.yExtent = state.yExtent;
        this.buildPeasants = state.buildPeasants;
		this.parent = state;
		this.requiredWood = state.requiredWood;
		this.currentGold = state.currentGold;
		this.currentWood = state.currentWood;
		this.requiredGold = state.requiredGold;
		this.townHall = state.townHall;
		this.currentFood = state.currentFood;
		this.map = returnMap(state.map);
		this.goldMapArray = returnMap(state.goldMapArray);
		this.woodmapArray = returnMap(state.woodmapArray);
		this.units = setUnits(state.units);
		this.playerUnits = setUnits(state.playerUnits);

        List<Peasant> peasants = new ArrayList<>();
		for (Peasant p : state.peasantUnits) {
			Peasant p_unit = new Peasant(p.id, p.xPos, p.yPos, p.containsGold, p.containsWood, p.amount, p.adjPos);
			peasants.add(p_unit);
		}
		this.peasantUnits = peasants;
		
		List<ResourceView> resource1 = new ArrayList<>();
		state.resourceNodes.stream().forEach(view1 -> {
			resource1.add(new ResourceView(new ResourceNode(view1.getType(), view1.getXPosition(),view1.getYPosition(), view1.getAmountRemaining(), view1.getID())));
		});
		this.resourceNodes = resource1;

		this.cost = getCost();
		List<StripsAction> stripActionsList = new ArrayList<>();
		for (StripsAction element : state.plan) {
			stripActionsList.add(element);
		}
		this.plan = stripActionsList;
		this.heuristic=heuristic();
	}
	
	private boolean[][] returnMap(boolean[][] intArray){
		boolean[][] array = new boolean[intArray.length][intArray[0].length];
		for (int i = 0; i < intArray.length; i++) {
			for (int j = 0; j <intArray[0].length; j++) {
				array[i][j] = intArray[i][j];
			}
		}
		return array;
	}
	
	private int[][] returnMap(int[][] intArray){
		int[][] array = new int[intArray.length][intArray[0].length];
		for (int i = 0; i < intArray.length; i++) {
			for (int j = 0; j <intArray[0].length; j++) {
				array[i][j] = intArray[i][j];
			}
		}
		return array;
	}
	
	private List<UnitView> setUnits(List<UnitView> unitss) {
		List<UnitView> units = new ArrayList<>();
		for (UnitView uv : unitss) {
			Unit unit = new Unit(new UnitTemplate(uv.getID()), uv.getID());
			unit.setxPosition(uv.getXPosition());
			unit.setyPosition(uv.getYPosition());
            unit.setCargo(uv.getCargoType(), uv.getCargoAmount());
			units.add(new UnitView(unit));
		}
		return units;
	}

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
	public boolean isGoal() {
		return (this.currentGold >= this.requiredGold && this.currentWood >= this.requiredWood);
	}

    /**
     * Write the function that computes the current cost to get to this node. This
     * is combined with your heuristic to determine which actions/states are better
     * to explore.
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
		double thisTotalCost = this.getCost() + this.heuristic();
		double totalCost = o.getCost() + o.heuristic();

		return (int) (thisTotalCost - totalCost);
	}

	/**
	 * The branching factor of this search graph are much higher than the planning.
	 * Generate all of the possible successor states and their associated actions in
	 * this method.
	 *
	 * @return A list of the possible successor states and their associated actions
	 */
	public List<GameState> generateChildren() {
		List<GameState> returnList = new ArrayList<>();
		for (List<Peasant> listPea : returnPeasantsList()) {
			Position goldBest = findBestResource(new Position(listPea.get(0).xPos, listPea.get(0).yPos), ResourceNode.Type.GOLD_MINE);
			if (goldBest != null) {
				StripActionImpl goToGold = new StripActionImpl(listPea, goldBest, this);
				if (goToGold.preconditionsMet(this)) {
					returnList.add(goToGold.apply(this));
				}
			}
			Position woodBest = findBestResource(new Position(peasantUnits.get(0).xPos, peasantUnits.get(0).yPos), ResourceNode.Type.TREE);
			if (woodBest != null) {
				StripActionImpl goToWood = new StripActionImpl(listPea, woodBest, this);
				if (goToWood.preconditionsMet(this)) {
					returnList.add(goToWood.apply(this));
				}
			}
			Position townhallPosition = new Position(this.townHall.getXPosition(), this.townHall.getYPosition());
			StripActionImpl depositResource = new StripActionImpl(listPea, townhallPosition,null, this);
			if (depositResource.preconditionsMet(this)) {
				returnList.add((depositResource.apply(this)));
			}
			StripActionImpl goToTownhall = new StripActionImpl(listPea, townhallPosition, this);
			if (goToTownhall.preconditionsMet(this)) {
				returnList.add(goToTownhall.apply(this));
			}
			StripActionImpl goldHarvest = new StripActionImpl(listPea, null, goldBest, this);
			if (goldHarvest.preconditionsMet(this)) {
				returnList.add(goldHarvest.apply(this));
			}
			StripActionImpl woodHarvest = new StripActionImpl(listPea, null,woodBest, this);
			if (woodHarvest.preconditionsMet(this)) {
				returnList.add(woodHarvest.apply(this));
			}
		}
		return returnList;
	}
    
	private Position findBestResource(Position currentPosition, ResourceNode.Type type) {
		Position best = null;
		int resource = 0;
		int dist = 0;
		int requiredAmount,currentAmount;
		int[][] map;
		if(type == ResourceNode.Type.GOLD_MINE) {
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
						dist = distance;
						best = new Position(i, j);
						resource = currentBest;
					} else {
						if(distance <= dist && currentBest >= resource || currentBest >= requiredAmount - currentAmount) {
							dist = distance;
							best = new Position(i, j);
							resource = currentBest;
						}else {
							if(resource >= 100 && currentBest >= resource && currentBest >= 100 && resource < requiredAmount - currentAmount) {
								dist = distance;
								best = new Position(i, j);
								resource = currentBest;
							}
						}
					}
				}
			}
		}
		return best;
	}
    
    private List<List<Peasant>> returnPeasantsList() {
		List<List<Peasant>> listPeasants = new ArrayList<>();
        List<Peasant> peasants = new ArrayList<>();
        this.peasantUnits.stream().forEach(p1->{
        	Peasant p2 = new Peasant(p1.id, p1.xPos, p1.yPos, p1.containsGold, p1.containsWood, p1.amount, p1.adjPos);
        	peasants.add(p2);
        });

		for (int i = 0; i < (1<<this.peasantUnits.size()); i++) {
			List<Peasant> list1 = new ArrayList<>();
			for (int j = 0; j < this.peasantUnits.size(); j++) {
				if ((i & (1 << j)) > 0) {
					list1.add(peasants.get(j));
				}
			}
			listPeasants.add(list1);
		}
		listPeasants.remove(0);
		return listPeasants;
	}

	/**
	 * Write your heuristic function here. Remember this must be admissible for the
	 * properties of A* to hold. If you can come up with an easy way of computing a
	 * consistent heuristic that is even better, but not strictly necessary.
	 *
	 * Add a description here in your submission explaining your heuristic.
	 *
	 * @return The value estimated remaining cost to reach a goal state from this
	 *         state.
	 */
     public double heuristic() {
        int goldDiffernce = requiredGold - currentGold;
     	int woodDifference = requiredWood - currentWood;
        double result = goldDiffernce + woodDifference;
		for (Peasant peasant : peasantUnits) {
			int cargoAmount = peasant.amount;
			if (cargoAmount > 0) {
				if (deposit(peasant)) {
					result = result - cargoAmount * 0.63;
				} else {
					result = result - cargoAmount * 0.5;
				}
			} else {
                int anount = harvest(peasant);
                if (anount > 0) {
                    result = result -  anount * 0.25;
                }
			}
		}
 		this.heuristic = result / peasantUnits.size();
 		return result;
 	}

	public boolean deposit(Peasant p) {
		//(Math.sqrt(Math.pow(p.x_pos - townHall.getXPosition(), 2) + Math.pow(p.y_pos - townHall.getYPosition(), 2)
		if (Math.abs(p.yPos - townHall.getYPosition()) <= 1 && Math.abs(p.xPos - townHall.getXPosition()) <= 1) {
			return true;
		}
		return false;
	}

    public int harvest(Peasant p) {
        int k = 0;
        for (int i = p.xPos - 1; i <= p.xPos + 1; i++) {
			for (int j = p.yPos - 1; j <= p.yPos + 1; j++) {
				if (goldMapArray[i][j] + woodmapArray[i][j] > k && goldMapArray[i][j] != 0 || woodmapArray[i][j] != 0) {
					k = goldMapArray[i][j] + woodmapArray[i][j];
                }
			}
		}
		return k>100 ? 100 : k;
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
    		if(gState.currentGold==this.currentGold && gState.currentWood== this.currentWood && gState.units==this.units) {
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

	public class Peasant {
	    public int id;
	    public Position adjPos;
	    public int xPos;
	    public int yPos;
	    public boolean containsWood;
	    public boolean containsGold;
	    public int amount = 0;

	    public void clear_cargo() {
			this.containsGold = false;
			this.containsWood = false;
			this.amount = 0;
		}

		public Peasant(int id, int xPos, int yPos, boolean containsGold, boolean containsWood, int anount, Position adjPos) {
			this.id = id;
			this.xPos = xPos;
			this.yPos = yPos;
			this.containsGold = containsGold;
			this.containsWood = containsWood;
			this.amount = anount;
			this.adjPos = adjPos;
		}

	}

}