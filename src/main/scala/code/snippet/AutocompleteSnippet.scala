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

object AutocompleteSnippet {

 val languages = List(
    "C", "C++", "Clojure", "CoffeeScript", "Java", "JavaScript", "Jython", "Lua",
    "POP-11", "Prolog", "Python", "Processing", "Groovy", "Ruby", "Haskell", "Kotlin",
    "Scala", "Scheme", "Smalltalk", "SuperCollider", "Dart", "Scheme", "Lisp"
  )

  def callback(in: NodeSeq): NodeSeq = {

    def findSuggestions(): JsCmd = {
      def search2(jvalue: JValue): JValue = {
        val JString(term) = jvalue \ "term"
        val words = AutocompleteSnippet.languages.filter(_.toLowerCase startsWith term.toLowerCase())
        words.sorted
      }
      Function("findSuggestions_callback", List("term", "callback"),
        SHtml.jsonCall(JsRaw("""{'term': term}"""), AjaxContext.json(Full("callback")), search2 _)._2)
    }

    in ++ Script(findSuggestions)
  }


  /**
   * Roundtrip implementation
   */
  def roundTrip(in: NodeSeq): NodeSeq = {

    // If an exception is thrown during the save, the client automatically
    // gets a Failure
    def doFind(param: String): JValue = {
      val words = AutocompleteSnippet.languages.filter(_.toLowerCase startsWith param.toLowerCase())
      words.sorted
    }

    // Associate the server functions with client-side functions
    val script = (for (sess <- S.session) yield {
      Script(
        JsCrVar("findSuggestions", sess.buildRoundtrip(List[RoundTripInfo](
        "find" -> doFind _))))

      }) openOr (Script(Noop))

    in ++ script
  }


  /**
   * Roundtrip for Typeahead
   */
  def typeAheadRoundTrip(in: NodeSeq): NodeSeq = {
    // If an exception is thrown during the save, the client automatically
    // gets a Failure
    def doFind(param: String): JValue =
      languages.filter(_.toLowerCase startsWith param.toLowerCase()).sorted


    // Associate the server functions with client-side functions
    val script = (for (sess <- S.session) yield {
      Script(
        JsCrVar("findSuggestions_typeahead", sess.buildRoundtrip(List[RoundTripInfo](
        "find" -> doFind _))))

      }) openOr (Script(Noop))

    in ++ script
  }

}