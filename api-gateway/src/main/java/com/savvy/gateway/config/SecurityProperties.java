// v2

package com.savvy.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> publicPaths = new ArrayList<>();
    private List<PermissionRule> permissionsMapping = new ArrayList<>();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public List<PermissionRule> getPermissionsMapping() {
        return permissionsMapping;
    }

    public void setPermissionsMapping(List<PermissionRule> permissionsMapping) {
        this.permissionsMapping = permissionsMapping;
    }

    public static class PermissionRule {
        private List<String> methods = new ArrayList<>();
        private List<String> paths = new ArrayList<>();

        private List<String> require = new ArrayList<>();

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getRequire() {
            return require;
        }

        public void setRequire(List<String> require) {
            this.require = require;
        }
    }
}
