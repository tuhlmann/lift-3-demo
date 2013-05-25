package code.snippet

import code.lib._

import java.util.Date
import scala.xml.{NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._

import net.liftmodules.extras.Gravatar

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  // replace the contents of the element with id "time" with the date
   def render = {
    "#time *" #> date.map(_.toString) &
    "#avatar *" #> Gravatar.imgTag("test@nowhere.com")
  }
}
