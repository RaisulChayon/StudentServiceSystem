package studentservice;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Entry point for the Student Service Management System.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        applyLookAndFeel();
        // Swing components must be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            StudentServiceGUI gui = new StudentServiceGUI(new StudentServiceSystem());
            gui.setVisible(true);
        });
    }

    /** Uses the Nimbus look and feel if available; otherwise keeps the default. */
    private static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set look and feel, using default: " + e.getMessage());
        }
    }
}
