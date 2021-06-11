import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainWindow extends JFrame {

	
	private static final int DEAD = 0;
	private static final int ALIVE = 1;
	
	private static final int WINDOW_WIDTH = 1375;
	private static final int WINDOW_HEIGHT = 782;
	
	private static final String PLAY = "START \u23F5";
	private static final String STOP = "STOP \u23F9";
	private static final String UP_ARROW = "\u2191";
	private static final String DOWN_ARROW = "\u2193"; 
	
	private static final int MIN_TICK_INTERVAL = 25;
	private static final int MAX_TICK_INTERVAL = 151;
	private static final int INITIAL_TICK_INTERVAL = 85;

	
	private static final int DEFAULT_GRID_SIZE = 75;
	private static final Color DEFAULT_GUI_BACKGROUND = Color.BLACK;
	private static final Color DEFAULT_CELL_COLOR = Color.GREEN;
	private static final Color DEFAULT_GUI_FOREGROUND = Color.GREEN;
	private static final Color DEFAULT_BACKGROUND = Color.BLACK;
	private static final Border DEFAULT_BORDER = BorderFactory.createLineBorder(Color.DARK_GRAY);
	private static final Color DEFAULT_DEAD_COLOR = Color.DARK_GRAY;
	private static final int THRESHOLD = (int)(Math.pow(DEFAULT_GRID_SIZE, 2) * (0.25f));
	
	private static final Font DEFAULT_GUI_FONT = new Font("Unifont", Font.PLAIN, 12);
	private static final Font INFO_FONT = new Font("Monaco", Font.BOLD, 14);
	private static final Font STATUS_FONT = new Font("Monaco", Font.BOLD, 12);
	private static final Font SLIDER_FONT = new Font("Monaco", Font.BOLD, 8);
	
	
	private JPanel[][] grid;
	private int[][] cells;
	private int curTickInterval;
	private int currentGridSize;
	private ArrayList<Integer> population;
	private JPanel gridPanel, settingsPanel;
	private JLabel statusLabel;
	private int curGeneration, curPopulation, maxPopulation, minPopulation, avgPopulation, totPopulation;
	private JLabel cgLabel, cpLabel, mxpLabel, mnpLabel, agpLabel, pdLabel;
	private JButton init, start, stop, plusOne, reset;
	private boolean inProgress;
	private boolean mousePressed;
	private JSlider frameRateSlider;
	private Point lastMouseOverPoint;
	private Timer tickTimer;
	public MainWindow() {
		super("Game of life");
		curTickInterval = INITIAL_TICK_INTERVAL;
		currentGridSize = DEFAULT_GRID_SIZE;
		grid = new JPanel[currentGridSize][currentGridSize];
		cells = new int[currentGridSize][currentGridSize];
		
		gridPanel = new JPanel();
		settingsPanel = new JPanel();
		
		initLabels();
		initValues();
		initGrid();
		initSettings();
		
		JPanel temp = new JPanel();
		temp.setLayout(new GridLayout(1, 2, 0, 0));
		temp.add(gridPanel);
		temp.add(settingsPanel);
		
		
		//status panel is only for mouse location on the grid 
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		statusLabel = getStylizedLabel(STATUS_FONT, DEFAULT_GUI_FOREGROUND);
		clearStatusLabel();
		statusPanel.add(statusLabel);
		
		this.getContentPane().setBackground(DEFAULT_GUI_BACKGROUND);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(temp, BorderLayout.CENTER);
		this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		// Window Listener to exit the game after confirmation.
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(confirmExit()) {
					MainWindow.this.dispose();
					System.exit(0);
				}
			}
		});
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	
	// Initializing all the variables.
	private void initValues() {
		curPopulation = 0;
		curGeneration = 0;
		maxPopulation = 0;
		minPopulation = 0;
		avgPopulation = 0;
		inProgress = false;
		mousePressed = false;
		updateLabels();
	}
	
	
	public void initSettings() {
		JPanel topPanel, centerPanel, bottomPanel, leftPanel;
		topPanel = new JPanel();
		centerPanel = new JPanel();
		bottomPanel = new JPanel();
		leftPanel = new JPanel();
		topPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		centerPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		bottomPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		leftPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		reset = new JButton("Reset");
		init = new JButton("Random Initialization");
		start = new JButton(PLAY);
		stop = new JButton(STOP);
		plusOne = new JButton("+1");
		reset.setFont(DEFAULT_GUI_FONT);
		init.setFont(DEFAULT_GUI_FONT);
		start.setFont(DEFAULT_GUI_FONT);
		stop.setFont(DEFAULT_GUI_FONT);
		plusOne.setFont(DEFAULT_GUI_FONT);
		
		start.setEnabled(false);
		stop.setEnabled(false);
		plusOne.setEnabled(false);
		
		
		reset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Resetting grid");
				if(inProgress)
					haltGame();
				killAll();
				initValues();
				initButtons();
			}
		});
		
		init.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(!inProgress) {
					generateRandomPopulation();
					start.setEnabled(true);
					reset.setEnabled(true);
					plusOne.setEnabled(true);
				}
			}
			
			
		});
		start.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				stop.setEnabled(true);
				plusOne.setEnabled(false);
				inProgress = true;
				startTicking();
			}
		
		});
		
		stop.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				haltGame();
				plusOne.setEnabled(true);
				start.setEnabled(true);
			}
		});
		
		plusOne.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				advanceToNextGen();
			}
		});
		
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		topPanel.add(init);
		topPanel.add(reset);
		
		
		bottomPanel.setLayout(new GridLayout(1, 3, 0, 0));
		bottomPanel.add(start);
		bottomPanel.add(stop);
		bottomPanel.add(plusOne);
		
		
		GridBagConstraints constraints = new GridBagConstraints();
		centerPanel.setLayout(new GridBagLayout());
		centerPanel.add(getStatsPanel(), constraints);
		centerPanel.setForeground(DEFAULT_GUI_FOREGROUND);
		
		
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		if(frameRateSlider == null)
			initSlider();
		leftPanel.add(frameRateSlider);
		
		settingsPanel.setLayout(new BorderLayout());
		settingsPanel.add(topPanel, BorderLayout.NORTH);
		settingsPanel.add(centerPanel, BorderLayout.CENTER);
		settingsPanel.add(bottomPanel, BorderLayout.SOUTH);
		settingsPanel.add(leftPanel, BorderLayout.WEST);
	}
	
	// Initializing all the labels.
	private void initLabels() {
		cgLabel = getStylizedLabel("Current Generation: " + curGeneration);
		cpLabel = getStylizedLabel("Current Population: " + curPopulation);
		mxpLabel = getStylizedLabel("Maximum Population: " + maxPopulation);
		mnpLabel = getStylizedLabel("Minimum Population: " + minPopulation);
	}
	
	// function to make Grid panel with all the grid and with mouse litener.
	public void initGrid() {
		gridPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		gridPanel.setLayout(new GridLayout(grid.length, grid.length, 0, 0));
		for(int i=0; i<grid.length; i++) {
			for(int j=0; j<grid[i].length; j++) {
				final int x =i , y = j;
				grid[i][j] = new JPanel();
				grid[i][j].setBackground(DEFAULT_BACKGROUND);
				grid[i][j].setBorder(DEFAULT_BORDER);
				grid[i][j].addMouseListener(new MouseListener(){

					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						if(!inProgress) {
							flipCell(x ,y);
							if(!start.isEnabled()) {
								start.setEnabled(true);
								plusOne.setEnabled(true);
							}
							minPopulation = curPopulation;
						}
						
					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
						mousePressed = true;
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
						mousePressed = false;
						if(!start.isEnabled()) {
							start.setEnabled(true);
							plusOne.setEnabled(true);
						}
						minPopulation = curPopulation;
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						setLastPoint(new Point(x, y));
						if(mousePressed && !inProgress) {
							flipCell(x, y);
						}
					}

					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						clearStatusLabel();
					}
				});
				gridPanel.add(grid[i][j]);
			}
		}
	}
	
	
	private void updateLabels() {
		cgLabel.setText("Current Generation: " + curGeneration);
		cpLabel.setText("Current Population: " + curPopulation);
		mxpLabel.setText("Maximum Population: " + maxPopulation);
		mnpLabel.setText("Minimum Population: " + minPopulation);
	}
	
	private JPanel getStatsPanel() {
		JPanel temp = new JPanel();
		temp.setBackground(DEFAULT_GUI_BACKGROUND);
		temp.setForeground(DEFAULT_GUI_FOREGROUND);
		temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
		temp.add(getStylizedLabel("Cells can be selected or randomly initialized"));
		temp.add(getStylizedLabel("----- ----- ----- ----- ----- ----- ----- -----"));
		temp.add(cgLabel);
		temp.add(cpLabel);
		temp.add(mxpLabel);
		temp.add(mnpLabel);
		//temp.setBorder(getTextPanelBorder("STATS"));
		return temp;
	}
		
		
	
	// Used for Style Jlabel
		private JLabel getStylizedLabel() {
			return getStylizedLabel("");
		}
		
		private JLabel getStylizedLabel(String text) {
			return getStylizedLabel(INFO_FONT, DEFAULT_GUI_FOREGROUND, text);
		}
		
		private JLabel getStylizedLabel(Font font, Color fontColor) {
			return getStylizedLabel(font, fontColor, "");
		}
		
		private JLabel getStylizedLabel(Font font, Color fontColor, String labelText) {
			JLabel label = new JLabel();
			label.setFont(font);
			label.setForeground(fontColor);
			label.setForeground(DEFAULT_GUI_FOREGROUND);
			label.setText(labelText);
			return label;
		}
		
		private void clearStatusLabel() {
			statusLabel.setText("..");
		}
		
		
		
		
		private void initSlider() {
			
			frameRateSlider = new JSlider(JSlider.VERTICAL, MIN_TICK_INTERVAL, 
					MAX_TICK_INTERVAL, INITIAL_TICK_INTERVAL);
			
			frameRateSlider.setSnapToTicks(true);
			frameRateSlider.setPaintLabels(true);
			frameRateSlider.setPaintTicks(true);
			frameRateSlider.setForeground(DEFAULT_GUI_FOREGROUND);
			
			int medium = (MAX_TICK_INTERVAL + MIN_TICK_INTERVAL)/2;
			Hashtable<Integer, JLabel> sliderLabels = new Hashtable<Integer, JLabel>();
			
			sliderLabels.put(new Integer(MIN_TICK_INTERVAL), getStylizedLabel(SLIDER_FONT, DEFAULT_GUI_FOREGROUND ,"FAST"));
			sliderLabels.put(new Integer(medium), getStylizedLabel(SLIDER_FONT, DEFAULT_GUI_FOREGROUND ,"MED"));
			sliderLabels.put(new Integer(MAX_TICK_INTERVAL), getStylizedLabel(SLIDER_FONT, DEFAULT_GUI_FOREGROUND ,"SLOW"));
			
			frameRateSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider slider = (JSlider)e.getSource();
					System.out.println("current frame rate: " + slider.getValue());
					setTickInterval(slider.getValue());
				}
			});
			
			frameRateSlider.setLabelTable(sliderLabels);
			
		}
		
		// Changing alive cell to dead and vice versa
		private void flipCell(int x, int y) {
			if(isValid(x, y)) {
				if(isAlive(x, y))
					makeDead(x, y);
				else
					makeAlive(x, y);
			}
		}
		
		
		private void makeAlive(int x, int y) {
			cells[x][y] = ALIVE;
			grid[x][y].setBackground(DEFAULT_CELL_COLOR);
			curPopulation++;
			updateStats();
		}
		
		
		private void updateStats() {
			if(maxPopulation < curPopulation)
				maxPopulation = curPopulation;
			if(minPopulation > curPopulation)
				minPopulation = curPopulation;
		}
		private void makeDead(int x, int y) {
			makeDead(x, y, DEFAULT_DEAD_COLOR);
		}
		
		private void makeDead(int x, int y, Color color) {
			cells[x][y] = DEAD;
			grid[x][y].setBackground(color);
			curPopulation--;
			updateStats();
		}
		private boolean isValid(int x, int y) {
			return (x>=0 && x<currentGridSize) && (y>=0 && y<currentGridSize);
		}
		
		private boolean isAlive(int x, int y) {
			if(isValid(x , y))
				return (cells[x][y] == ALIVE);
			else
				return false;
		}
		
		//showing position of mouse
		private void setLastPoint(Point p) {
			lastMouseOverPoint = p;
			updateStatusLabel("Position: " + p.toString());
		}
		
		private void updateStatusLabel(String text) {
			statusLabel.setText(text);
		}
		private void disableActionButtons() {
			start.setEnabled(false);
			stop.setEnabled(false);
			plusOne.setEnabled(false);
		}
		
		private void haltGame() {
			inProgress = false;
			stopTicking();	
			start.setText(PLAY);
			disableActionButtons();
		}
		
		private void killAll() {
			for(int i=0; i<cells.length; i++)
				for(int j=0; j<cells[i].length; j++) {
					if(isAlive(i, j))
						makeDead(i, j, DEFAULT_BACKGROUND);
					else
						grid[i][j].setBackground(DEFAULT_BACKGROUND);
				}
		}
		
		private void initButtons() {
			start.setEnabled(false);
			stop.setEnabled(false);
			plusOne.setEnabled(false);
			init.setEnabled(true);
		}
		
		
		private void generateRandomPopulation() {
			Random r = new Random();
			while(curPopulation < THRESHOLD)
				flipCell(r.nextInt(currentGridSize), r.nextInt(currentGridSize));
			minPopulation = curPopulation;
			//updateStats();
		}
		
		// Schedule method is used to schedule the specified task for repeated fixed-delay execution, beginning after the specified delay.
		// public void schedule(TimerTask task,long delay,long period)
		// task − This is the task to be scheduled.
        //delay − This is the delay in milliseconds before task is to be executed.
        //period − This is the time in milliseconds between successive task executions
		private void startTicking() {
			tickTimer = new Timer();
			tickTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					advanceToNextGen();
				}
			}, 0, curTickInterval);
		}
		
		private void stopTicking() {
			if(tickTimer != null) {
				tickTimer.cancel();
			}
		}
		
		
		private void advanceToNextGen() {
			int[][] nextGen = getNextGeneration();
			for(int i=0; i<cells.length; i++) {
				for(int j=0; j<cells.length; j++) {
					if(nextGen[i][j] != cells[i][j]) {
						flipCell(i, j);
					}
				}
			}
			curGeneration++;
			totPopulation += curPopulation;
			//avgPopulation = totPopulation/population.size();
			updateLabels();
			if(isTerminalStage() || curPopulation == 0) {
				haltGame();
			}
		}
		
		//return value of cells after next gen
		private int[][] getNextGeneration() {
			int[][] nextGeneration = new int[cells.length][cells.length];
			for(int i=0; i<nextGeneration.length; i++) {
				for(int j=0; j<nextGeneration[i].length; j++) {
					int aliveNeighbours = getAliveNeighbourCount(i, j);
					if(aliveNeighbours == 3 || (isAlive(i, j) && aliveNeighbours == 2))
						nextGeneration[i][j] = ALIVE;
					else
						nextGeneration[i][j] = DEAD;
				}
			}
			return nextGeneration;
		}
		
		// Count of Neighbour live cell
		private int getAliveNeighbourCount(int x, int y) {
			int count = 0;
			if(isAlive(x-1, y-1))
				count++;
			if(isAlive(x-1, y))
				count++;
			if(isAlive(x-1, y+1))
				count++;
			if(isAlive(x, y-1))
				count++;
			if(isAlive(x, y+1))
				count++;
			if(isAlive(x+1, y-1))
				count++;
			if(isAlive(x+1, y))
				count++;
			if(isAlive(x+1, y+1))
				count++;
			return count;
		}
		
		private boolean isTerminalStage() {
			/*if(population.size() > 25) {
				int i;
				for(i=population.size()-2; i>0; i--)
					if(population.get(i+1) != population.get(i))
						break;
				if(population.size() - i >= 25)
					return true;
			}*/
			return false;
		}
		
		//Changing the slider speed calls this, to change the time between two succesive nextgen state time.
		private void setTickInterval(int value) {
			if(curTickInterval != value) {
				curTickInterval = value;
				if(inProgress && tickTimer != null) {
					stopTicking();
					startTicking();
				}
			}
		}
		
		
		private boolean confirmExit() {
			int result = showConfirmDialog("Confirm Exit", "Sure you want to exit?");
			return result == JOptionPane.YES_OPTION;
		}
		private int showConfirmDialog(String title, String text) {
			return JOptionPane.showConfirmDialog(this, text, title, JOptionPane.YES_NO_OPTION);
		}
		
		
		
		
	public static void main(String[] args) {
	    new MainWindow();
	}
}
