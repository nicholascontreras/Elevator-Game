package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class Building extends JPanel {

	public static final int BUILDING_WIDTH = 600;

	public static final String DEFAULT_SCRIPT = "function assignElevator(startFloor, destFloor) {"
			+ System.lineSeparator() + "\treturn 1;" + System.lineSeparator() + "}";

	private JFrame frame;

	private ArrayList<Floor> floors;
	private ArrayList<Elevator> elevators;

	private JTextArea codeWindow;
	private JTextArea outputWindow;

	private JButton setScriptButton;

	private ScriptEngine scriptEngine;

	public static void main(String[] args) {
		new Building();
	}

	public Building() {

		floors = new ArrayList<Floor>();

		for (int i = 0; i < 6; i++) {
			floors.add(new Floor(this, i + 1));
		}

		elevators = new ArrayList<Elevator>();
		elevators.add(new Elevator(1, floors));
		elevators.add(new Elevator(2, floors));

		int numFloors = floors.size();

		setUIFont(new FontUIResource("SansSerif", Font.PLAIN, 18));

		frame = new JFrame("Elevator Game");

		JPanel outerPanel = new JPanel(new BorderLayout());

		this.setPreferredSize(new Dimension(BUILDING_WIDTH, numFloors * Floor.FLOOR_HEIGHT));
		JScrollPane sp = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		outerPanel.add(sp, BorderLayout.WEST);

		JPanel scriptPanel = new JPanel(new BorderLayout());
		scriptPanel.setPreferredSize(new Dimension(600, 400));

		setScriptButton = new JButton("Save Script");
		setScriptButton.addActionListener((ActionEvent e) -> setScript());
		scriptPanel.add(setScriptButton, BorderLayout.NORTH);

		codeWindow = new JTextArea(DEFAULT_SCRIPT);
		codeWindow.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scriptPanel.add(new JScrollPane(codeWindow), BorderLayout.CENTER);

		outputWindow = new JTextArea(15, 10);
		outputWindow.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		outputWindow.setEditable(false);
		scriptPanel.add(new JScrollPane(outputWindow), BorderLayout.SOUTH);

		outerPanel.add(scriptPanel, BorderLayout.CENTER);

		frame.add(outerPanel);

		frame.pack();

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setScript();

		JScrollBar vertical = sp.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());

		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 0, 1000 / 60);

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				Floor randomFloor = getFloors().get((int) (Math.random() * getFloors().size()));
				randomFloor.addPerson();
			}
		}, 0, 2500);

		frame.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		g2d.translate(0, getHeight());
		
		for (int i = 0; i < floors.size(); i++) {
			Floor curFloor = floors.get(i);
			curFloor.draw(g2d);
		}

		for (int i = 0; i < elevators.size(); i++) {
			Elevator curElevator = elevators.get(i);
			curElevator.update();
			curElevator.draw(g2d);
		}
		g2d.translate(0, -getHeight());
	}

	private void setConsoleOutput(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("(h:mm:ss)");
		s = sdf.format(new Date()) + ": " + s + System.lineSeparator();
		outputWindow.append(s);
	}

	public void setScript() {
		try {
			ScriptEngineManager engineManager = new ScriptEngineManager();
			scriptEngine = engineManager.getEngineByName("nashorn");
			String output = scriptEngine.eval(codeWindow.getText()).toString();
			if (output.equals(codeWindow.getText())) {
				setConsoleOutput("Script syntax check passed");
			}
		} catch (Exception e) {
			setConsoleOutput(e.getMessage());
		}
	}

	public int determineElevatorNumber(int startFloor, int destFloor) {
		try {
			String output = scriptEngine.eval("assignElevator(" + startFloor + "," + destFloor + ");").toString();
			try {
				int eleNum = Integer.parseInt(output);

				if (eleNum > 0 && eleNum <= elevators.size()) {
					return eleNum;
				} else {
					setConsoleOutput("The returned elevator number '" + eleNum + "' is not valid");
				}

			} catch (NumberFormatException e1) {
				setConsoleOutput("The script did not return a elevator number, instead returned: " + output);
			}

		} catch (ScriptException e) {
			setConsoleOutput(e.getMessage());
		}

		return 0;
	}

	public static void setUIFont(FontUIResource f) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}

	public ArrayList<Floor> getFloors() {
		return floors;
	}

	public ArrayList<Elevator> getElevators() {
		return elevators;
	}

	public int getNumElevators() {
		return elevators.size();
	}
	
	public int getNumFloors() {
		return floors.size();
	}
}
