# Hyperpoet

Hyperpoet is a Groovy-friendly library written in Java 8. It is backed by OkHttp and was inspired from
[httpbuilder](https://github.com/jgritman/httpbuilder) library. 

You can check out the doc [here](https://github.com/tambapps/hyperpoet/wiki)

## Example

```groovy
import com.tambapps.http.hyperpoet.HttpPoet
import com.tambapps.http.hyperpoet.ContentType
import com.tambapps.http.hyperpoet.io.composer.Composers

poet = new HttpPoet(url: API_URL, contentType: ContentType.JSON, acceptContentType: ContentType.JSON)
def data = poet.get("/posts", query: [author: 'someone@gmail.com'])
processData(data)

newPost = [title: 'a new post', author: 'me@gmail.com', body: 'This is new!']
poet.post("/posts", body: newPost)

postFile = new File("/path/from/post.csv")
poet.post("/posts", body: postFile.text, contentType: new ContentType('text/csv'), composer: Composers.&composeStringBody)
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
    <version>v1.0.0</version>
  </dependency>
```