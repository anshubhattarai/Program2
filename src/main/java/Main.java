import agents.HeuristicAgent;
import agents.RandomAgent;
import exception.ElementUnplacableException;
import wumpus.Agent;
import wumpus.World;

import java.io.*;
import java.util.Scanner;

/**
 * Entry point for the application.
 */
public class Main {

    public static void main(String[] args){
    	
        try {
        	World world = new World(10, 10);
            // Create a 4x4 world	
        	/*Scanner scr = new Scanner(System.in);  	 
        	System.out.println("\nEnter the number of pits.");
    		int np = scr.nextInt();    		
    		System.out.println("Positions of pit, gold and wumpus should not overlap.");
    		System.out.println("Enter the position of pits in format: column row");
    		
    		for (int i = 0; i < np; i++) {
    			System.out.println("Enter Column Number of Pit");		
    			int col = scr.nextInt();
    			System.out.println("Enter Row Number of Pit");
    			int row=scr.nextInt();
    		    world.setPit(col, row);	
    		}
    		
    		System.out.println("\nEnter the number of No Trespassing Area.");
    		int nta = scr.nextInt();    		
    		System.out.println("Positions of pit, gold, no trespassing area and wumpus should not overlap.");
    		if(nta!=0){
    			System.out.println("Enter the position of No Trespassing Area in format: column row");
    		}

    		for (int i = 0; i < nta; i++) {
    			System.out.println("Enter Column Number of No Trespassing Area");		
    			int col = scr.nextInt();
    			System.out.println("Enter Row Number of No Trespassing Area");
    			int row=scr.nextInt();
    		    world.setNoTrespass(col, row);	
    		}

    		System.out.println("Enter the position of wumpus in format: column row.");
    		System.out.println("Enter Column Number of Wumpus");	
    		int wp_col = scr.nextInt();
    		System.out.println("Enter Row Number of Wumpus");	
    		int wp_row=scr.nextInt();
    		world.setWumpus(wp_col, wp_row);
    		
    		System.out.println("Enter the position of supmuw  in format: column row.");
    		System.out.println("Enter Column Number of supmuw ");	
    		int s_col = scr.nextInt();
    		System.out.println("Enter Row Number of supmuw");	
    		int s_row=scr.nextInt();
    		world.setSupmuw(s_col, s_row);
    		   		
    		System.out.println("Enter the position of Gold in format: column row.");
    		System.out.println("Enter Column Number of Gold");	
    		int g_col = scr.nextInt();
    		System.out.println("Enter Row Number of Gold");	
    		int g_row=scr.nextInt();
    		world.setGold(g_col, g_row); */
    		
            //world.setWumpus(0, 1);
            //world.setPit(2, 2);
            //world.setPit(3, 0);
            //world.setGold(1, 1);
           // Print the game title
        	String FILENAME = "inputs.txt";
//      	int numberofpits;
//        	PositionofPits 4 3
//        	PositionofPits 6 1
//        	NumberOfNoTrespassArea 1
//        	PositionOfNoTrespassArea 5 5
//        	PositionOfWumpus 6 8
//        	PositionOfSumpuw 2 8
//        	PositiongOfGold 8 8
        	
        	BufferedReader br = null;
    		FileReader fr = null;
    		//br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {				
				String line = sCurrentLine;
				System.out.println("line: "+line);
				if (line.contains("PositionofPits")){
					String[] words = line.split(" ");
					int pit_col=Integer.parseInt(words[1]);
					int pit_row=Integer.parseInt(words[2]);
					world.setPit(pit_col, pit_row);					
				}
				
				if (line.contains("PositionOfNoTrespassArea")){
					String[] words = line.split(" ");
					int tp_col=Integer.parseInt(words[1]);
					int tp_row=Integer.parseInt(words[2]);
					world.setNoTrespass(tp_col, tp_row);					
				}
				
				if (line.contains("PositionOfWumpus")){
					String[] words = line.split(" ");
					int w_col=Integer.parseInt(words[1]);
					int w_row=Integer.parseInt(words[2]);
					world.setWumpus(w_col, w_row);					
				}	
				if (line.contains("PositionOfSumpuw")){
					String[] words = line.split(" ");
					int s_col=Integer.parseInt(words[1]);
					int s_row=Integer.parseInt(words[2]);
					world.setSupmuw(s_col, s_row);					
				}	
				if (line.contains("PositiongOfGold")){
					String[] words = line.split(" ");
					int g_col=Integer.parseInt(words[1]);
					int g_row=Integer.parseInt(words[2]);
					world.setGold(g_col, g_row);					
				}	
				
			}   	
            System.out.println("Hunt the Wumpus!");

            // Start and execute the AI agent
            Agent agent = new HeuristicAgent(world.getWidth(), world.getHeight());
            world.execute(agent);

            // Print the board and score table
            System.out.println("Board:");
            System.out.println(world.renderAll());

            System.out.format("Results for %s:%n", world.getAgentName());
            System.out.println(world.renderScore());
        } catch (ElementUnplacableException exception){
			System.out.println(exception.getMessage());
		} catch (Exception error) {
			error.printStackTrace();
		}
    }
}
