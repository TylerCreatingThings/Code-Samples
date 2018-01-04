import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.Stack;

public class Main {
	private static final int SQUARE_WIDTH = 3;
	private static final int SQUARE_HEIGHT = 3;
	
	private static int[] ROW_FINAL = {1,2,3,4,5,6,7,8,9};
	private static int FINAL_SUM = 0;
	
	public static void main(String[] args) {		
		
		//Read in the sudoku game
		Sudoku game = new Sudoku("testSudokuIncomplete.txt");
		
		//Update local variables based on games input.
		createFinalRow(game);
		
		//Create Queue for AC-3, creates the Arcs
		Queue<Arc> q = game.makeArcQueue(SQUARE_WIDTH, SQUARE_HEIGHT);
				
		//Implement AC-3
		AC3(q);
		
		//Sets the values for the nodes that have a domain of one possible value.
		SetThatGameAfterAC3(game);
		
		Sudoku solution;
		
		//Checks if AC-3 comes up with a solution, if it is print it out, else run backtrack search.
		if(isGoalState(game)){
			System.out.println("Solution is found after AC3.");
			game.printPuzzle();
		}
		else{
			
			//Runs backtrack search, if a solution is found print it out, else do nothing.
			solution = BacktrackSearch(game);
		}
	}
	
	//Asks for a Queue of arcs.
	//Implements AC3
	private static boolean AC3(Queue<Arc> arcs) {
		
		while (!arcs.isEmpty()) {
			
			Arc arc = (Arc) arcs.poll();
			
			boolean revised = false;
			
			for (int i = 0; i < arc.start.domain.length; i++) {
				
				if (arc.start.domain[i] == arc.end.getValue()) {
					
					arc.start.domain[i] = 0;
					
					revised = true;
				}
			}
			
			System.out.println("Number of arcs: " + arcs.size());
			
			if (!revised)
				continue;
			
			if (Collections.frequency(Arrays.asList(arc.start.domain), 0) == arc.start.domain.length)
				return false;
			
			for (Arc a : arc.start.arcs) {
				
				arcs.add(new Arc(a.end, a.start));
			}
		}
		
		return true;
	}
	
	//Asks for a game to solve
	//Returns a solved game or null if not solvable.
	private static Sudoku BacktrackSearch(Sudoku game){
		//tree.
		
		Stack<Sudoku> games = new Stack<Sudoku>();
		games.add(game);
		games= Backtrack(games);
		if (!games.isEmpty()){
		game = games.pop();}else{
			System.out.println("No Solution can be found.");
			return null;
		}
		
		return games.pop();
		//return UpdateValuesOnDomain(games.pop());
	}
	
	//Recursively solves game using DFS/Backtrack search.
	private static Stack<Sudoku> Backtrack(Stack<Sudoku> games){
		Sudoku game = games.peek();
		
		//If most recent inputed game is a solution return result.
		
		
		if(isGoalState(game)){
			games.peek().printPuzzle();
			return games;
		}
		
		//Find the first unassigned variable and return its location.
		int[] index = SelectUnassignedVariable(game);
		
		//if no variable left and not solved, then unsolvable here, time to backtrack!
		if(index == null){
			
			games.pop();
			return games;
		}
		
		game=games.peek();
		int row = index[0];
		int col = index[1];
		
		for(int i :game.puzzle[row][col].domain){
			
			if (i != 0){
				Sudoku otherGame= new Sudoku(game);
				int[] domain = new int[otherGame.puzzle[row][col].domain.length];
				
				//assign i to a game and insert it into games.
				otherGame.puzzle[row][col].value = i;
				
				for(int x=0;x<domain.length;x++){
					if(x == i-1){
						domain[x] = i;
					}
					else{
						domain[x] = 0;
					}
				}
				
				
				//Assign new domain vals.
				otherGame.puzzle[row][col].domain = domain;
				
				//updates all other domains.
				otherGame = UpdateOtherDomains(row,col,otherGame,i);
				
				if (otherGame!=null){
					
					//Sets values with only 1 domain value.
					SetThatGameAfterAC3(otherGame);
					
					//Checks if the game is valid
					if(games!=null){
					if(IsGamevalid(otherGame) ){
						
						//Adds it to the stack if it is.
						games.add(otherGame);
						
						//Recursively calls itself.
						games = Backtrack(games);
					
					
				}}
				}
				
				if(games!=null){
				if (isGoalState(games.peek())){
					return games;
				}}
			}
		}
		
		// if(games.peek()!=null){
			// System.out.println("in");
			//return games;
			//}
		//if(games!=null){
	    games.pop();
	    return games;//}
	//return null;
	}
	
	//Updates local variables.
	private static void createFinalRow(Sudoku game){
		int[] array = new int[SQUARE_WIDTH*SQUARE_HEIGHT];
		int sum= 0;
		for(int i=0;i<array.length;i++){
			array[i] = i+1;
			sum += i+1;
		}
		
		ROW_FINAL = array;
		FINAL_SUM = sum;
		
	}
	
	//takes a game
	//Looks at each value and updates it if it only has one value left in its domain.
	private static Sudoku SetThatGameAfterAC3(Sudoku game){
		for (int row = 0; row < game.puzzle.length; row++) {
			
			for (int col = 0; col < game.puzzle[row].length; col++) {
				if(isValidAssignment(game.puzzle[row][col].domain) != -1){
					game.puzzle[row][col].value = isValidAssignment(game.puzzle[row][col].domain);
				}
			}
		}
		return game;
	}
	
