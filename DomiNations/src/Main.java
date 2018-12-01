import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Random;

class Plateau {
	static final int NB_CASES = 9;
	String[][] cases = new String[NB_CASES][NB_CASES];
	int[][] couronnes = new int[NB_CASES][NB_CASES];
	ArrayList<Integer> score = new ArrayList<Integer>();

	static ArrayList<Plateau> allPlateau = new ArrayList<Plateau>();

	public static void initialize() {
		int nbRemove; // number of dominos to be removed

		createPlateau();
		placeCastles();
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

		// gives a random order for players
		Players.shuffleKings();

		Domino.nextDominos(); // first domino line

		// copy playable into current
		Domino.currentDominos.addAll(Domino.playableDominos);

		// TODO first turn to select first dominos
		nextTurn();
	}

	private static void nextTurn() {
		String orientation;
		int[] coordinates = new int[2];

		// clears the next order of players
		Players.newOrder.clear();
		// new domino line
		Domino.nextDominos();

		// TO SEE WORKING PROTOTYPE, ADD MULTI LINE COMMENT: /*

		for (Players player : Players.allPlayers) {

			coordinates = Domino.chooseCoordinates();
			orientation = Domino.chooseOrientation();

			// ERROR i know it's normal I don't have user input yet
			Domino.placeDomino(player.playerDominos.get(0), player.playerPlateau, coordinates[0], coordinates[1],
					orientation);
			// remove the placed domino
			player.playerDominos.remove(player.playerDominos.get(0));

			// choose his next domino
			player.playerDominos.add(Domino.chooseDomino(player));
		}

		// clears current then copy playable into it
		Domino.currentDominos.clear();
		Domino.currentDominos.addAll(Domino.playableDominos);

		// same with allPlayers and newOrder
		Players.allPlayers.clear();
		Players.allPlayers.addAll(Players.newOrder);

		if (Domino.allDominoes.size() == 0) { // TODO: other finishing checks
			endGame();
		} else {
			nextTurn();
		}

		// END OF MULTI LINE COMMENT FOR DEBUG */

		// TODO: if can't place domino throw it away
	}

	private static void createPlateau() {
		for (int i = 0; i < Players.nbPlayers; i++) {
			Plateau plat = new Plateau();
			allPlateau.add(plat);
		}
	}

	private static void placeCastles() {
		for (Plateau plateau : allPlateau) {
			plateau.cases[4][4] = "Château"; // Castle at the center
		}
	}

	public static void afficherPlateau() {
		for (Plateau plateau : allPlateau) {
			System.out.println("Plateau:");
			for (int i = 0; i < plateau.cases.length; i++) { // rows
				for (int j = 0; j < plateau.cases[0].length; j++) { // columns
					System.out.print("	" + plateau.cases[i][j]); // add a draw here or whatnot
				}
				System.out.println(""); // end line
			}
		}
		System.out.println("");
	}

	public static void endGame() {
		int score[] = { 0, 0 }; // mult, couronnes
		boolean[][] checkedCases = new boolean[NB_CASES][NB_CASES];

		for (Plateau plateau : allPlateau) {
			for (int i = 0; i < plateau.cases.length; i++) { // rows
				for (int j = 0; j < plateau.cases[0].length; j++) { // columns
					int mult[] = Plateau.countScore(plateau, i, j, score, checkedCases);

					plateau.score.add(mult[0] * mult[1]);
					score[0] = 0;
					score[1] = 0;
				}
			}
			checkedCases = new boolean[NB_CASES][NB_CASES]; // resets the checked cases
			System.out.println("");
		}

		Players.winner();

	}

