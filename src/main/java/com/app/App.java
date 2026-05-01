package com.app;

import Infrastructure.logging.LoggerFactory;
import com.app.Service.PawnService;
import com.app.UI.Frame.LoginFrame;
import org.slf4j.Logger;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Run background task to expire overdue pawns automatically (RF-04.6)
        CompletableFuture.runAsync(() -> {
            try {
                int expiredCount = new PawnService().processOverduePawns();
                log.info("Proceso de expiración automática completado. Empeños vencidos actualizados: {}", expiredCount);
            } catch (Exception e) {
                log.error("Error al procesar empeños vencidos en el arranque", e);
            }
        });

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.warn("No se pudo cargar el LookAndFeel del sistema, usando el por defecto.", e);
            }
            new LoginFrame().setVisible(true);
        });
    }
}