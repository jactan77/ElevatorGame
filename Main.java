import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ElevatorGame");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(650, 800);
            
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setMaximumSize(new Dimension(600,800));
            centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ElevatorTrack elevatorTrack = ElevatorTrack.getElevatorTrack();
            centerPanel.add(elevatorTrack, BorderLayout.CENTER);
            
            gbc.gridx = 1; gbc.gridy = 0;
            gbc.weightx = 0.6; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 5, 10, 5);
            mainPanel.add(centerPanel, gbc);
            
            JPanel leftPanel = new JPanel();
            leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
            ElevatorButtons elevatorButtons = new ElevatorButtons();
            leftPanel.add(elevatorButtons);
            
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.weightx = 0.2; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 10, 10, 5);
            mainPanel.add(leftPanel, gbc);
            
            JPanel rightPanel = new JPanel();
            rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
            DestinationPanel callPanel = DestinationPanel.getDestinationPanel();
            rightPanel.add(callPanel);
            
            gbc.gridx = 2; gbc.gridy = 0;
            gbc.weightx = 0.2; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 5, 10, 10);
            mainPanel.add(rightPanel, gbc);
            
            JButton startButton = new JButton("START");
            startButton.setPreferredSize(new Dimension(100, 40));
            startButton.addActionListener(e -> {
                startButton.setEnabled(false);
                elevatorTrack.startSimulation();  
                   
            });
            
            elevatorTrack.setElevatorButtons(elevatorButtons);
            elevatorTrack.setStartButton(startButton);
            
            frame.add(mainPanel, BorderLayout.CENTER);
            frame.add(startButton, BorderLayout.SOUTH);
            frame.setResizable(true);
            frame.setVisible(true);
        });
    }
}