package agents;

import wumpus.Agent;
import wumpus.Environment;
import wumpus.Environment.Action;
import wumpus.Player;
import wumpus.Player.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * An Agent that implements a basic heuristic strategy. The heuristic actions are as following:
 * H1: Grab the gold if sees glitter;
 * H2: Shoots to every not visited tiles if feels a stench;
 * H3: Mark the adjacent, not visited tiles has dangerous if feels a breeze;
 * H4: If do not have gold, choose the branch the non visited branch with less turns to take;
 * H5: Choose the path that surely does not have a danger;
 * H5: If have found the gold get back by the visited path;
 */
public class HeuristicAgent implements Agent {
    private int w, h;

    private boolean debug = true;
    private double[][] pitDangers;
    private boolean[][] noTrespass;
    private boolean[][] visited;
    private boolean[][] shoot;
    private double[][] canshoot;
    private double[][] supmuwDanger;
    private double[][] supmuwBenefit;

    private LinkedList<Action> nextActions = new LinkedList<Action>();

    /**
     * The strategy constructor.
     *
     * @param width  The board width
     * @param height The board height
     */
    public HeuristicAgent(int width, int height) {
        w = width;
        h = height;
        pitDangers = new double[w][h];
        supmuwDanger = new double[w][h];
        supmuwBenefit = new double[w][h];
        noTrespass = new boolean[w][h];
        canshoot = new double[w][h];
        visited = new boolean[w][h];
        shoot = new boolean[w][h];
    }

    /**
     * Sets weather to show the debug messages or not.
     *
     * @param value <tt>true</tt> to display messages
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Prints the player board and debug message.
     *
     * @param player The player instance
     */
    public void beforeAction(Player player) {
        if (debug) {
            System.out.println(player.render());
            System.out.println(player.debug());
        }
    }

    /**
     * Prints the last action taken.
     *
     * @param player The player instance
     */
    public void afterAction(Player player) {
        if (debug) {
            // Players Last action
            System.out.println(player.getLastAction());
            // Show a very happy message
            if (player.isDead()) {
                System.out.println("GAME OVER!");
            }
            // Turn on step-by-step
            Environment.trace();
        }
    }

