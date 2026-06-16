package com.minimarket.security.oauth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OAuth2IdaasStatusService {

    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;
    private final OAuth2IdaasProperties properties;

    public OAuth2IdaasStatusService(
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository,
            OAuth2IdaasProperties properties
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.properties = properties;
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("protocol", "OAuth 2.0 / OpenID Connect");
        status.put("clientEnabled", properties.isClientEnabled());
        status.put("resourceServerEnabled", properties.isResourceServerEnabled());
        status.put("issuerUri", properties.getIssuerUri());
        status.put("registrationId", properties.getRegistrationId());

        if (properties.isClientEnabled()) {
            ClientRegistrationRepository repository = clientRegistrationRepository.getIfAvailable();
            if (repository != null) {
                ClientRegistration registration = repository.findByRegistrationId(properties.getRegistrationId());
                if (registration != null) {
                    status.put("authorizationUri", registration.getProviderDetails().getAuthorizationUri());
                    status.put("ssoLoginPath", "/oauth2/authorization/" + registration.getRegistrationId());
                }
            }
        }
        return status;
    }
}
