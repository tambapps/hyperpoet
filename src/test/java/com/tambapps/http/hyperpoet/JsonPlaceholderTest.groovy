package com.tambapps.http.hyperpoet

import com.tambapps.http.garcon.Garcon
import com.tambapps.http.garcon.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

abstract class JsonPlaceholderTest {

  private static final String HOST = "localhost"
  private static final int PORT = 8082
  public static final String PLACEHOLDER_API_URL = "http://$HOST:$PORT"

  private static final List TODOS = [
      [
        userId: 1,
        id: 1,
        title: "delectus aut autem",
        completed: false
      ],
      [
        userId: 1,
        id: 2,
        title: "quis ut nam facilis et officia qui",
        completed: false
      ],
      [
        userId: 1,
        id: 3,
        title: "fugiat veniam minus",
        completed: false
      ],
      [
        userId: 1,
        id: 4,
        title: "et porro tempora",
        completed: true
      ],
      [
        userId: 1,
        id: 5,
        title: "laboriosam mollitia et enim quasi adipisci quia provident illum",
        completed: false
      ],
      [
        userId: 1,
        id: 6,
        title: "qui ullam ratione quibusdam voluptatem quia omnis",
        completed: false
      ],
      [
        userId: 1,
        id: 7,
        title: "illo expedita consequatur quia in",
        completed: false
      ],
      [
        userId: 1,
        id: 8,
        title: "quo adipisci enim quam ut ab",
        completed: true
      ],
      [
        userId: 1,
        id: 9,
        title: "molestiae perspiciatis ipsa",
        completed: false
      ],
      [
        userId: 1,
        id: 10,
        title: "illo est ratione doloremque quia maiores aut",
        completed: true
      ]
  ]

  private static final List POSTS = [
      [
        userId: 1,
        id: 1,
        title: "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
        body: "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
      ],
      [
        userId: 1,
        id: 2,
        title: "qui est esse",
        body: "est rerum tempore vitae\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\nqui aperiam non debitis possimus qui neque nisi nulla"
      ],
      [
        userId: 1,
        id: 3,
        title: "ea molestias quasi exercitationem repellat qui ipsa sit aut",
        body: "et iusto sed quo iure\nvoluptatem occaecati omnis eligendi aut ad\nvoluptatem doloribus vel accusantium quis pariatur\nmolestiae porro eius odio et labore et velit aut"
      ],
      [
        userId: 1,
        id: 4,
        title: "eum et est occaecati",
        body: "ullam et saepe reiciendis voluptatem adipisci\nsit amet autem assumenda provident rerum culpa\nquis hic commodi nesciunt rem tenetur doloremque ipsam iure\nquis sunt voluptatem rerum illo velit"
      ],
      [
        userId: 1,
        id: 5,
        title: "nesciunt quas odio",
        body: "repudiandae veniam quaerat sunt sed\nalias aut fugiat sit autem sed est\nvoluptatem omnis possimus esse voluptatibus quis\nest aut tenetur dolor neque"
      ],
      [
        userId: 1,
        id: 6,
        title: "dolorem eum magni eos aperiam quia",
        body: "ut aspernatur corporis harum nihil quis provident sequi\nmollitia nobis aliquid molestiae\nperspiciatis et ea nemo ab reprehenderit accusantium quas\nvoluptate dolores velit et doloremque molestiae"
      ],
      [
        userId: 1,
        id: 7,
        title: "magnam facilis autem",
        body: "dolore placeat quibusdam ea quo vitae\nmagni quis enim qui quis quo nemo aut saepe\nquidem repellat excepturi ut quia\nsunt ut sequi eos ea sed quas"
      ],
      [
        userId: 1,
        id: 8,
        title: "dolorem dolore est ipsam",
        body: "dignissimos aperiam dolorem qui eum\nfacilis quibusdam animi sint suscipit qui sint possimus cum\nquaerat magni maiores excepturi\nipsam ut commodi dolor voluptatum modi aut vitae"
      ],
      [
        userId: 1,
        id: 9,
        title: "nesciunt iure omnis dolorem tempora et accusantium",
        body: "consectetur animi nesciunt iure dolore\nenim quia ad\nveniam autem ut quam aut nobis\net est aut quod aut provident voluptas autem voluptas"
      ],
      [
        userId: 1,
        id: 10,
        title: "optio molestias id quia eum",
        body: "quo et expedita modi cum officia vel magni\ndoloribus qui repudiandae\nvero nisi sit\nquos veniam quod sed accusamus veritatis error"
      ]
  ]

  private static final Garcon GARCON = new Garcon(HOST, PORT).define {
    contentType = com.tambapps.http.garcon.ContentType.JSON
    accept = com.tambapps.http.garcon.ContentType.JSON

    get "/todos", {
      return TODOS.collect()
    }
    post "/todos", {
      def newTodo = parsedRequestBody
      newTodo.id = TODOS.size() + 1
      TODOS.add(newTodo)
      return newTodo
    }
    get '/todos/{id}', {
      def id = pathVariables.id as int
      def todo = TODOS.find { it.id == id } ?: [:]
      if (!todo) {
        response.statusCode = HttpStatus.NOT_FOUND
      }
      return todo
    }
    patch '/todos/{id}', {
      def id = pathVariables.id as int
      def todo = TODOS.find { it.id == id }
      if (!todo) {
        response.statusCode = HttpStatus.NOT_FOUND
        return [:]
      }
      def body = parsedRequestBody
      if (body.userId) todo.userId = body.userId
      if (body.title) todo.title = body.title
      if (body.completed) todo.completed = body.completed
      return todo
    }
    delete '/todos/{id}', {
      def id = pathVariables.id as int
      TODOS.removeIf { it.id == id }
      return null
    }

    get "/posts", {
      return POSTS.collect()
    }
    post "/posts", {
      def newPost = parsedRequestBody
      newPost.id = POSTS.size() + 1
      POSTS.add(newPost)
      return newPost
    }
    get '/posts/{id}', {
      def id = pathVariables.id as int
      def post = POSTS.find { it.id == id } ?: [:]
      if (!post) {
        response.statusCode = HttpStatus.NOT_FOUND
      }
      return post
    }
    patch '/posts/{id}', {
      def id = pathVariables.id as int
      def post = POSTS.find { it.id == id }
      if (!post) {
        response.statusCode = HttpStatus.NOT_FOUND
        return [:]
      }
      def body = parsedRequestBody
      if (body.userId) post.userId = body.userId
      if (body.title) post.title = body.title
      if (body.body) post.body = body.body
      return post
    }
    delete '/posts/{id}', {
      def id = pathVariables.id as int
      POSTS.removeIf { it.id == id }
      return null
    }

    get 'page.html', contentType: com.tambapps.http.garcon.ContentType.HTML, {
      $/
<html></html>
/$
    }
  }
  @BeforeAll
  static void initServer() {
    GARCON.start()
  }


  @AfterAll
  static void disposeServer() {
    GARCON.stop()
  }
}
