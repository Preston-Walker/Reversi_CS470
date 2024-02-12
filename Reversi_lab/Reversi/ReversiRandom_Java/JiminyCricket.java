import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.math.*;
import java.text.*;
import java.time.Instant;
import java.time.Duration;


class JiminyCricket {
    // Declare some constants to use for infinity
    final int INF = Integer.MAX_VALUE;
    final int NEG_INF = Integer.MIN_VALUE;
    int MAX_DEPTH = 8;

    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int opponent;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;
    
    int validMoves[] = new int[64];
    int numValidMoves;

    Instant startMove;
    Double timeLeft;
    
    
    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public JiminyCricket(int _me, String host) {

        me = _me;
        // Simple logic to determine which player is the player vs opponent
        if (me == 1){
            opponent = 2;
        }
        else{
            opponent = 1;
        }

        if(host == null) return; // for testing purposes

        initClient(host);
        

        // System.out.println("this is proof that it compiled 1234567");

        int myMove;
        // System.out.print("Me:  ");
        // System.out.println(me);
        while (true) {
            // System.out.println("Read");
            readMessage();
            
            if (turn == me) {
                
                // System.out.println("Move");
                getValidMoves(round, state);
                
                if(me == 1){
                    timeLeft = t1;
                }
                else{
                    timeLeft = t2;
                }
                startMove = Instant.now();
                myMove = move(round);
                //myMove = generator.nextInt(numValidMoves);        // select a move randomly
                
                String sel = validMoves[myMove] / 8 + "\n" + validMoves[myMove] % 8;
                
                System.out.println("Selection: " + validMoves[myMove] / 8 + ", " + validMoves[myMove] % 8);
                
                sout.println(sel);
            }
        }
        //while (turn == me) {
        //    System.out.println("My turn");
            
            //readMessage();
        //}
    }

    private void timeDepth(){
        Instant currentGame = Instant.now();
        Long startMoveTimeMilli = (long)(timeLeft * 1000);
        Long passedTime = Duration.between(startMove, currentGame).toMillis();
        Long timeLeft = startMoveTimeMilli - passedTime;
        // System.out.println("true passed time  " + Long.toString(timeLeft));
        // if ((Long)(timeLeft * 1000) - PassedTime < 2000
        if(timeLeft < 2000){ // less than 2 seconds
            MAX_DEPTH = 2;
            // System.out.println("set depth to two at time: " + Double.toString(t2));
        }
        else if(timeLeft < 10000){
            MAX_DEPTH = 6;
        }
    }
    
    // You should modify this function
    // validMoves is a list of valid locations that you could place your "stone" on this turn
    // Note that "state" is a global variable 2D list that shows the state of the game
    private int move(int round) {
        // Declare the move that I will return
        int myMove = 0;

        // If it's before round 4, Just return a random move.
        // There might be an advantage to one of the configurations, but for now I'll ignore that.
        if (round < 4) {
        // just move randomly for now
        myMove = generator.nextInt(numValidMoves);
        }

        // If it's after round 4, actually use a heuristic function to find the next move.
        else{
            // // Get the moves into a usable format.
            // ArrayList<int[]> moves = new ArrayList<>();
            // for (int i = 0; i < 8; i++) {
            //     for (int j = 0; j < 8; j++) {
            //         if (state[i][j] == 0) {
            //             if (couldBe(state, i, j)) {
            //                 // Each item in the array list has an x, y, and heuristic value (in that order)
            //                 moves.add(new int[]{i,j,0});
            //             }
            //         }
            //     }
            // }

            // System.out.println("Moves that I can see: ");
            // // Find out how many tiles each move will flip
            // for (int[] move: moves){
            //     move[2] = HeuristicFuntion(me, move, );
            //     System.out.println(Integer.toString(move[0]) + ", "+ Integer.toString(move[1]) + "   flips: " + move[2]);
            // }
            // int max = 0;

            // // Pick the move that flips the most tiles
            // for (int[] move: moves){
            //     if (move[2] > max){
            //         max = move[2];
            //         myMove = moves.indexOf(move);
            //     }
            // }

            // Make a copy of the state to pass into the function
            int current_state[][] = CopyState(state);
            
            // Get a list of all possible moves in a usable format
            // format: x, y, heuristic value (initialized to 0)
            ArrayList<int[]> moves = new ArrayList<>();
            moves = GetMovesFromState(current_state, me);

            // Make decision with the Alpha_beta_recursive function
            // System.out.print("\n//////// Start of the next turn ////////\n");
            // Format of Values: alpha, beta, value, x, y
            // Instant startMove = Instant.now();
            ArrayList<Integer> values = Alpha_beta_recursive(NEG_INF, INF, true, 0, current_state);
            // Instant endMove = Instant.now();
            // long time = Duration.between(startMove, endMove).toMillis();
            // System.out.println();
            // System.out.println(time+" Milli seconds");
            // System.out.println("//////// End of turn ////////\n");

            // Find the selected move from the list of moves.
            for (int[] move: moves){
                if (move[0] == values.get(3) && move[1] == values.get(4)){
                    myMove = moves.indexOf(move);
                }
            }
        }
        
        return myMove;
    }


