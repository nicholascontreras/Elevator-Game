package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * @author Nicholas Contreras
 */

public class Floor {

	public static final int FLOOR_HEIGHT = 100;

	private Building building;

	private int floor;
	private int yPos;

	private ArrayList<Person> people;

	public Floor(Building building, int floor) {
		this.building = building;
		this.floor = floor;
		yPos = -FLOOR_HEIGHT * floor;
		people = new ArrayList<Person>();
	}

	public void draw(Graphics2D g2d) {
		g2d.translate(0, yPos);

		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawLine(0, 0, Building.BUILDING_WIDTH, 0);
		g2d.drawLine(0, FLOOR_HEIGHT, Building.BUILDING_WIDTH, FLOOR_HEIGHT);
		g2d.drawString("Floor " + floor, Building.BUILDING_WIDTH / 2, FLOOR_HEIGHT / 4);

		int startingX = (int) ((building.getNumElevators() + 0.5) * Building.BUILDING_WIDTH / 6);

		for (int i = 0; i < people.size(); i++) {
			Person curPerson = people.get(i);
			int xPos = startingX + i * 25;
			int yPos = FLOOR_HEIGHT - Person.HEIGHT;
			curPerson.draw(g2d, xPos, yPos);
		}

		g2d.translate(0, -yPos);
	}

	public int getFloorNum() {
		return floor;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Floor) {
			Floor other = (Floor) o;
			return this.floor == other.floor;
		} else {
			return false;
		}
	}

	public void addPerson() {
		people.add(new Person(this));
	}
	
	public void removePerson(Person p) {
		people.remove(p);
	}
	
	public ArrayList<Person> getPeople() {
		return people;
	}
	
	public int getYPos() {
		return yPos;
	}

	public Building getBuilding() {
		return building;
	}
}
