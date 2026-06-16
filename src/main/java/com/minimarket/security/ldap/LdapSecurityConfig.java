package com.minimarket.security.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

@Configuration
@ConditionalOnProperty(name = "app.security.ldap.enabled", havingValue = "true")
public class LdapSecurityConfig {

    @Bean
    public LdapContextSource ldapContextSource(InMemoryDirectoryServer embeddedLdapServer) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:" + embeddedLdapServer.getListenPort());
        contextSource.setBase("dc=minimarket,dc=cl");
        contextSource.setUserDn("dc=minimarket,dc=cl");
        contextSource.setPassword("secret");
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public BindAuthenticator ldapBindAuthenticator(LdapContextSource ldapContextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource);
        authenticator.setUserDnPatterns(new String[]{"uid={0},ou=people"});
        return authenticator;
    }

    @Bean
    public DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator(LdapContextSource ldapContextSource) {
        DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(
                ldapContextSource,
                "ou=groups"
        );
        populator.setGroupRoleAttribute("cn");
        populator.setRolePrefix("ROLE_");
        populator.setSearchSubtree(true);
        return populator;
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(
            BindAuthenticator ldapBindAuthenticator,
            DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator
    ) {
        return new LdapAuthenticationProvider(ldapBindAuthenticator, ldapAuthoritiesPopulator);
    }
}
