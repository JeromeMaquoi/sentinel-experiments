package be.unamur.snail.core;

import java.io.File;
import java.util.List;

public class Context {
    private List<String> classPath;
    private String repoPath;
    private String commit;
    private String energyToolPath;
    private String energyToolVersion;
    private File initScript;
    private String javaHome;
    private String currentWorkingDir;

    public List<String> getClassPath() {
        return classPath;
    }

    public void setClassPath(List<String> classPath) {
        this.classPath = classPath;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getEnergyToolPath() {
        return energyToolPath;
    }

    public void setEnergyToolPath(String energyToolPath) {
        this.energyToolPath = energyToolPath;
    }

    public String getEnergyToolVersion() {
        return energyToolVersion;
    }

    public void setEnergyToolVersion(String energyToolVersion) {
        this.energyToolVersion = energyToolVersion;
    }

    public File getInitScript() {
        return initScript;
    }

    public void setInitScript(File initScript) {
        this.initScript = initScript;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getCurrentWorkingDir() {
        return currentWorkingDir;
    }

    public void setCurrentWorkingDir(String currentWorkingDir) {
        this.currentWorkingDir = currentWorkingDir;
    }
}
