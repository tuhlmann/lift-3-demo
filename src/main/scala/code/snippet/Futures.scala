package code.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.actor.LAFuture
import scala.xml.Text

object Futures {

  def delay = {
    Thread.sleep(100)
    "@long-running-calculation *" #> Text("Phew. It took a while to collect all these bits...")
  }

}