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
import net.liftweb.util.Schedule

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
      // Please ignore the commented lines.
      // They were created in a hacking session with David for code going into Lift.
      // I leave them in as a reminder.
      // is applied only once, so if already computed doesn't do anything.
      // Todo: create def for the error responses so its overridable
//      Schedule.apply(() => reply.apply(NotFoundResponse("timeout")), asyncTimeout)
//      Schedule.apply(() => in.fail(ParamFailure("timed out", Empty, Empty, 408)), asyncTimeout)
      in.onSuccess(t => reply.apply(c(t)))
      in.onFail {
//        case ParamFailure(msg, _, _, code: Int) => // ...
//        case ParamFailure(msg, _, _, resp: LiftResponse) => reply(resp)
        case Failure(msg, _, _) => reply.apply(NotFoundResponse(msg))
        case _                  => reply.apply(NotFoundResponse("Error"))
      }
    })
  }

  def asyncTimeout = 110 seconds

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

    val u = url("https://en.wikipedia.org/w/api.php") <<? Map("action" -> "opensearch", "limit" -> "10", "search" -> query)
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
