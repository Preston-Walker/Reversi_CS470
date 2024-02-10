import java.util.ArrayList;

class TestRandomGuy{


    public static void printgame(int[][] table) {
        for(int[] row : table) {
            for (int i : row) {
                System.out.print(i);
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        
        RandomGuy randomGuy = new RandomGuy(2, null);

        // int current_state[][] = {{0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0},
        // {0, 0, 0, 0, 0, 0, 0, 0}};

        // 2 us computer
        // 1 opponent

        int current_state[][] = {{0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 1, 1, 0},
                                {0, 0, 0, 0, 0, 2, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0}};

        // int current_state[][] = {{0, 0, 0, 0, 0, 0, 0, 0},
        // {1, 0, 0, 0, 0, 0, 0, 0},
        // {2, 0, 0, 0, 0, 0, 0, 0},
        // {3, 0, 0, 0, 0, 0, 0, 0},
        // {4, 0, 0, 0, 0, 0, 0, 0},
        // {5, 0, 0, 0, 0, 0, 0, 0},
        // {6, 0, 0, 0, 0, 0, 0, 0},
        // {7, 0, 0, 0, 0, 0, 0, 0}};
        


        int[] temp = current_state[0];
        current_state[0] = current_state[7];
        current_state[7] = temp;
        temp = current_state[1];
        current_state[1] = current_state[6];
        current_state[6] = temp;
        temp = current_state[2];
        current_state[2] = current_state[5];
        current_state[5] = temp;
        temp = current_state[3];
        current_state[3] = current_state[4];
        current_state[4] = temp;

        // printgame(current_state);
        // System.out.print("gamestate " + Integer.toString(current_state[0][0]));
        
        
        int[] move = {0,0};
        int me = 2;
        int opponent = 1;
        int turn = 2;


        // int heuristic = randomGuy.HeuristicFuntion(me, opponent, turn, move, current_state);

        ArrayList<int[]> moves = randomGuy.GetMovesFromState(current_state, me);
        int myMove = -1;

        ArrayList<Integer> values = randomGuy.Alpha_beta_recursive(Integer.MIN_VALUE, Integer.MAX_VALUE, true, 0, current_state);
        //  Alpha_beta_recursive(alpha, beta, maximize, depth, current_state[][])

        // System.out.println("Heuristic Function " + heuristic);
        // for (int[] avalibleMove: moves){
        //     if (avalibleMove[0] == values.get(3) && avalibleMove[1] == values.get(4)){
        //         myMove = moves.indexOf(avalibleMove);
        //     }
        // }
    
        System.out.println("decided moves: " + Integer.toString(values.get(3)) + " " + values.get(4));
    }

}
