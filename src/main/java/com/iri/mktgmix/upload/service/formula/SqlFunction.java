package com.iri.mktgmix.upload.service.formula;

import java.util.List;


public interface SqlFunction {

    String toSql(List<String> arguments);
}

