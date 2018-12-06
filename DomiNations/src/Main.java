import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

class Plateau {
	static final int NB_CASES = 9;
	String[][] cases = new String[NB_CASES][NB_CASES];
	int[][] couronnes = new int[NB_CASES][NB_CASES];
	ArrayList<Integer> score = new ArrayList<Integer>();

	static ArrayList<Plateau> allPlateau = new ArrayList<Plateau>();

	public static void initialize() {
		int nbRemove; // number of dominos to be removed

		Players.createPlayers(); // explicit
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
			// we only have 3 dominos
			Players.newOrder.remove(0);
			break;
		case 4:
			nbRemove = 0;
			break;
		default: // for 1 & 2 players (we assume 1 player is player vs AI)
			nbRemove = 24;
			break;
		}
		Domino.removeDominos(nbRemove);

		Players.givePlayersKings(); // explicit

		// endGame();
		firstTurn();
	}

	private static void firstTurn() {
		// gives a random order for players
		Players.shuffleKings();
		Domino.nextDominos(); // first domino line

		do {
			for (Players player : Players.allPlayers) {
				// choose his next domino
				player.playerDominos.add(Domino.chooseDomino(player));
			}
		} while (!Domino.playableDominos.isEmpty());

		// copy playable into current
		Domino.currentDominos.addAll(Domino.playableDominos);
		// TODO: order not taken into account here
		nextTurn();
	}

	private static void nextTurn() {
		String orientation;
		int[] coordinates = new int[2];

		// clears the next order of players
		Players.newOrder.replaceAll(e -> null);
		System.out.println(Players.newOrder);
		// new domino line
		Domino.nextDominos();

		// TO SEE WORKING PROTOTYPE, ADD MULTI LINE COMMENT: /*
		do {
			for (Players player : Players.allPlayers) {

				coordinates = Domino.chooseCoordinates();
				orientation = Domino.chooseOrientation();

				// TODO place again if not correct
				Domino.placeDomino(player.playerDominos.get(0), player.playerPlateau, coordinates[0], coordinates[1],
						orientation);
				// remove the placed domino
				player.playerDominos.remove(player.playerDominos.get(0));

				// choose his next domino
				player.playerDominos.add(Domino.chooseDomino(player));
			}

			// currentDominos deleted in placeDomino
			// added in chooseDomino then sorted
			// slight issue that will be fixed when all dominos will be played
			Collections.sort(Domino.currentDominos);

			// same with allPlayers and newOrder
			System.out.println(Players.newOrder);
			Players.allPlayers.clear();
			Players.allPlayers.addAll(Players.newOrder);

			if (Domino.allDominoes.size() == 0) { // TODO: other finishing checks
				endGame();
			} else {
				nextTurn();
			}

		} while (!Domino.playableDominos.isEmpty());

		// END OF MULTI LINE COMMENT FOR DEBUG */

		// TODO: if can't place domino throw it away
	}

	private static void createPlateau() {
		for (int i = 0; i < Players.nbPlayers; i++) {
			Plateau plat = new Plateau();
			allPlateau.add(plat);
		}

		int i = 0;
		for (Players player : Players.allPlayers) {
			player.playerPlateau = Plateau.allPlateau.get(i);
			i++;
		}
	}

	private static void placeCastles() {
		for (Plateau plateau : allPlateau) {
			plateau.cases[4][4] = "Ch�teau"; // Castle at the center
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
		if (plateau.cases[i][j] != null) {
			// check cases exist, don't check below for imax
			if (i < (NB_CASES - 1) && plateau.cases[i + 1][j] != null) {
				if (plateau.cases[i][j].equals(plateau.cases[i + 1][j]) && !checkedCases[i + 1][j]) { // check below
					score = countScore(plateau, i + 1, j, score, checkedCases);
				}
			}
			// check cases exist, don't check right for jmax
			if (j < (NB_CASES - 1) && plateau.cases[i][j + 1] != null) {
				if (plateau.cases[i][j].equals(plateau.cases[i][j + 1]) && !checkedCases[i][j + 1]) { // check right
					score = countScore(plateau, i, j + 1, score, checkedCases);
				}
			}
			// check cases exist, don't check above for imin
			if (i > 0 && plateau.cases[i - 1][j] != null) {
				if (plateau.cases[i][j].equals(plateau.cases[i - 1][j]) && !checkedCases[i - 1][j]) { // check above
					score = countScore(plateau, i - 1, j, score, checkedCases);
				}
			}
			// check cases exist, don't check left for jmin
			if (j > 0 && plateau.cases[i][j - 1] != null) {
				if (plateau.cases[i][j].equals(plateau.cases[i][j - 1]) && !checkedCases[i][j - 1]) { // check left
					score = countScore(plateau, i, j - 1, score, checkedCases);
				}
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
			dataScanner.close();
		}
		scanner.close();
	}

	public static int[] chooseCoordinates() {
		int[] coordinates = new int[2];

		Scanner scan = new Scanner(System.in);
		System.out.println("Entrez la coordonée x du domino: ");
		// while scan does not have an int between 0 and 9
		while (!scan.hasNext("[0-9]")) {
			System.out.println("Entrez un nombre positif !");
			scan.next(); // this is important!
		}
		int x = scan.nextInt();

		System.out.println("Entrez la coordonée y du domino: ");
		while (!scan.hasNext("[0-9]")) {
			System.out.println("Entrez un nombre positif !");
			scan.next(); // this is important!
		}
		int y = scan.nextInt();

		coordinates[0] = x;
		coordinates[1] = y;

		return coordinates;
	}

	public static String chooseOrientation() {
		String orientation = "";

		Scanner scan = new Scanner(System.in);
		System.out.println("Entrez le sens du domino (up, down, left ou right) :");
		orientation = scan.nextLine();

		// verify input
		if (!orientation.matches("up|down|left|right")) {
			System.out.println("Orientation incorrecte !");
			chooseOrientation();
		}

		return orientation;
	}

	public static Domino chooseDomino(Players player) {
		int chosenNumber = 0;
		int index = 0;
		Domino chosenDomino = null;

		Scanner scan = new Scanner(System.in);
		System.out.print("Domino jouables : ");
		for (Domino domino : playableDominos) {
			System.out.print("Domino " + domino.numDomino + ", ");
		}
		System.out.println("\n" + player.name + ":" + " Entrez le numéro du prochain domino que vous voulez jouer: ");

		while (!scan.hasNextInt()) {
			System.out.println("Entrez un nombre positif !");
			scan.next(); // this is important!
		}
		chosenNumber = scan.nextInt();

		for (Domino domino : playableDominos) {
			if (domino.numDomino == chosenNumber) {
				chosenDomino = domino;
				break;
			}
			index++;
		}

		// if still null chosenNumber not valid
		if (chosenDomino == null) {
			System.out.println("Entrez un numéro de domino valide !");
			index = 0;
			chooseDomino(player);
		}

		playableDominos.remove(chosenDomino);
		currentDominos.add(chosenDomino);
		Players.newOrder.set(index, player); // set order accordingly

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

			// check if surrounding cases are the same type
			/* regex below if we want
			 * if (near1.toString().matches(".*"+domino.type1+".*|.*Ch.teau.*")) {
			 * System.out.println("Success !"); }
			 */

			if (near1.contains(domino.type1) || near1.contains("Château") || near2.contains(domino.type2)
					|| near2.contains("Château")) {
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
			System.out.println("Cases d�j� prises !");
		}

		checkSize(plateau, x1, y1, x2, y2);

		currentDominos.remove(domino);

		afficherPlateau(); // redraw the board
	}

	private static void checkSize(Plateau plateau, int x1, int y1, int x2, int y2) {
		loopColumns(x1, plateau);
		if (x2 != x1) {
			loopColumns(x2, plateau);
		}

		loopRows(y1, plateau);
		if (y2 != y1) {
			loopRows(y2, plateau);
		}

	}

	private static void loopRows(int y, Plateau plateau) {
		// loop through and count the number of nulls
		int count = 0;
		for (int i = 0; i < NB_CASES; i++) {
			if (plateau.cases[i][y] != null) {
				count++;
			}
		}
		if (count > 4) {
			System.out.println("Votre colonne est plus grande que 5 éléments !");
		}
		count = 0;
	}

	private static void loopColumns(int x, Plateau plateau) {
		// loop through and count the number of nulls
		int count = 0;
		for (int i = 0; i < NB_CASES; i++) {
			if (plateau.cases[x][i] != null) {
				count++;
			}
		}
		if (count > 4) {
			System.out.println("Votre ligne est plus grande que 5 éléments !");
		}
		count = 0;
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
		playersNumber();

		for (int i = 0; i < nbPlayers; i++) {
			Players player = new Players();
			allPlayers.add(player);
		}

		for (int i = 0; i < 4; i++) {
			// for now we don't have any order set.
			newOrder.add(null);
		}

		playersNames();
	}

	private static void playersNumber() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Entrez le nombre de joueurs: ");
		while (!scan.hasNext("[1-4]")) {
			System.out.println("Entrez un nombre entre 1 et 4 !");
			scan.next(); // this is important!
		}
		Players.nbPlayers = scan.nextInt();

	}

	private static void playersNames() {
		int i = 0;
		Scanner scan = new Scanner(System.in);
		System.out.println("Entrez le nom de chaque joueur: ");

		for (Players player : allPlayers) {
			System.out.println("Joueur " + i + ":");
			player.name = scan.nextLine();
			i++;
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
			System.out.println(player.name + ": " + player.playerKings);
		}
	}

	// get a random starting order
	public static void shuffleKings() {

		/*
		 * Used the Fisher�Yates shuffle, hope we were allowed to implement already
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
			System.out.println(player.name + "	" + player.scoreTotal);
		}

		Collections.sort(allPlayers); // sort players to get positions by score
		// last player in the list is the winner
		System.out.println("Winner :" + Players.allPlayers.get(allPlayers.size() - 1).name);
	}

	@Override
	public int compareTo(Players player) {
		return this.scoreTotal - player.scoreTotal;
	}

}

public class Main {
	public static void main(String[] args) {

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

		System.out.println(Plateau.allPlateau.get(0).cases.length);
		Plateau.endGame();

	}
}
