package studentservice;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Student Service Management System.
 * Initial version: opens an empty window to confirm the project runs.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student Service Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
