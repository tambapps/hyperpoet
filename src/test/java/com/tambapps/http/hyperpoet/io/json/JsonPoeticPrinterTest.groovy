package com.tambapps.http.hyperpoet.io.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.tambapps.http.hyperpoet.io.poeticprinter.JsonPoeticPrinter
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test

class JsonPoeticPrinterTest {

    final JsonPoeticPrinter generator = new JsonPoeticPrinter(new JsonGenerator())
    @Test
    void test() {
        def map = [foo: [bar: 1, yes: true], zzz: 'yiiii', n:12345, nil: null, list: [
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true]
        ]]

        generator.printBytes(JsonOutput.toJson(map).bytes)
    }
}
