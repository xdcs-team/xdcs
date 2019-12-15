package pl.edu.agh.xdcs.security.web.authenticators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.xdcs.config.SecurityPasswordPolicy;
import pl.edu.agh.xdcs.security.web.WebAuthenticator;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Properties;
import java.util.stream.IntStream;

public class LdapWebAuthenticator implements WebAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(LdapWebAuthenticator.class);
    private static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private final SecurityPasswordPolicy.Ldap directive;

    public LdapWebAuthenticator(SecurityPasswordPolicy.Ldap directive) {
        this.directive = directive;
    }

    @Override
    public boolean authenticate(String name, String password) {
        DirContext serviceContext = null;
        try {
            Properties serviceEnv = new Properties();
            serviceEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            serviceEnv.put(Context.PROVIDER_URL, directive.getUrl().toString());
            serviceEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
            serviceEnv.put(Context.SECURITY_PRINCIPAL, directive.getBindDn());
            serviceEnv.put(Context.SECURITY_CREDENTIALS, directive.getBindPassword());
            serviceContext = new InitialDirContext(serviceEnv);

            SearchControls sc = new SearchControls();
            sc.setReturningAttributes(new String[]{directive.getIdentifyingAttribute()});
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchFilter = "(" + escapeLdapSearchFilter(directive.getIdentifyingAttribute()) +
                    "=" + escapeLdapSearchFilter(name) + ")";
            NamingEnumeration<SearchResult> results = serviceContext.search(directive.getBase(), searchFilter, sc);

            if (results.hasMore()) {
                SearchResult result = results.next();
                String distinguishedName = result.getNameInNamespace();

                Properties authEnv = new Properties();
                authEnv.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
                authEnv.put(Context.PROVIDER_URL, directive.getUrl().toString());
                authEnv.put(Context.SECURITY_PRINCIPAL, distinguishedName);
                authEnv.put(Context.SECURITY_CREDENTIALS, password);
                new InitialDirContext(authEnv);

                return true;
            }
        } catch (NamingException e) {
            logger.error("Error during authentication", e);
        } finally {
            if (serviceContext != null) {
                try {
                    serviceContext.close();
                } catch (NamingException e) {
                    logger.error("Error while closing context", e);
                }
            }
        }

        return false;
    }

    private String escapeLdapSearchFilter(String filter) {
        return filter.codePoints()
                .flatMap(ch -> {
                    switch (ch) {
                        case '\\':
                            return "\\5c".codePoints();
                        case '*':
                            return "\\2a".codePoints();
                        case '(':
                            return "\\28".codePoints();
                        case ')':
                            return "\\29".codePoints();
                        case '\u0000':
                            return "\\00".codePoints();
                    }

                    return IntStream.of(ch);
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
