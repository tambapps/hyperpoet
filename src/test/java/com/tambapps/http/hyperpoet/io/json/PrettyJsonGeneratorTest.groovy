package com.tambapps.http.hyperpoet.io.json

import org.junit.jupiter.api.Test

class PrettyJsonGeneratorTest {

    final PrettyJsonGenerator generator = new PrettyJsonGenerator()
    @Test
    void test() {
        def map = [foo: [bar: 1, yes: true], zzz: 'yiiii', n:12345, nil: null, list: [
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true],
                [a: 1, b: "2", c: true]
        ]]

        println(generator.toJson(map))
    }
}
