package org.aion;

import org.aion.gui.controller.MainWindow;
import org.aion.log.AionLoggerFactory;
import org.aion.zero.impl.config.CfgAion;

import java.util.ServiceLoader;

/**
 * Entry-point for the graphical front-end for Aion kernel.
 */
public class AionGraphicalFrontEnd {
    public static void main(String args[]) {


        // Load the UI
        javafx.application.Application.launch(MainWindow.class, args);
    }
}
