package edu.cwru.sepia.agent.minimax;

import java.util.*;
import edu.cwru.sepia.action.*;
import edu.cwru.sepia.agent.minimax.AstarAgent.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.util.*;

public class GameState {
	
	/**
	 * Pojo class to store the players data like HP, xPos, yPos, and damageIncured by the Player
	 * @author 
	 *
	 */
	public class PlayerData {
		public int id, playerHp, xPosition, yPosition, playerDamage;

		public PlayerData(Unit.UnitView unitView) {
			this.playerHp = unitView.getHP();
			this.xPosition = unitView.getXPosition();
			this.yPosition = unitView.getYPosition();
			this.playerDamage = unitView.getTemplateView().getBasicAttack();
			id=unitView.getID();
		}


		public PlayerData(PlayerData data){
	        this.id = data.id;
			this.xPosition = data.xPosition;
			this.yPosition = data.yPosition;
			this.playerHp = data.playerHp;
			this.playerDamage = data.playerDamage;
		}

	}

    int xRange=0, yRange=0, playerHp, playerDamage, utility;
    AstarAgent aStartAgent;

    List<ResourceNode.ResourceView> blockedResources;

    List<Direction> directions ;
	public List<PlayerData> players;
    public List<PlayerData> archers;


    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs beloning to the player.
     * You control player 0, the enemy controls player 1.
     *
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     *
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     *
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */
	public GameState(State.StateView state){

    	System.out.println("Constructing GameState");
    	this.xRange= state.getXExtent();
    	this.yRange=state.getYExtent();
    	aStartAgent = new AstarAgent(xRange, yRange);
    	this.blockedResources = state.getAllResourceNodes();
        List<Unit.UnitView> playersTemp, archersEnemyTemp;
        playersTemp = state.getUnits(0);
        archersEnemyTemp = state.getUnits(1);
        this.archers= new ArrayList<>();
        this.players= new ArrayList<>();
        this.directions = new ArrayList<>();
		for (Unit.UnitView player : playersTemp) {
			this.players.add(new PlayerData(player));
		}
		for (Unit.UnitView archer : archersEnemyTemp) {
			this.archers.add(new PlayerData(archer));
		}
		for (Direction ValidDirection : Direction.values()) {
			if (ValidDirection == Direction.WEST || ValidDirection == Direction.SOUTH ||
					ValidDirection == Direction.NORTH || ValidDirection == Direction.EAST) {
				this.directions.add(ValidDirection);
            }
		}
		System.out.println("Ended Object Construct");
	}

	public GameState(GameState gameState){

        this.xRange = gameState.xRange;
		this.yRange = gameState.yRange;
        this.blockedResources = new ArrayList<>();
        for (ResourceNode.ResourceView r : gameState.blockedResources){
			this.blockedResources.add(r);
		}
        aStartAgent = new AstarAgent(xRange, yRange);

		this.players = new ArrayList<>();
		for (PlayerData data : gameState.players) {
			this.players.add(new PlayerData(data));
		}
		this.archers = new ArrayList<>();
		for (PlayerData data : gameState.archers) {
			this.archers.add(new PlayerData(data));
		}
		Direction[] availableDirections = Direction.values();
		List<Direction> directions = new ArrayList<>();
		for(Direction dir : availableDirections) {
			if (dir == Direction.NORTH || dir == Direction.EAST || dir == Direction.WEST || dir == Direction.SOUTH) {
				directions.add(dir);
            }
		}
		this.directions = directions;
	}

	public void enforceActions(Map<Integer, Action> actions) throws Exception {
        for (Map.Entry<Integer, Action> action: actions.entrySet()) {
            if (action.getValue().getType() == ActionType.COMPOUNDATTACK) {
                TargetedAction actionTargeted = (TargetedAction) action.getValue();
                int idPlayer = actionTargeted.getUnitId();
                int idArcher = actionTargeted.getTargetId();
                PlayerData archer = getUnit(idArcher);
                archer.playerHp -= getUnit(idPlayer).playerDamage;
            }
            else if (action.getValue().getType() == ActionType.PRIMITIVEMOVE) {
                DirectedAction actionDirection = (DirectedAction) action.getValue();
                int idPlayer = actionDirection.getUnitId();
                PlayerData player = getUnit(idPlayer);
                Direction playerDirection = actionDirection.getDirection();
                player.xPosition += playerDirection.xComponent();
                player.yPosition += playerDirection.yComponent();
            }
            else {
            	throw new Exception("Action not found");
            }
        }
	}

