import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

public class RenderingEngine {
    private final ElevatorTrack elevatorTrack;
    
    public RenderingEngine(ElevatorTrack elevatorTrack) {
        this.elevatorTrack = elevatorTrack;
    }
    
    public void render(Graphics g) {
        for(int i = 10; i >= 0; i--) {
            Rectangle bounds = elevatorTrack.getFloorBounds().get(i);
            if(bounds != null) {
                drawFloor(g, i, bounds);
            }
        }
        
        if(elevatorTrack.getSimulationController().isSimulationStarted()) {
            drawFloorCallButtons(g);
            drawPassengersOnFloors(g);
        }
        
        if(elevatorTrack.getCabinBounds() != null) {
            drawCabin(g);
        }
    }
    
    private void drawFloor(Graphics g, int floorNumber, Rectangle bounds) {
        if(floorNumber == elevatorTrack.getHighlightedFloor()) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.LIGHT_GRAY);
        }
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        g.setColor(Color.DARK_GRAY);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String text = "Piętro " + floorNumber;
        FontMetrics fm = g.getFontMetrics();
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2;
        g.drawString(text, textX, textY);
    }
    
    private void drawFloorCallButtons(Graphics g) {
        for(int floor = 0; floor <= 10; floor++) {
            Rectangle callBounds = elevatorTrack.getCallButtonBounds().get(floor);
            if(callBounds == null) continue;
            
            boolean isEnabled = elevatorTrack.getQueueManager().getFloorCallButtons().get(floor);
            boolean hasPassengers = elevatorTrack.getPassengerManager().hasPassengersOnFloor(floor);
            boolean inQueue = elevatorTrack.getQueueManager().getElevatorTargetsList().contains(floor);
            
            if(!hasPassengers) {
                g.setColor(Color.DARK_GRAY);
            } else if(inQueue) {
                g.setColor(Color.ORANGE);
            } else if(isEnabled && hasPassengers) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }
            
            g.fillRect(callBounds.x, callBounds.y, callBounds.width, callBounds.height);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(callBounds.x, callBounds.y, callBounds.width, callBounds.height);
            
          
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString(String.valueOf(floor), callBounds.x + 2, callBounds.y + 10);
        }
    }
    
    private void drawPassengersOnFloors(Graphics g) {
        for(int floor = 0; floor <= 10; floor++) {
            List<Passenger> passengers = elevatorTrack.getPassengerManager().getPassengersOnFloors().get(floor);
            Rectangle bounds = elevatorTrack.getFloorBounds().get(floor);
            
            if(bounds != null && !passengers.isEmpty()) {
                int passengerSize = 8;
                int startX = bounds.x + bounds.width + 60;
                int y = bounds.y + (bounds.height - passengerSize) / 2;
                
                int visiblePassengers = Math.min(passengers.size(), 8);
                for(int i = 0; i < visiblePassengers; i++) {
                    Passenger passenger = passengers.get(i);
                    int x = startX + (i * (passengerSize + 2));
                    
                    g.setColor(passenger.getPassengerColor());
                    g.fillOval(x, y, passengerSize, passengerSize);
                    g.setColor(Color.BLACK);
                    g.drawOval(x, y, passengerSize, passengerSize);
                }
                
              
            }
        }
    }
    
    private void drawCabin(Graphics g) {
        Rectangle cabinBounds = elevatorTrack.getCabinBounds();
        
        g.setColor(Color.GRAY);
        g.fillRect(cabinBounds.x, cabinBounds.y, cabinBounds.width, cabinBounds.height);
        
        g.setColor(Color.BLACK);
        g.drawRect(cabinBounds.x, cabinBounds.y, cabinBounds.width, cabinBounds.height);
        g.drawRect(cabinBounds.x + 1, cabinBounds.y + 1, cabinBounds.width - 2, cabinBounds.height - 2);
        
        if(!elevatorTrack.getPassengerManager().getPassengersInElevator().isEmpty() || 
           !elevatorTrack.getAnimationController().getAnimatingPassenger().isEmpty()) {
            drawPassengerInCabin(g);
        }
    }
    
    private void drawPassengerInCabin(Graphics g) {
        Rectangle cabinBounds = elevatorTrack.getCabinBounds();
        int passengerSize = 6;
        
        int startX = cabinBounds.x + 5;
        int startY = cabinBounds.y + 5;
        int cols = (cabinBounds.width - 10) / (passengerSize + 2);
        
        java.util.List<Passenger> passengersInElevator = elevatorTrack.getPassengerManager().getPassengersInElevator();
        for(int i = 0; i < Math.min(passengersInElevator.size(), 20); i++) {
            Passenger passenger = passengersInElevator.get(i);
            int x = startX + (i % cols) * (passengerSize + 2);
            int y = startY + (i / cols) * (passengerSize + 2);
            
            g.setColor(passenger.getPassengerColor());
            g.fillOval(x, y, passengerSize, passengerSize);
            g.setColor(Color.WHITE);
            g.drawOval(x, y, passengerSize, passengerSize);
        }
        
        for(Passenger passenger : elevatorTrack.getAnimationController().getAnimatingPassenger()) {
            if(passenger.isAnimating()) {
                Point pos = passenger.getCurrentPos();
                g.setColor(passenger.getPassengerColor());
                g.fillOval(pos.x, pos.y, passengerSize, passengerSize);
                g.setColor(Color.BLACK);
                g.drawOval(pos.x, pos.y, passengerSize, passengerSize);
            }
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String passengerCount = (passengersInElevator.size() + 
            elevatorTrack.getAnimationController().getAnimatingPassenger().size()) + " os.";
        g.drawString(passengerCount, cabinBounds.x + 5, cabinBounds.y + cabinBounds.height - 5);
    }
}