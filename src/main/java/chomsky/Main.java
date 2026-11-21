package chomsky;

import chomsky.ui.MainWindow;

import javax.swing.*;

/**
 * Punto de entrada del Simulador de la Jerarquía de Chomsky.
 * Ejecutar:  java -jar chomsky-simulator.jar
 */
public final class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // se usa el aspecto por defecto
            }
            new MainWindow().setVisible(true);
        });
    }
}
