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
        // Initialize the best value as negative infinity
    	    double bestValue = Double.NEGATIVE_INFINITY;
    	    // Initialize the best child as null.
    	 	GameStateChild bestChild = null;

    	    for (GameStateChild child : node.state.getChildren()) {
    	    	//to call the MinMax method
    	        double value = getMinMaxValue(child, depth, alpha, beta, true); 

    	        if (value > bestValue) {
    	        	//update the best value when more better one is available
    	            bestValue = value;  
    	           // Updating the best child node.
    	            bestChild = child;  
    	        }
                // updating the best value found till now for alpha
    	        alpha = Math.max(alpha, bestValue);  // Update alpha with the best value found so far.

    	        // checking beta <= alpha to prune 
    	        if (beta <= alpha) {
    	        	//remaining children we can prune because they won't affect output
    	            break;
    	        }
    	    }
    	    //return the bestChild
    	    return bestChild;
    }




    private double getMinMaxValue (GameStateChild node, int depth, double alpha, double beta, boolean b){
	// condition to retrun utility value , because if depth is zero means termminal node reached
        if (depth <= 0) {
            return node.state.getUtility();
        }
        double maxValue = Double.NEGATIVE_INFINITY;
        double minValue = Double.POSITIVE_INFINITY;
        double value = 0;
	// loop for current nodes children
	for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren())) {
	    if (b) {
		//to maximize players trun
		maxValue = Math.max(maxValue, getMinMaxValue(child, depth - 1, alpha, beta, false));
		//prune if better alternative is there
		if(maxValue >= beta){
			return maxValue;
		}
		alpha = Math.max(alpha, maxValue);
		value = maxValue;
	    }
	    else {
		//to minimize players turn
		minValue = Math.min(minValue, getMinMaxValue(child, depth - 1, alpha, beta, true));
		//prune if better alternative is there
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
	//sorts input list of GameStateChild descending order of utility values from getUtility method.
    	// This heuristic choose because highest utility values means more states. 
    	// by sorting descending highest states will appear at starting of list.
    	children.sort((child1, child2) -> Double.compare(child2.state.getUtility(), child1.state.getUtility()));
        return children;
    }
}