    /**
     * Implements the player artificial intelligence strategy.
     *
     * @param player The player instance
     * @return The next action
     */
    public Action getAction(Player player) {
        // Mark this block has visited
        int x = player.getX();
        int y = player.getY();

        // Set this block as visited
        visited[x][y] = true;
        // Apply actions pools if no bump is encountered
        if (nextActions.size() > 0) {
            if (nextActions.size() == 1 && player.hasBump()) {
                nextActions.clear();
            } else {
                return nextActions.poll();
            }
        }

        // Grab the gold if senses glitter
        if (player.hasGlitter()) return Action.GRAB;

        if (player.hasTaste() && !player.hasFood()) {
            return Action.EAT;
        }

        // Calculate the neighbor branches
        int[][] branches = getNeighbors(x, y);

        if (player.hasBump()) {
            if (player.getDirection() == Direction.N && player.getY() != 0) {
                noTrespass[x][y - 1] = true;
            } else if (player.getDirection() == Direction.E && player.getX() != w - 1) {
                noTrespass[x + 1][y] = true;
            } else if (player.getDirection() == Direction.W && player.getX() != 0) {
                noTrespass[x - 1][y] = true;
            } else if (player.getDirection() == Direction.S && player.getY() != h - 1) {
                noTrespass[x][y + 1] = true;
            }
        }

        //Case when supmuw behaves like wumpus. Need to estimate these locations and avoid them
        if (player.hasStench() && player.hasMoo()) {
            estimateDangerLocationForSupmuw(branches);
        }

        if (player.hasMoo() && !player.hasStench()) {
            estimateSupmuwLocation(branches);
        }

        // Shoot an arrow to every non visited tiles if senses a stench
        // Shoot an arrow to every non visited tiles if senses a stench
        if (player.hasStench() && player.hasArrows()) {
            boolean shootconfirm = false;
            // Apply killer instinct
//            for(int[] branch : branches) {
//                if (!visited[branch[0]][branch[1]] && !shoot[branch[0]][branch[1]]) {
//                    shoot[branch[0]][branch[1]] = true;
//
//                    ArrayList<Action> actions = getActionsToShoot(player, branch);
//                    nextActions.addAll(actions);
//                    return nextActions.poll();
//                }
//            }
            boolean shootbranch = false;
            // Verify if a pit was already found
            int[] branchToShoot = {-1,-1};
            for (int[] branch : branches) {
                if (canshoot[branch[0]][branch[1]] == 1 && shootconfirm == true) {
                    shootbranch = true;
                    shoot[branch[0]][branch[1]] = true;
                    branchToShoot = branch;
                    //break;
                }
            }
            if(shootbranch){
                ArrayList<Action> actions = getActionsToShoot(player, branchToShoot);
                nextActions.addAll(actions);
                return nextActions.poll();
            }
            // Estimate the pit location
            //if (!shootconfirm && !visited[branch[0]][branch[1]] && !shoot[branch[0]][branch[1]]) {
            // Increase by 50% the probability of having some danger
            if (!shootbranch) {
                for (int[] branch : branches) {
                    if (!visited[branch[0]][branch[1]]) {
                        if (canshoot[branch[0]][branch[1]] < 1) {
                            canshoot[branch[0]][branch[1]] += 0.5;
                        }
                        // Pit was found
                        if (canshoot[branch[0]][branch[1]] == 1) {
                            shootconfirm = true;

                        }
                    }
                }
                // If a pit was found clear the dangers from other tiles
                if (shootconfirm) {
                    for (int[] branch : branches) {
                        if (canshoot[branch[0]][branch[1]] < 1) {
                            canshoot[branch[0]][branch[1]] = 0.0;
                        }
                    }
                }
            }

        }

        // Mark non visited neighbors has dangerous
        if (player.hasBreeze()) {
            estimatePitLocation(branches);
        } else {
            // From this tile nothing has sensed so set the neighbors to pitDangers
            for (int[] branch : branches) {
                if (pitDangers[branch[0]][branch[1]] < 1) {
                    pitDangers[branch[0]][branch[1]] = 0.0;
                }
            }
        }

        // Evaluate the cost of neighbor branches
        int currentCost = 999;
        int[] next = {-1, -1};
        for (int[] branch : branches) {
            int cost = getCost(player, branch);
            System.out.println("COST OF BRANCH " + branch[0] + branch[1] +  " = " + cost);
            if (cost < currentCost) {
                currentCost = cost;
                next = branch;
                System.out.println("ACTUAL COST " + cost);
            }
        }
        // Print the chosen tile
        if (debug) {
            System.out.format("Go to (%d,%d)%n", next[0], next[1]);
        }

        // Execute the action to get to the branch with less cost
        ArrayList<Action> actions = getActionsTo(player, next);
        nextActions.addAll(actions);

        // Auto execute the first action
        return nextActions.poll();
    }

    private void estimateDangerLocationForSupmuw(int[][] branches) {
        boolean knowSupmuwLocation = false;
        // Verify if a supmuw was already found
        for (int[] branch : branches) {
            if (supmuwDanger[branch[0]][branch[1]] == 1) {
                knowSupmuwLocation = true;
                break;
            }
        }
        // Estimate the supmuw location
        if (!knowSupmuwLocation) {
            // Increase by 50% the probability of having some danger
            for (int[] branch : branches) {
                if (!visited[branch[0]][branch[1]]) {
                    if (supmuwDanger[branch[0]][branch[1]] < 1) {
                        supmuwDanger[branch[0]][branch[1]] += 0.5;
                    }
                    // Supmuw was found
                    if (supmuwDanger[branch[0]][branch[1]] == 1) {
                        knowSupmuwLocation = true;
                    }
                }
            }
            // If a pit was found clear supmuw danger from other tiles
            if (knowSupmuwLocation) {
                for (int[] branch : branches) {
                    if (supmuwDanger[branch[0]][branch[1]] < 1) {
                        supmuwDanger[branch[0]][branch[1]] = 0.0;
                    }
                }
            }
        }
    }

