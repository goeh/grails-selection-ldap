/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.groovyldap;


import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.apache.directory.groovyldap.util.Util;


/**
 * A wrapper class which provides LDAP functionality to Groovy.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class LDAP
{
    private static final String DEFAULT_URL = "ldap://localhost:389/";

    private String url;

    private boolean anonymousBind;

    private String bindUser;

    private String bindPassword;


    protected Properties createEnvironment()
    {
        Properties env = new Properties();
        env.setProperty( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.setProperty( Context.PROVIDER_URL, url );
        env.setProperty( Context.OBJECT_FACTORIES, "org.apache.directory.groovyldap.jndi.DirContextToMapObjectFactory" );
        if ( !anonymousBind )
        {
            env.setProperty( Context.SECURITY_PRINCIPAL, bindUser );
            env.setProperty( Context.SECURITY_CREDENTIALS, bindPassword );
        }
        return env;
    }


    protected LDAP()
    {
        this( DEFAULT_URL );
    }


    protected LDAP( String url )
    {
        this.url = url;
        this.anonymousBind = true;
    }


    protected LDAP( String url, String bindUser, String bindPassword )
    {
        this.url = url;
        this.anonymousBind = false;
        this.bindUser = bindUser;
        this.bindPassword = bindPassword;
    }


    /**
     * Creates a new LDAP object with default parameters. It will anonymously connect to localhost on port 389. 
     */
    public static LDAP newInstance()
    {
        return new LDAP();
    }


    public static LDAP newInstance( String url )
    {
        return new LDAP( url );
    }

    public static LDAP newInstance( String url, String bindUser, String bindPassword )
    {
        return new LDAP( url, bindUser, bindPassword );
    }

    /**
     * Search scope ONE (one level)
     */
    public static final SearchScope ONE = SearchScope.ONE;

    /**
     * Search scope SUB (subtree)
     */
    public static final SearchScope SUB = SearchScope.SUB;

    /**
     * Search scope BASE (only the search base itself)
     */
    public static final SearchScope BASE = SearchScope.BASE;


    /**
     * Open an LDAP context and perform a given task within this context.
     *  
     * @param <T>
     * @param action
     * @return
     * @throws NamingException
     */
    protected <T> T performWithContext( WithContext<T> action ) throws NamingException
    {
        LdapContext ctx = null;
        try
        {
            ctx = new InitialLdapContext( createEnvironment(), null );
            return action.perform( ctx );
        }
        catch ( NamingException ne )
        {
            throw ne;
        }
        finally
        {
            try
            {
                if ( ctx != null )
                {
                    ctx.close();
                }
            }
            catch ( Exception e )
            {
                // Ignored
            }
        }
    }


    /**
     * LDAP add operation. Adds a new entry to the directory. The attributes have to be provided as a map.
     * 
     * @param dn DN of the entry
     * @param attributes attributes of the entry
     * @throws NamingException
     */
    public void add( final String dn, final Map<String, Object> attributes ) throws NamingException
    {
        WithContext<Object> action = new WithContext<Object>()
        {
            public Object perform( LdapContext ctx ) throws NamingException
            {
                BasicAttributes attrs = new BasicAttributes();
                for ( String key : attributes.keySet() )
                {
                    Attribute attr = Util.createAttribute( key, attributes.get( key ) );
                    attrs.put( attr );
                }

                ctx.createSubcontext( dn, attrs );
                return null;
            }
        };
        performWithContext( action );
    }


    /**
     * LDAP delete operation. Deletes an entry from the directory.
     * 
     * @param dn DN of the entry
     * @throws NamingException
     */
    public void delete( final String dn ) throws NamingException
    {
        // Check, whether entry exists. If not, throw an exception
        if ( !exists( dn ) )
        {
            throw new NameNotFoundException( "Entry " + dn + " does not exist!" );
        }

        WithContext<Object> action = new WithContext<Object>()
        {
            public Object perform( LdapContext ctx ) throws NamingException
            {
                ctx.destroySubcontext( dn );
                return null;
            }
        };
        performWithContext( action );
    }


    /**
     * Reads an entry by its DN.
     */
    public Object read( final String dn ) throws NamingException
    {
        WithContext<Object> action = new WithContext<Object>()
        {
            public Object perform( LdapContext ctx ) throws NamingException
            {
                return ctx.lookup( dn );
            }
        };
        return performWithContext( action );
    }


    /**
     * Check whether an entry with the given DN exists. The method performs a search to check this, which is more efficient than reading the entry.
     */
    public boolean exists( final String dn ) throws NamingException
    {
        WithContext<Boolean> action = new WithContext<Boolean>()
        {
            public Boolean perform( LdapContext ctx ) throws NamingException
            {
                SearchControls ctls = new SearchControls();
                ctls.setSearchScope( SearchControls.OBJECT_SCOPE );
                ctls.setReturningAttributes( new String[0] );
                ctls.setReturningObjFlag( false );

                try
                {
                    ctx.search( dn, "(objectClass=*)", ctls );
                    return Boolean.TRUE;
                }
                catch ( NameNotFoundException nne )
                {
                }
                return Boolean.FALSE;
            }

        };
        return performWithContext( action );
    }


    /**
     * LDAP compare operation.
     * 
     * @param dn Distinguished name of the entry.
     * @param assertion attribute assertion.
     * @return
     * @throws NamingException
     */
    public boolean compare( final String dn, final Map<String, Object> assertion ) throws NamingException
    {
        if ( assertion.size() != 1 )
        {
            throw new IllegalArgumentException( "Assertion may only include one attribute" );
        }

        WithContext<Boolean> action = new WithContext<Boolean>()
        {
            public Boolean perform( LdapContext ctx ) throws NamingException
            {
                SearchControls ctls = new SearchControls();
                ctls.setReturningAttributes( new String[0] );
                ctls.setSearchScope( SearchControls.OBJECT_SCOPE );
                ctls.setReturningObjFlag( false );

                String attrName = assertion.keySet().iterator().next();
                String filter = "(" + attrName + "={0})";
                Object value = assertion.get( attrName );

                NamingEnumeration<SearchResult> enumeration = ctx.search( dn, filter, new Object[]
                    { value }, ctls );

                return enumeration.hasMore();
            }
        };
        return performWithContext( action );
    }


    /**
     * LDAP modify DN operation.
     * 
     * @param dn
     * @param newRdn
     * @param deleteOldRdn
     * @param newSuperior
     * @throws NamingException
     */
    public void modifyDn( final String dn, final String newRdn, final boolean deleteOldRdn, final String newSuperior )
        throws NamingException
    {
        WithContext<Object> action = new WithContext<Object>()
        {
            public Object perform( LdapContext ctx ) throws NamingException
            {
                LdapName source = new LdapName( dn );
                LdapName target = new LdapName( newSuperior );

                target.add( newRdn );
                ctx.addToEnvironment( "java.naming.ldap.deleteRDN", Boolean.valueOf( deleteOldRdn ).toString() );
                ctx.rename( source, target );

                return null;
            }
        };
        performWithContext( action );
    }


    // search

    // modify

    // modify DN

    // compare

    public void eachEntry( String filter, String base, SearchScope scope, Closure closure ) throws NamingException
    {

        Search search = new Search();
        search.setFilter( filter );
        search.setBase( base );
        search.setScope( scope );

        eachEntry( search, closure );
    }


    public void eachEntry( Map<String, Object> searchParams, Closure closure ) throws NamingException
    {
        Search search = new Search( searchParams );
        eachEntry( search, closure );
    }


    public void eachEntry( Search search, Closure closure ) throws NamingException
    {

        LdapContext ctx = null;
        try
        {
            ctx = new InitialLdapContext( createEnvironment(), null );

            SearchControls ctls = new SearchControls();
            ctls.setSearchScope( search.getScope().getJndiValue() );
            ctls.setReturningAttributes( search.getAttrs() );
            ctls.setReturningObjFlag( true );

            NamingEnumeration<SearchResult> enm = ctx.search( search.getBase(), search.getFilter(), search
                .getFilterArgs(), ctls );
            while ( enm.hasMore() )
            {
                SearchResult sr = enm.next();
                Object obj = sr.getObject();
                closure.call( obj );
            }
        }
        catch ( NamingException ne )
        {
            throw ne;
        }
        finally
        {
            ctx.close();
        }
    }


    public void eachEntry( String filter, Closure closure ) throws NamingException
    {
        eachEntry( filter, "", SearchScope.SUB, closure );
    }


    public void modify( String dn, ModificationType modType, Map<String, Object> attributes ) throws NamingException
    {

        // Create a list of modification item for a JNDI op 
        //
        List<ModificationItem> mods = new ArrayList<ModificationItem>();
        for ( String key : attributes.keySet() )
        {
            Attribute attr = Util.createAttribute( key, attributes.get( key ) );
            ModificationItem item = new ModificationItem( modType.getJndiValue(), attr );
            mods.add( item );
        }
        ModificationItem[] modItems = mods.toArray( new ModificationItem[mods.size()] );

        LdapContext ctx = null;
        try
        {
            ctx = new InitialLdapContext( createEnvironment(), null );
            ctx.modifyAttributes( dn, modItems );
        }
        catch ( NamingException ne )
        {
            throw ne;
        }
        finally
        {
            ctx.close();
        }
    }


    public void modify( String dn, String modType, Map<String, Object> attributes ) throws NamingException
    {
        ModificationType type = ModificationType.valueOf( modType );
        modify( dn, type, attributes );
    }


    public void modify( String dn, List<List> modificationItem ) throws NamingException
    {
        List<ModificationItem> mods = new ArrayList<ModificationItem>();
        for ( List pair : modificationItem )
        {
            if ( pair.size() != 2 )
            {
                throw new IllegalArgumentException( "parameter 2 is not a list of pairs" );
            }

            Object oModType = pair.get( 0 );
            ModificationType modType = null;
            if ( oModType instanceof ModificationType )
            {
                modType = ( ModificationType ) oModType;
            }
            else if ( oModType instanceof String )
            {
                modType = ModificationType.valueOf( ( String ) oModType );
            }
            else
            {
                throw new IllegalArgumentException( "parameter 1 of pair is not o valid ModificationType" );
            }

            Map<String, Object> attributes = ( Map<String, Object> ) pair.get( 1 );
            for ( String key : attributes.keySet() )
            {
                Attribute attr = Util.createAttribute( key, attributes.get( key ) );
                ModificationItem item = new ModificationItem( modType.getJndiValue(), attr );
                mods.add( item );
            }
        }
        ModificationItem[] modItems = mods.toArray( new ModificationItem[mods.size()] );

        LdapContext ctx = null;
        try
        {
            ctx = new InitialLdapContext( createEnvironment(), null );
            ctx.modifyAttributes( dn, modItems );
        }
        catch ( NamingException ne )
        {
            throw ne;
        }
        finally
        {
            ctx.close();
        }
    }


    public List<Object> search( Map<String, Object> searchParams ) throws NamingException
    {
        Search search = new Search( searchParams );
        return this.search( search );
    }


    public List<Object> search( Search search ) throws NamingException
    {

        LdapContext ctx = null;
        List<Object> result = new ArrayList<Object>();
        try
        {
            Properties env = createEnvironment();
            ctx = new InitialLdapContext( env, null );

            SearchControls ctls = new SearchControls();
            ctls.setSearchScope( search.getScope().getJndiValue() );
            ctls.setReturningAttributes( search.getAttrs() );
            ctls.setReturningObjFlag( true );

            NamingEnumeration<SearchResult> enm = ctx.search( search.getBase(), search.getFilter(), search
                .getFilterArgs(), ctls );

            while ( enm.hasMoreElements() )
            {
                SearchResult sr = enm.nextElement();
                result.add( sr.getObject() );
            }
        }
        catch ( NamingException ne )
        {
            throw ne;
        }
        finally
        {
            if (ctx != null) {
                ctx.close();
            }
        }
        return result;
    }
    
    public Object searchUnique( Search search ) throws NamingException
    {
        List<Object> results = this.search( search );
        switch (results.size()) {
            case 0:
                return null;
            case 1:
                return results.get( 0 );
            default:
                throw new NamingException("Result of search is not unique");
        }
    }
    
    public Object searchUnique ( Map<String, Object> searchParams ) throws NamingException
    {
        Search search = new Search( searchParams );
        return this.searchUnique( search );
    }



    public List<Object> search( String filter, String base, SearchScope scope ) throws NamingException
    {
        LdapContext ctx = null;
        List<Object> result = new ArrayList<Object>();
        try
        {
            ctx = new InitialLdapContext( createEnvironment(), null );

            SearchControls ctls = new SearchControls();
            ctls.setSearchScope( scope.getJndiValue() );
            ctls.setReturningObjFlag( true );

            NamingEnumeration<SearchResult> enm = ctx.search( base, filter, ctls );
            while ( enm.hasMoreElements() )
            {
                SearchResult sr = enm.nextElement();
                result.add( sr.getObject() );
            }
        }
        catch ( NamingException ne )
        {
            throw ne;
        }
        finally
        {
            ctx.close();
        }
        return result;
    }
}