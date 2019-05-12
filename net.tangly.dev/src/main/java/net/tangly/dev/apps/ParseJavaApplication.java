package net.tangly.dev.apps;

import net.tangly.dev.model.Clazz;
import net.tangly.dev.model.Packages;

public class ParseJavaApplication {

    /**
     * A module is identified as [module]/src/main/java/**. The java source files are located in the module under [module]/src/main/java, the java
     * test source files are located in the module under [module]/src/test/java. The package name is identified as
     * [module]/src/main/java/[package]/[class].java
     */
    public void ParseModules() {

    }

    public Module findOrCreateModule(String name) {
        return null;
    }

    public Packages findOrCreatePackage(String name) {
        return null;
    }

    public Clazz findOrCreateClass(String name) {
        return null;
    }
}
