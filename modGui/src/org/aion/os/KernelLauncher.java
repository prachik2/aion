package org.aion.os;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.mcf.config.CfgGuiLauncher;
import org.slf4j.Logger;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Facilitates launching an instance of the Kernel. */
public class KernelLauncher {
    private final CfgGuiLauncher config;
    private final KernelLaunchConfigurator kernelLaunchConfigurator;

    private KernelInstancePidImpl currentInstance = null;

    private static String PID_LOCATION = "/tmp/kernel-pid"; // TODO Where is it supposed to actually go?
    private static Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.GUI.name());

    /**
     * Constructor.
     *
     * @see {@link CfgGuiLauncher#AUTODETECTING_CONFIG} if you want Kernel Launcher to auto-detect the parameters
     */
    public KernelLauncher(CfgGuiLauncher cfgGuiLauncher) {
        this(cfgGuiLauncher, new KernelLaunchConfigurator());
    }

    /** Ctor with injectable parameters for unit testing */
    @VisibleForTesting protected KernelLauncher(CfgGuiLauncher config, KernelLaunchConfigurator klc) {
        this.config = config;
        this.kernelLaunchConfigurator = klc;
    }

    /**
     * Launch a separate JVM in a new OS process and within it, run the Aion kernel.
     *
     * @return if successful, a {@link Optional<Process>} whose value is the Process of the aion.sh
     *         wrapper script; otherwise, {@link Optional#empty()}.
     */
    public Optional<Process> launch() throws IOException {
        return Optional.ofNullable(launch(new ProcessBuilder()));
    }

    @VisibleForTesting protected Process launch(ProcessBuilder processBuilder) throws IOException {
        if(config.isAutodetectJavaRuntime()) {
            kernelLaunchConfigurator.configureAutomatically(processBuilder);
        } else {
            kernelLaunchConfigurator.configureManually(processBuilder, config);
        }
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        final Process proc;
        try {
            proc = processBuilder.start();

            // Note: proc is a reference to a shell script that calls the kernel
            // as a background task.  So here we're blocking until the shell script
            // exits, not until the kernel Java process exits.
            proc.waitFor();
        } catch (IOException ioe) {
            if(ioe.getCause() instanceof IOException) {
                //TODO Better exception msg
                LOGGER.error("Could not find the aion.sh script for launching the Aion Kernel.  " +
                        "Check your configuration; or if auto-detection is used, please manually configure.");
            }
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.error("Kernel launch interrupted.  Aborting.  ");
            return null;
        }

        try (
                InputStream is = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, Charsets.UTF_8);
        ) {
            String pid = CharStreams.toString(isr).replace("\n", "");
            System.out.println("pid = " + pid);
            setAndPersistPid(Long.valueOf(pid));
        } catch (IOException ioe) {
            LOGGER.error("PID Serialization error.", ioe);
        }

        return proc;
    }

    public boolean tryResume() throws Exception {
        if(this.currentInstance != null) {
            throw new IllegalArgumentException("Can't try to resume because there is already an associated instance.");
        }

        File pidFile = new File(PID_LOCATION);
        if(pidFile.exists() && !pidFile.isDirectory()) {
            try {
                this.currentInstance = retrieveAndSetPid(pidFile);
                System.out.println("Resumed pid = " + this.currentInstance.getPid());
                return true;
            } catch (Exception e) {
                // TODO Better Exceptions
                System.out.println("Error during deserilaiztaion");
                throw e;
            }
        } else {
            return false;
        }
    }

    public int terminate() throws IOException, InterruptedException {
        // TODO: add a terminate function to the AionAPI that will
        // exit the kernel and use that instead of killing unix scripts

        System.out.println("terminate " + currentInstance.getPid());

        if(currentInstance == null) {
            throw new IllegalArgumentException("Trying to terminate when there is no running instance");
        }
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command("kill",
                        String.valueOf(currentInstance.getPid()))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);
        Process proc = processBuilder.start();
        proc.waitFor(5, TimeUnit.SECONDS);
        this.currentInstance = null;
        System.out.println("kill returned " + proc.exitValue());
        return proc.exitValue();
    }

    public boolean hasLaunchedInstance() {
        return currentInstance != null;
    }

    protected KernelInstancePidImpl retrieveAndSetPid(File pidFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(PID_LOCATION);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (KernelInstancePidImpl) ois.readObject();
    }

    protected KernelInstancePidImpl setAndPersistPid(long pid) throws IOException {
        KernelInstancePidImpl kernel = new KernelInstancePidImpl(pid);
        FileOutputStream fos = new FileOutputStream(PID_LOCATION);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(kernel);
        this.currentInstance = kernel;
        return kernel;
    }
}
