import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Random;

class Plateau {
	static String[][] cases = new String[5][5];
	static int[][] couronnes = new int[5][5];

	public static void initialize() {
		int nbRemove; // number of dominos to be removed

		Kings.createKings(); // explicit

		try {
			Domino.getDominoes(); // load dominos
		} catch (FileNotFoundException e) {
			System.out.print("File not found !");
		}

		switch (Players.nbPlayers) {
		case 3:
			nbRemove = 12;
			Kings.removeKing(); // only case with 3 kings
			break;
		case 4:
			nbRemove = 0;
			break;
		default: // for 1 & 2 players (we assume 1 player is player vs AI)
			nbRemove = 24;
			break;
		}
		Domino.removeDominos(nbRemove);

		Players.createPlayers(); // explicit
		Players.givePlayersKings(); // explicit

		nextTurn();
	}

	private static void nextTurn() {
		Domino.nextDominos();
		Kings.randomKing();

		/*
		 * TODO: player with king domino placement castle placement if can't place
		 * domino throw it away
		 * tell who won
		 * 
		 * DO WE HAVE 1 BOARD OR 1 FOR EACH PLAYER ???	
		 * 
		 */

	}

	public static void afficher() {
		for (int i = 0; i < cases.length; i++) { // rows
			for (int j = 0; j < cases[0].length; j++) { // columns
				System.out.print("	" + cases[i][j]); // add a draw here or whatnot
			}
			System.out.println(""); // end line
		}
		System.out.println("");
	}

	public static void countScoree() { // not working great
		int score = 0;
		boolean[][] checkedCases = new boolean[5][5];

		for (int i = 0; i < cases.length; i++) { // rows
			for (int j = 0; j < cases[0].length; j++) { // columns
				checkedCases[i][j] = true;

				if (j < 4 && cases[i][j] != null && cases[i][j + 1] != null) {
					if (cases[i][j].equals(cases[i][j + 1]) && !checkedCases[i][j + 1]) { // check to the right
						score++;
					}
				} else if (j == 4 && cases[i][j] != null && cases[i][j - 1] != null) { // for the last column
					if (cases[i][j].equals(cases[i][j - 1])) { // check to the left (if not not counted properly)
						score++;
					}
				}

				if (i < 4 && cases[i][j] != null && cases[i + 1][j] != null) {
					if (cases[i][j].equals(cases[i + 1][j]) && !checkedCases[i + 1][j]) { // check below
						score++;
					}
				} else if (i == 4 && cases[i][j] != null && cases[i - 1][j] != null) { // for the last row
					if (cases[i][j].equals(cases[i - 1][j])) { // check above (if not not counted properly)
						score++;
					}
				}

			}
		}

		System.out.print(score);

	}

	public static void endGame() {
		int score[] = { 0, 0 }; // mult, couronnes
		boolean[][] checkedCases = new boolean[5][5];

		for (int i = 0; i < Plateau.cases.length; i++) { // rows
			for (int j = 0; j < Plateau.cases[0].length; j++) { // columns
				int mult[] = Plateau.countScore(i, j, score, checkedCases);
				System.out.print(mult[0] * mult[1] + ", ");
				score[0] = 0;
				score[1] = 0;
			}
		}
	}

	public static int[] countScore(int i, int j, int[] score, boolean[][] checkedCases) { // bit ugly but working fine
		checkedCases[i][j] = true; // mark current one as checked
		score[0]++; // mult++
		score[1] += couronnes[i][j];

		// check order : below, right, above, left

		if (i < 4 && cases[i][j] != null && cases[i + 1][j] != null) { // check cases exist, don't check below for imax
			if (cases[i][j].equals(cases[i + 1][j]) && !checkedCases[i + 1][j]) { // check below
				score = countScore(i + 1, j, score, checkedCases);
			}
		}

		if (j < 4 && cases[i][j] != null && cases[i][j + 1] != null) { // check cases exist, don't check right for jmax
			if (cases[i][j].equals(cases[i][j + 1]) && !checkedCases[i][j + 1]) { // check to the right
				score = countScore(i, j + 1, score, checkedCases);
			}
		}

		if (i > 0 && cases[i][j] != null && cases[i - 1][j] != null) { // check cases exist, don't check above for imin
			if (cases[i][j].equals(cases[i - 1][j]) && !checkedCases[i - 1][j]) { // check above
				score = countScore(i - 1, j, score, checkedCases);
			}
		}

		if (j > 0 && cases[i][j] != null && cases[i][j - 1] != null) { // check cases exist, don't check left for jmin
			if (cases[i][j].equals(cases[i][j - 1]) && !checkedCases[i][j - 1]) { // check to the left
				score = countScore(i, j - 1, score, checkedCases);
			}
		}

		return score; // once everything around current have been checked, return score+1

	}

}

