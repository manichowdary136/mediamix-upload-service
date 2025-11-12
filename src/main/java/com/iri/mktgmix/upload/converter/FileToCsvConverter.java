package com.iri.mktgmix.upload.converter;

import java.io.IOException;
import java.nio.file.Path;

public interface FileToCsvConverter {

    Path convert(Path inputFile) throws IOException;
}

