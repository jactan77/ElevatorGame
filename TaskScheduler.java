import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.swing.Timer;

public class TaskScheduler {
    private final ElevatorTrack elevatorTrack;
    private List<Integer> passengerFloorRequest;
    private HashMap<Integer, Boolean> floorCallButtons;
    private Directions currentDirection = null;
    
    public TaskScheduler(ElevatorTrack elevatorTrack) {
        this.elevatorTrack = elevatorTrack;
        this.passengerFloorRequest = new ArrayList<>();
        this.floorCallButtons = new HashMap<>();
        
        for(int i = 0; i <= 10; i++) {
            floorCallButtons.put(i, true);
        }
    }
    
    public void handleFloorCallButtonClick(int floor) {
        if(elevatorTrack.getSimulationController().isSimulationEnded() ||
           elevatorTrack.getAnimationController().isAnimationRunning()) {
            scheduleRetry(() -> handleFloorCallButtonClick(floor), 500);
            return;
        }
        
        if(!elevatorTrack.getPassengerManager().hasPassengersOnFloor(floor) || !floorCallButtons.get(floor)) {
            return;
        }
        
        floorCallButtons.put(floor, false);
        
        if(!passengerFloorRequest.contains(floor)) {
            passengerFloorRequest.add(floor);
        }
        
        if(elevatorTrack.getSimulationController().canElevatorMove()) {
            callElevatorToFloor(floor, true);
        }
        
        elevatorTrack.repaint();
    }
    
    public void addFloorTarget(int targetFloor) {
        if(elevatorTrack.getSimulationController().isSimulationEnded()) {
            return;
        }
        
        if(elevatorTrack.getAnimationController().isAnimationRunning()) {
            scheduleRetry(() -> addFloorTarget(targetFloor), 200);
            return;
        }
        
        if(!passengerFloorRequest.contains(targetFloor) && targetFloor != elevatorTrack.getCabinFloor()) {
            passengerFloorRequest.add(targetFloor);
            elevatorTrack.getPassengerManager().setPassengerDestinations(targetFloor);
            
            if(elevatorTrack.getSimulationController().canElevatorMove()) {
                callElevatorToFloor(targetFloor, false);
            }
        }
    }
    
    public void processFloorRequestQueue() {
        if(passengerFloorRequest.isEmpty() || elevatorTrack.getSimulationController().isMoving() || 
           elevatorTrack.getAnimationController().isAnimationRunning()) {
            return;
        }
        
        passengerFloorRequest.remove(Integer.valueOf(elevatorTrack.getCabinFloor()));
        
        if(passengerFloorRequest.isEmpty()) {
            return;
        }
        
        int nextFloor = findNearestFloorFromQueue();
        passengerFloorRequest.remove(Integer.valueOf(nextFloor));
        
        elevatorTrack.getSimulationController().setWaitingOnFloor(false);
        callElevatorToFloor(nextFloor, false);
    }
    
    private void callElevatorToFloor(int floor, boolean isCallRequest) {
        if(elevatorTrack.getAnimationController().isAnimationRunning() || 
           elevatorTrack.getSimulationController().isMoving()) {
            return;
        }
        
        if(isCallRequest && !elevatorTrack.getPassengerManager().hasPassengersOnFloor(floor)) {
            return;
        }
        
        elevatorTrack.getAnimationController().animateCabinToFloor(floor, 
            () -> elevatorTrack.getSimulationController().arriveAtFloor(floor));
    }
    
    private int findNearestFloorFromQueue() {
        if(passengerFloorRequest.isEmpty()) return elevatorTrack.getCabinFloor();
        
        List<Integer> upwardTargets = getUpwardTargets();
        List<Integer> downwardTargets = getDownwardTargets();
        
        if(currentDirection == null) {
            int nearest = passengerFloorRequest.get(0);
            int minDistance = Math.abs(nearest - elevatorTrack.getCabinFloor());
            
            for(int floor : passengerFloorRequest) {
                int distance = Math.abs(floor - elevatorTrack.getCabinFloor());
                if(distance < minDistance) {
                    minDistance = distance;
                    nearest = floor;
                }
            }
            
            currentDirection = (nearest > elevatorTrack.getCabinFloor()) ? Directions.UP : Directions.DOWN;
            return nearest;
        }
        
        if(currentDirection == Directions.UP && !upwardTargets.isEmpty()) {
            return Collections.min(upwardTargets);
        }
        
        if(currentDirection == Directions.DOWN && !downwardTargets.isEmpty()) {
            return Collections.max(downwardTargets);
        }
        
        if(currentDirection == Directions.UP && !downwardTargets.isEmpty()) {
            currentDirection = Directions.DOWN;
            return Collections.max(downwardTargets);
        }
        
        if(currentDirection == Directions.DOWN && !upwardTargets.isEmpty()) {
            currentDirection = Directions.UP;
            return Collections.min(upwardTargets);
        }
        
        return passengerFloorRequest.get(0);
    }
    
    private List<Integer> getUpwardTargets() {
        return passengerFloorRequest.stream()
            .filter(floor -> floor > elevatorTrack.getCabinFloor())
            .sorted()
            .collect(Collectors.toList());
    }
    
    private List<Integer> getDownwardTargets() {
        return passengerFloorRequest.stream()
            .filter(floor -> floor < elevatorTrack.getCabinFloor())
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toList());
    }
    

    
    private void scheduleRetry(Runnable action, int delay) {
        Timer retryTimer = new Timer(delay, e -> {
            if(!elevatorTrack.getAnimationController().isAnimationRunning()) {
                action.run();
            }
            ((Timer)e.getSource()).stop();
        });
        retryTimer.setRepeats(false);
        retryTimer.start();
    }
    
    public void resetFloorCallButtons() {
        for(int i = 0; i <= 10; i++) {
            floorCallButtons.put(i, true);
        }
    }
    
    public void clearQueue() {
        passengerFloorRequest.clear();
        currentDirection = null;
    }
    
    public List<Integer> getElevatorTargetsList() {
        return passengerFloorRequest;
    }
    
    public HashMap<Integer, Boolean> getFloorCallButtons() {
        return floorCallButtons;
    }
    
    public Directions getCurrentDirection() {
        return currentDirection;
    }
}