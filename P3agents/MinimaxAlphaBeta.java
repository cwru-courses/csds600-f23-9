package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;


public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of players");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta) {
        double getValue = getMinMaxValue(node, depth, alpha, beta, true);
        List<GameStateChild> childrens = node.state.getChildren();
        for (GameStateChild children : childrens) {
            if (children.state.getUtility() == getValue) {
                return children;
            }
        }
        return childrens.get(0);
    }




    private double getMinMaxValue (GameStateChild node, int depth, double alpha, double beta, boolean b){
        if (depth <= 0) {
            return node.state.getUtility();
        }
        double maxValue = Double.NEGATIVE_INFINITY;
        double minValue = Double.POSITIVE_INFINITY;
        
        double value = 0;
		for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren())) {
            if (b) {
                maxValue = Math.max(maxValue, getMinMaxValue(child, depth - 1, alpha, beta, false));
                if(maxValue >= beta){
        			return maxValue;
        		}
        		alpha = Math.max(alpha, maxValue);
                value = maxValue;
            }
            else {
                minValue = Math.min(minValue, getMinMaxValue(child, depth - 1, alpha, beta, true));
                if(minValue <= alpha){
    				return minValue;
    			}
    			beta = Math.min(minValue, beta);
                value = minValue;
            }
    	}
        return value;

    }


    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     * Sort base on utility.
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children) {
    	List<GameStateChild> child = new ArrayList<>();
    	children.stream().forEach(t->{
    		child.add(t);
    	});
    	child.sort(new Comparator<GameStateChild>() {
    		@Override
    		public int compare(GameStateChild child1, GameStateChild child2) {
	    	        if (child2.state.getUtility() < child1.state.getUtility()) {
	    	    		return 1;
	    	    	} 
			else if (child1.state.getUtility() > child2.state.getUtility()) {
	    	    		return -1;
	    	    	}
	    	        else {
	    	    		return 0;
	    	    	}
    		}
	});
        return child;
    }
}
