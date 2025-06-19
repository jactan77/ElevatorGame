import javax.swing.Timer;

public class SimulationController {
    private final ElevatorTrack elevatorTrack;
    private boolean simulationStarted = false;
    private boolean simulationEnded = false;
    private boolean isMoving = false;
    private boolean isWaitingOnFloor = false;
    private boolean isPassengerEntry = false;
    
    private Timer waitingTimer;
    private Timer endSimulationTimer;
    
    public SimulationController(ElevatorTrack elevatorTrack) {
        this.elevatorTrack = elevatorTrack;
    }
    
    public void startSimulation() {
        if(simulationStarted) {
            return;
        }
        
        simulationStarted = true;
        simulationEnded = false;
        isWaitingOnFloor = true;
        isPassengerEntry = false;
        
        elevatorTrack.getPassengerManager().generateRandomPassenger();
        elevatorTrack.getQueueManager().resetFloorCallButtons();
        elevatorTrack.getQueueManager().clearQueue();
        
        elevatorTrack.requestFocus();
        elevatorTrack.repaint();
        
        Timer initialCheckTimer = new Timer(1000, e -> {
            checkSimulationEndConditions();
            ((Timer)e.getSource()).stop();
        });
        initialCheckTimer.setRepeats(false);
        initialCheckTimer.start();
    }

    public void arriveAtFloor(int floor) {
        if(elevatorTrack.getAnimationController().isAnimationRunning()) {
            scheduleRetry(() -> arriveAtFloor(floor), 1000);
            return;
        }
        
        elevatorTrack.getQueueManager().getElevatorTargetsList().remove(Integer.valueOf(floor));
        
        if(elevatorTrack.getPassengerManager().hasPassengersOnFloor(floor) && 
           elevatorTrack.getPassengerManager().getPassengersInElevator().size() < 5) {
            
            elevatorTrack.getPassengerManager().loadPassengersAutomatically(floor);
            
            if(elevatorTrack.getPassengerManager().hasPassengersInElevator() && 
               elevatorTrack.getElevatorButtons() != null) {
                elevatorTrack.getElevatorButtons().enableFloorSelection();
            }
        }
        
        if(!elevatorTrack.getQueueManager().getElevatorTargetsList().contains(floor)) {
            elevatorTrack.getQueueManager().getFloorCallButtons().put(floor, true);
        }
        elevatorTrack.getElevatorButtons().enableFloorSelection();;
        startWaitingTimer();
        elevatorTrack.repaint();
    }
    
    private void startWaitingTimer() {
        if(waitingTimer != null && waitingTimer.isRunning()) {
            waitingTimer.stop();
        }
        
        isWaitingOnFloor = true;
        isMoving = false;
        waitingTimer = new Timer(5000, e -> {
            if (!elevatorTrack.getAnimationController().isAnimationRunning()) {
                checkSimulationEndConditions();
                elevatorTrack.getQueueManager().processFloorRequestQueue();
            }
            ((Timer)e.getSource()).stop();
        });
        
        waitingTimer.setRepeats(false);
        waitingTimer.start();
    }
    
    private void scheduleRetry(Runnable action, int delay) {
        Timer retryTimer = new Timer(delay, e -> {
            if(!elevatorTrack.getAnimationController().isAnimationRunning()) {
                action.run();
            } else {
                scheduleRetry(action, delay);
            }
            ((Timer)e.getSource()).stop();
        });
        retryTimer.setRepeats(false);
        retryTimer.start();
    }

    public boolean canElevatorMove() {
        boolean result = !elevatorTrack.getAnimationController().isAnimationRunning() && 
                        !isMoving &&
                        (waitingTimer == null || !waitingTimer.isRunning());
        
        return result;
    }
   
    public void checkSimulationEndConditions() {
        if(simulationEnded) return;
        
        boolean cabinStopped = !isMoving && isWaitingOnFloor;
        boolean allPassengersOut = elevatorTrack.getPassengerManager().getPassengersInElevator().isEmpty();
        boolean noNewRequests = elevatorTrack.getQueueManager().getElevatorTargetsList().isEmpty();
        
        if(cabinStopped && allPassengersOut && noNewRequests) {
            if(endSimulationTimer == null || !endSimulationTimer.isRunning()) {
                startSimulationEndTimer();
            }
        } else {
            if(endSimulationTimer != null && endSimulationTimer.isRunning()) {
                endSimulationTimer.stop();
            }
        }
    }
    
    private void startSimulationEndTimer() {
        endSimulationTimer = new Timer(10000, e -> {
            endSimulation();
            ((Timer)e.getSource()).stop();
        });
        endSimulationTimer.setRepeats(false);
        endSimulationTimer.start();
    }
    
    private void endSimulation() {
        simulationEnded = true;
        simulationStarted = false;
        isWaitingOnFloor = false;
        
        if(waitingTimer != null && waitingTimer.isRunning()) {
            waitingTimer.stop();
        }
        if(endSimulationTimer != null && endSimulationTimer.isRunning()) {
            endSimulationTimer.stop();
        }
        
        elevatorTrack.getQueueManager().clearQueue();
        elevatorTrack.getPassengerManager().clearAllPassengers();
        elevatorTrack.getAnimationController().getAnimatingPassenger().clear();
        
        if(elevatorTrack.getElevatorButtons() != null) {
            elevatorTrack.getElevatorButtons().disableFloorSelection();
            elevatorTrack.getElevatorButtons().clearAllRequests();
        }
    
        elevatorTrack.getAnimationController().animateCabinToFloor(0, () -> {
            if(elevatorTrack.getStartButton() != null) {
                elevatorTrack.getStartButton().setEnabled(true);
            }
            elevatorTrack.repaint();
        });
    }

    public boolean canExitPassenger() {
        return isWaitingOnFloor && !elevatorTrack.getAnimationController().isAnimationRunning() && 
               !isMoving && simulationStarted && !simulationEnded;
    }
    
    public boolean isSimulationStarted() { return simulationStarted; }
    public boolean isSimulationEnded() { return simulationEnded; }
    public boolean isMoving() { return isMoving; }
    public void setMoving(boolean moving) { this.isMoving = moving; }
    public boolean isWaitingOnFloor() { return isWaitingOnFloor; }
    public void setWaitingOnFloor(boolean waiting) { this.isWaitingOnFloor = waiting; }
    public boolean isPassengerEntry() { return isPassengerEntry; }
    public void setPassengerEntry(boolean passengerEntry) { this.isPassengerEntry = passengerEntry; }
    public Timer getWaitingTimer() { return waitingTimer; }
    public Timer getEndSimulationTimer() { return endSimulationTimer; }
}