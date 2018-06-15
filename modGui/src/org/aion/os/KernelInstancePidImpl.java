package org.aion.os;

import java.io.Serializable;

/**
 * Implementation of {@link KernelInstance} in which reference to the kernel process
 * is its UNIX pid.  Management of the process facilitated by calls to UNIX programs,
 * i.e. `kill`
 */
public class KernelInstancePidImpl implements Serializable {
    private final long pid;
    private static final long serialVersionUID = 4L;

    public KernelInstancePidImpl(long pid) {
        this.pid = pid;
    }

    public long getPid() {
        return pid;
    }
}
