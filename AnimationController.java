import javax.swing.Timer;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class AnimationController {
    private final ElevatorTrack elevatorTrack;
    private final DestinationPanel destinationPanel;
    private List<Passenger> animatingPassenger = new ArrayList<>();
    private Timer animationTimer;
    private boolean isAnimationRunning = false;
    
    public AnimationController(ElevatorTrack elevatorTrack) {
        this.elevatorTrack = elevatorTrack;
        this.destinationPanel = DestinationPanel.getDestinationPanel();
    }
    
    public void animateMultiplePassengersBoarding(List<Passenger> passengers, int floor) {
        if(passengers.isEmpty()) return;
        
        Rectangle floorRect = elevatorTrack.getFloorBounds().get(floor);
        
        for(int i = 0; i < passengers.size(); i++) {
            Passenger passenger = passengers.get(i);
            
            Point startPos = new Point(
                floorRect.x + floorRect.width + 60 + (i * 12),
                floorRect.y + floorRect.height / 2
            );
            
            Point endPos = calculateCabinPositionForPassenger(
                elevatorTrack.getPassengerManager().getPassengersInElevator().size() + i
            );
            
            passenger.startAnimation(startPos, endPos, "entering");
            animatingPassenger.add(passenger);
        }
        
        startPassengerAnimation();
    }
    
    public void animatePassengerExit(Passenger exitingPassenger, int passengerIndex) {
        Point startPos = calculatePassengerPositionInCabin(passengerIndex);
        Point endPos = new Point(elevatorTrack.getCabinBounds().x + elevatorTrack.getCabinBounds().width + 50, startPos.y);
        
        exitingPassenger.startAnimation(startPos, endPos, "exiting");
        animatingPassenger.add(exitingPassenger);
        elevatorTrack.getPassengerManager().getPassengersInElevator().remove(passengerIndex);
        
        startPassengerAnimation();
    }
    
    public void animateCabinToFloor(int targetFloor, Runnable onComplete) {
        if(isAnimationRunning) {
            return;
        }
        
        if(elevatorTrack.getSimulationController().isMoving()) {
            return;
        }
        
        elevatorTrack.getSimulationController().setMoving(true);
        elevatorTrack.setHighlightedFloor(targetFloor);
        if(targetFloor > elevatorTrack.getCabinFloor()){
            destinationPanel.updateDirectionIndicator(Directions.UP);
        } else{
                destinationPanel.updateDirectionIndicator(Directions.DOWN);
        }


        int distance = Math.abs(targetFloor - elevatorTrack.getCabinFloor());
        int timePerFloor = 200;
        int totalTime = distance * timePerFloor;
        int totalSteps = totalTime / 16;
        
        Rectangle startBounds = new Rectangle(elevatorTrack.getCabinBounds());
        Rectangle endBounds = elevatorTrack.getFloorBounds().get(targetFloor);
        
        Timer timer = new Timer(16, null);
        final int[] step = {0};
        
        timer.addActionListener(e -> {
            if(step[0] >= totalSteps) {
                timer.stop();
                elevatorTrack.setCabinFloor(targetFloor);
                elevatorTrack.calculatePositions();
                elevatorTrack.setHighlightedFloor(-1);
                elevatorTrack.getSimulationController().setMoving(false);
                destinationPanel.resetDirectionIndicator();
                elevatorTrack.repaint();
                
                if(onComplete != null) {
                    onComplete.run();
                }
                
                return;
            }
            
            float progress = (float)step[0] / totalSteps;
            int currentY = (int)(startBounds.y + (endBounds.y - startBounds.y) * progress);
            
            elevatorTrack.getCabinBounds().y = currentY;
            step[0]++;
            elevatorTrack.repaint();
        });
        timer.start();
    }
    
    public void startPassengerAnimation() {
        if(isAnimationRunning || animatingPassenger.isEmpty()) return;
        
        isAnimationRunning = true;
        final int[] step = {0};
        final int totalSteps = 20;
        
        animationTimer = new Timer(16, e -> {
            if(step[0] >= totalSteps) {
                finishAnimation();
                return;
            }
            
            float progress = (float)step[0] / totalSteps;
            
            for(Passenger passenger : animatingPassenger) {
                passenger.updateAnimationPosition(progress);
            }
            
            step[0]++;
            elevatorTrack.repaint();
        });
        
        animationTimer.start();
    }
    
  private void finishAnimation() {
    int exitingCount = 0;
    for(Passenger passenger : animatingPassenger) {
        passenger.finishAnimation();
        if("entering".equals(passenger.getAnimationType())) {
            elevatorTrack.getPassengerManager().getPassengersInElevator().add(passenger);
     } else if("exiting".equals(passenger.getAnimationType())) {
            exitingCount++;
        }
    }
    
    animatingPassenger.clear();
    isAnimationRunning = false;
    
    if(animationTimer != null) {
        animationTimer.stop();
    }
    
    if(exitingCount > 0) {
        checkForImmediateBoardingAfterExit();
    }
    
    if(elevatorTrack.getPassengerManager().getPassengersInElevator().isEmpty() && 
       elevatorTrack.getElevatorButtons() != null) {
        elevatorTrack.getElevatorButtons().disableFloorSelection();
    } else if(elevatorTrack.getElevatorButtons() != null) {
        elevatorTrack.getElevatorButtons().enableFloorSelection();
    }
    
    elevatorTrack.getSimulationController().checkSimulationEndConditions();
    
    Timer queueCheckTimer = new Timer(1000, evt -> {
        if(!isAnimationRunning && !elevatorTrack.getSimulationController().isMoving()) {
            elevatorTrack.getQueueManager().processFloorRequestQueue();
        }
        ((Timer)evt.getSource()).stop();
    });
    queueCheckTimer.setRepeats(false);
    queueCheckTimer.start();
    
    elevatorTrack.repaint();
}
private void checkForImmediateBoardingAfterExit() {
    if(!elevatorTrack.getSimulationController().isWaitingOnFloor() || 
       elevatorTrack.getSimulationController().isMoving()) {
        return;
    }
    
    int currentFloor = elevatorTrack.getCabinFloor();
    
    if(!elevatorTrack.getPassengerManager().hasPassengersOnFloor(currentFloor)) {
        return;
    }
    
    int currentPassengers = elevatorTrack.getPassengerManager().getPassengersInElevator().size();
    if(currentPassengers >= 5) {
        return;
    }
    
    
    
    elevatorTrack.getPassengerManager().loadPassengersAutomatically(currentFloor);
}

    
    private Point calculateCabinPositionForPassenger(int targetIndex) {
        int passengerSize = 6;
        Rectangle cabinBounds = elevatorTrack.getCabinBounds();
        int startX = cabinBounds.x + 5;
        int startY = cabinBounds.y + 5;
        int cols = (cabinBounds.width - 10) / (passengerSize + 2);
        
        int x = startX + (targetIndex % cols) * (passengerSize + 2);
        int y = startY + (targetIndex / cols) * (passengerSize + 2);
        
        return new Point(x, y);
    }
    
    private Point calculatePassengerPositionInCabin(int index) {
        return calculateCabinPositionForPassenger(index);
    }
    
    public boolean isAnimationRunning() {
        return isAnimationRunning;
    }
    
    public List<Passenger> getAnimatingPassenger() {
        return animatingPassenger;
    }
}