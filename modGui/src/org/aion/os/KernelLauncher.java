package org.aion.os;

import com.google.common.annotations.VisibleForTesting;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.mcf.config.CfgGuiLauncher;
import org.slf4j.Logger;

import java.io.IOException;

/** Facilitates launching an instance of the Kernel. */
public class KernelLauncher {
    private CfgGuiLauncher config;
    private KernelLaunchConfigurator kernelLaunchConfigurator;

    private static Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.GUI.name());

    /**
     * Constructor.
     *
     * @see {@link CfgGuiLauncher#AUTODETECTING_CONFIG} if you want Kernel Launcher to auto-detect the parameters
     */
    public KernelLauncher(CfgGuiLauncher cfgGuiLauncher) {
        this(cfgGuiLauncher, new KernelLaunchConfigurator());
    }

    /** Ctor with injectible parameters for unit testing */
    @VisibleForTesting protected KernelLauncher(CfgGuiLauncher config, KernelLaunchConfigurator klc) {
        this.config = config;
        this.kernelLaunchConfigurator = klc;
    }

    /**
     * Launch a separate JVM in a new OS process and within it, run the Aion kernel
     */
    public Process launch() throws IOException {
        return launch(new ProcessBuilder());
    }

    @VisibleForTesting protected Process launch(ProcessBuilder processBuilder) throws IOException {
        if(config.isAutodetectJavaRuntime()) {
            kernelLaunchConfigurator.configureAutomatically(processBuilder);
        } else {
            kernelLaunchConfigurator.configureManually(processBuilder, config);
        }
        processBuilder
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            return processBuilder.start();
        } catch (IOException ioe) {
            if(ioe.getCause() instanceof IOException) {
                //TODO Better exception msg
                LOGGER.error("Could not find the aion.sh script for launching the Aion Kernel.  " +
                        "Check your configuration; or if auto-detection is used, please manually configure.");
            }
            throw ioe;
        }
    }
}
