package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;

import javax.management.RuntimeErrorException;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {
	
    int xRange=0, yRange=0, playerHp, playerDamage, utility;

    List<ResourceNode.ResourceView> blockedResources;

    List<Direction> directions ;
	public List<PlayerData> players;
    public List<PlayerData> archers;


//	public AstarAgent aStarAgent;
//
//	public int utility;

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
    public GameState(State.StateView state) {
    	System.out.println("Constructing GameState");
    	this.xRange= state.getXExtent();
    	this.yRange=state.getYExtent();
    	this.blockedResources = state.getAllResourceNodes();
        List<Unit.UnitView> playersTemp, archersEnemyTemp;
        playersTemp = state.getUnits(0);
        archersEnemyTemp = state.getUnits(1);
		
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
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     * @throws Exception 
     */
    public double getUtility() {
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
        utility = (1 * playerInitialHP) + (-7 * archersInitialHP) + (-2 * initialDistance);
		return utility;
    }
    
    private int getPositionDistance(PlayerData footman, PlayerData archer) {
    	return Math.abs(footman.xPosition - archer.xPosition) + Math.abs(footman.yPosition - archer.yPosition);
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
    public List<GameStateChild> getChildren() {return null;}
    
	
	
}
