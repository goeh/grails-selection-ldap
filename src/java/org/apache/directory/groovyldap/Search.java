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


import java.util.Collection;
import java.util.Collections;
import java.util.Map;


/**
 * Contains all parameters for an LDAP search. Filled with default values.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class Search
{
    private String base;

    private SearchScope scope;

    private String filter;

    private Object[] filterArgs;

    private String[] attrs;


    public Search()
    {
        this.base = "";
        this.scope = SearchScope.SUB;
        this.filter = "(objectClass=*)";
        this.filterArgs = null;
        this.attrs = null;
    }


    public Search( Map<String, Object> map )
    {
        this();

        for ( String key : map.keySet() )
        {
            if ( key.equalsIgnoreCase( "base" ) )
            {
                this.setBase( map.get( key ).toString() );
            }
            else if ( key.equalsIgnoreCase( "filter" ) )
            {
                this.setFilter( map.get( key ).toString() );
            }
            else if ( key.equalsIgnoreCase( "scope" ) )
            {
                this.setScope( SearchScope.valueOf( map.get( key ).toString() ) );
            }
            else if ( key.equalsIgnoreCase( "filterArgs" ) )
            {
                Object value = map.get( key );
                Object[] values = null;
                if (value instanceof Object[]) {
                    values = ( Object[] ) value;
                } else if (value instanceof Collection) {
                    Collection c = ( Collection ) value;
                    values = c.toArray();
                } else {
                    values = new Object[] { value };
                }
                this.setFilterArgs( values );
            }
            else
            {
                throw new IllegalArgumentException( "Unknown parameter for search: " + key );
            }
        }
    }


    public String[] getAttrs()
    {
        return attrs;
    }


    public void setAttrs( String[] attrs )
    {
        this.attrs = attrs;
    }


    public String getBase()
    {
        return base;
    }


    public void setBase( String base )
    {
        this.base = base;
    }


    public String getFilter()
    {
        return filter;
    }


    public void setFilter( String filter )
    {
        this.filter = filter;
    }


    public Object[] getFilterArgs()
    {
        return filterArgs;
    }


    public void setFilterArgs( Object[] filterArgs )
    {
        this.filterArgs = filterArgs;
    }


    public SearchScope getScope()
    {
        return scope;
    }


    public void setScope( SearchScope scope )
    {
        this.scope = scope;
    }

}
