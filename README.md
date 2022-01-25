# Hyperpoet

Hyperpoet is a Groovy-friendly HTTP client written in Java 8. It is backed by OkHttp and was inspired from
[httpbuilder](https://github.com/jgritman/httpbuilder) library. 
Its purpose is to perform HTTP requests with the less code possible.

Check out the full doc [here](https://github.com/tambapps/hyperpoet/wiki)


You can also use the printing version to perform beautiful HTTP requests, with [groovysh](https://groovy-lang.org/groovysh.html)

![PrintingHttpPoet](https://github.com/tambapps/hyperpoet/blob/main/examples/printinghttppoet.png?raw=true)

## Example

### Get an url
```groovy
HttpPoet poet = new HttpPoet(url: API_URL, contentType: ContentType.JSON, acceptContentType: ContentType.JSON)
def posts = poet.get("/posts", params: [author: 'someone@gmail.com'])
processPosts(posts)
```

### Post data
```groovy
newPost = [title: 'a new post', author: 'me@gmail.com', body: 'This is new!']
try {
  poet.post("/posts", body: newPost)
} catch (ErrorResponseException e) {
  println "Couldn't create new post!"
}
```

## How to use
The library is in Maven central.

You can import it to your project with Maven

```xml
  <dependency>
    <groupId>com.tambapps.http</groupId>
    <artifactId>hyperpoet</artifactId>
    <version>1.2.1</version>
  </dependency>
```

Or see [this link](https://search.maven.org/artifact/com.tambapps.http/hyperpoet/1.2.1/jar)
for other dependency management tools.