class Domino extends Plateau implements Comparable<Domino> {
	int nbCouronne1;
	String type1;
	int nbCouronne2;
	String type2;
	int numDomino;

	int posX; // position on game board
	int posY;

	static ArrayList<Domino> allDominoes = new ArrayList<Domino>();

	public static void getDominoes() throws FileNotFoundException { // import dominoes, throws exception if file not
																	// found
		Scanner scanner = new Scanner(new File("dominos.csv")); // open csv file
		scanner.nextLine(); // skip first line

		while (scanner.hasNextLine()) { // read line by line

			Scanner dataScanner = new Scanner(scanner.nextLine()); // take one line
			dataScanner.useDelimiter(","); // indicates that ',' separates the data

			Domino dom = new Domino(); // create a new domino

			while (dataScanner.hasNext()) { // read every element of the line
				dom.nbCouronne1 = dataScanner.nextInt();
				dom.type1 = dataScanner.next();
				dom.nbCouronne2 = dataScanner.nextInt();
				dom.type2 = dataScanner.next();
				dom.numDomino = dataScanner.nextInt();
			}

			allDominoes.add(dom); // add the populated domino
			// System.out.println(dom.nbCouronne1 + "," + dom.type1 + "," + dom.nbCouronne2
			// + "," + dom.type2 + "," + dom.numDomino);
			dataScanner.close();
		}
		scanner.close();
	}

	public static void removeDominos(int nbRemove) {

		for (int i = 0; i < nbRemove; i++) {
			Random rand = new Random();
			int n = rand.nextInt(allDominoes.size()); // max = size - 1.

			allDominoes.remove(n);
		}
		System.out.println(allDominoes.size());
	}

	public static void placeDomino(Domino domino, int x1, int y1, int x2, int y2) {

		ArrayList<String> near1 = new ArrayList<String>();
		ArrayList<String> near2 = new ArrayList<String>();

		if (cases[x1][y1] == null && cases[x1][y1] == null) { // check if cases are empty
			if (x1 < 4 && cases[x1 + 1][y1] != null) { // avoid null & out of bound
				near1.add(cases[x1 + 1][y1]); // below
			}
			if (y1 < 4 && cases[x1][y1 + 1] != null) {
				near1.add(cases[x1][y1 + 1]); // right
			}
			if (x1 > 0 && cases[x1 - 1][y1] != null) {
				near1.add(cases[x1 - 1][y1]); // above
			}
			if (y1 > 0 && cases[x1][y1 - 1] != null) {
				near1.add(cases[x1][y1 - 1]); // left
			}

			if (x2 < 4 && cases[x2 + 1][y2] != null) {
				near2.add(cases[x2 + 1][y2]); // below
			}
			if (y2 < 4 && cases[x2][y2 + 1] != null) {
				near2.add(cases[x2][y2 + 1]); // right
			}
			if (x2 > 0 && cases[x2 - 1][y2] != null) {
				near2.add(cases[x2 - 1][y2]); // above
			}
			if (y2 > 0 && cases[x2][y2 - 1] != null) {
				near2.add(cases[x2][y2 - 1]); // left
			}

			// check if surrounding cases are the same type
			if (near1.contains(domino.type1) || near2.contains(domino.type2)) {
				cases[x1][y1] = domino.type1;
				cases[x2][y2] = domino.type2;
				couronnes[x1][y1] = domino.nbCouronne1;
				couronnes[x2][y2] = domino.nbCouronne2;
			} else {
				System.out.println("Les dominos ne correspondent pas !");
			}

			// below is used to skip validation for debug
			cases[x1][y1] = domino.type1;
			cases[x2][y2] = domino.type2;
			couronnes[x1][y1] = domino.nbCouronne1;
			couronnes[x2][y2] = domino.nbCouronne2;

		} else {
			System.out.println("Cases déjà prises !");
		}
		afficher(); // redraw the board
	}