	private PlayerData getUnit(int id) {
        List<PlayerData> playerData = new ArrayList<>(players);
		playerData.addAll(archers);

		for (PlayerData player : playerData) {
			if (player.id == id) {
				return player;
			}
		}
		return null;
	}

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features:
     * A simple utility calculator based on the distance between footmen and archers, and also if foormen are attacking or not.
     *
     * @return The weighted linear combination of the features
     */
	public int getUtility() {
    	int playerInitialHP=0;
    	int archersInitialHP=0;
    	for(PlayerData data: this.players) {
    		playerInitialHP= playerInitialHP+data.playerHp;
    	}
    	for(PlayerData data: this.archers) {
    		archersInitialHP = archersInitialHP + data.playerHp;
    	}
    	
    	int initialDistance =0;
    	if (players.size() == 1 && archers.size() == 1) {
        	initialDistance += getPositionDistance(players.get(0), archers.get(0));
        }
        else if (players.size() == 2 && archers.size() == 1) {
        	initialDistance += getPositionDistance(players.get(0), archers.get(0));
        	initialDistance += getPositionDistance(players.get(1), archers.get(0));
        }
        else if (players.size() == 1 && archers.size() == 2) {
        	initialDistance += getPositionDistance(players.get(0), archers.get(0));
        	initialDistance += getPositionDistance(players.get(0), archers.get(1));
        }
        else if (players.size() == 2 && archers.size() == 2) {
    		initialDistance += getPositionDistance(players.get(0), archers.get(0));
    		initialDistance += getPositionDistance(players.get(1), archers.get(1));
        }
        else {
        	try {
				throw new Exception("Invalid no:of archers/players");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        utility = (1 * playerInitialHP) + (-10 * archersInitialHP) + (-2 * initialDistance);
		return utility;
	}


	/**
	 * 
	 * @param footman
	 * @param archer
	 * @return
	 */
    private int getPositionDistance(PlayerData footman, PlayerData archer) {
        return (int) Math.sqrt(Math.pow(Math.abs(footman.xPosition - archer.xPosition),2)+Math.pow(Math.abs(footman.yPosition - archer.yPosition), 2));
    }


    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     *
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     *
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     *
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     *
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     *
     * @return All possible actions and their associated resulting game state
     */
	public List<GameStateChild> getChildren() {

    	int playerId1 = players.get(0).id;
		List<Action> playerActions1 = getActions(players.get(0), archers);

        int playerId2=0;
        List<Action> playerActions2 = new ArrayList<>();
        boolean isTwoPlayers = false;
		if (players.size() == 2) {
			playerId2 = players.get(1).id;
			playerActions2 = getActions(players.get(1), archers);
			isTwoPlayers = true;
		}

		List<GameStateChild> childrensAllowed = new ArrayList<>();
		Map<Integer, Action> actions = new HashMap<>();
        if (isTwoPlayers) {
            for (Action action1 : playerActions1) {
                for (Action action2 : playerActions2) {
                	actions = new HashMap<>();
                	actions.put(playerId1, action1);
                	actions.put(playerId2, action2);
                    if (!actionType(actions, playerId1, playerId2)) {
                        GameState gState = new GameState(this);
                        try {
							gState.enforceActions(actions);
						} catch (Exception e) {
							e.printStackTrace();
						}
                        childrensAllowed.add(new GameStateChild(actions, gState));
                    }
                }
            }
        }
        else {
            for (Action action1 : playerActions1) {
            	actions = new HashMap<>();
            	actions.put(playerId1, action1);
                GameState gameState = new GameState(this);
                try {
					gameState.enforceActions(actions);
				} catch (Exception e) {
					e.printStackTrace();
				}
                childrensAllowed.add(new GameStateChild(actions, gameState));
            }
        }
        return childrensAllowed;
    }
    
	public GameState(Integer utility) {
		archers = new ArrayList<PlayerData>();
		archers = new ArrayList<PlayerData>();
		aStartAgent = new AstarAgent(xRange, yRange);
        this.utility = utility;
        this.blockedResources = new ArrayList<>();
		for (Direction ValidDirection : Direction.values()) {
			if (ValidDirection == Direction.WEST || ValidDirection == Direction.SOUTH ||
					ValidDirection == Direction.NORTH || ValidDirection == Direction.EAST) {
				this.directions.add(ValidDirection);
            }
		}
	}



	/**
	 * 
	 * @param actionMap
	 * @param player1
	 * @param player2
	 * @return
	 */
	private boolean actionType(Map<Integer, Action> actionMap, int player1, int player2) {

		PlayerData playerOneData = getUnit(player1);
		PlayerData player2Data = getUnit(player2);
		Action playerOneAction = actionMap.get(player1);
		Action playerTwoAction = actionMap.get(player2);

		if (playerOneAction.getType() == ActionType.PRIMITIVEMOVE && playerTwoAction.getType() == ActionType.PRIMITIVEMOVE) {
            DirectedAction action1 = (DirectedAction) playerOneAction;
            DirectedAction action2 = (DirectedAction) playerTwoAction;
            int playerOnex = playerOneData.xPosition + action1.getDirection().xComponent();
            int playerOney = playerOneData.yPosition + action1.getDirection().yComponent();
            int playerTwox = player2Data.xPosition + action2.getDirection().xComponent();
            int playerTwoy = player2Data.yPosition + action2.getDirection().yComponent();

            return (playerOnex == playerTwox) && (playerOney == playerTwoy);
		}
		return false;
	}



	/**
	 * 
	 * @param player
	 * @param archersList
	 * @return
	 */
	private List<Action> getActions(PlayerData player, List<PlayerData> archersList) {

        List<PlayerData> players = new ArrayList<>();
        players.addAll(archers);
		List<Action> actions = new ArrayList<>();
		if (blockedResources.size() > 0 ) {
			Stack<MapLocation> aStartPath = aStartAgent.findPath(blockedResources, player, getArcher(player, archersList));
			if (aStartPath != null && aStartPath.size() > 0) {
				MapLocation loc = aStartPath.pop();
				actions.add(Action.createPrimitiveMove(player.id, getDirection(player, loc)));
			}
		}
		else {
			for (Direction direction : directions) {
				if (validateMove(player.xPosition + direction.xComponent(), player.yPosition + direction.yComponent(), players)) {
					actions.add(Action.createPrimitiveMove(player.id, direction));
				}
			}
		}
		for (PlayerData data : getArchersWhoCanAttack(player)) {
			actions.add(Action.createCompoundAttack(player.id, data.id));
		}
		return actions;
	}



	/**
	 * Gives valid Directions based on the position of player
	 * @param player
	 * @param loc
	 * @return
	 */
	private Direction getDirection(PlayerData player, MapLocation loc) {
		if (player.xPosition == loc.x) {
			if (player.yPosition - loc.y == 1) {
                return Direction.NORTH;
            }
			else if (player.yPosition - loc.y == -1) {
                return Direction.SOUTH;
            }
            else {
                return null;
            }
		}
		else if (player.yPosition == loc.y) {
			if (player.xPosition - loc.x == 1) {
                return Direction.WEST;
            }
            else if (player.xPosition - loc.x == -1) {
                return Direction.EAST;
            }
            else {
                return null;
            }
		}
		return null;
	}


    private boolean validateMove(int xPosition, int yPosition, List<PlayerData> players) {
        int counter = 0;
        if ((xPosition >= 0 && xPosition < xRange) && (yPosition >= 0 && yPosition < yRange)) {
            for (PlayerData data : players) {
				if (data.xPosition == xPosition && data.yPosition == yPosition) {
                    counter++;
                    if (counter == 2) {
                        return false;
                    }
				}
            }
            return true;
        }
		return false;
	}


    /**
     * Get archer playerData 
     * @param player
     * @param archerslist
     * @return archer
     */
    private PlayerData getArcher(PlayerData player, List<PlayerData> archerslist) {
		int minDist = Integer.MAX_VALUE;

		PlayerData archer = null;
		for (PlayerData data : archerslist) {
            int archerDist = Math.abs(player.xPosition - data.xPosition) + Math.abs(player.yPosition - data.yPosition);
			if (archerDist < minDist){
				minDist = archerDist;
				archer = data;
			}
		}
        return archer;
	}


    /**
     * Gives details of archers who can attack.
     * @param player
     * @return archers
     */
	private List<PlayerData> getArchersWhoCanAttack(PlayerData player) {
		List<PlayerData> archers = new ArrayList<>();
		for (PlayerData data : this.archers) {
			if (1 >= (Math.abs(player.xPosition - data.xPosition) + Math.abs(player.yPosition - data.yPosition))) {
				archers.add(data);
			}
		}
		return archers;
	}


}