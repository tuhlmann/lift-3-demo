package code.service

import net.liftweb.http.rest.RestHelper
import net.liftweb.actor.LAFuture
import net.liftweb.util.Helpers._
import dispatch._
import dispatch.Defaults._
import net.liftweb.json._
import net.liftweb.http.LiftResponse
import net.liftweb.common._
import net.liftweb.http.rest.RestContinuation
import net.liftweb.http.NotFoundResponse

case class QueryResult(suggestion: String, link: String)

object FuturesRest extends RestHelper {
  serve {
    case "search" :: query :: Nil Get _ => futureToResponse2(searchWikipedia(query))
    case "delay" :: Nil Get _ =>
      LAFuture(() => {
        Thread.sleep(2000)
        <b>Waiting for 2 seconds</b>
      })
  }

  /**
   * If we're returning a future, then automatically turn the request into an Async request
   * @param in the LAFuture of the response type
   * @param c the implicit conversion from T to LiftResponse
   * @tparam T the type
   * @return Nothing
   */
  protected def futureToResponse2[T](in: LAFuture[T])(implicit c: T => LiftResponse): () => Box[LiftResponse] = () => {
    RestContinuation.async(reply => {
      in.onSuccess(t => reply.apply(c(t)))
      in.onFail {
        case Failure(msg, _, _) => reply.apply(NotFoundResponse(msg))
        case _                  => reply.apply(NotFoundResponse("Error"))
      }
    })
  }

  implicit def scalaFutureToLaFuture[T](scf: Future[T])(implicit m: Manifest[T]): LAFuture[T] = {
    val laf = new LAFuture[T]
    scf.onSuccess {
      case v: T => laf.satisfy(v)
      case _ => laf.abort
    }
    scf.onFailure {
      case e: Throwable => laf.fail(Failure(e.getMessage(), Full(e), Empty))
    }
    laf
  }

  def toJson[T](t: T): JValue = Extraction.decompose(t)

  def searchWikipedia(query: String): LAFuture[JValue] = {

    def space2us(s: String) = s.replaceAll("\\s", "_")

    val u = url("http://en.wikipedia.org/w/api.php") <<? Map("action" -> "opensearch", "limit" -> "100", "search" -> query)
    for (str <- Http(u OK as.String)) yield {
      val suggestions = for (JArray(re) <- parse(str)(1); JString(s) <- re) yield s
      val re = suggestions.map { r =>
        val url = s"http://en.wikipedia.org/wiki/${space2us(r)}"
        QueryResult(r, url)
      }.take(5)
      toJson(re)
    }

  }

}
