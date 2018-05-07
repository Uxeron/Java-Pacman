package com.gab.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.lang.System;
import java.lang.Thread;

public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture map;
	private Texture pellet;
	private Texture powerPill;
	private Texture filler;
	private Pacman pacman;
	private Ghost[] ghosts;
	private int[][] mapArray;
	private int offsetX = 18;
	private int offsetY = 193;
	private int firstDraw = 2;
	private int switched = 3;
	private boolean sw = false;

	private long FRAME_PERIOD;
	// 0 - Left, 1 - Right, 2 - Up, 3 - Down

	@Override
	public void create () {
		mapArray = trasposeMap(makeMap());

		batch = new SpriteBatch();
		map = new Texture("Map.png");
		pellet = new Texture("Pellet.png");
		powerPill = new Texture("PowerPill.png");
		filler = new Texture("Filler.png");
		pacman = new Pacman(mapArray);

		ghosts = new Ghost[4];
		for (int i = 0; i < 4; i++)
			ghosts[i] = new Ghost(mapArray, i);

		FRAME_PERIOD = 1000/60;		// FPS limit
	}

	@Override
	public void render () {
		long beginTime = System.currentTimeMillis();

		// Need to redraw the entire scene twice because of double-buffering
		if (firstDraw > 0) {
			firstDraw--;
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(map, 4, 22);

			for (int i = 0; i < mapArray.length; i++) {
				for (int j = 0; j < mapArray[0].length; j++) {
					if (mapArray[i][j] == 2) batch.draw(pellet, i*24 + offsetX, Gdx.graphics.getHeight() - j*24 - offsetY);
					if (mapArray[i][j] == 3) batch.draw(powerPill, i*24 + offsetX, Gdx.graphics.getHeight() - j*24 - offsetY);
				}
			}
			batch.end();
		}

		// Begin loop

		batch.begin();

		// Powerup
		if (pacman.powerup) {
			for (Ghost ghost: ghosts)
				ghost.hunted();
			pacman.powerup = false;
		}

		// Clear sides
		if (sw) {
			switched--;
			batch.draw(filler, offsetX - 2, Gdx.graphics.getHeight() - 13 * 24 - offsetY - 2);
			batch.draw(filler, (mapArray.length - 1) * 24 + offsetX - 2, Gdx.graphics.getHeight() - 13 * 24 - offsetY - 2);
			batch.draw(map, 4, 22);
			if (switched == 0) {
				sw = false;
				switched = 3;
			} else {
				batch.end();
				return;
			}
		}



		if (pacman.x == 0 && pacman.y == 13 * 24) {
			sw = true;
			pacman.x = (mapArray.length - 1) * 24 - 2;
		}
		if (pacman.x == (mapArray.length - 1) * 24 && pacman.y == 13 * 24) {
			sw = true;
			pacman.x = 2;
		}

		for (Ghost ghost: ghosts) {
			if (ghost.x == 0 && ghost.y == 13 * 24) {
				sw = true;
				ghost.x = (mapArray.length - 1) * 24 - 2;
			}
			if (ghost.x == (mapArray.length - 1) * 24 && ghost.y == 13 * 24) {
				sw = true;
				ghost.x = 2;
			}
		}

		pacman.update();
		for (Ghost ghost: ghosts) ghost.update();

		// Drawing start



		// Draw fillers
		batch.draw(filler, pacman.x + offsetX - 2, Gdx.graphics.getHeight() - pacman.y - offsetY - 2);
		for (Ghost ghost: ghosts) batch.draw(filler, ghost.x + offsetX - 2, Gdx.graphics.getHeight() - ghost.y - offsetY - 2);

		// Redraw the 3x3 map tiles around the pacman and ghosts
		for (int i = pacman.x/24 - 2; i < pacman.x/24 + 3; i++) {
			for (int j = pacman.y/24 - 2; j < pacman.y/24 + 3; j++) {
				if (i < 0 || i >= mapArray.length || j < 0 || j >= mapArray[0].length) continue;
				if (mapArray[i][j] == 2) batch.draw(pellet, i*24 + offsetX, Gdx.graphics.getHeight() - j*24 - offsetY);
				if (mapArray[i][j] == 3) batch.draw(powerPill, i*24 + offsetX, Gdx.graphics.getHeight() - j*24 - offsetY);
			}
		}

		for (Ghost ghost: ghosts) {
			for (int i = ghost.x/24 - 2; i < ghost.x/24 + 3; i++) {
				for (int j = ghost.y/24 - 2; j < ghost.y/24 + 3; j++) {
					if (i < 0 || i >= mapArray.length || j < 0 || j >= mapArray[0].length) continue;
					if (mapArray[i][j] == 2) batch.draw(pellet, i*24 + offsetX, Gdx.graphics.getHeight() - j*24 - offsetY);
					if (mapArray[i][j] == 3) batch.draw(powerPill, i*24 + offsetX, Gdx.graphics.getHeight() - j*24 - offsetY);
				}
			}
		}

		batch.draw(map, 4, 22);


		for (Ghost ghost: ghosts) batch.draw(ghost.sprite, ghost.x + offsetX, Gdx.graphics.getHeight() - ghost.y - offsetY);
		batch.draw(pacman.sprite, pacman.x + offsetX, Gdx.graphics.getHeight() - pacman.y - offsetY);
		batch.end();

		// Drawing end



		// End loop


		// Lock FPS
		long timeDiff = System.currentTimeMillis() - beginTime;

		if(FRAME_PERIOD > timeDiff){
			try { // Thread.sleep throws interruptedException exceptions, need to neutralize them
				Thread.sleep(FRAME_PERIOD - timeDiff);
			} catch (InterruptedException e) {}
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		map.dispose();
	}

	private int[][] trasposeMap(int[][] map) {
		int m = map.length;
		int n = map[0].length;

		int[][] trasposedMap = new int[n][m];

		for(int x = 0; x < n; x++)
			for(int y = 0; y < m; y++)
				trasposedMap[x][y] = map[y][x];

		return trasposedMap;
	}

	private int[][] makeMap() {
		// 0 - wall, 1 - empty, 2 - pellet, 3 - powerPill
		return new int[][] {
				{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
				{2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2},
				{3, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3},
				{2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2},
				{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
				{2, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2},
				{2, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2},
				{2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2},
				{0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{1, 1, 1, 1, 1, 2, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 1, 1, 1, 1, 1},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0},
				{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
				{2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2},
				{2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 2},
				{3, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 3},
				{0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0},
				{0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0},
				{2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2},
				{2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
				{2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
				{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
		};
	}
}
