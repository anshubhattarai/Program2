import agents.HeuristicAgent;
import agents.RandomAgent;
import wumpus.Agent;
import wumpus.World;

import java.io.*;

/**
 * Entry point for the application.
 */
public class Main {

    public static void main(String[] args) throws Exception {
    	
        try {
            // Create a 4x4 world	
        	
        	 

        	
        	
            World world = new World(4, 4);
//            world.setWumpus(0, 1);
//            world.setPit(2, 2);
//            world.setPit(3, 0);
//            world.setGold(1, 1);
            // Print the game title
            System.out.println("Hunt the Wumpus!");

            // Start and execute the AI agent
            Agent agent = new HeuristicAgent(world.getWidth(), world.getHeight());
            world.execute(agent);

            // Print the board and score table
            System.out.println("Board:");
            System.out.println(world.renderAll());

            System.out.format("Results for %s:%n", world.getAgentName());
            System.out.println(world.renderScore());
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