	//Do all values in each row add up to the final sum? Then its a goal state congrats!
	private static boolean isGoalState(Sudoku game){
		//for each line isValidAssignment true, and or = to <1 through 16>
		for (int row = 0; row < game.puzzle.length; row++) {	
			int sum =0;
			for (int col = 0; col < game.puzzle[row].length; col++) {
				sum += game.puzzle[row][col].value;
			}
			if(sum != FINAL_SUM){
				return false;
			}
		}
		return true;
		
	}
	
	//if returns -1 its invalid meaning there is more then one possible domain value, otherwise it returns the int value of the domain.
	private static int isValidAssignment(int[] assignment){
		int isValid = -1;
		int temp =0;
		int count =0;
		
		for (int i : assignment){
			if(i!=0){
				count++;
				temp = i;
			}
		}
		
		if(count == 1)
			isValid = temp;
		
		return isValid;
	}
	
	//Checks if the node is a valid assignment either the domain sums up to ROW_FINAL or it has one value in its domain return true else return false.
	private static boolean FullValidDomain(Sudoku game, int row, int col){
		if(Arrays.equals(game.puzzle[row][col].domain, ROW_FINAL))
			return true;
		else if(isValidAssignment(game.puzzle[row][col].domain) != -1)
			return true;
		return false;
	}
	
	//Gives index of unassigned node domain.
	//Takes a game and finds the first false value from the FullValidDomain method.
	private static int[] SelectUnassignedVariable(Sudoku game){
		if(game == null)
			return null;
		for (int row = 0; row < game.puzzle.length; row++) {
			
			for (int col = 0; col < game.puzzle[row].length; col++) {
				
				if(FullValidDomain(game, row, col))
					continue;
				
				
				int[] index = {row,col};
				return index;
			}
		}
		return null;
	}
	
	//Is the game still valid? Are each of the constraints still enforced? If it is continue, else return false.
	private static boolean IsGamevalid(Sudoku game){
		
		for (int row = 0; row < game.puzzle.length; row++) {
			
				for (int col = 0; col < game.puzzle[row].length; col++) {
					int temp = game.puzzle[row][col].value;
					if (temp!=0){
						for(int temp_row=0; temp_row < game.puzzle.length; temp_row++){
							if (temp_row!=row && game.puzzle[temp_row][col].value==temp){
								return false;
							}
						}
						for(int temp_col=0; temp_col< game.puzzle.length; temp_col++){
							if (temp_col!=col && game.puzzle[row][temp_col].value==temp){
							return false;
							}
						}
					
							
				}
				}
				
			
			}
		return true;
		
	}
	
	//Updates the domains of all other values in the row/col and square after the assignment is made.
	private static Sudoku UpdateOtherDomains(int rowIndex, int colIndex, Sudoku game, int unassignValue){
		int[] newDomain = new int[game.puzzle[rowIndex][colIndex].domain.length];
		int count = 0;
		
		//Check columns and update domains accordingly.
		for(int row = 0; row < game.puzzle.length;row++){
			//use colIndex.

			if(row == rowIndex)
				continue;
			else if(game.puzzle[row][colIndex].value == unassignValue){
				return null;
			}
			else if(!FullValidDomain(game, row, colIndex)){
				for(int i : game.puzzle[row][colIndex].domain){
					if(i == unassignValue){
						newDomain[count] = 0;
					}
					else{
						newDomain[count] = i;
					}
					count++;
				}
				game.puzzle[row][colIndex].domain = newDomain;
				newDomain = new int[game.puzzle[rowIndex][colIndex].domain.length];
				count = 0;
			}
		}
		
		//Check rowIndex.
		for(int col = 0; col < game.puzzle.length;col++){
			
			if(rowIndex == 0 && col == 2)
				rowIndex =0;
			
			if(col==colIndex)
				continue;
			else if(game.puzzle[rowIndex][col].value == unassignValue){
				return null;
			}
			else if(!FullValidDomain(game, rowIndex, col)){
				for(int i : game.puzzle[rowIndex][col].domain){
					if(i == unassignValue){
						newDomain[count] = 0;
					}
					else{
						newDomain[count] = i;
					}
					count++;
				}
				game.puzzle[rowIndex][col].domain = newDomain;
				newDomain = new int[game.puzzle[rowIndex][colIndex].domain.length];
				count = 0;
			}
		}
		
		//Check Square
		int startRow = rowIndex;
		int startCol = colIndex;
		
		if(rowIndex == 9 && colIndex == 3)
			rowIndex = 9;
			
		if(rowIndex != 0){
			startRow = rowIndex/SQUARE_HEIGHT;
			startRow = startRow * SQUARE_HEIGHT;
		}
		
		if(colIndex != 0){
			startCol = colIndex/SQUARE_WIDTH;
			startCol = startCol * SQUARE_WIDTH;
		}
		
		for(int row = startRow;row<startRow+SQUARE_HEIGHT;row++){
			for(int col = startCol; col<startCol+SQUARE_WIDTH;col++){
				if(row == rowIndex && col == colIndex)
					continue;
				else if(game.puzzle[row][col].value == unassignValue){
					return null;
				}
				else if(!FullValidDomain(game, row, col)){
					for(int i : game.puzzle[row][col].domain){
						if(i == unassignValue){
							newDomain[count] = 0;
						}
						else{
							newDomain[count] = i;
						}
						count++;
					}
					game.puzzle[row][col].domain = newDomain;
					newDomain = new int[game.puzzle[rowIndex][colIndex].domain.length];
					count = 0;
				}
			}
		}
		return game;
	}
}


















