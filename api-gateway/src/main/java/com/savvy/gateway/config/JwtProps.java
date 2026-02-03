package com.savvy.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProps {
    private String signerKey;

    public String getSignerKey() {
        return signerKey;
    }

    public void setSignerKey(String signerKey) {
        this.signerKey = signerKey;
    }
}
