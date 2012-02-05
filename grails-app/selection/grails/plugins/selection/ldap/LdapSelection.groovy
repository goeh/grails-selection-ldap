/*
 *  Copyright 2012 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

import org.apache.directory.groovyldap.*
import grails.plugins.selection.SelectionUtils

/**
 * This selection handler provides LDAP search features.
 */
class LdapSelection {

    String bindUser
    String bindPassword

    /**
     * Check that the URI scheme is 'ldap'.
     * @param uri the URI to check support for
     * @return true if uri.scheme is 'ldap'
     */
    boolean supports(URI uri) {
        return uri?.scheme == 'ldap'
    }

    def select(URI uri, Map params) {
        def ldapURL = uri.scheme + "://" + uri.host
        if (uri.port > 0) {
            ldapURL += ":" + uri.port
        }
        if(uri.path) {
            ldapURL += uri.path
        }

        def ldap = bindUser ? LDAP.newInstance(ldapURL, bindUser, bindPassword) : LDAP.newInstance(ldapURL)
        def query = SelectionUtils.queryAsMap(uri.query)
        def filter = query.filter ?: "(objectClass=*)"

        if(log.isDebugEnabled()) {
            log.debug "LDAP search: $ldapURL filter=$filter"
        }

        ldap.search(filter, ldapURL, SearchScope.SUB)
    }
}