package com.iri.mktgmix.upload.service.formula;

import java.util.List;

/**
 * Strategy abstraction for SQL function translation.
 */
public interface SqlFunction {

    /**
     * @param arguments expressions already rendered as SQL fragments
     * @return SQL fragment representing invocation of this function
     */
    String toSql(List<String> arguments);
}

