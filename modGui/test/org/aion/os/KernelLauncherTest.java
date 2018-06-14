package org.aion.os;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KernelLauncherTest {
//    @Test
//    public void test() throws Exception {
//        Process proc = new KernelLauncher(CfgGuiLauncher.AUTODETECTING_CONFIG).launch();
//        Thread.sleep(15000);
//    }

    /*
    @Test
    public void testLaunchAuto() throws Exception {
        KernelLaunchConfigurator klc = mock(KernelLaunchConfigurator.class);
        KernelLauncher unit = new KernelLauncher(CfgGuiLauncher.AUTODETECTING_CONFIG, klc);

        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        unit.launch(processBuilder);
        verify(klc).configureAutomatically(processBuilder);
        verify(processBuilder).start();
    }

    @Test
    public void testLaunchManual() throws Exception {
        KernelLaunchConfigurator klc = mock(KernelLaunchConfigurator.class);
        CfgGuiLauncher cfg = new CfgGuiLauncher();
        cfg.setAutodetectJavaRuntime(false);
        KernelLauncher unit = new KernelLauncher(CfgGuiLauncher.AUTODETECTING_CONFIG, klc);

        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        unit.launch(processBuilder);
        verify(klc).configureManually(eq(processBuilder), any(CfgGuiLauncher.class));
        verify(processBuilder).start();
    }
    */
}