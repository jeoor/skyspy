package com.kayro.dungeon.lwjgl3;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public final class StartupHelper {
    private static final String JVM_RESTARTED_ARG = "jvmIsRestarted";

    private StartupHelper() {
    }

    public static boolean startNewJvmIfRequired() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (!osName.contains("mac") && !osName.contains("darwin")) {
            return false;
        }
        if (System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + ProcessHandle.current().pid()) != null) {
            return false;
        }
        long pid = ProcessHandle.current().pid();
        String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);
        if ("1".equals(env)) {
            return false;
        }

        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : jvmArgs) {
            if (arg.contains(JVM_RESTARTED_ARG)) {
                return false;
            }
        }

        String separator = System.getProperty("file.separator");
        String javaExecPath = System.getProperty("java.home") + separator + "bin" + separator + "java";
        if (!new File(javaExecPath).exists()) {
            System.err.println("Unable to find java executable at " + javaExecPath);
            return false;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(javaExecPath);
        cmd.add("-XstartOnFirstThread");
        cmd.add("-D" + JVM_RESTARTED_ARG + "=true");
        cmd.addAll(jvmArgs);
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        if (mainClass == null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            mainClass = trace[trace.length - 1].getClassName();
        }
        cmd.add(mainClass);

        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.inheritIO();
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Failed to restart JVM with -XstartOnFirstThread");
            e.printStackTrace();
        }
        return true;
    }
}
