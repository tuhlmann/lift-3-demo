package code.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.http.ScopedLiftActor
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import code.comet.ClientServerActor
import code.comet.ChatMessage
import code.model.User
import net.liftweb.http.CometListener
import net.liftweb.http.AddAListener
import code.comet.ChatMessage
import code.comet.AllMessages
import code.comet.InitMessages
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.DefaultFormats
import net.liftweb.json
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JE.JsVar
import net.liftmodules.extras.JsExtras
import net.liftweb.http.js.JE.Call
import net.liftmodules.extras.SnippetHelper
import net.liftweb.http.js.JsExp


class ClientServerActorSnippet extends SnippetHelper {

  implicit val formats = DefaultFormats

  def render(in: NodeSeq): NodeSeq = {
    (for {
      sess <- S.session
      user <- User.currentUser
    } yield {
      // get a server-side actor that when we send
      // a JSON serializable object, it will send it to the client
      // and call the named function with the parameter
      val clientProxy = sess.serverActorForClient("window.actorsBridge.messageFromServer")

      // Create a server-side Actor that will receive messages when
      // a function on the client is called
      val serverActor = new ScopedLiftActor {
        override def lowPriority = {

          case JString(str) =>
            ClientServerActor ! ChatMessage(user.username.get, str)

          case msg @ ChatMessage(from, _) if from != user.username.get =>
            clientProxy ! JArray(List(decompose(msg)))

          case AllMessages(msgs) => clientProxy ! decompose(msgs)
        }
      }
      ClientServerActor ! AddAListener(serverActor, {case _ => true})
      ClientServerActor ! InitMessages(serverActor)

      // ALTERNATIVE:
      // You could alternatively define your JS variable like this. Either append the
      // Script to in ++ script or add it like below..
      //Script(JsRaw("var messageToServer = " + sess.clientActorFor(serverActor).toJsCmd).cmd &
      //       JsRaw("function messageFromServer(data) { $(document).trigger('new-chat-msg', data); }").cmd)

      S.appendJs(SetExp(JsVar("window.actorsBridge"),
      JsExtras.CallNew("App.angular.ActorsBridge", sess.clientActorFor(serverActor)) ))

      in
    }) openOr NodeSeq.Empty
  }

}