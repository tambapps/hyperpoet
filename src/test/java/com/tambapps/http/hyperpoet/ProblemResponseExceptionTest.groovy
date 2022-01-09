package com.tambapps.http.hyperpoet

import groovy.json.JsonOutput
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

class ProblemResponseExceptionTest {
  private HttpPoet poet = new HttpPoet(url: "https://jsonplaceholder.typicode.com",
      contentType: ContentType.JSON, acceptContentType: ContentType.JSON).tap {
    errorResponseHandler = ErrorResponseHandlers.problemResponseHandler()
  }

  @Test
  void testCatchProblem() {
    def problem = [type: '/errors/type', title: 'Bad Request', status: 400,
                   detail: 'You were bad', instance: '/instance/0', additionalProperty: 1]
    // simulate error
    poet.addInterceptor {
      return new Response.Builder()
      .code(400)
      .request(it.request())
      .protocol(Protocol.HTTP_1_0)
      .message("Bad Request")
      .body(ResponseBody.create(JsonOutput.toJson(problem).bytes, ContentType.JSON.toMediaType()))
      .build()
    }
    def exception = assertThrows(ProblemResponseException.class) {
      poet.get("/something")
    }
    assertEquals(problem.type, exception.type)
    assertEquals(problem.title, exception.title)
    assertEquals(problem.detail, exception.detail)
    assertEquals(problem.instance, exception.instance)
    assertEquals(problem.status, exception.status)
    assertEquals(problem.additionalProperty, exception['additionalProperty'])
    assertEquals(problem.title, exception['title'])
    assertEquals(problem.additionalProperty, exception.additionalProperty)
  }
}