	public static int[] countScore(Plateau plateau, int i, int j, int[] score, boolean[][] checkedCases) {
		// bit ugly but working fine

		checkedCases[i][j] = true; // mark current one as checked
		score[0]++; // mult++
		score[1] += plateau.couronnes[i][j];

		// check order : below, right, above, left

		// check cases exist, don't check below for imax
		if (i < (plateau.cases.length - 1) && plateau.cases[i][j] != null && plateau.cases[i + 1][j] != null) {
			if (plateau.cases[i][j].equals(plateau.cases[i + 1][j]) && !checkedCases[i + 1][j]) { // check below
				score = countScore(plateau, i + 1, j, score, checkedCases);
			}
		}
		// check cases exist, don't check right for jmax
		if (j < (plateau.cases.length - 1) && plateau.cases[i][j] != null && plateau.cases[i][j + 1] != null) {
			if (plateau.cases[i][j].equals(plateau.cases[i][j + 1]) && !checkedCases[i][j + 1]) { // check to the right
				score = countScore(plateau, i, j + 1, score, checkedCases);
			}
		}
		// check cases exist, don't check above for imin
		if (i > 0 && plateau.cases[i][j] != null && plateau.cases[i - 1][j] != null) {
			if (plateau.cases[i][j].equals(plateau.cases[i - 1][j]) && !checkedCases[i - 1][j]) { // check above
				score = countScore(plateau, i - 1, j, score, checkedCases);
			}
		}
		// check cases exist, don't check left for jmin
		if (j > 0 && plateau.cases[i][j] != null && plateau.cases[i][j - 1] != null) {
			if (plateau.cases[i][j].equals(plateau.cases[i][j - 1]) && !checkedCases[i][j - 1]) { // check to the left
				score = countScore(plateau, i, j - 1, score, checkedCases);
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
	static ArrayList<Domino> currentDominos = new ArrayList<Domino>();
	static ArrayList<Domino> playableDominos = new ArrayList<Domino>();

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

	public static int[] chooseCoordinates() {
		int[] coordinates = new int[2];
		// TODO choose coordinates
		coordinates[0] = 0;
		coordinates[1] = 0;

		return coordinates;
	}

	public static String chooseOrientation() {
		String orientation = "";
		// TODO choose orientation

		return orientation;
	}

	public static Domino chooseDomino(Players player) {
		int chosenNumber = 0;
		Domino chosenDomino;

		// TODO: select domino number

		chosenDomino = playableDominos.get(chosenNumber);
		Players.newOrder.set(chosenNumber, player);

		return chosenDomino;
	}

	public static void removeDominos(int nbRemove) {

		for (int i = 0; i < nbRemove; i++) {
			Random rand = new Random();
			int n = rand.nextInt(allDominoes.size()); // max = size - 1.

			allDominoes.remove(n);
		}
		System.out.println(allDominoes.size() + " Dominos en jeu");
	}

	public static void placeDomino(Domino domino, Plateau plateau, int x1, int y1, String orientation) {
		int x2, y2;

		// TODO: ask for coordinates
		// Maybe a separate function ?
		switch (orientation) {
		case ("up"):
			x2 = x1 - 1;
			y2 = y1;
			break;
		case ("down"):
			x2 = x1 + 1;
			y2 = y1;
			break;
		case ("left"):
			x2 = x1;
			y2 = y1 - 1;
			break;
		case ("right"):
			x2 = x1;
			y2 = y1 + 1;
			break;
		default: // right by default
			x2 = x1;
			y2 = y1 + 1;
			break;
		}

		// Below creates an array with the cases around the selected case.
		if (plateau.cases[x1][y1] == null && plateau.cases[x2][y2] == null) { // check if cases are empty

			ArrayList<String> near1 = getNearCases(plateau, x1, y1);
			ArrayList<String> near2 = getNearCases(plateau, x2, y2);
			near1.add("Château");
			near2.add("Château");

			// check if surrounding cases are the same type
			if (near1.contains(domino.type1) || near2.contains(domino.type2)) {
				plateau.cases[x1][y1] = domino.type1;
				plateau.cases[x2][y2] = domino.type2;
				plateau.couronnes[x1][y1] = domino.nbCouronne1;
				plateau.couronnes[x2][y2] = domino.nbCouronne2;
			} else {
				System.out.println("Les dominos ne correspondent pas !");
			}

			// below is used to skip validation for debug
			plateau.cases[x1][y1] = domino.type1;
			plateau.cases[x2][y2] = domino.type2;
			plateau.couronnes[x1][y1] = domino.nbCouronne1;
			plateau.couronnes[x2][y2] = domino.nbCouronne2;

		} else {
			System.out.println("Cases déjà prises !");
		}

		afficherPlateau(); // redraw the board
	}

	private static ArrayList<String> getNearCases(Plateau plateau, int x, int y) {
		ArrayList<String> near = new ArrayList<String>();

		if (x < NB_CASES - 1 && plateau.cases[x + 1][y] != null) { // avoid null & out of bound
			near.add(plateau.cases[x + 1][y]); // below
		}
		if (y < NB_CASES - 1 && plateau.cases[x][y + 1] != null) {
			near.add(plateau.cases[x][y + 1]); // right
		}
		if (x > 0 && plateau.cases[x - 1][y] != null) {
			near.add(plateau.cases[x - 1][y]); // above
		}
		if (y > 0 && plateau.cases[x][y - 1] != null) {
			near.add(plateau.cases[x][y - 1]); // left
		}
		return near;
	}

	protected static void nextDominos() {
		// clear old list
		playableDominos.clear();

		for (int i = 0; i < Kings.allKings.size(); i++) { // for each king in game
			Random rand = new Random();
			int n = rand.nextInt(allDominoes.size()); // get a random domino number

			playableDominos.add(allDominoes.get(n));
			allDominoes.remove(n);// removes the selected domino from the list

			// TODO: draw the domino and then flip it ; draw(allDominoes.get(n))
		}
		Collections.sort(playableDominos);

		System.out.print("Domino current : ");
		for (Domino domino : currentDominos) {
			System.out.print("Domino " + domino.numDomino + ", ");
		}
		System.out.println();
		System.out.print("Domino playable : ");
		for (Domino domino : playableDominos) {
			System.out.print("Domino " + domino.numDomino + ", ");
		}
		System.out.println("\n");

	}

	@Override
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

	static void removeKing() {
		allKings.remove(3); // removes the last King
	}

}

class Players implements Comparable<Players> {
	static int nbPlayers;
	static ArrayList<Players> allPlayers = new ArrayList<Players>();
	public static ArrayList<Players> newOrder = new ArrayList<Players>();

	ArrayList<Kings> playerKings = new ArrayList<Kings>();
	ArrayList<Domino> playerDominos = new ArrayList<Domino>();
	Plateau playerPlateau;
	String name;
	int scoreTotal;

	static void createPlayers() {
		for (int i = 0; i < nbPlayers; i++) {
			Players player = new Players();
			allPlayers.add(player);
			// for now we don't have any order set.
			newOrder.add(null);

			player.playerPlateau = Plateau.allPlateau.get(i);
		}
	}

	static void givePlayersKings() {
		int i = 0;

		while (i < Kings.allKings.size()) { // while we still have kings to give
			for (Players player : allPlayers) { // for each player
				player.playerKings.add(Kings.allKings.get(i)); // give the player a king
				i++;
			}
		}

		for (Players player : allPlayers) {
			System.out.println(player + ": " + player.playerKings);
		}
	}

	// get a random starting order
	public static void shuffleKings() {

		/*
		 * Used the Fisher–Yates shuffle, hope we were allowed to implement already
		 * existing methods. Did the implementation myself.
		 */

		int m = allPlayers.size();
		int i;
		Players temp;
		Random rand = new Random();

		while (m != 0) {
			// Pick a remaining element, decrement m
			i = rand.nextInt(m);
			m--;

			// Swap it with current element.
			temp = allPlayers.get(m);
			allPlayers.set(m, allPlayers.get(i));
			allPlayers.set(i, temp);
		}
		System.out.println(allPlayers);
	}

	static void winner() {

		for (Players player : allPlayers) {
			for (Integer score : player.playerPlateau.score) {
				player.scoreTotal += score;
			}
			System.out.println(player + "	" + player.scoreTotal);
		}

		Collections.sort(allPlayers); // sort players to get positions by score
		// last player in the list is the winner
		System.out.println("Vainceur :" + Players.allPlayers.get(allPlayers.size() - 1));
	}

	@Override
	public int compareTo(Players player) {
		return this.scoreTotal - player.scoreTotal;
	}

}

public class Main {
	public static void main(String[] args) {

		// TODO: Select number of players with UI

		Players.nbPlayers = 2;
		Plateau.initialize();

		int x1 = 1;
		int y1 = 3;

		System.out.println((Domino.allDominoes.get(1).type1 + "" + Domino.allDominoes.get(1).type2));
		Domino.placeDomino(Domino.allDominoes.get(1), Plateau.allPlateau.get(0), x1, y1, "right");
		Domino.placeDomino(Domino.allDominoes.get(8), Plateau.allPlateau.get(0), 0, 0, "right");
		Domino.placeDomino(Domino.allDominoes.get(3), Plateau.allPlateau.get(0), 1, 0, "down");
		Domino.placeDomino(Domino.allDominoes.get(5), Plateau.allPlateau.get(0), 0, 2, "down");

		Plateau.allPlateau.get(0).couronnes[0][0] = 1; // for testing purposes

		Plateau.allPlateau.get(1).couronnes[0][0] = 1; // for testing purposes
		Plateau.allPlateau.get(1).cases[0][0] = "Mer";
		Plateau.allPlateau.get(1).cases[0][1] = "Mer";
		Plateau.allPlateau.get(1).cases[1][0] = "Mer"; // should have 3 points

		Plateau.allPlateau.get(1).couronnes[2][0] = 2; // for testing purposes
		Plateau.allPlateau.get(1).cases[2][0] = "Mine";
		Plateau.allPlateau.get(1).cases[2][1] = "Mine";
		Plateau.allPlateau.get(1).cases[3][1] = "Mine"; // should have 6 points
		Plateau.afficherPlateau();

		Plateau.endGame();

	}
}
