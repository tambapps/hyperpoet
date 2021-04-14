# Hyperpoet

Hyperpoet is a Groovy-friendly library written in Java 8. It is backed by OkHttp and was inspired from
[httpbuilder](https://github.com/jgritman/httpbuilder) library. 

You can check out the doc [here](https://github.com/tambapps/hyperpoet/wiki)

## Example

```groovy
import com.tambapps.http.hyperpoet.HttpPoet
import com.tambapps.http.hyperpoet.ContentType
import com.tambapps.http.hyperpoet.io.Composers
import com.tambapps.http.hyperpoet.io.Composers

poet = new HttpPoet(url: API_URL, contentType: ContentType.JSON, acceptContentType: ContentType.JSON)
def data = poet.get("/posts/1", query: [author: 'someone@gmail.com'])
processData(data)

newPost = [title: 'a new post', author: 'me@gmail.com', body: 'This is new!']
poet.post("/posts", body: newPost)

postFile = new File("/path/from/post.csv")
poet.post("/posts", body: postFile.text, contentType: new ContentType('text/csv'), composer: Composers.&composeStringBody)
```