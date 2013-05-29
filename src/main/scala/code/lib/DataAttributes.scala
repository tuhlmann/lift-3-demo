package code.lib

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.actor.LAFuture
import scala.xml.Text
import scala.xml.Elem

object DataAttributes {

  def adProcessor(str: String, nodes: Elem) = {
    LAFuture.build({
        //Thread.sleep(5000)
        ("div *+" #> str).apply(Text("This currently doesn't work right, but when it does, this text is asynchroneously pushed to the page when its ready..."))
      })
  }

}