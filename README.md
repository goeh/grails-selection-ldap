# Grails LDAP Selection Plugin

##This plugin adds LDAP search features to the Grails Selection plugin.

The selection plugin provides unified search for information.
It uses a URI based syntax to select any information from any resource.

**Example**

def result = selectionService.select("ldap:dc=example,dc=com&filter=(objectClass=people)", params)
