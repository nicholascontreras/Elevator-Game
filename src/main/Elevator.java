package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Nicholas Contreras
 */

public class Elevator {

	private static final int MAX_CAPACITY = 4;

	private int elevatorNum;
	private ArrayList<Floor> servicedFloors;
	private ArrayList<Person> passengers;
	private boolean[] floorsToVisit;

	private boolean moving;
	private boolean goingUp;
	private boolean busy;

	private Floor doorsOpenFloor;

	private int yPos;

	public Elevator(int elevatorNum, ArrayList<Floor> servicedFloors) {
		this.elevatorNum = elevatorNum;
		this.servicedFloors = new ArrayList<Floor>(servicedFloors);
		this.passengers = new ArrayList<Person>();
		doorsOpenFloor = servicedFloors.get(0);
		yPos = doorsOpenFloor.getYPos();
		floorsToVisit = new boolean[servicedFloors.size()];
		moving = false;
		goingUp = true;
		busy = false;
	}

	public void update() {
		if (moving) {

			System.out.println("going up: " + goingUp);
			System.out.println(yPos);

			yPos += goingUp ? -1 : 1;

			if (yPos >= servicedFloors.get(0).getYPos()) {
				goingUp = true;
			} else if (yPos <= servicedFloors.get(servicedFloors.size() - 1).getYPos()) {
				goingUp = false;
			}

			for (int i = 0; i < servicedFloors.size(); i++) {
				Floor curFloor = servicedFloors.get(i);
				if (floorsToVisit[i]) {
					if (yPos == curFloor.getYPos()) {
						moving = false;
						floorsToVisit[i] = false;
						System.out.println("arrived at floor");

						busy = true;
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								doorsOpenFloor = curFloor;
								busy = false;
							}
						}, 1000);
					}
				}
			}
		} else {
			if (!busy) {
				attemptOffloadPassengers();
			}

			if (!busy) {
				attemptLoadPassengers();
			}

			if (!busy) {
				boolean switchDirection = true;
				int startFloor = doorsOpenFloor.getFloorNum() - 1 + (goingUp ? 1 : -1);
				for (int i = startFloor; i >= 0 && i < servicedFloors.size(); i += goingUp ? 1 : -1) {
					if (floorsToVisit[i]) {
						System.out.println("dont switch");
						switchDirection = false;
						break;
					}
				}

				if (switchDirection) {
					goingUp = !goingUp;
				}

				busy = true;
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						doorsOpenFloor = null;
						moving = true;
						busy = false;
					}
				}, 1000);
			}
		}
	}

	private void attemptOffloadPassengers() {
		Person passengerToOffload = null;
		for (int i = 0; i < passengers.size(); i++) {
			Person curPassenger = passengers.get(i);

			if (curPassenger.getDestFloor().equals(doorsOpenFloor)) {
				passengerToOffload = curPassenger;
				break;
			}
		}

		final Person passengerToOffloadF = passengerToOffload;
		if (passengerToOffload != null) {
			busy = true;
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					passengers.remove(passengerToOffloadF);
					busy = false;
				}
			}, 1000);
		}
	}

	private void attemptLoadPassengers() {
		Person passengerToLoad = null;
		for (int i = 0; i < doorsOpenFloor.getPeople().size(); i++) {
			Person curWaiting = doorsOpenFloor.getPeople().get(i);

			if (curWaiting.getAssignedElevator().equals(this)) {
				passengerToLoad = curWaiting;
				break;
			}
		}

		final Person passengerToLoadF = passengerToLoad;
		if (passengerToLoad != null) {
			if (passengers.size() < MAX_CAPACITY) {
				busy = true;
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						passengers.add(passengerToLoadF);
						passengerToLoadF.getStartingFloor().removePerson(passengerToLoadF);
						callTo(passengerToLoadF.getDestFloor());
						busy = false;
					}
				}, 1000);
			} else {
				callTo(doorsOpenFloor);
			}
		}
	}

	public void draw(Graphics2D g2d) {
		int transX = ((Building.BUILDING_WIDTH / 6) * (elevatorNum - 1)) + Building.BUILDING_WIDTH / 12;
		g2d.translate(transX, 0);

		drawElevatorCar(g2d);

		for (int i = 0; i < servicedFloors.size(); i++) {
			Floor curFloor = servicedFloors.get(i);
			g2d.translate(0, curFloor.getYPos());
			drawElevatorDoor(g2d, curFloor.equals(doorsOpenFloor));
			g2d.translate(0, -curFloor.getYPos());
		}
		g2d.translate(-transX, 0);
	}

	private void drawElevatorDoor(Graphics2D g2d, boolean doorsOpen) {
		if (!doorsOpen) {
			g2d.setColor(Color.GRAY);
			g2d.setStroke(new BasicStroke(3));
			g2d.fillRect(0, Floor.FLOOR_HEIGHT * 1 / 4, Building.BUILDING_WIDTH / 8, Floor.FLOOR_HEIGHT * 3 / 4);
		}

		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawRect(0, Floor.FLOOR_HEIGHT * 1 / 4, Building.BUILDING_WIDTH / 8, Floor.FLOOR_HEIGHT * 3 / 4);

		if (!doorsOpen) {
			g2d.drawLine(Building.BUILDING_WIDTH / 16, Floor.FLOOR_HEIGHT * 1 / 4, Building.BUILDING_WIDTH / 16,
					Floor.FLOOR_HEIGHT);
		}
	}

	private void drawElevatorCar(Graphics2D g2d) {

		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawRect(-Building.BUILDING_WIDTH / 64, yPos + Floor.FLOOR_HEIGHT * 1 / 4,
				Building.BUILDING_WIDTH / 8 + Building.BUILDING_WIDTH / 32, Floor.FLOOR_HEIGHT * 3 / 4);

		int passengerSpacing = (Building.BUILDING_WIDTH / 8 + Building.BUILDING_WIDTH / 32) / MAX_CAPACITY;

		for (int i = 0; i < passengers.size(); i++) {
			passengers.get(i).draw(g2d, (-Building.BUILDING_WIDTH / 64) + (passengerSpacing * i),
					yPos + (Floor.FLOOR_HEIGHT - Person.HEIGHT));
		}
	}

	public void callTo(Floor startingFloor) {
		floorsToVisit[startingFloor.getFloorNum() - 1] = true;
	}
}
