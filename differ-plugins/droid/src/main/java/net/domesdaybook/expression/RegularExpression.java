/*
 * Copyright Matt Palmer 2009-2011, All rights reserved.
 *
 */

package net.domesdaybook.expression;

import net.domesdaybook.reader.ByteReader;

/**
 *
 * @author Matt Palmer
 */
public class RegularExpression implements Expression {

    public MatchResults matchForwards(ByteReader reader, long fromPosition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MatchResults matchBackwards(ByteReader reader, long fromPosition ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
