package grails.plugins.selection.ldap

class LdapSelectionTests extends GroovyTestCase {

    def selectionService

    def TEST_HOST = "localhost"
    def TEST_DOMAIN = "example"
    def TEST_TLD = "com"

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
