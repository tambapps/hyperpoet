package com.tambapps.http.hyperpoet.url;

/**
 * Enum representing the way to handle query param list,sets
 */
public enum MultivaluedQueryParamComposingType {
    /**
     * Use brackets and separate elements with a comma
     */
    BRACKETS,
    /**
     * separate elements with a comma
     */
    COMMA,
    /**
     * Repeat the parameter for each element of the list
     */
    REPEAT
}