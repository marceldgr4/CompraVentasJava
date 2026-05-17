package com.app;

import Infrastructure.logging.LoggerFactory;
import com.app.Service.PawnService;
import com.app.UI.Frame.LoginFrame;
import org.slf4j.Logger;

import javax.swing.*;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.warn("No se pudo cargar el LookAndFeel del sistema, usando el por defecto.", e);
            }
            new LoginFrame().setVisible(true);
        });

        // RF-04.6: expirar empeños vencidos al inicio en un SwingWorker
        // (no CompletableFuture, para garantizar que cualquier callback de UI
        //  ocurra en el EDT — RNF-02.1)
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return new PawnService().processOverduePawns();
            }

            @Override
            protected void done() {
                try {
                    int expired = get();
                    log.info("Expiración automática completada. Empeños vencidos actualizados: {}", expired);
                } catch (Exception e) {
                    log.error("Error al procesar empeños vencidos en el arranque", e);
                }
            }
        }.execute();
    }
}