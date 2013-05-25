package code.lib.util

import net.liftweb.util.Helpers._
import net.liftweb.common.Box
import net.liftweb.util.CssSel
import net.liftweb.http.S
import scala.xml.NodeSeq
import net.liftweb.http.Templates
import net.liftmodules.extras.SnippetHelper

trait SnippetHelpers extends SnippetHelper {

  def platformTpl(name: String): NodeSeq = S.runTemplate(List("templates-hidden", "platform", name)).openOr(<div>Template not found</div>)
  def assocTpl(name: String): NodeSeq = S.runTemplate(List("templates-hidden", "associations", name)).openOr(<div>Template not found</div>)
  def partTpl(name: String): NodeSeq = S.runTemplate(List("templates-hidden", "parts", name)).openOr(<div>Template not found</div>)
  def formPartTpl(name: String): NodeSeq = S.runTemplate(List("templates-hidden", "parts", "form", name)).openOr(<div>Template not found</div>)

  val notExistent = "#notExistent" #> ""

  def onFull[T](valueBox: Box[T])(selFunc: T => CssSel): CssSel = {
    (for (value <- valueBox) yield {
      selFunc(value)
    }) openOr notExistent
  }

}

object SnippetHelpers extends SnippetHelpers