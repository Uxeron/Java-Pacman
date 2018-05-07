package com.gab.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class Pacman {
	private Texture[][] pacmanAnim;
	private Texture[] pacmanDeath;
	private Texture baseSpr;
	private int index = 0;
	private int dir = 4;
	private int vel = 2;			// Movement velocity
	private int animSpeed = 5;		// Animation speed - animation frame changes every n frames
	private int animCounter = animSpeed;
	public int x = 12*25;
	public int y = 16*24;
	public int score = 0;
	public boolean powerup = false;
	private int[][] mapArray;
	private int[][] velArray;
	// 0 - Left, 1 - Right, 2 - Up, 3 - Down, 4 - Still, 5 - Dead

	public Texture sprite;

	public Pacman(int[][] mapArray) {
		baseSpr = new Texture("Pacman/Base.png");

		pacmanAnim = new Texture[4][4];
		pacmanAnim[0][0] = baseSpr;
		pacmanAnim[0][1] = new Texture("Pacman/Left_1.png");
		pacmanAnim[0][2] = new Texture("Pacman/Left_2.png");
		pacmanAnim[0][3] = new Texture("Pacman/Left_1.png");

		pacmanAnim[1][0] = baseSpr;
		pacmanAnim[1][1] = new Texture("Pacman/Right_1.png");
		pacmanAnim[1][2] = new Texture("Pacman/Right_2.png");
		pacmanAnim[1][3] = new Texture("Pacman/Right_1.png");

		pacmanAnim[2][0] = baseSpr;
		pacmanAnim[2][1] = new Texture("Pacman/Up_1.png");
		pacmanAnim[2][2] = new Texture("Pacman/Up_2.png");
		pacmanAnim[2][3] = new Texture("Pacman/Up_1.png");

		pacmanAnim[3][0] = baseSpr;
		pacmanAnim[3][1] = new Texture("Pacman/Down_1.png");
		pacmanAnim[3][2] = new Texture("Pacman/Down_2.png");
		pacmanAnim[3][3] = new Texture("Pacman/Down_1.png");

		pacmanDeath = new Texture[11];
		for(int i = 0; i < 11; i++)
			pacmanDeath[i] = new Texture("Pacman/Dead_" + Integer.toString(i+1) + ".png");

		this.mapArray = mapArray;
		velArray = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {0, 0}};
	}

	public void update() {
		move();

		animCounter -= 1;
		if(animCounter == 0) {
			index++;
			animCounter = animSpeed;
		}
		if(dir < 4 && index > 3)
			index = 0;
		if(dir == 5 && index > 10)
			deadDone();

		if(dir < 4)
			sprite = pacmanAnim[dir][index];
		else if (dir == 4)
			sprite = baseSpr;
		else
			sprite = pacmanDeath[index];
	}

	private void move() {
		if(dir == 5) return;

		if(Gdx.input.isKeyPressed(Input.Keys.A))
			if(checkCollision(0))
				dir = 0;
		if(Gdx.input.isKeyPressed(Input.Keys.D))
			if(checkCollision(1))
				dir = 1;
		if(Gdx.input.isKeyPressed(Input.Keys.W))
			if(checkCollision(2))
				dir = 2;
		if(Gdx.input.isKeyPressed(Input.Keys.S))
			if(checkCollision(3))
				dir = 3;

		if(checkCollision(dir)) {
			x += velArray[dir][0] * vel;
			y += velArray[dir][1] * vel;
		}
	}

	private boolean checkCollision(int nextDir) {
		Map<Integer, Integer> dict = new HashMap<Integer, Integer>() {{put(0, 1); put(1, 0); put(2, 3); put(3, 2);}};
		if(dir == 4) return true;
		// Snap pacman to 24x24 cell size
		if (x % 24 != 0 || y % 24 != 0) {
			if (dir != nextDir && dir != dict.get(nextDir)) { 	// if trying to change direction
				return false;
			} else {
				return true;
			}
		} else {
			int mapX = x / 24;
			int mapY = y / 24;
			if(mapArray[mapX][mapY] == 2) {
				score += 200;
				mapArray[mapX][mapY] = 1;
				System.out.println(score);
			}
			if(mapArray[mapX][mapY] == 3) {
				powerup = true;
				mapArray[mapX][mapY] = 1;
			}

			// Check borders
			if ((mapX == 0 && nextDir == 0) || (mapX == mapArray.length - 1 && nextDir == 1) || (mapY == 0 && nextDir == 2) || (mapY == mapArray[0].length - 1 && nextDir == 3))
				return false;

			if (mapArray[velArray[nextDir][0] + mapX][velArray[nextDir][1] + mapY] == 0) {
				return false;
			}
			return true;
		}
	}

	public void dead() {
		index = 0;
		dir = 4;
	}

	private void deadDone() {

	}
}
