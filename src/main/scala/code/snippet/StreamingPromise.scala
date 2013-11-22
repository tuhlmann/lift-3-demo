package code.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.S
import net.liftweb.http.RoundTripInfo
import scala.xml.NodeSeq
import net.liftweb.http.js.JsCmd
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.AjaxContext
import code.lib.util.Languages
import Stream._

object StreamingPromise {

  def render(in: NodeSeq): NodeSeq = {
    // If an exception is thrown during the save, the client automatically
    // gets a Failure
    def doFind(param: String): Stream[String] = {
      val words = Languages.l.filter(_.toLowerCase startsWith param.toLowerCase()).sorted

      from (1) take words.size map (num => {
        Thread.sleep(1000)
        words(num-1)
      }): Stream[String]

    }

    // Associate the server functions with client-side functions
    for (sess <- S.session) {
      val script = JsCrVar("streamingPromise",
          sess.buildRoundtrip(List[RoundTripInfo]("find" -> doFind _)))
      S.appendGlobalJs(script)
    }

    in

  }

}