package code.lib

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.actor.LAFuture
import scala.xml.Text
import scala.xml.Elem

object DataAttributes {

  def adProcessor(str: String, nodes: Elem) = {
    LAFuture.build({
        Thread.sleep(100)
        ("div *+" #> str).apply(
            Text("These very important bits are calculated in parallel and then merged into the page we sent to the client."))
      })
  }

}