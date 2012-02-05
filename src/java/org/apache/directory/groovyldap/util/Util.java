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
package org.apache.directory.groovyldap.util;


import java.util.Collection;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;


/**
 * Utility methods.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class Util
{
    private Util()
    {

    }


    /** 
     * Creates an attribute from the given parameters. If value is a collection, a multi-valued attribute will be created.
     * 
     * @param name Name of the attribute
     * @param value 
     * @return a JNDI attribute object
     */
    public static Attribute createAttribute( String name, Object value )
    {
        Attribute attr = new BasicAttribute( name );
        if ( value instanceof Collection )
        {
            Collection values = ( Collection ) value;
            for ( Object val : values )
            {
                attr.add( val );
            }
        }
        else
        {
            attr.add( value );
        }
        return attr;
    }
}
