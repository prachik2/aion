package org.aion.os;

/** Implementation of {@link KernelInstance} backed by {@link java.lang.Process} */
public class KernelInstanceProcessImpl implements KernelInstance {
    private final Process process;

    public KernelInstanceProcessImpl(Process process) {
        this.process = process;
    }

    @Override
    public void terminate() {
        this.process.destroy();
    }
}
