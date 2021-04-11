# Getpack

Getpack is a Groovy-friendly library written in Java 8. It is backed by OkHttp and was inspired from
[httpbuilder](https://github.com/jgritman/httpbuilder) library. 

You can check out the doc [here](https://github.com/tambapps/getpack/wiki)

## Example

```groovy
import com.tambapps.http.getpack.ContentType
import com.tambapps.http.getpack.io.Encoders

client = new GetpackClient(url: API_URL, contentType: ContentType.JSON, acceptContentType: ContentType.JSON)
def data = client.get("/posts/1", query: [author: 'someone@gmail.com'])
processData(data)

newPost = [title: 'a new post', author: 'me@gmail.com', body: 'This is new!']
client.post("/posts", body: newPost)

postFile = new File("/path/from/post.csv")
client.post("/posts", body: postFile.text, contentType: new ContentType('text/csv'), encoder: Encoders.&encodeStringBody)
```