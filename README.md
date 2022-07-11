# Hyperpoet

Hyperpoet is a Groovy-friendly HTTP client written in Java 8 backed by OkHttp.


The main goal of this library is to be able to perform HTTP requests with as less code as possible.

For that, several functionalities were implemented

- **Automatic I/O handling** :
No need to open and close Input/OutputStream, the poet does it for you

- **Automatic response body parsing** :
The poet automatically parse the response's data given its `Content-Type` header. You can also explicitly specify it yourself.

- **Automatic request body composing** : 
The client/poet automatically compose the request's body (if any) given a provided content type.


You can also find features such as
- customize error handling
- handle authentication
- get history of requests/responses
- perform requests using Domain Specific Language
- printing request/responses (on a Linux terminal, useful with [groovysh](https://groovy-lang.org/groovysh.html))

Check out the full doc [here](https://github.com/tambapps/hyperpoet/wiki)

## How to use
The library is in Maven central.

You can import it to your project with Maven

```xml
  <dependency>
    <groupId>com.tambapps.http</groupId>
    <artifactId>hyperpoet</artifactId>
    <version>1.3.0</version>
  </dependency>
```


Or Gradle

```groovy
implementation 'com.tambapps.http:hyperpoet:1.3.0'
```


Or see [this link](https://search.maven.org/artifact/com.tambapps.http/hyperpoet/1.3.0/jar)
for other dependency management tools.

## Examples

### Get an url

```groovy
import com.tambapps.http.hyperpoet.HttpHaiku

HttpPoet poet = new HttpPoet(url: API_URL)
def posts = poet.get("/posts", params: [author: 'someone@gmail.com'])
processPosts(posts)
// or if you don't want to instantiate a Poet
def todos = HttpHaiku.get("$API_URL/todos", [author: 'someone@gmail.com'])
```

### Post data
```groovy
HttpPoet poet = new HttpPoet(url: API_URL, contentType: ContentType.JSON)
newPost = [title: 'a new post', author: 'me@gmail.com', body: 'This is new!']
try {
  poet.post("/posts", body: newPost)
} catch (ErrorResponseException e) {
  println "Couldn't create new post!"
}
// or if you don't want to instantiate a Poet
HttpHaiku.post("$API_URL/todos", contentType: ContentType.JSON, body: newPost)
```

### Printing request/response data

![Example](https://github.com/tambapps/hyperpoet/blob/main/examples/example.png?raw=true)


