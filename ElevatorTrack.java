import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ElevatorTrack extends JPanel {
    private static ElevatorTrack elevatorTrack = new ElevatorTrack();
    
    private PassengerManager passengerManager;
    private AnimationController animationController;
    private TaskScheduler queueManager;
    private SimulationController simulationController;
    private RenderingEngine renderingEngine;
    
    private HashMap<Integer, Rectangle> floorBounds;
    private HashMap<Integer, Rectangle> callButtonBounds;
    private Rectangle cabinBounds;
    private int cabinFloor = 0;
    private int highlightedFloor = -1;
    
    private ElevatorButtons elevatorButtons;
    private static DestinationPanel destinationPanel = DestinationPanel.getDestinationPanel();
    private JButton startButton;
    
    private ElevatorTrack() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        passengerManager = new PassengerManager(this);
        animationController = new AnimationController(this);
        queueManager = new TaskScheduler(this);
        simulationController = new SimulationController(this);
        renderingEngine = new RenderingEngine(this);
        
        floorBounds = new HashMap<>();
        callButtonBounds = new HashMap<>();
    }
    
    private void setupLayout() {
        setPreferredSize(new Dimension(300, 500));   
        setMinimumSize(new Dimension(280, 450)); 
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setBackground(Color.WHITE);
        setFocusable(true);
    }
    
    private void setupEventHandlers() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                calculatePositions();
            }
        });
    }
    
    private void handleMouseClick(MouseEvent e) {
        if(animationController.isAnimationRunning()) {
            return;
        }
        
        if(simulationController.isWaitingOnFloor() && cabinBounds != null && cabinBounds.contains(e.getPoint())) {
            passengerManager.unloadSpecificPassenger(e.getPoint());
            return;
        }
        
        for(int floor = 0; floor <= 10; floor++) {
            Rectangle callBounds = callButtonBounds.get(floor);
            if(callBounds != null && callBounds.contains(e.getPoint())) {
                queueManager.handleFloorCallButtonClick(floor);
                return;
            }
        }
    }
    
    private void handleKeyPress(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
            if(simulationController.canExitPassenger()) {
                passengerManager.animatePassengerExit();
            }
        }
    }
    
    public static ElevatorTrack getElevatorTrack() {
        if(elevatorTrack == null) {
            elevatorTrack = new ElevatorTrack();
        }
        return elevatorTrack;
    }
    
    public void startSimulation() {
        simulationController.startSimulation();
    }
    
    public void addFloorTarget(int targetFloor) {
        queueManager.addFloorTarget(targetFloor);
    }
    
    public void calculatePositions() {
        calculateFloorPositions();
        calculateCabinPosition();
    }
    
    private void calculateFloorPositions() {
        int availableHeight = getHeight() - 40;
        int floorHeight = availableHeight / 11;
        int startY = 20;
        
        floorBounds.clear();
        callButtonBounds.clear();
        
        for(int i = 0; i <= 10; i++) {
            int y = startY + (10-i) * floorHeight;
            floorBounds.put(i, new Rectangle(10, y, 100, floorHeight-2));
            
            int callButtonX = 10 + 100 + 10;
            int callButtonY = y + (floorHeight - 25) / 2;
            callButtonBounds.put(i, new Rectangle(callButtonX, callButtonY, 40, 25));
        }
    }
    
    private void calculateCabinPosition() {
        Rectangle floorRect = floorBounds.get(cabinFloor);
        if(floorRect != null) {
            cabinBounds = new Rectangle(
                floorRect.x + 5, 
                floorRect.y + 5, 
                floorRect.width - 10, 
                floorRect.height - 10
            );
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderingEngine.render(g);
    }
    
    public PassengerManager getPassengerManager() { return passengerManager; }
    public AnimationController getAnimationController() { return animationController; }
    public TaskScheduler getQueueManager() { return queueManager; }
    public SimulationController getSimulationController() { return simulationController; }
    public RenderingEngine getRenderingEngine() { return renderingEngine; }
    
    public HashMap<Integer, Rectangle> getFloorBounds() { return floorBounds; }
    public HashMap<Integer, Rectangle> getCallButtonBounds() { return callButtonBounds; }
    public Rectangle getCabinBounds() { return cabinBounds; }
    public int getCabinFloor() { return cabinFloor; }
    public void setCabinFloor(int floor) { this.cabinFloor = floor; }
    public int getHighlightedFloor() { return highlightedFloor; }
    public void setHighlightedFloor(int floor) { this.highlightedFloor = floor; }
    
    public void setElevatorButtons(ElevatorButtons elevatorButtons) {
        this.elevatorButtons = elevatorButtons;
    }
    
    public ElevatorButtons getElevatorButtons() {
        return elevatorButtons;
    }
    
    public static DestinationPanel getDestinationPanel() {
        return destinationPanel;
    }
    
    public void setStartButton(JButton startButton) {
        this.startButton = startButton;
    }
    
    public JButton getStartButton() {
        return startButton;
    }
}