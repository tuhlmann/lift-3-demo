package code.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds._
import scala.xml.Text
import net.liftweb.util.MarkdownParser

class Markdown {

  def parse = {
    var txt = ""

    def process() = {
      val result = MarkdownParser.parse(txt).openOr(Text("Could not parse Text"))
      SetHtml("markdoown-preview", result)
    }

    "@markdown-text" #> textarea(txt, txt = _) &
    "@submit *+" #> hidden(process)

  }

}