    private void estimatePitLocation(int[][] branches) {
        boolean knowPitPosition = false;
        // Verify if a pit was already found
        for (int[] branch : branches) {
            if (pitDangers[branch[0]][branch[1]] == 1) {
                knowPitPosition = true;
                break;
            }
        }
        // Estimate the pit location
        if (!knowPitPosition) {
            // Increase by 50% the probability of having some danger
            for (int[] branch : branches) {
                if (!visited[branch[0]][branch[1]]) {
                    if (pitDangers[branch[0]][branch[1]] < 1) {
                        pitDangers[branch[0]][branch[1]] += 0.5;
                    }
                    // Pit was found
                    if (pitDangers[branch[0]][branch[1]] == 1) {
                        knowPitPosition = true;
                    }
                }
            }
            // If a pit was found clear the pitDangers from other tiles
            if (knowPitPosition) {
                for (int[] branch : branches) {
                    if (pitDangers[branch[0]][branch[1]] < 1) {
                        pitDangers[branch[0]][branch[1]] = 0.0;
                    }
                }
            }
        }
    }


    private void estimateSupmuwLocation(int[][] branches) {
        boolean knowSupmuwLocation = false;
        // Verify if a pit was already found
        for (int[] branch : branches) {
            if (supmuwBenefit[branch[0]][branch[1]] == 1) {
                knowSupmuwLocation = true;
                break;
            }
        }
        // Estimate the supmuw location
        if (!knowSupmuwLocation) {
            // Increase by 50% the probability of having supmuw
            for (int[] branch : branches) {
                if (!visited[branch[0]][branch[1]]) {
                    if (supmuwBenefit[branch[0]][branch[1]] < 1) {
                        supmuwBenefit[branch[0]][branch[1]] += 0.5;
                    }
                    // supmuw was found
                    if (supmuwBenefit[branch[0]][branch[1]] == 1) {
                        knowSupmuwLocation = true;
                    }
                }
            }
            // If supmuw was found clear the chances from other tiles
            if (knowSupmuwLocation) {
                for (int[] branch : branches) {
                    if (supmuwBenefit[branch[0]][branch[1]] < 1) {
                        supmuwBenefit[branch[0]][branch[1]] = 0.0;
                    }
                }
            }
        }
    }

    /**
     * Gets the adjacent tiles of the given coordinates.
     *
     * @param x The tile X coordinate
     * @param y The tile Y coordinate
     * @return An array of 2D coordinates
     */
    private int[][] getNeighbors(int x, int y) {
        HashMap<Direction, Integer> nodesMap = new HashMap<Direction, Integer>();

        // Calculate the next block
        int north = y - 1;
        int south = y + 1;
        int east = x + 1;
        int west = x - 1;

        // Check if branch is into bounds
        if (north >= 0) nodesMap.put(Direction.N, north);
        if (south < h) nodesMap.put(Direction.S, south);
        if (east < w) nodesMap.put(Direction.E, east);
        if (west >= 0) nodesMap.put(Direction.W, west);

        // Build the branches array
        int branch = 0;
        int[][] nodes = new int[nodesMap.size()][2];
        for (Direction direction : nodesMap.keySet()) {
            switch (direction) {
                case N:
                    nodes[branch] = new int[]{x, north};
                    break;
                case S:
                    nodes[branch] = new int[]{x, south};
                    break;
                case E:
                    nodes[branch] = new int[]{east, y};
                    break;
                case W:
                    nodes[branch] = new int[]{west, y};
                    break;
            }
            branch++;
        }

        return nodes;
    }

    /**
     * Returns the amount of turns player need to take to get into given position.
     *
     * @param player The player's instance
     * @param to     The destination tile
     * @return The number of turns
     */
    private int getTurns(Player player, int[] to) {
        // The current vector
        int[] from = {1, 0};
        switch (player.getDirection()) {
            case N:
                from[0] = 0;
                from[1] = 1;
                break;
            case S:
                from[0] = 0;
                from[1] = -1;
                break;
            case W:
                from[0] = -1;
                from[1] = 0;
                break;
        }
        // The destination vector
        int[] dest = {to[0] - player.getX(), player.getY() - to[1]};
        // The angle between the two vectors
        double dotProduct = from[0] * dest[0] + from[1] * dest[1];
        double lenProduct = Math.hypot(from[0], from[1]) * Math.hypot(dest[0], dest[1]);
        double theta = Math.acos(dotProduct / lenProduct);
        // Inverts when facing backwards
        if (player.getDirection() == Direction.N && getDirection(dest) == Direction.E ||
                player.getDirection() == Direction.E && getDirection(dest) == Direction.S ||
                player.getDirection() == Direction.S && getDirection(dest) == Direction.W ||
                player.getDirection() == Direction.W && getDirection(dest) == Direction.N) {
            theta *= -1;
        }
        // Count how many turns
        return (int) (theta / (Math.PI / 2));
    }

