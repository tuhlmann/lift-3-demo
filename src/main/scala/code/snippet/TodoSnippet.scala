package code.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.RoundTripInfo
import net.liftweb.json.JsonAST._
import code.model.Todo
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.DefaultFormats
import code.model.User
import net.liftweb.http.js.JE._


object TodoSnippet {

  implicit val formats = DefaultFormats

  /**
   * Roundtrip implementation
   */
  def render(in: NodeSeq): NodeSeq = {

    for (user <- User.currentUser) {

      def doLoad(item: JValue): JValue = decompose(Todo.findAllByUser())

      // Save the item. We get a JSON object and manually decode it
      // If an exception is thrown during the save, the client automatically
      // gets a Failure
      def doSave(item: JValue): JValue = {
        println("doSave Called: "+item)
        (for { todo <- Todo(item) } yield {
          println("Saving this item: "+todo)
          todo.userId(user.id.get).saveMe
          val re = Todo.toJson(todo)
          println("The result we return: "+re)
          re
        }) openOr JNull
      }

      def doRemove(item: JValue): JValue = {
        println("doRemove: "+item)
        Todo(item).flatMap{ todo =>
          println("item to remove: "+todo)
          Todo.delete(todo.id.get).map(Todo.toJson)
        } openOr JNull
      }

      // Associate the server functions with client-side functions
      for (sess <- S.session) {
        val script = SetExp(JsVar("window", "backend"), sess.buildRoundtrip(List[RoundTripInfo](
            "load" -> doLoad _, "save" -> doSave _, "remove" -> doRemove _)))
        S.appendGlobalJs(script)
      }

    }

    in
  }


}

