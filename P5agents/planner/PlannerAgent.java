package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;



public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);

        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);
        System.out.println(plan.size());

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

	@Override
	public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

	}

	@Override
	public void savePlayerData(OutputStream outputStream) {

	}

	@Override
	public void loadPlayerData(InputStream inputStream) {

	}

	/**
	 * Perform an A* search of the game graph. This should return your plan as a
	 * stack of actions. This is essentially the same as your first assignment. The
	 * implementations should be very similar. The difference being that your nodes
	 * are now GameState objects not MapLocation objects.
	 *
	 * @param startState
	 *            The state which is being planned from
	 * @return The plan or null if no plan is found.
	 */
     private Stack<StripsAction> AstarSearch(GameState startState) {
         // Initialize the stack to store the final path, the closed list to keep track of visited states,
         // and the open list to manage states to explore.
          Stack<StripsAction> finalPath= new Stack<>();
   		Set<GameState> closedList = new HashSet<>();
   		PriorityQueue<GameState> openList = new PriorityQueue<>();
          // Add the initial state to the open list to start the search.
   		openList.add(startState);
          // While there are states to explore in the open list
   		while (!openList.isEmpty()) {
              // Get the state with the lowest cost from the open list.
   			GameState currentNode = openList.poll();
              // If the current node is the goal state, reconstruct and return the path.
   			if (currentNode.isGoal()) {
   				Stack<StripsAction> stackResult = new Stack<>();
   				GameState state = currentNode;
   				List<StripsAction> list = state.plan;
                  // Reverse the list to get the correct order of actions in the path.
   				while (!list.isEmpty()) {
   					stackResult.push(list.remove(list.size()-1));
   				}
   				state = state.parent;
   				finalPath = stackResult;
   				return finalPath;
   			}
              // Add the current node to the closed list, indicating that it has been visited.
   			closedList.add(currentNode);
              // Generate children of the current node and consider adding them to the open list.
   			for (GameState state : currentNode.generateChildren()) {
   				boolean b = false;
                  // If the child state is not in the closed list, proceed.
   				if (!closedList.contains(state)) {
                      // Check if there's a better path to this state in the open list.
   	 				for(GameState state1:openList) {
   	 					if(!(state1.getCost()<state.getCost() && state1.equals(state))) {
                              // Remove the state from the open list if a better path is found.
   	 						openList.remove(state);
   	 					}
   	 				}
                      // Add the state to the open list.
   					openList.add(state);
   				}
   			}
   		}
          // If no path to the goal state is found, return an empty path.
   		return finalPath;
      }

	/**
	 * This has been provided for you. Each strips action is converted to a string
	 * with the toString method. This means each class implementing the StripsAction
	 * interface should override toString. Your strips actions should have a form
	 * matching your included Strips definition writeup. That is <action
	 * name>(<param1>, ...). So for instance the move action might have the form of
	 * Move(peasantID, X, Y) and when grounded and written to the file Move(1, 10,
	 * 15).
	 *
	 * @param plan
	 *            Stack of Strips Actions that are written to the text file.
	 */
	private void savePlan(Stack<StripsAction> plan) {
		if (plan == null) {
			System.err.println("Cannot save null plan");
			return;
		}

		File outputDir = new File("saves");
		outputDir.mkdirs();

		File outputFile = new File(outputDir, "plan.txt");

		PrintWriter outputWriter = null;
		try {
			outputFile.createNewFile();

			outputWriter = new PrintWriter(outputFile.getAbsolutePath());

			Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
			while (!tempPlan.isEmpty()) {
				outputWriter.println(tempPlan.pop().toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputWriter != null)
				outputWriter.close();
		}
	}
}
