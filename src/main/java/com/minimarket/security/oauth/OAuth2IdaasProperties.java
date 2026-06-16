package com.minimarket.security.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.oauth2")
public class OAuth2IdaasProperties {

    private boolean clientEnabled;
    private boolean resourceServerEnabled;
    private String issuerUri;
    private String registrationId = "idaas";

    public boolean isClientEnabled() {
        return clientEnabled;
    }

    public void setClientEnabled(boolean clientEnabled) {
        this.clientEnabled = clientEnabled;
    }

    public boolean isResourceServerEnabled() {
        return resourceServerEnabled;
    }

    public void setResourceServerEnabled(boolean resourceServerEnabled) {
        this.resourceServerEnabled = resourceServerEnabled;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }
}
