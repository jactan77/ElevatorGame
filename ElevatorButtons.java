import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;


public class ElevatorButtons extends JPanel {
    private GridBagConstraints gbc;
    private ElevatorTrack elevatorTrack;
    private Map<Integer, JButton> floorButtons;
    private boolean isMoving = false;
    private boolean buttonsEnabled = false;
    
    public ElevatorButtons() {
        setupLayout();
        this.elevatorTrack = ElevatorTrack.getElevatorTrack();
        this.floorButtons = new HashMap<>(11);
        initializeButtons();
    }
    
    private void setupLayout() {
        setBackground(Color.DARK_GRAY);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        this.gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.BOTH;
    }
    
    private void initializeButtons() {
        for (int i = 10; i >= 0; i--) {
            JButton floorButton = new JButton(String.valueOf(i));
            final int targetFloor = i;
            
            setupButton(floorButton, targetFloor);
            positionButton(floorButton, i);
            
            floorButtons.put(targetFloor, floorButton);
            this.add(floorButton, gbc);
        }
        
    }
    
    private void setupButton(JButton button, int targetFloor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(50, 35));
        button.setEnabled(false); 
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFloorButtonClick(targetFloor);
            }
        });
    }
    
    private void positionButton(JButton button, int floor) {
        gbc.gridx = (floor % 3);
        gbc.gridy = 10 - floor / 3;
    }
    
 
    private void handleFloorButtonClick(int targetFloor) {
            if (elevatorTrack.getAnimationController().isAnimationRunning()) {
                scheduleRetry(() -> handleFloorButtonClick(targetFloor), 200);
            return;
        }
        
       
        if (!elevatorTrack.getSimulationController().isSimulationStarted() ||
            elevatorTrack.getSimulationController().isSimulationEnded()) {
            return;
        }
        
        if (!elevatorTrack.getPassengerManager().hasPassengersInElevator()) {
            return;
        }
        
        if (targetFloor == elevatorTrack.getCabinFloor()) {
            return;
        }
        
        List<Integer> elevatorTargets = elevatorTrack.getQueueManager().getElevatorTargetsList();
        if (elevatorTargets.contains(targetFloor)) {
            return;
        }
        
        elevatorTrack.addFloorTarget(targetFloor);
        
        updateButtonStates();
    }
    
    private void scheduleRetry(Runnable action, int delay) {
        Timer retryTimer = new Timer(delay, e -> {
            if (!elevatorTrack.getAnimationController().isAnimationRunning()) {
                action.run();
            } else {
            }
            ((Timer) e.getSource()).stop();
        });
        retryTimer.setRepeats(false);
        retryTimer.start();
    }
    
    public void enableFloorSelection() {
        if (!elevatorTrack.getSimulationController().isSimulationStarted() ||
            elevatorTrack.getSimulationController().isSimulationEnded()) {
            return;
        }
        
        if (!elevatorTrack.getPassengerManager().hasPassengersInElevator()) {
            return;
        }
        
        if (elevatorTrack.getAnimationController().isAnimationRunning()) {
            scheduleRetry(this::enableFloorSelection, 100);
            return;
        }
        
        buttonsEnabled = true;
        updateButtonStates();
    }
    
    public void disableFloorSelection() {
        buttonsEnabled = false;
        
        for (JButton button : floorButtons.values()) {
            button.setEnabled(false);
            button.setBackground(Color.DARK_GRAY);
        }
        
    }
    
    public void synchronizeWithTrackTargets() {
        if (elevatorTrack.getAnimationController().isAnimationRunning()) {
            scheduleRetry(this::synchronizeWithTrackTargets, 100);
            return;
        }
        
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        List<Integer> elevatorTargets = elevatorTrack.getQueueManager().getElevatorTargetsList();
        int currentFloor = elevatorTrack.getCabinFloor();
        boolean hasPassengers = elevatorTrack.getPassengerManager().hasPassengersInElevator();
        
        for (Map.Entry<Integer, JButton> entry : floorButtons.entrySet()) {
            int floor = entry.getKey();
            JButton button = entry.getValue();
            
            updateSingleButtonState(button, floor, elevatorTargets, currentFloor,hasPassengers);
        }
    }
    
    private void updateSingleButtonState(JButton button, int floor, List<Integer> elevatorTargets, 
                                       int currentFloor, boolean hasPassengers) {
        
        if (!buttonsEnabled || !hasPassengers) {
            button.setEnabled(false);
            button.setBackground(Color.DARK_GRAY);
            
        } else if (elevatorTargets.contains(floor)) {
            button.setEnabled(false);
            button.setBackground(Color.ORANGE);
            
        } else if (floor == currentFloor) {
            button.setEnabled(false);
            button.setBackground(Color.LIGHT_GRAY);
            
        }  else {
            button.setEnabled(true);
            button.setBackground(Color.WHITE);
        }
    }
    
    public void clearAllRequests() {
        elevatorTrack.getQueueManager().clearQueue();
        isMoving = false;
        
        Timer checkTimer = new Timer(100, e -> {
            elevatorTrack.getSimulationController().checkSimulationEndConditions();
            ((Timer) e.getSource()).stop();
        });
        checkTimer.setRepeats(false);
        checkTimer.start();
        
        if (buttonsEnabled) {
            updateButtonStates();
        }
    }
    
    public List<Integer> getQueuedRequests() {
        return new ArrayList<>(elevatorTrack.getQueueManager().getElevatorTargetsList());
    }
}