package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class StripsActionImpl implements StripsAction{
	
	Boolean b;
	GameState parent;
	Action ActionSEPIA ;
	Position townHallPos =null;
	Position posNew;
	UnitView peasant;


	
	public StripsActionImpl(UnitView peasant, Position newPosition, Position townHallPosition, GameState parent, Boolean b) {
		this.peasant = peasant;
		this.posNew = newPosition;
		this.townHallPos = townHallPosition;
		this.parent = parent;
		this.b=b;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		int xPos = posNew.x;
		int yPos = posNew.y;
		if(null==this.b) {
			if (yPos >= state.yExtent || state.map[xPos][yPos] || xPos >= state.xExtent || xPos < 0 || yPos < 0) {
				return false;
			} else {
				for (UnitView unit : state.playerUnits) {
					if (xPos == unit.getXPosition() && yPos == unit.getYPosition()) {
						return false;
					}
				}
			}
			return true;
		} else if(null==this.townHallPos&& b==true) {
			return checkPreConditionsMet(xPos,yPos,state,state.goldMapArray);
		}else if(null!=this.townHallPos&& b==true) {
			if (checkForGold() && posNew.equals(townHallPos)) {
				return true;
			} else {
				return false;
			}
		}else if(b==false && null==this.townHallPos) {
			return checkPreConditionsMet(xPos,yPos,state,state.woodmapArray);
		}else if(b==false && null!=this.townHallPos) {
			if (checkForWood() && posNew.equals(townHallPos)) {
				return true;
			}else {
				return false;
			}
		} 
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		GameState gameState = new GameState(state);
		UnitView uniView1 = gameState.getUnit(peasant.getID(), gameState.playerUnits);
		UnitTemplate unitTemplate = new UnitTemplate(uniView1.getID());
		unitTemplate.setCanGather(true);
		Unit unit = new Unit(unitTemplate, uniView1.getID());
		unit.setxPosition(peasant.getXPosition());
		unit.setyPosition(peasant.getYPosition());
		int xPos1 = posNew.x;
		int yPos1 = posNew.y;
		if(null==this.b) {
			unit.setxPosition(xPos1);
			unit.setyPosition(yPos1);
			if (peasant.getCargoAmount() > 0) {
				unit.setCargo(peasant.getCargoType(), peasant.getCargoAmount());
			}
			gameState.playerUnits.remove(uniView1);
			gameState.playerUnits.add(new UnitView(unit));
			ActionSEPIA = Action.createCompoundMove(uniView1.getID(), xPos1, yPos1);
			Position peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
			double cost = peasantPos.chebyshevDistance(posNew);
			gameState.cost+=cost;
			gameState.heuristic();
			gameState.plan.add(this);
			return gameState;
		} else if(null==this.townHallPos && b==true) {
			int goldRemaining = gameState.goldMapArray[xPos1][yPos1];
			ResourceView rView = state.getResource(xPos1, yPos1, state.resourceNodes);
			ResourceNode rNode;
			if (goldRemaining < 100) {
				gameState.goldMapArray[xPos1][yPos1] = 0;
				unit.setCargo(ResourceType.GOLD, goldRemaining);
				goldRemaining = 0;
				rNode = new ResourceNode(ResourceNode.Type.GOLD_MINE, xPos1, yPos1, goldRemaining, rView.getID());
			} else {
				gameState.goldMapArray[xPos1][yPos1] -= 100;
				unit.setCargo(ResourceType.GOLD, 100);
				goldRemaining -= 100;
				rNode = new ResourceNode(ResourceNode.Type.GOLD_MINE, xPos1, yPos1, goldRemaining,rView.getID());
			}
			gameState.resourceNodes.remove(rView);
			gameState.resourceNodes.add(new ResourceView(rNode));
			ActionSEPIA = Action.createPrimitiveGather(uniView1.getID(), getDir(unit.getxPosition(),unit.getyPosition(), xPos1, yPos1));
			return setGameStateProperties(gameState, uniView1,unit);
		} else if(null!=this.townHallPos && b==true) {
			gameState.currentGold+= peasant.getCargoAmount();
			ActionSEPIA = Action.createPrimitiveDeposit(uniView1.getID(), getDir(unit.getxPosition(),unit.getyPosition(), townHallPos.x, townHallPos.y));
			return setGameStateProperties(gameState, uniView1,unit);
		} else if(b==false && null==this.townHallPos) {
			int i = gameState.woodmapArray[xPos1][yPos1];
			ResourceView rView = state.getResource(xPos1, yPos1, state.resourceNodes);
			ResourceNode rNode;
			if (i < 100) {
				gameState.woodmapArray[xPos1][yPos1] = 0;
				unit.setCargo(ResourceType.WOOD, i);
				rNode = new ResourceNode(ResourceNode.Type.TREE, xPos1, yPos1, 0, rView.getID());
			} else {
				gameState.woodmapArray[xPos1][yPos1] -= 100;
				unit.setCargo(ResourceType.WOOD, 100);
				i = i - 100;
				rNode = new ResourceNode(ResourceNode.Type.TREE, xPos1, yPos1, i, rView.getID());
			}
			gameState.resourceNodes.remove(rView);
			gameState.resourceNodes.add(new ResourceView(rNode));
			ActionSEPIA = Action.createPrimitiveGather(uniView1.getID(), getDir(unit.getxPosition(),unit.getyPosition(), xPos1, yPos1));
			return setGameStateProperties(gameState, uniView1,unit);
		} else if(b==false && null!=this.townHallPos) {
			ActionSEPIA = Action.createPrimitiveDeposit(uniView1.getID(), getDir(unit.getxPosition(),unit.getyPosition(), townHallPos.x, townHallPos.y));
			return setGameStateProperties(gameState, uniView1,unit);
		}
		return new GameState();
	}
	
	private Direction getDir(int pX, int pY, int gX, int gY) {
		int x = gX - pX;
		int y = gY - pY;
		for (Direction d : Direction.values()) {
			if(y == d.yComponent() && x == d.xComponent()) {
				return d;
			}
		}
		return null;
	}
	
	private GameState setGameStateProperties(GameState gameState, UnitView uniView, Unit unit) {
		gameState.playerUnits.remove(uniView);
		gameState.playerUnits.add(new UnitView(unit));
		gameState.currentWood+= peasant.getCargoAmount();
		gameState.cost = gameState.cost + 1;
		gameState.heuristic();
		gameState.plan.add(this);
		return gameState;
	}
	
	private boolean checkForGold() {
		if ( peasant.getCargoType() == ResourceType.GOLD && peasant.getCargoAmount() > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean checkForWood() {
		if (peasant.getCargoType() == ResourceType.WOOD && peasant.getCargoAmount() > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean checkPreConditionsMet(int xPos, int yPos, GameState state, int[][] array) {
		if (xPos >= state.xExtent || xPos < 0 || yPos >= state.yExtent || yPos < 0) {
			return false;
		} else {
			if (array[xPos][yPos] > 0 && peasant.getCargoAmount() == 0) {
				return true;
			}
			return false;
		}
	}

	@Override
	public GameState getParent() {
		return this.parent;
	}

	@Override
	public Action returnSepaiaActionBack() {
		return this.ActionSEPIA;
	}

}
