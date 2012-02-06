package grails.plugins.selection.ldap

class LdapSelectionTests extends GroovyTestCase {

    def grailsApplication
    def selectionService

    def TEST_HOST = "localhost"
    def TEST_DOMAIN = "example"
    def TEST_TLD = "com"

    protected void setUp() {
        super.setUp()
        TEST_HOST = grailsApplication.config.ldapSelection.test.host
        TEST_DOMAIN= grailsApplication.config.ldapSelection.test.domain
        TEST_TLD = grailsApplication.config.ldapSelection.test.tld
    }

    void testLdapSearchAll() {
        def result = selectionService.select("ldap://$TEST_HOST:389/dc=$TEST_DOMAIN,dc=$TEST_TLD")
        for(entry in result) {
            println "$entry"
        }
    }

    void testLdapSearchPerson() {
        def result = selectionService.select("ldap://$TEST_HOST:389/dc=$TEST_DOMAIN,dc=$TEST_TLD?filter=(objectClass=inetOrgPerson)")
        for(entry in result) {
            println "$entry"
        }
    }
}
