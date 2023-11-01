package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.util.Direction;

public class StripsActionImpl implements StripsAction{
	
	Position positionCurrent;
	Position townHallPos;
    UnitView unitView;
    Action sepiaAction;
    GameState parent;
    Boolean gold;
    
	public StripsActionImpl (UnitView unit, Position pos, Position townHall, GameState parent, Boolean gold1) {
		this.unitView = unit;
		this.positionCurrent = pos;
		this.townHallPos = townHall;
		this.parent = parent;
        this.gold = gold1;
	}
	
	
    private Direction getDirection(int originalX, int originalY, int currentX, int currentY) {
        for (Direction d : Direction.values()) {
            if((currentX - originalX) == d.xComponent() && (currentY - originalY) == d.yComponent()) {
                return d;
            }
        }
        return null;
    }

	@Override
	public boolean preconditionsMet(GameState state) {
		int x = positionCurrent.x;
		int y = positionCurrent.y;
		if(null!=townHallPos) {
	        if (this.gold == true) {
	            if ((unitView.getCargoAmount() > 0 && unitView.getCargoType() == ResourceType.GOLD && positionCurrent.equals(townHallPos))) {
	    			return true;
	    		}
	            return false;
	        }

	        if (this.gold == false) {
	            if ((unitView.getCargoAmount() > 0 && unitView.getCargoType() == ResourceType.WOOD && positionCurrent.equals(townHallPos))) {
	    			return true;
	    		}
	            return false;
	        }
		}else if(null!=gold) {
			if (x >= state.xExtent || x < 0 || y >= state.yExtent || y < 0) {
				return false;
			}

			else {
	            if (this.gold == true) {
	                if (state.goldMapArray[x][y] > 0 && unitView.getCargoAmount() == 0) {
	    				return true;
	    			}
	            }
	            if (this.gold == false) {
	                if (state.woodmapArray[x][y] > 0 && unitView.getCargoAmount() == 0) {
	    				return true;
	    			}
	            }

				return false;
			}
		}else if(null == gold) {
			if (x < state.xExtent&& x > 0 && y < state.yExtent && y > 0
					&& !state.map[x][y] ) {
				for (UnitView u : state.playerUnits) {
					if (x == u.getXPosition() && y == u.getYPosition()) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		GameState result = new GameState(state);
		int xPos = positionCurrent.x;
		int yPos = positionCurrent.y;
		int amount=0;
        UnitView originalUnit = result.getUnit(unitView.getID(), result.playerUnits);
		
		if(null!=townHallPos) {
			return applyWhenTownhallNotNull(result, originalUnit);
		}else if(null!=gold) {
			return applyWhenForGoldNotNull(result, originalUnit, amount, xPos, yPos);
		}else if(null == gold){
			UnitTemplate currentTemplate = new UnitTemplate(originalUnit.getID());
			currentTemplate.setCanGather(true);

			Unit currentUnit = new Unit(currentTemplate, originalUnit.getID());
	        currentUnit.setxPosition(unitView.getXPosition());
			currentUnit.setyPosition(unitView.getYPosition());

			if (unitView.getCargoAmount() > 0) {
				currentUnit.setCargo(unitView.getCargoType(), unitView.getCargoAmount());
			}
	        result.playerUnits.remove(originalUnit);
			result.playerUnits.add(new UnitView(currentUnit));

	        Position originalPosition = new Position(unitView.getXPosition(), unitView.getYPosition());
	        double dist = originalPosition.chebyshevDistance(positionCurrent);
	        result.cost = dist + 1;
	        result.heuristic();
	        result.plan.add(this);

			sepiaAction = Action.createCompoundMove(originalUnit.getID(), xPos, yPos);

			return result;
		}
		
		return null;
	}
	
	private GameState applyWhenTownhallNotNull(GameState result, UnitView originalUnit) {
        Unit currentUnit = new Unit(new UnitTemplate(originalUnit.getID()), originalUnit.getID());

        currentUnit.setxPosition(unitView.getXPosition());
        currentUnit.setyPosition(unitView.getYPosition());
        currentUnit.clearCargo();
        result.playerUnits.remove(originalUnit);
        result.playerUnits.add(new UnitView(currentUnit));

        if (this.gold == true) {
        	result.currentGold = result.currentGold + unitView.getCargoAmount();
        }
        else {
        	result.currentWood = result.currentWood + unitView.getCargoAmount();
        }
        result.cost = result.cost + 1;
        result.heuristic();
        result.plan.add(this);

        sepiaAction = Action.createPrimitiveDeposit(originalUnit.getID(), getDirection(currentUnit.getxPosition(),currentUnit.getyPosition(), townHallPos.x, townHallPos.y));
        return result;
	}
	
	private GameState applyWhenForGoldNotNull(GameState result, UnitView originalUnit, int amount, int xPos, int yPos) {
        if (this.gold == true) {
            amount = result.goldMapArray[xPos][yPos];
        }
        if (this.gold == false) {
            amount = result.woodmapArray[xPos][yPos];
        }
		UnitTemplate currentTemplate = new UnitTemplate(originalUnit.getID());
        currentTemplate.setCanGather(true);
        
		Unit currentUnit = new Unit(currentTemplate, originalUnit.getID());
		currentUnit.setxPosition(unitView.getXPosition());
		currentUnit.setyPosition(unitView.getYPosition());
		
		ResourceView originalResource = result.getResource(xPos, yPos, result.resourceNodes);
		ResourceNode currentResource = null;
		
        if (this.gold == true) {
    		if (amount < 100) {
    			result.goldMapArray[xPos][yPos] = 0;
    			currentUnit.setCargo(ResourceType.GOLD, amount);
    			amount = 0;
    		}
            else {
    			result.goldMapArray[xPos][yPos] -= 100;
    			currentUnit.setCargo(ResourceType.GOLD, 100);
    			amount -= 100;
    		}
            currentResource = new ResourceNode(ResourceNode.Type.GOLD_MINE, xPos, yPos, amount, originalResource.getID());
        }

        if (this.gold == false) {
            if (amount < 100) {
    			result.woodmapArray[xPos][yPos] = 0;
    			currentUnit.setCargo(ResourceType.WOOD, amount);
                amount = 0;
    		}
    		else {
    			result.woodmapArray[xPos][yPos] -= 100;
    			currentUnit.setCargo(ResourceType.WOOD, 100);
    			amount -= 100;
    		}
            currentResource = new ResourceNode(ResourceNode.Type.TREE, xPos, yPos, amount, originalResource.getID());
        }
        result.playerUnits.remove(originalUnit);
        result.resourceNodes.remove(originalResource);
        result.playerUnits.add(new UnitView(currentUnit));
        result.resourceNodes.add(new ResourceView(currentResource));
        result.cost = result.cost + 1;
        result.heuristic();
        result.plan.add(this);

        sepiaAction = Action.createPrimitiveGather(originalUnit.getID(), getDirection(originalUnit.getXPosition(), currentUnit.getyPosition(), xPos, yPos));
        return result;
	}
	
	@Override
	public Action createActionSEPIA() {
		return sepiaAction;
	}

}
