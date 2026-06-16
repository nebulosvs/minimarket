package com.minimarket.security.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Configuration
@ConditionalOnProperty(name = "app.security.ldap.enabled", havingValue = "true")
public class EmbeddedLdapServerConfig {

    @Value("classpath:ldap/users.ldif")
    private Resource ldifResource;

    @Bean(destroyMethod = "shutDown")
    public InMemoryDirectoryServer embeddedLdapServer() throws Exception {
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=minimarket,dc=cl");
        config.setSchema(null);
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("ldap", 0));
        config.addAdditionalBindCredentials("dc=minimarket,dc=cl", "secret");

        InMemoryDirectoryServer server = new InMemoryDirectoryServer(config);
        try (InputStream ldifStream = ldifResource.getInputStream();
             LDIFReader reader = new LDIFReader(ldifStream)) {
            server.importFromLDIF(true, reader);
        }
        server.startListening();
        return server;
    }

    @Bean
    public int embeddedLdapPort(InMemoryDirectoryServer embeddedLdapServer) throws LDAPException {
        return embeddedLdapServer.getListenPort();
    }
}
