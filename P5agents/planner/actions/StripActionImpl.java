package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.util.Direction;

public class StripActionImpl implements StripsAction{
    List<Peasant> peasants;
	List<Position> newPosition = new ArrayList<>();
    Position townHallPos;
    Position resourcePos;
	List<Action> sepiaAction = new ArrayList<>();
	GameState parent;
    Position startPos;
    Position destPos;
    List<Position> pos = new ArrayList<>();
	
	public StripActionImpl(List<Peasant> peasants, Position townHallPosition,Position resourcePosition, GameState parent) {
        peasants.stream().forEach(pos->{newPosition.add(new Position(pos.xPos, pos.yPos));});
        this.peasants = peasants;
        this.resourcePos=resourcePosition;
        this.townHallPos = townHallPosition;
        this.parent = parent;
        this.destPos=null;
        this.startPos=null;
	}
	
	public StripActionImpl(List<Peasant> peasants, Position position, GameState parent) {
		this.peasants = peasants;
		this.destPos = position;
		this.parent = parent;
		this.startPos = peasants.get(0).adjPos;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(null!=this.destPos) {
			List<Position> positions = new ArrayList<>();
			for (int i = destPos.x - 1; i <= destPos.x + 1; i++) {
				for (int j = destPos.y - 1; j <= destPos.y + 1; j++) {
					positions.add(new Position(i, j));
				}
			}
			int gAmount = parent.goldMapArray[destPos.x][destPos.y];
			int wAmount = parent.woodmapArray[destPos.x][destPos.y];
			if(gAmount != 0 || wAmount != 0) {
				boolean b = getBool(gAmount,wAmount,peasants,parent);
				if(!b) {
					return b;
				}
			}
			positions.forEach(position->{boolean[] boolArray = {false};
				if(getLegalPos(position)) {
					state.peasantUnits.forEach(peasant->{if(peasant.xPos == position.x && peasant.yPos == position.y) {boolArray[0] = true;}});
					if (!boolArray[0]) {
						pos.add(position);
					}
				}
			});
			return pos.size() > peasants.size();
		}
		return returnflag();
	}
	
	private boolean getBool(int gAmount,int wAmount,  List<Peasant> peasants, GameState parent) {
		if((gAmount != 0 || wAmount != 0) && ((gAmount - 1) / 100) + 1 < peasants.size()) {
			return false;
		} else if((gAmount != 0 ) &&(((parent.requiredGold - parent.currentGold) - 1) / 100) + 1 < peasants.size()) {
			return false;
		} else if((wAmount != 0) &&(((parent.requiredWood - parent.currentWood) - 1) / 100) + 1 < peasants.size()) {
			return false;
		}
		return true;
	}
	
    private boolean getLegalPos(Position position) {
        if (position.x < 0 || position.x >= parent.xExtent || position.y < 0 || position.y >= parent.yExtent) {
            return false;
        } else if (null!=resourcePos &&( Math.abs(position.x - resourcePos.x) > 1 || Math.abs(position.y - resourcePos.y) > 1)) {
            return false;
        } else if (null!=townHallPos &&( Math.abs(position.x - townHallPos.x) > 1 || Math.abs(position.y - townHallPos.y) > 1)) {
            return false;
        } else if (parent.map[position.x][position.y]) {
            return false;
        }
        return true;
    }
    
    private Direction getDitectionLegal(int x, int y, int x1, int y1) {
    	Direction[] dirArray=new Direction[1];
    	Arrays.asList(Direction.values()).stream().forEach(direction->{if((x1 - x) == direction.xComponent() && (y1 - y) == direction.yComponent()) {dirArray[0]= direction;}});
    	return dirArray[0];
    }
    
    private boolean returnflag() {
    	int i=0;
    	for(Peasant pea: this.peasants) {
			if (null!=this.townHallPos && pea.amount <= 0) {
                return false;
            } else if (null!=this.resourcePos && pea.amount != 0) {
                return false;
            } else if (!getLegalPos(newPosition.get(i))) {
                return false;
            }
			i++;
		}
		return true;
    }

