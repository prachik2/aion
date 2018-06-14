package org.aion.os;

import org.aion.mcf.config.CfgGuiLauncher;

import java.io.File;
import java.util.Map;

/** Sets up configuration for launching kernel in a separate OS process. */
public class KernelLaunchConfigurator {
    private static final String NOHUP_WRAPPER = "script/nohup_wrapper.sh";

    /**
     * Set parameters on a {@link ProcessBuilder} to configure it so it is ready to launch kernel.
     *
     * Parameters on the ProcessBuilder that clash with the parameters that this method is trying
     * to set will be overwritten, but others will be left alone.
     *
     * @param processBuilder object in which parameters will be applied
     */
    public void configureAutomatically(ProcessBuilder processBuilder) {
        CfgGuiLauncher config = new CfgGuiLauncher();
        String javaHome = System.getProperty("java.home");
        String workingDir = System.getProperty("user.dir");

        config.setJavaHome(javaHome);
        config.setWorkingDir(workingDir);
        config.setAionSh(String.format("%s/aion.sh", workingDir)); // will this blow up on Windows?
        configureManually(processBuilder, config);
    }

    /**
     * Set parameters on a {@link ProcessBuilder} to configure it so it is ready to launch kernel.
     *
     * Parameters on the ProcessBuilder that clash with the parameters that this method is trying
     * to set will be overwritten, but others will be left alone.
     *
     * @param processBuilder object in which parameters will be applied
     * @param config configuration parameters
     */
    public void configureManually(ProcessBuilder processBuilder, CfgGuiLauncher config) {
        Map<String, String> envVars = processBuilder.environment();
        envVars.put("JAVA_HOME", config.getJavaHome());
        processBuilder.directory(new File(config.getWorkingDir()));

        // invoke the actual command from nohup; otherwise, if a user sends ctrl-C
        // to the GUI program, the spawned process will also be killed
        processBuilder.command(
                String.format("%s/%s", config.getWorkingDir(), NOHUP_WRAPPER),
                config.getAionSh()
        );
    }
}
