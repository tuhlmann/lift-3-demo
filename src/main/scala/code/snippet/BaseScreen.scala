package code
package snippet

import scala.xml._

import net.liftweb._
import common._
import http._
import http.js._
import http.js.JsCmds._
import http.js.JE._
import util.CssSel
import util.Helpers._

/**
  * A screen with some bootstrap settings.
  */
trait Bs3BootstrapScreen extends LiftScreen {
  override val cancelButton = super.cancelButton % ("class" -> "btn btn-default") % ("tabindex" -> "1")
  override val finishButton = super.finishButton % ("class" -> "btn btn-primary") % ("tabindex" -> "1")

  override protected def renderHtml(): NodeSeq = {
    S.appendJs(afterScreenLoad)
    super.renderHtml()
  }

  def displayOnly(fieldName: => String, html: => NodeSeq) =
    new Field {
      type ValueType = String
      override def name = fieldName
      override implicit def manifest = buildIt[String]
      override def default = ""
      override def toForm: Box[NodeSeq] = Full(html)
    }

  protected def afterScreenLoad: JsCmd = JsRaw("""
    |$(".alert-block ul").each(function() {
    |  $(this).closest("div.control-group").addClass("error");
    |});
    """.stripMargin)
}

/*
 * Base all LiftScreens off this. Currently configured to use bootstrap.
 */
abstract class BaseScreen extends Bs3BootstrapScreen {
  override def defaultToAjax_? = true
}

