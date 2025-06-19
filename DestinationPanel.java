import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class DestinationPanel extends JPanel {
    private static DestinationPanel destinationPanel = new DestinationPanel(); 
    private JButton buttonUp; 
    private JButton buttonDown;
    private Color defaultColor;
    
    private DestinationPanel(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        buttonUp = new JButton("∧");
        buttonUp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        buttonUp.setEnabled(false); 
        defaultColor = buttonUp.getBackground();
        
        buttonDown = new JButton("∨");
        buttonDown.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        buttonDown.setEnabled(false); 
        
        buttonPanel.add(buttonUp);
        buttonPanel.add(buttonDown);
        
        add(buttonPanel, BorderLayout.CENTER);
        
    }
    
    public static DestinationPanel getDestinationPanel(){
        if(destinationPanel == null){
            destinationPanel = new DestinationPanel();
            return destinationPanel;
        }
        return destinationPanel;
    }
    
    public void updateDirectionIndicator(Directions direction){
        switch (direction) {
            case Directions.UP:
                buttonUp.setBackground(Color.RED);
                break;
            case Directions.DOWN:
                buttonDown.setBackground(Color.RED);
                break;
        }
    }
    
    public void resetDirectionIndicator(){
        buttonDown.setBackground(defaultColor);
        buttonUp.setBackground(defaultColor);
    }
}