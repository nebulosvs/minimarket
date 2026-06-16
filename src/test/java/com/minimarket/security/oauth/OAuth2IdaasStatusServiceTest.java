package com.minimarket.security.oauth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2IdaasStatusServiceTest {

    @Mock
    private ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private OAuth2IdaasProperties properties;

    @InjectMocks
    private OAuth2IdaasStatusService statusService;

    @Test
    void status_clienteDeshabilitado_retornaConfiguracionBase() {
        when(properties.isClientEnabled()).thenReturn(false);
        when(properties.isResourceServerEnabled()).thenReturn(false);
        when(properties.getIssuerUri()).thenReturn("");
        when(properties.getRegistrationId()).thenReturn("idaas");

        Map<String, Object> status = statusService.status();

        assertEquals("OAuth 2.0 / OpenID Connect", status.get("protocol"));
        assertFalse((Boolean) status.get("clientEnabled"));
    }

    @Test
    void status_clienteHabilitado_incluyeRutasSso() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("idaas")
                .clientId("client")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .authorizationUri("https://idaas.example/authorize")
                .tokenUri("https://idaas.example/token")
                .issuerUri("https://idaas.example")
                .build();

        when(properties.isClientEnabled()).thenReturn(true);
        when(properties.isResourceServerEnabled()).thenReturn(true);
        when(properties.getIssuerUri()).thenReturn("https://idaas.example");
        when(properties.getRegistrationId()).thenReturn("idaas");
        when(clientRegistrationRepositoryProvider.getIfAvailable()).thenReturn(clientRegistrationRepository);
        when(clientRegistrationRepository.findByRegistrationId("idaas")).thenReturn(registration);

        Map<String, Object> status = statusService.status();

        assertEquals("https://idaas.example/authorize", status.get("authorizationUri"));
        assertEquals("/oauth2/authorization/idaas", status.get("ssoLoginPath"));
    }
}
