package org.aion.os;

import org.aion.mcf.config.CfgGuiLauncher;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

/** Test {@link KernelLaunchConfigurator} */
public class KernelLaunchConfiguratorTest {

    @Test
    public void testConfigureAutomatically() throws IOException {
        KernelLaunchConfigurator unit = new KernelLaunchConfigurator();
        ProcessBuilder processBuilder = new ProcessBuilder();

        // unfortunately the method we're testing uses static methods in the System
        // class that we can't really mock/modify safely.  Will just verify that the
        // method uses whatever values are in those System static methods.
        String expectedJavaHome = System.getProperty("java.home");
        String expectedWorkingDir = System.getProperty("user.dir");
        String expectedAionSh = String.format("%s/aion.sh", expectedWorkingDir);

        unit.configureAutomatically(processBuilder);

        assertThat(processBuilder.directory(), is(new File(expectedWorkingDir)));
        assertThat(processBuilder.environment().get("JAVA_HOME"), is(expectedJavaHome));
        assertThat(processBuilder.command().size(), is(1));
        assertThat(processBuilder.command().get(0), is(expectedAionSh));
    }

    @Test
    public void testConfigureManually() {
        KernelLaunchConfigurator unit = new KernelLaunchConfigurator();
        ProcessBuilder processBuilder = new ProcessBuilder();
        CfgGuiLauncher config = new CfgGuiLauncher();
        config.setAutodetectJavaRuntime(false);
        config.setAionSh("aionSh");
        config.setWorkingDir("workingDir");
        config.setJavaHome("javaHome");

        unit.configureManually(processBuilder, config);

        assertThat(processBuilder.directory(), is(new File(config.getWorkingDir())));
        assertThat(processBuilder.environment().get("JAVA_HOME"), is(config.getJavaHome()));
        assertThat(processBuilder.command().size(), is(1));
        assertThat(processBuilder.command().get(0), is(config.getAionSh()));
    }
}