	@Override
	public GameState apply(GameState state) {
		GameState result = new GameState(state);
		if(null!=townHallPos) {
			for(Peasant pea: this.peasants) {
				Peasant p1 = getPeasant(pea.id, result.peasantUnits);
	            if (p1.containsGold) {
	            	result.currentGold +=p1.amount;
	            } else if (p1.containsWood) {
	            	result.currentWood += p1.amount;
	            }
	            p1.clear_cargo();
	            sepiaAction.add(Action.createPrimitiveDeposit(p1.id, getDitectionLegal(p1.xPos, p1.yPos, townHallPos.x, townHallPos.y)));
			}
		}else if(null!=resourcePos) {
            int xValue = resourcePos.x;
            int yValue = resourcePos.y;
			int amount = Math.max(result.goldMapArray[xValue][yValue], result.woodmapArray[xValue][yValue]);
			ResourceView rView = getResource(xValue, yValue, state.resourceNodes);
			ResourceNode rNode = null;
			for(Peasant pea: this.peasants) {
				Peasant p2 = getPeasant(pea.id, result.peasantUnits);
				if (rView.getType() == ResourceNode.Type.GOLD_MINE) {
					if (amount <= 100) {
						result.map[xValue][yValue] = false;
						result.goldMapArray[xValue][yValue] = -1;
						setPeasantvalues(p2,true,amount);
						amount = 0;
					} else {
						result.goldMapArray[xValue][yValue] -= 100;
						setPeasantvalues(p2,true,100);
						amount = amount - 100;
					}
					rNode = new ResourceNode(ResourceNode.Type.GOLD_MINE, xValue, yValue, amount, rView.getID());
				} else if (rView.getType() == ResourceNode.Type.TREE) {
					if (amount <= 100) {
						result.map[xValue][yValue] = false;
						result.woodmapArray[xValue][yValue] = -1;
						setPeasantvalues(p2,false,amount);
						amount = 0;
					} else {
						result.woodmapArray[xValue][yValue] -= 100;
						setPeasantvalues(p2,false,100);
						amount = amount- 100;
					}
					rNode = new ResourceNode(ResourceNode.Type.TREE, xValue, yValue, amount, rView.getID());
				}
				result.resourceNodes.remove(rView);
				result.resourceNodes.add(new ResourceView(rNode));
				sepiaAction.add(Action.createPrimitiveGather(p2.id, getDitectionLegal(p2.xPos, p2.yPos, xValue, yValue)));
			}
		} else if(null!=this.destPos) {
			peasants.stream().forEach(peasant->{
				Position position = pos.get(0);
				Peasant peasantNew = getPeasant(peasant.id, result.peasantUnits);
	            peasantNew.xPos = position.x;
	            peasantNew.yPos = position.y;
				peasantNew.adjPos = destPos;
				pos.remove(position);
				sepiaAction.add(Action.createCompoundMove(peasant.id, position.x, position.y));
			});
		}
		result.cost +=1;
        result.heuristic();
        result.plan.add(this);
		return result;
	}
	
    private ResourceView getResource(int x, int y, List<ResourceView> resources) {
        for (ResourceView resource : resources) {
            if (resource.getXPosition() == x && resource.getYPosition() == y) {
                return resource;
            }
        }
        return null;
    }
	
	private Peasant getPeasant(int peasantId, List<Peasant> peasants) {
		for (Peasant p : peasants) {
			if (p.id == peasantId) {
				return p;
			}
		}
		return null;
	}
	
	private void setPeasantvalues(Peasant peasant, boolean b, int amount) {
		peasant.amount = amount;
		peasant.containsGold = b;
		peasant.containsWood = !b;
	}

	@Override
	public GameState getParent() {
		return this.parent;
	}

	@Override
	public List<Action> createSEPIAaction() {
		return this.sepiaAction;
	}
	

}
