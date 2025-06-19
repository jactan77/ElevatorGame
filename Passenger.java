import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;


public class Passenger extends JPanel {
    private int floor;
    private boolean inElevator;
    private int targetFloor;
    private Color passengerColor;
    
    private boolean isAnimating = false;
    private Point animationStartPos;
    private Point animationEndPos;
    private Point currentPos;
    private String animationType;
    
    public Passenger(int floor) {
        this.floor = floor;
        this.inElevator = false;
        this.targetFloor = -1;
        this.passengerColor = generateRandomColor();
        this.currentPos = new Point(0, 0);
        setOpaque(false);
        setPreferredSize(new Dimension(12, 12));
    }
    
    private Color generateRandomColor() {
        int r = 150 + (int)(Math.random() * 100);
        int g = 150 + (int)(Math.random() * 100); 
        int b = 150 + (int)(Math.random() * 100);
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g){
            super.paintComponent(g);
            
            g.setColor(passengerColor);
            int diameter = 8; 
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g.fillOval(x, y, diameter, diameter);
            
            g.setColor(Color.BLACK);
            g.drawOval(x, y, diameter, diameter);
        }
    public void startAnimation(Point startPos, Point endPos, String type) {
        this.animationStartPos = new Point(startPos);
        this.animationEndPos = new Point(endPos);
        this.currentPos = new Point(startPos);
        this.animationType = type;
        this.isAnimating = true;
    }
    
    public void updateAnimationPosition(float progress) {
        if(isAnimating) {
            int x = (int)(animationStartPos.x + (animationEndPos.x - animationStartPos.x) * progress);
            int y = (int)(animationStartPos.y + (animationEndPos.y - animationStartPos.y) * progress);
            currentPos.setLocation(x, y);
        }
    }
    
    public void finishAnimation() {
        isAnimating = false;
        if("entering".equals(animationType)) {
            inElevator = true;
        } else if("exiting".equals(animationType)) {
        }
    }
    
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    
    public boolean isInElevator() { return inElevator; }
    public void setInElevator(boolean inElevator) { this.inElevator = inElevator; }
    
    public int getTargetFloor() { return targetFloor; }
    public void setTargetFloor(int targetFloor) { this.targetFloor = targetFloor; }
    
    public Color getPassengerColor() { return passengerColor; }
    
    public boolean isAnimating() { return isAnimating; }
    public Point getCurrentPos() { return currentPos; }
    public String getAnimationType() { return animationType; }
}