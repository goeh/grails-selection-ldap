package org.apache.directory.groovyldap;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

public interface WithContext <T>
{
    T perform(LdapContext ctx) throws NamingException;
}