    /**
     * Returns the cost for to reach the given branch.
     *
     * @param player The player's instance
     * @param to     The destination block coordinates
     * @return The cost estimation tho reach the tile
     */
    private int getCost(Player player, int[] to) {
        // Start with at least one forward
        int sum = 1;
        // If found gold choose the safest path otherwise costs more to return
        if (visited[to[0]][to[1]]) {
            System.out.println("VISITED TRUE");
            if (player.hasGold()) sum -= 5;
            else sum += 5;
        } else if (noTrespass[to[0]][to[1]]) {
            sum += 300;
        } else if (player.hasStench()) {
            if (canshoot[to[0]][to[1]] < 1) {
                sum += 10;
            } else if (canshoot[to[0]][to[1]] == 1) {
                // Avoid tiles marked as 100% danger
                sum += 100;
            }
        } else {
            // If senses a breeze avoid unvisited path
            if (player.hasBreeze()) {
                if (pitDangers[to[0]][to[1]] < 1) {
                    sum += 10;
                } else if (pitDangers[to[0]][to[1]] == 1) {
                    // Avoid tiles marked as 100% danger
                    sum += 100;
                }
            }

            if (player.hasBreeze() && player.hasMoo()) {
                if (supmuwBenefit[to[0]][to[1]] == 1) {
                    sum += 5;
                }
                //Case when you the square has chance of both pit and supmuw. Unless there is other safe way, visit it.
                else if (supmuwBenefit[to[0]][to[1]] == 1 && pitDangers[to[0]][to[1]] < 1) {
                    sum += 7;
                } else if (pitDangers[to[0]][to[1]] < 1) {
                    sum += 10;
                } else if (pitDangers[to[0]][to[1]] == 1) {
                    // Avoid tiles marked as 100% danger
                    sum += 100;
                }
            }

            //If senses a moo and stench, avoid unvisited path
            if (player.hasMoo() && player.hasStench()) {
                if (supmuwDanger[to[0]][to[1]] < 1) {
                    sum += 10;
                } else if (supmuwDanger[to[0]][to[1]] == 1) {
                    // Avoid tiles marked as 100% danger
                    sum += 100;
                }
            }

            //If it is visiting lone standing supmuw for the first time it will get some food
            if (player.hasMoo() && !player.hasStench() && !player.hasBreeze()) {
                if (supmuwBenefit[to[0]][to[1]] == 1) {
                    sum -= 5;
                }
            }
        }

        // The amount fo turns to take
        int turns = getTurns(player, to);
        System.out.println("BEFORE ADDING SUM FROM TURNS SUM + "+ sum);
        sum += Math.abs(turns);

        return sum;
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     *
     * @param player The player's instance
     * @param to     The destination tile coordinates
     * @return An array of actions
     */
    private ArrayList<Action> getActionsTo(Player player, int[] to) {
        ArrayList<Action> actions = new ArrayList<Action>();
        int turns = getTurns(player, to);
        for (int i = 0; i < Math.abs(turns); i++) {
            if (turns < 0) actions.add(Action.TURN_RIGHT);
            if (turns > 0) actions.add(Action.TURN_LEFT);

        }
        // Go to the block
        actions.add(Action.GO_FORWARD);
        return actions;
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     *
     * @param player The player's instance
     * @param to     The destination tile coordinates
     * @return An array of actions
     */
    private ArrayList<Action> getActionsToShoot(Player player, int[] to) {
        ArrayList<Action> actions = new ArrayList<Action>();
        int turns = getTurns(player, to);
        for (int i = 0; i < Math.abs(turns); i++) {
            if (turns < 0) actions.add(Action.TURN_RIGHT);
            if (turns > 0) actions.add(Action.TURN_LEFT);

        }
        // Go to the block
        actions.add(Action.SHOOT_ARROW);

        return actions;
    }

    /**
     * Returns the direction based on the vector coordinates
     *
     * @param coords The 2D coordinates
     * @return The direction
     */
    private Direction getDirection(int[] coords) {
        if (coords[0] == +0 && coords[1] == +1) return Direction.N;
        if (coords[0] == +1 && coords[1] == +0) return Direction.E;
        if (coords[0] == +0 && coords[1] == -1) return Direction.S;
        if (coords[0] == -1 && coords[1] == +0) return Direction.W;
        return Direction.E;
    }
}