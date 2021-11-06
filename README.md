# Hyperpoet

Hyperpoet is a Groovy-friendly HTTP client written in Java 8. It is backed by OkHttp and was inspired from
[httpbuilder](https://github.com/jgritman/httpbuilder) library. 
Its purpose is to perform HTTP requests with the less code possible. 


You can check out the full doc [here](https://github.com/tambapps/hyperpoet/wiki)

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
This library is [hosted on Jitpack](https://jitpack.io/#tambapps/hyperpoet/1.0.0)

You can import it to your project by adding the Jitpack repository
```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
```

And then add the dependency
```xml
  <dependency>
    <groupId>com.github.tambapps</groupId>
    <artifactId>hyperpoet</artifactId>
    <version>v1.1.0</version>
  </dependency>
```