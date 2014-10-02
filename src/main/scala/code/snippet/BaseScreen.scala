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
import net.liftmodules.extras.Bootstrap3Screen

/*
 * Base all LiftScreens off this. Currently configured to use bootstrap.
 */
abstract class BaseScreen extends Bootstrap3Screen {
  override def defaultToAjax_? = true
  
  protected override lazy val cssClassBinding = new CssClassBinding {
    override val label = "screen-label"
  }

  override val labelSuffix = NodeSeq.Empty
  
}

