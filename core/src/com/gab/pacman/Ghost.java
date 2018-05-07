package com.gab.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import static java.lang.Math.abs;

public class Ghost {
	private Texture[][] ghostAnim;
	private Texture[] ghostBlinking;
	private int index = 0;
	private int dir = 0;
	private int vel = 2;			// Movement velocity
	private int animSpeed = 10;		// Animation speed - animation frame changes every n frames
	private int animCounter = animSpeed;
	private boolean vulnerable = false;
	private int countdownFull = 1200;
	private int countdown = countdownFull;
	public int x = 12*25;
	public int y = 16*24;
	private int[][] mapArray;
	private int[][] velArray;
	private Random rand = new Random();
	private String[] colors = new String[] {"Cyan", "Orange", "Pink", "Red"};
	// 0 - Left, 1 - Right, 2 - Up, 3 - Down

	public Texture sprite;

	public Ghost(int[][] mapArray, int ghostIndex) {
		String color = colors[ghostIndex];

		ghostAnim = new Texture[5][2];
		ghostAnim[0][0] = new Texture("Ghosts/" + color + "/Left_1.png");
		ghostAnim[0][1] = new Texture("Ghosts/" + color + "/Left_2.png");

		ghostAnim[1][0] = new Texture("Ghosts/" + color + "/Right_1.png");
		ghostAnim[1][1] = new Texture("Ghosts/" + color + "/Right_2.png");

		ghostAnim[2][0] = new Texture("Ghosts/" + color + "/Up_1.png");
		ghostAnim[2][1] = new Texture("Ghosts/" + color + "/Up_2.png");

		ghostAnim[3][0] = new Texture("Ghosts/" + color + "/Down_1.png");
		ghostAnim[3][1] = new Texture("Ghosts/" + color + "/Down_2.png");

		ghostAnim[4][0] = new Texture("Ghosts/Blue_1.png");
		ghostAnim[4][1] = new Texture("Ghosts/Blue_2.png");

		ghostBlinking = new Texture[4];
		ghostBlinking[0] = new Texture("Ghosts/Blue_1.png");
		ghostBlinking[1] = new Texture("Ghosts/Blue_2.png");
		ghostBlinking[2] = new Texture("Ghosts/White_1.png");
		ghostBlinking[3] = new Texture("Ghosts/White_2.png");

		this.mapArray = mapArray;
		velArray = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {0, 0}};
	}

	public void hunted() {
		vulnerable = true;
		countdown = countdownFull;
	}

	public void update() {
		move();
		if (vulnerable) {
			countdown--;
			if (countdown == 0) {
				vulnerable = false;
				countdown = countdownFull;
			}
		}

		animCounter -= 1;
		if (animCounter == 0) {
			index++;
			animCounter = animSpeed;
		}
		if (countdown >= 300 && index >= 2)
				index = 0;
		else if (index >= 4)
			index = 0;


		if (!vulnerable)
			sprite = ghostAnim[dir][index];
		else
			if (countdown < 300)
				sprite = ghostBlinking[index];
			else
				sprite = ghostAnim[4][index];
	}

	private void move() {
		Map<Integer, Integer> dict = new HashMap<Integer, Integer>() {{put(0, 1); put(1, 0); put(2, 3); put(3, 2);}};
		Vector<Integer> choices = new Vector<Integer>();


		for (int i = 0; i < 4; i++) {
			if (i == dict.get(dir)) continue; // Don't go backwards
			if (checkCollision(i))
				choices.add(i);
		}

		if (choices.size() > 0)
			dir = choices.get(abs(rand.nextInt()) % choices.size());
		else
			dir = dict.get(dir);


		if (vulnerable) {
			x += velArray[dir][0] * (vel / 2);
			y += velArray[dir][1] * (vel / 2);
		} else {
			x += velArray[dir][0] * vel;
			y += velArray[dir][1] * vel;
		}
	}

	private boolean checkCollision(int nextDir) {
		Map<Integer, Integer> dict = new HashMap<Integer, Integer>() {{put(0, 1); put(1, 0); put(2, 3); put(3, 2);}};
		if(dir == 4) return true;
		// Snap ghost to 24x24 cell size
		if (x % 24 != 0 || y % 24 != 0) {
			if (dir != nextDir && dir != dict.get(nextDir)) { 	// if trying to change direction
				return false;
			} else {
				return true;
			}
		} else {
			int mapX = x / 24;
			int mapY = y / 24;

			// Check borders
			if ((mapX == 0 && nextDir == 0) || (mapX == mapArray.length - 1 && nextDir == 1) || (mapY == 0 && nextDir == 2) || (mapY == mapArray[0].length - 1 && nextDir == 3))
				return false;

			if (mapArray[velArray[nextDir][0] + mapX][velArray[nextDir][1] + mapY] == 0) {
				return false;
			}
			return true;
		}
	}
}
