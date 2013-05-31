package code.comet

import net.liftweb.http.ListenerManager
import net.liftweb.actor.LiftActor

case class ChatMessage(from: String, text: String)
case class InitMessages(who: LiftActor)
case class AllMessages(msgs: List[ChatMessage])

object ClientServerActor extends LiftActor with ListenerManager {

  private var messages = List[ChatMessage]()

  protected def createUpdate = ""

  override def lowPriority = {
    case InitMessages(who) => who ! AllMessages(messages)
    case el: ChatMessage =>
      messages = el :: messages
      updateListeners(el)
  }



}