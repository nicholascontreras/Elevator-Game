package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

/**
 * @author Nicholas Contreras
 */

public class Person {

	private static final BufferedImage image;
	public static final int HEIGHT = (int) (Floor.FLOOR_HEIGHT * 6.0 / 10);
	private static final int WIDTH;

	private Building building;

	private Floor startingFloor;
	private Floor destFloor;

	private boolean onElevator;
	private Elevator assignedElevator;

	static {
		BufferedImage x = null;
		try {
			x = ImageIO.read(Person.class.getResourceAsStream("/person.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		double aspectRatio = (double) x.getWidth() / x.getHeight();
		image = resize(x, (int) (HEIGHT * aspectRatio), HEIGHT);
		WIDTH = image.getWidth();
	}

	public Person(Floor f) {
		building = f.getBuilding();
		startingFloor = f;
		destFloor = startingFloor;

		while (destFloor.equals(startingFloor)) {
			destFloor = building.getFloors().get((int) (Math.random() * building.getFloors().size()));
		}

		onElevator = false;
		assignedElevator = null;

		assignToElevator();
	}

	private void assignToElevator() {
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				int eleNum = building.determineElevatorNumber(startingFloor.getFloorNum(), destFloor.getFloorNum());
				if (eleNum > 0) {
					t.cancel();
					assignedElevator = building.getElevators().get(eleNum - 1);
					assignedElevator.callTo(startingFloor);
				}
			}
		}, 0, 3000);
	}

	public void draw(Graphics2D g2d, int xPos, int yPos) {
		g2d.drawImage(image, xPos, yPos, null);
		g2d.setColor(Color.WHITE);
		g2d.drawString(destFloor.getFloorNum() + "", xPos + WIDTH / 4, yPos + HEIGHT / 2);
	}

	public Floor getStartingFloor() {
		return startingFloor;
	}

	public Floor getDestFloor() {
		return destFloor;
	}

	public Elevator getAssignedElevator() {
		return assignedElevator;
	}

	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}
}