    // A function to find all moves from a state that hasn't actually happened.
    public ArrayList<int[]> GetMovesFromState(int current_state[][], int player){
        ArrayList<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (current_state[i][j] == 0) {
                    if (couldBe(current_state, i, j, player)) {
                        // Each item in the array list has an x, y, and heuristic value (in that order)
                        moves.add(new int[]{i,j,0});
                    }
                }
            }
        }
        return moves;
    }    

    public ArrayList<Integer> Alpha_beta_recursive(int alpha, int beta, boolean maximize, int depth, int current_state[][]){
        timeDepth();
        // format of bestMove & values: alpha, beta, value, i, j 
        ArrayList<Integer> bestMove = new ArrayList<Integer>(Arrays.asList(NEG_INF, INF, NEG_INF, 0, 0));
        // values is a temp variable, used to store the values of the current move, it is saved into bestMove if it the best so far
        ArrayList<Integer> values = new ArrayList<>(Arrays.asList(alpha, beta, alpha));

        int currentPlayer; // who the algorythm is checking moves for this time
        if(maximize){
            currentPlayer = me;
        }
        else{
            currentPlayer = opponent;
        }

        // System.out.print("At depth = " + Integer.toString(depth) + " maximize is ");
        // System.out.print(maximize);
        // System.out.print("\n");
        int value; // value of the current move, either comes from the heuristic value (if at the base depth), or the selected node beneath
        // base case
        if (MAX_DEPTH <= depth){
            ArrayList<int[]> possibleMoves = GetMovesFromState(current_state, currentPlayer);
            for (int[] move : possibleMoves) {
                // System.out.println("\t--- Next Move ---");
                // System.out.print("\tmove is at " +  Integer.toString(move[0]) + " " + Integer.toString(move[1]) + "\n");
                value = HeuristicFuntion(me, opponent, currentPlayer, move, current_state);
                // System.out.print("\tHeuristic value: " + Integer.toString(value) + " currently minimizing.\n");
                // System.out.println("\tAlpha and Beta: " + Integer.toString(alpha) + ", " +  Integer.toString(beta));
                if (alpha < beta){ //if true, we don't prune
                    values = new ArrayList<Integer>(Arrays.asList(alpha, beta, value, move[0], move[1]));
                    if(maximize && value > alpha){
                        bestMove = values;
                        alpha = value;
                        values.set(0, alpha);
                    }
                    else if(!maximize && value < beta){
                        bestMove = values;
                        beta = value;
                        values.set(1, beta); // use the new beta value
                        // System.out.print("\tNew best move:\n\t\tValue: " + Integer.toString(value) + " at " + Integer.toString(move[0]) + " " + Integer.toString(move[1]) + " \n");
                        // System.out.println("\tBest move: i: " + bestMove.get(3) + " j: " + bestMove.get(4));
                    }
                }
                else{
                    // System.out.println("\tNot the best move... Moving on.");
                }
            }
            return bestMove;
        }
        // Not the base case yet
        else{
            ArrayList<int[]> possibleMoves = GetMovesFromState(current_state, currentPlayer);
            for (int[] move : possibleMoves) {
                // System.out.println("\t--- Next Move ---");
                // System.out.print("\tmove is at " +  Integer.toString(move[0]) + " " + Integer.toString(move[1]) + "\n");
                // System.out.println("\tAlpha and Beta: " + Integer.toString(alpha) + ", " +  Integer.toString(beta));
                if (alpha < beta){  // we don't prune
                    // Make a new temp array for the current move and flip the tiles in our local copy of the state before diving deeper
                    int next_state[][] = FlipTiles(currentPlayer, move, CopyState(current_state));
                    // print state before going deeper
                    // for (int i = 7; i >= 0; i--){
                    //     for (int j = 0; j < 8; j++){
                    //         System.out.print(next_state[i][j] + " ");
                    //     }
                    //     System.out.print("\n");
                    // }
                    values = Alpha_beta_recursive(alpha, beta, !maximize, depth + 1, next_state);
                    // System.out.println("Returning to depth " + Integer.toString(depth));
                    if (maximize && values.get(1) > alpha){
                        bestMove = values;
                        bestMove.set(3, move[0]);
                        bestMove.set(4, move[1]);
                        alpha = values.get(1);
                        values.set(0, alpha); // use the new alpha value
                        // System.out.print("\tNew best move:\n\t\tValue: " + Integer.toString(values.get(2)) + " at " + Integer.toString(move[0]) + " " + Integer.toString(move[1]) + " \n");
                        // System.out.println("\tBest move: i: " + bestMove.get(3) + " j: " + bestMove.get(4));
                    }
                    else if (!maximize && values.get(0) < beta){ // if the value of alpha from the move > beta, update bet, and the best move
                        bestMove = values;
                        bestMove.set(3, move[0]);
                        bestMove.set(4, move[1]);
                        beta = values.get(0);
                        values.set(1, beta); // use the new alpha value
                        // System.out.print("\tNew best move:\n\t\tValue: " + Integer.toString(values.get(2)) + " at " + Integer.toString(move[0]) + " " + Integer.toString(move[1]) + " \n");
                        // System.out.println("\tBest move: i: " + bestMove.get(3) + " j: " + bestMove.get(4));
                    }
                    else{
                        // System.out.println("\tNot the best move... Moving on.");
                    }
                }
                else{
                    // System.out.println("\tPruned that whole branch at depth " + Integer.toString(depth) + " which was considering move " + Integer.toString(move[0]) + " " + Integer.toString(move[0]));
                }
            }
            return new ArrayList<Integer>(Arrays.asList(alpha, beta, bestMove.get(2), bestMove.get(3), bestMove.get(4)));
        }
    }

    private int nearbyOpponents(int me, int opponent, int turn, int[] move, int current_state[][]){
        // A heuristic function to determine the number of bordering opponent tiles to the move about to be made
        int nearbyOpponentTiles = 0;
        int next_state[][] = FlipTiles(turn, move, current_state);
        for (int i = move[0]-1;  i < move[0]+2; i++){
            for (int j = move[1]-1;  j < move[1]+2; j++){
                if (i >= 0 && i <= 7 && j >= 0 && j <= 7){
                    if (next_state[i][j] == opponent){
                        nearbyOpponentTiles++;
                    }
                }
            }
        }
        return nearbyOpponentTiles;
    }

    private int CoinParity(int me, int opponent, int turn, int[] move, int current_state[][]){
        // Number of tiles for me (our program)
        int myTiles;
        // Number of tiles for my opponent
        int opponentTiles;

        // Debug info
        // System.out.println("\t\tTiles Flipped by move: " + Integer.toString(NumTilesFlip(turn, move, current_state)));
        // System.out.println("\t\tBefore Move: My Current tiles: " + Integer.toString(numTiles(me, current_state)));
        // System.out.println("\t\tBefore Move: Opponent Current tiles: " + Integer.toString(numTiles(opponent, current_state)));
        
        if (turn == me){
            // System.out.println("\t\tMy (the algorithm) turn");
            // My tiles: the number I currently have based on game state, + the number I flip with this move, + the 1 tile I play
            myTiles = numTiles(me, current_state) + NumTilesFlip(me, move, current_state) + 1;
            // Opponent tiles: the number they currently have based on game state, - the number of tiles I flip from them with my move
            opponentTiles = numTiles(opponent, current_state) - NumTilesFlip(me, move, current_state); 
        }
        else{
            // System.out.println("\t\tOpponent's turn");
            // My tiles: the number I currently have - the number the opponet flips from me
            myTiles = numTiles(me, current_state) - NumTilesFlip(opponent, move, current_state);
            // Opponent tiles: the number the currently have, + the number they flip from me, + the 1 tile they play
            opponentTiles = numTiles(opponent, current_state) + NumTilesFlip(opponent, move, current_state) + 1;
        }

        // Debug info
        // System.out.print("\t\tAfter Move: My Current tiles: " + Integer.toString(myTiles) + "\n");
        // System.out.print("\t\tAfter Move: Opponent Current tiles: " + Integer.toString(opponentTiles) + "\n");

        // Coin parity is just my tiles - my opponent's tiles
        return myTiles-opponentTiles;
    }

    private int CornerParity(int me, int opponent, int turn, int[] move, int current_state[][]){
        // Count the number of corners  in the current state of the game
        int myCorners = countCorners(me, current_state);
        int opponentConers = countCorners(opponent, current_state);
        // Check to see if the next move is going to be a corner
        if ((move[0] == 0 && move[1] == 0) ||
            (move[0] == 7 && move[1] == 0) ||
            (move[0] == 7 && move[1] == 7) ||
            (move[0] == 0 && move[1] == 7)){
            // If the next move is a corner and it's my turn, add a corner to my total
            if (turn == me){
                myCorners++;
                // System.out.println("\t\tI am getting a corner");
            }
            // If the next move is a corner and it's my opponent's turn, add a corner to their total
            else{
                opponentConers++;
                // System.out.println("\t\tThe oppoment is getting a corner");
            }
        }
        // Return the difference in corners
        return myCorners-opponentConers;
    }

    private int countCorners(int player, int current_state[][]){
        int playerCournerCount = 0;
        if (current_state[0][0] == player){
            playerCournerCount += 1;
        }
        if (current_state[0][7] == player){
            playerCournerCount += 1;
        }
        if (current_state[7][0] == player){
            playerCournerCount += 1;
        }
        if (current_state[7][7] == player){
            playerCournerCount += 1;
        }
        return playerCournerCount;
    }

    private int MobilityParity(int me, int opponent, int turn, int[] move, int current_state[][]){
        // find the number of moves I can take right now
        int myPossibleMoves = GetMovesFromState(current_state, me).size();
        // simulate taking the turn listed
        int next_state[][] = FlipTiles(turn, move, current_state);
        // find available moves for opponent
        int opponentPossibleMoves = GetMovesFromState(next_state, opponent).size();

        return myPossibleMoves-opponentPossibleMoves;
    }

    private int StabilityMeasure(int me, int opponent, int turn, int[] move, int current_state[][]){
        // I'm defining tiles in the middle to be unstable, tiles on the edge to be semi-stable, and tiles in a corner to be stable
        // yes, it's a somewhat simplistic measure of stability, but I think it captures the essence of the idea
        int myStability = 0;
        int opponentStability = 0;
        // simulate taking the turn listed
        int next_state[][] = FlipTiles(turn, move, current_state);
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                // first adjust my stability
                if (next_state[i][j] == me){
                    // first check for corner, and increment by 1
                    if ((i == 0 && j == 0) ||
                        (i == 7 && j == 0) ||
                        (i == 7 && j == 7) ||
                        (i == 0 && j == 7)){   
                        myStability += 3;
                    }
                    // next check for edges, don't increase stability
                    else if ((i == 0) ||
                         (i == 7) ||
                         (j == 7) ||
                         (j == 0)){
                        myStability++;
                    }
                    // unstable node, don't change stability
                    else{
                        continue;
                    }
                }
                // next adjust opponent stability
                else if (next_state[i][j] == opponent){
                    // first check for corner, and increment by 3
                    if ((i == 0 && j == 0) ||
                        (i == 7 && j == 0) ||
                        (i == 7 && j == 7) ||
                        (i == 0 && j == 7)){   
                        opponentStability += 3;
                    }
                    // next check for edges, increase stability by 1
                    else if ((i == 0) ||
                         (i == 7) ||
                         (j == 7) ||
                         (j == 0)){
                        opponentStability++;
                    }
                    // unstable node, don't change stability
                    else{
                        continue;
                    }
                }
            }
        }
        return myStability - opponentStability;
    }

    public int HeuristicFuntion(int me, int opponent, int turn, int[] move, int current_state[][]){
        // System.out.println("\t\t--- Heuristic Function ---");
        
        int HeuristicValue = 2 * CoinParity(me, opponent, turn, move, current_state) +  10 * CornerParity(me, opponent, turn, move, current_state) + 1 * StabilityMeasure(me, opponent, turn, move, current_state) + 1 * MobilityParity(me, opponent, turn, move, current_state) + 1 * nearbyOpponents(me, opponent, turn, move, current_state);
        // int HeuristicValue = CoinParity(me, opponent, turn, move, current_state);
        return HeuristicValue;
    }

    private int[][] FlipTiles(int me, int[] move, int next_state[][]){
        next_state[move[0]][move[1]] = me;
        // variables for the layout of the board
        int i,j;
        ArrayList<int[]> tilesToFlip = new ArrayList<>();

        // Pure right:
        i = move[0];
        j = move[1];
        while(i+1 < 8 && next_state[i+1][j] ==  opponent){
            i++;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (i+1 < 8 && next_state[i+1][j] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }
        }
        tilesToFlip.clear();
        // Pure up:
        i = move[0];
        j = move[1];
        while(j+1 < 8 && next_state[i][j+1] ==  opponent){
            j++;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (j+1 < 8 && next_state[i][j+1] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }        
        }
        tilesToFlip.clear();
        // Pure left:
        i = move[0];
        j = move[1];
        while(i-1 >= 0 && next_state[i-1][j] ==  opponent){
            i--;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (i-1 >= 0 && next_state[i-1][j] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }        }
        tilesToFlip.clear();
        // Pure down:
        i = move[0];
        j = move[1];
        while(j-1 >= 0 && next_state[i][j-1] ==  opponent){
            j--;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (j-1 >= 0 && next_state[i][j-1] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }        
        }
        tilesToFlip.clear();
        // up,right:
        i = move[0];
        j = move[1];
        while(i+1 < 8 && j+1 < 8 &&  next_state[i+1][j+1] ==  opponent){
            i++;
            j++;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (i+1 < 8 && j+1 < 8 && next_state[i+1][j+1] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }        
        }
        tilesToFlip.clear();
        // up,left:
        i = move[0];
        j = move[1];
        while(i-1 >= 0 && j+1 < 8 &&  next_state[i-1][j+1] ==  opponent){
            i--;
            j++;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (i-1 >= 0 && j+1 < 8 && next_state[i-1][j+1] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }                
        }
        tilesToFlip.clear();
        // down,left:
        i = move[0];
        j = move[1];
        while(i-1 >= 0 && j-1 >= 0 &&  next_state[i-1][j-1] ==  opponent){
            i--;
            j--;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (i-1 >= 0 && j-1 >= 0 && next_state[i-1][j-1] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }                
        }
        tilesToFlip.clear();
        // down,right:
        i = move[0];
        j = move[1];
        while(i+1 < 8 && j-1 >= 0 &&  next_state[i+1][j-1] ==  opponent){
            i++;
            j--;
            tilesToFlip.add(new int[]{i,j});
        }   
        if (i+1 < 8 && j-1 >= 0 && next_state[i+1][j-1] == me){
            for (int[] tile:tilesToFlip){
                next_state[tile[0]][tile[1]] = me;
            }                
        }
        tilesToFlip.clear();        

        return next_state;
    }

    private int numTiles(int playerID, int game_state[][]){
        int playerNumTiles = 0;
        
        for (int i = 0; i < game_state.length; i++) {
            for (int j = 0; j < game_state[i].length; j++) {
                if(game_state[i][j] == playerID){
                    playerNumTiles += 1;
                }
            }
        }
        return playerNumTiles;
    }

    private int NumTilesFlip(int me, int[] move, int current_state[][]){
        // A running count of how many opposing tiles will flip
        int numToFlip = 0;
        int temp = 0;
        // variables for the layout of the board
        int i,j;

        // Pure right:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i+1 < 8 && current_state[i+1][j] ==  opponent){
            i++;
            temp++;
        }   
        if (i+1 < 8 && current_state[i+1][j] == me){
            numToFlip += temp;
        }
        // Pure up:
        i = move[0];
        j = move[1];
        temp = 0;
        while(j+1 < 8 && current_state[i][j+1] ==  opponent){
            j++;
            temp++;
        }   
        if (j+1 < 8 && current_state[i][j+1] == me){
            numToFlip += temp;
        }
        // Pure left:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i-1 >= 0 && current_state[i-1][j] ==  opponent){
            i--;
            temp++;
        }   
        if (i-1 >= 0 && current_state[i-1][j] == me){
            numToFlip += temp;
        }
        // Pure down:
        i = move[0];
        j = move[1];
        temp = 0;
        while(j-1 >= 0 && current_state[i][j-1] ==  opponent){
            j--;
            temp++;
        }   
        if (j-1 >= 0 && current_state[i][j-1] == me){
            numToFlip += temp;
        }
        // up,right:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i+1 < 8 && j+1 < 8 &&  current_state[i+1][j+1] ==  opponent){
            i++;
            j++;
            temp++;
        }   
        if (i+1 < 8 && j+1 < 8 && current_state[i+1][j+1] == me){
            numToFlip += temp;
        }
        // up,left:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i-1 >= 0 && j+1 < 8 &&  current_state[i-1][j+1] ==  opponent){
            i--;
            j++;
            temp++;
        }   
        if (i-1 >= 0 && j+1 < 8 && current_state[i-1][j+1] == me){
            numToFlip += temp;
        }
        // down,left:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i-1 >= 0 && j-1 >= 0 &&  current_state[i-1][j-1] ==  opponent){
            i--;
            j--;
            temp++;
        }   
        if (i-1 >= 0 && j-1 >= 0 && current_state[i-1][j-1] == me){
            numToFlip += temp;
        }
        // down,right:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i+1 < 8 && j-1 >= 0 &&  current_state[i+1][j-1] ==  opponent){
            i++;
            j--;
            temp++;
        }   
        if (i+1 < 8 && j-1 >= 0 && current_state[i+1][j-1] == me){
            numToFlip += temp;
        }
        
        return numToFlip;
    }

    private int[][] CopyState(int current_state[][]){
        int[][] state_copy = new int[8][8];
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                state_copy[i][j] = current_state[i][j];
            }
        }
        return state_copy;
    }
    
    // generates the set of valid moves for the player; returns a list of valid moves (validMoves)
    private void getValidMoves(int round, int state[][]) {
        int i, j;
        
        numValidMoves = 0;
        if (round < 4) {
            if (state[3][3] == 0) {
                validMoves[numValidMoves] = 3*8 + 3;
                numValidMoves ++;
            }
            if (state[3][4] == 0) {
                validMoves[numValidMoves] = 3*8 + 4;
                numValidMoves ++;
            }
            if (state[4][3] == 0) {
                validMoves[numValidMoves] = 4*8 + 3;
                numValidMoves ++;
            }
            if (state[4][4] == 0) {
                validMoves[numValidMoves] = 4*8 + 4;
                numValidMoves ++;
            }
            // System.out.println("Valid Moves:");
            // for (i = 0; i < numValidMoves; i++) {
            //     System.out.println(validMoves[i] / 8 + ", " + validMoves[i] % 8);
            // }
        }
        else {
            // System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j, me)) {
                            validMoves[numValidMoves] = i*8 + j;
                            numValidMoves ++;
                            // System.out.println(i + ", " + j);
                        }
                    }
                }
            }
        }
        
        
        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
    }
    
    private boolean checkDirection(int state[][], int row, int col, int incx, int incy, int player) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;
        
        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row+incy*i;
            c = col+incx*i;
        
            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;
        
            sequence[seqLen] = state[r][c];
            seqLen++;
        }
        
        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (player == 1) {
                if (sequence[i] == 2)
                    count ++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            }
            else {
                if (sequence[i] == 1)
                    count ++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }
        
        return false;
    }
    
    private boolean couldBe(int state[][], int row, int col, int player) {
        int incx, incy;
        
        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;
            
                if (checkDirection(state, row, col, incx, incy, player))
                    return true;
            }
        }
        
        return false;
    }
    
    public void readMessage() {
        int i, j;
        String status;
        try {
            //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());
            
            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                
                System.exit(1);
            }
            
            //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
            System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        
        
        // System.out.println("Turn: " + turn);
        // System.out.println("Round: " + round);
        // for (i = 7; i >= 0; i--) {
        //     for (j = 0; j < 8; j++) {
        //         System.out.print(state[i][j]);
        //     }
        //     System.out.println();
        // }
        // System.out.println();
    }
    
    public void initClient(String host) {
        int portNumber = 3333+me;
        
        try {
			s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            String info = sin.readLine();
            System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    
    // compile on your machine: javac *.java
    // call: java RandomGuy [ipaddress] [player_number]
    //   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
    //   player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new RandomGuy(Integer.parseInt(args[1]), args[0]);
    }
    
}
