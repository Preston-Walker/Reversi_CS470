import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.math.*;
import java.text.*;

class RandomGuy {

    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;
    
    int validMoves[] = new int[64];
    int numValidMoves;
    
    
    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public RandomGuy(int _me, String host) {
        me = _me;
        initClient(host);

        int myMove;
        System.out.print("Me:  ");
        System.out.println(me);
        while (true) {
            System.out.println("Read");
            readMessage();
            
            if (turn == me) {
                
                System.out.println("Move");
                getValidMoves(round, state);
                
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
            // Get the moves into a usable format.
            ArrayList<int[]> moves = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            // Each item in the array list has an x, y, and heuristic value (in that order)
                            moves.add(new int[]{i,j,0});
                        }
                    }
                }
            }

            System.out.println("Moves that I can see: ");
            // Find out how many tiles each move will flip
            for (int[] move: moves){
                move[2] = NumTilesFlip(me, move);
                System.out.println(Integer.toString(move[0]) + ", "+ Integer.toString(move[1]) + "   flips: " + move[2]);
            }
            int max = 0;

            // Pick the move that flips the most tiles
            for (int[] move: moves){
                if (move[2] > max){
                    max = move[2];
                    myMove = moves.indexOf(move);
                }
            }
        }
        
        return myMove;
    }

    private int NumTilesFlip(int me, int[] move){
        int opponent;
        if (me == 1){
            opponent = 2;
        }
        else{
            opponent = 1;
        }
        // A running count of how many opposing tiles will flip
        int numToFlip = 0;
        // variables for the layout of the board
        int i,j,temp;

        // Pure right:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i+1 < 8 && state[i+1][j] ==  opponent){
            i++;
            temp++;
        }   
        if (i+1 < 8 && state[i+1][j] == me){
            numToFlip += temp;
        }
        // Pure up:
        i = move[0];
        j = move[1];
        temp = 0;
        while(j+1 < 8 && state[i][j+1] ==  opponent){
            j++;
            temp++;
        }   
        if (j+1 < 8 && state[i][j+1] == me){
            numToFlip += temp;
        }
        // Pure left:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i-1 >= 0 && state[i-1][j] ==  opponent){
            i--;
            temp++;
        }   
        if (i-1 >= 0 && state[i-1][j] == me){
            numToFlip += temp;
        }
        // Pure down:
        i = move[0];
        j = move[1];
        temp = 0;
        while(j-1 >= 0 && state[i][j-1] ==  opponent){
            j--;
            temp++;
        }   
        if (j-1 >= 0 && state[i][j-1] == me){
            numToFlip += temp;
        }
        // up,right:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i+1 < 8 && j+1 < 8 &&  state[i+1][j+1] ==  opponent){
            i++;
            j++;
            temp++;
        }   
        if (i+1 < 8 && j+1 < 8 && state[i+1][j+1] == me){
            numToFlip += temp;
        }
        // up,left:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i-1 >= 0 && j+1 < 8 &&  state[i-1][j+1] ==  opponent){
            i--;
            j++;
            temp++;
        }   
        if (i-1 >= 0 && j+1 < 8 && state[i-1][j+1] == me){
            numToFlip += temp;
        }
        // down,left:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i-1 >= 0 && j-1 >= 0 &&  state[i-1][j-1] ==  opponent){
            i--;
            j--;
            temp++;
        }   
        if (i-1 >= 0 && j-1 >= 0 && state[i-1][j-1] == me){
            numToFlip += temp;
        }
        // down,right:
        i = move[0];
        j = move[1];
        temp = 0;
        while(i+1 < 8 && j-1 >= 0 &&  state[i+1][j-1] ==  opponent){
            i++;
            j--;
            temp++;
        }   
        if (i+1 < 8 && j-1 >= 0 && state[i+1][j-1] == me){
            numToFlip += temp;
        }
        
        return numToFlip;
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
            System.out.println("Valid Moves:");
            for (i = 0; i < numValidMoves; i++) {
                System.out.println(validMoves[i] / 8 + ", " + validMoves[i] % 8);
            }
        }
        else {
            System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            validMoves[numValidMoves] = i*8 + j;
                            numValidMoves ++;
                            System.out.println(i + ", " + j);
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
    
    private boolean checkDirection(int state[][], int row, int col, int incx, int incy) {
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
            if (me == 1) {
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
    
    private boolean couldBe(int state[][], int row, int col) {
        int incx, incy;
        
        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;
            
                if (checkDirection(state, row, col, incx, incy))
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
        
        System.out.println("Turn: " + turn);
        System.out.println("Round: " + round);
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++) {
                System.out.print(state[i][j]);
            }
            System.out.println();
        }
        System.out.println();
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