	protected static void nextDominos() {
		ArrayList<Domino> givenDominos = new ArrayList<Domino>();

		for (int i = 0; i < Kings.allKings.size(); i++) { // for each king in game
			Random rand = new Random();
			int n = rand.nextInt(allDominoes.size()); // get a random domino number

			if (!givenDominos.contains(allDominoes.get(n))) { // prevents having the same domino twice
				givenDominos.add(allDominoes.get(n));
			} else {
				i--; // decrement i and retry
			}

			// TODO: draw the domino and then flip it ; draw(allDominoes.get(n))
		}
		Collections.sort(givenDominos);
		System.out.println(givenDominos);

	}

	public int compareTo(Domino dom) { // comes with the implement, read the doc
		return this.numDomino - dom.numDomino; // compares dominos numbers
	}

}

class Kings {
	String color;
	static ArrayList<Kings> allKings = new ArrayList<Kings>();

	static void createKings() {
		String[] possibleColors = { "pink", "yellow", "green", "blue" }; // allows to easily init all kings in for loop
		for (int i = 0; i < 4; i++) {
			Kings king = new Kings();
			king.color = possibleColors[i];
			allKings.add(king);
		}
	}

	public static void randomKing() {
		ArrayList<Kings> givenKings = new ArrayList<Kings>();

		for (int i = 0; i < Kings.allKings.size(); i++) { // for each king in game
			Random rand = new Random();
			int n = rand.nextInt(allKings.size()); // get a random domino number

			if (!givenKings.contains(allKings.get(n))) { // prevents having the same king twice
				givenKings.add(allKings.get(n));
			} else {
				i--; // decrement i and retry
			}
		}

		System.out.println(givenKings);

	}

	static void removeKing() {
		allKings.remove(3); // removes the last King
	}

}

class Players {
	static int nbPlayers;
	static ArrayList<Players> allPlayers = new ArrayList<Players>();
	ArrayList<Kings> playerKings = new ArrayList<Kings>();
	ArrayList<Domino> playerDominos = new ArrayList<Domino>();

	static void createPlayers() {
		for (int i = 0; i < 4; i++) {
			Players player = new Players();
			allPlayers.add(player);
		}
	}

	static void givePlayersKings() {
		int i = 0;

		while (i < Kings.allKings.size()) { // while we still have kings to give
			for (int j = 0; j < nbPlayers; j++) { // for each player
				allPlayers.get(j).playerKings.add(Kings.allKings.get(i)); // give the player a king
				i++; // indicates that a king have been given
			}
		}

		for (int j = 0; j < nbPlayers; j++) {
			System.out.println(allPlayers.get(j).playerKings);
		}
	}

}

public class Main {
	public static void main(String[] args) {

		// TODO: Select number of players with UI ; Count score

		Players.nbPlayers = 2;
		Plateau.initialize();

		int x1 = 1;
		int y1 = 3;
		int x2 = 1;
		int y2 = 4;

		System.out.println((Domino.allDominoes.get(1).type1 + "" + Domino.allDominoes.get(1).type2));
		Domino.placeDomino(Domino.allDominoes.get(1), x1, y1, x2, y2);
		Domino.placeDomino(Domino.allDominoes.get(8), 0, 0, 0, 1);
		Domino.placeDomino(Domino.allDominoes.get(3), 1, 0, 2, 0);
		Domino.placeDomino(Domino.allDominoes.get(5), 0, 2, 1, 2);

		Plateau.couronnes[0][0] = 1; // for testing purposes

		Plateau.endGame();

	}
}
