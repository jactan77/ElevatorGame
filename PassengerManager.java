import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Timer;

public class PassengerManager {
    private final ElevatorTrack elevatorTrack;
    private HashMap<Integer, List<Passenger>> passengersOnFloors;
    private List<Passenger> passengersInElevator;
    private final int MAX_CABIN_CAPACITY = 5;
    
    public PassengerManager(ElevatorTrack elevatorTrack) {
        this.elevatorTrack = elevatorTrack;
        initializePassengers();
    }
    
    private void initializePassengers() {
        passengersOnFloors = new HashMap<>();
        passengersInElevator = new ArrayList<>();
        
        for(int i = 0; i <= 10; i++) {
            passengersOnFloors.put(i, new ArrayList<>());
        }
    }
    
    public void generateRandomPassenger() {
        for(int floor = 0; floor <= 10; floor++) {
            int passengerCount = (int)(Math.random() * 6);
            List<Passenger> floorPassengers = passengersOnFloors.get(floor);
            
            for(int i = 0; i < passengerCount; i++) {
                Passenger passenger = new Passenger(floor);
                floorPassengers.add(passenger);
            }
        }
    }
    
    public void loadPassengersAutomatically(int floor) {
        List<Passenger> floorPassengers = passengersOnFloors.get(floor);
        
        if(floorPassengers.isEmpty() || passengersInElevator.size() >= MAX_CABIN_CAPACITY || 
           elevatorTrack.getAnimationController().isAnimationRunning()) {
            return;
        }
        
        int availableSpaces = MAX_CABIN_CAPACITY - passengersInElevator.size();
        int passengersToBoard = Math.min(availableSpaces, floorPassengers.size());
        
        java.util.List<Passenger> boardingPassengers = new ArrayList<>();
        for(int i = 0; i < passengersToBoard; i++) {
            Passenger enteringPassenger = floorPassengers.remove(0);
            boardingPassengers.add(enteringPassenger);
        }
        
        elevatorTrack.getAnimationController().animateMultiplePassengersBoarding(boardingPassengers, floor);
        elevatorTrack.repaint();
    }
    
   public void animatePassengerExit() {
        if(passengersInElevator.isEmpty() || elevatorTrack.getAnimationController().isAnimationRunning()) {
            return;
        }
        
        int randomIndex = (int)(Math.random() * passengersInElevator.size());
        Passenger exitingPassenger = passengersInElevator.get(randomIndex);
        
        elevatorTrack.getAnimationController().animatePassengerExit(exitingPassenger, randomIndex);
}
    
   public void unloadSpecificPassenger(Point clickPoint) {
    if(passengersInElevator.isEmpty() || elevatorTrack.getAnimationController().isAnimationRunning()) {
        return;
    }
    
    Rectangle cabinBounds = elevatorTrack.getCabinBounds();
    int passengerSize = 6;
    int startX = cabinBounds.x + 5;
    int startY = cabinBounds.y + 5;
    int cols = (cabinBounds.width - 10) / (passengerSize + 2);
    
    for(int i = 0; i < Math.min(passengersInElevator.size(), 20); i++) {
        int x = startX + (i % cols) * (passengerSize + 2);
        int y = startY + (i / cols) * (passengerSize + 2);
        
        Rectangle passengerBounds = new Rectangle(x, y, passengerSize, passengerSize);
        
        if(passengerBounds.contains(clickPoint)) {
            Passenger exitingPassenger = passengersInElevator.get(i);
            Point startPos = new Point(x, y);
            Point endPos = new Point(cabinBounds.x + cabinBounds.width + 50, y);
            
            exitingPassenger.startAnimation(startPos, endPos, "exiting");
            elevatorTrack.getAnimationController().getAnimatingPassenger().add(exitingPassenger);
            passengersInElevator.remove(i);
            
            elevatorTrack.getAnimationController().startPassengerAnimation();
            
            return;
        }
    }
    
}

    public void checkForAdditionalPassengersAfterExit() {
    if(!elevatorTrack.getSimulationController().isWaitingOnFloor() || 
       elevatorTrack.getAnimationController().isAnimationRunning()) {
        return;
    }
    
    int currentFloor = elevatorTrack.getCabinFloor();
    List<Passenger> currentFloorPassengers = passengersOnFloors.get(currentFloor);
    int availableSpaces = MAX_CABIN_CAPACITY - passengersInElevator.size();
    
    if(!currentFloorPassengers.isEmpty() && availableSpaces > 0) {
        Timer immediateBoard = new Timer(50, e -> {
            if(!elevatorTrack.getAnimationController().isAnimationRunning()) {
                loadPassengersAutomatically(currentFloor);
            }
            ((Timer)e.getSource()).stop();
        });
        immediateBoard.setRepeats(false);
        immediateBoard.start();
    }
}
    
    public void setPassengerDestinations(int targetFloor) {
        for(Passenger passenger : passengersInElevator) {
            if(passenger.getTargetFloor() == -1) {
                passenger.setTargetFloor(targetFloor);
                break;
            }
        }
    }
    
    public void clearAllPassengers() {
        passengersInElevator.clear();
        for(int i = 0; i <= 10; i++) {
            passengersOnFloors.get(i).clear();
        }
    }
    
    public boolean hasPassengersOnFloor(int floor) {
        return elevatorTrack.getSimulationController().isSimulationStarted() && 
               !passengersOnFloors.get(floor).isEmpty();
    }
    
    public boolean hasPassengersInElevator() {
        return !passengersInElevator.isEmpty();
    }
    
    public List<Passenger> getPassengersInElevator() {
        return passengersInElevator;
    }
    
    public HashMap<Integer, List<Passenger>> getPassengersOnFloors() {
        return passengersOnFloors;
    }
    
    public int getCountPassengersOnFloor(int floor) {
        return passengersOnFloors.get(floor).size();
    }
}