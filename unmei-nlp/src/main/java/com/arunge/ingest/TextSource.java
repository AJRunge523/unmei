package com.arunge.ingest;

import java.util.stream.Stream;

import com.arunge.nlp.text.TextDocument;

public interface TextSource  {

    Stream<TextDocument> getDocuments();
    
}
