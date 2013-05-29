package code.snippet

import scala.xml.NodeSeq
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props
import scala.xml.Unparsed
import net.liftweb.http.LiftRules
import net.liftweb.http.S

object ProductionOnly {
  def render(in: NodeSeq): NodeSeq = if (Props.productionMode) in else NodeSeq.Empty
}

object NotInProduction {
  def render(in: NodeSeq): NodeSeq = if (Props.productionMode) NodeSeq.Empty else in
}

/**
 * Use this to generate script tags depending on the run mode.
 * With sbt-closure for instance you use a script.jsm file which contains the
 * different JavaScript files that should be combined.
 * That script lets you use the minified version in production and the single files in
 * development mode, sourced from the same file:
 * &lt;script lift="JavaScriptLoader?prod=/gen/script.js;dev=/js/script.jsm;pf=/js/"&gt;&lt;/script&gt;
 */
object JavaScriptLoader {

  def findFiles(path: String): List[String] = {
    (for (fileList <- LiftRules.loadResourceAsString(path)) yield {
      fileList.split("\\r?\\n").toList.flatMap{ line =>
        if ((!line.trim.isEmpty()) && (!line.trim.startsWith("#"))) {
          Full(line)
        } else Empty
      }
    }) openOr Nil
  }

  def adaptPath(path: String): String = S.attr("pf").map(_ + path) openOr path

  def render(in: NodeSeq): NodeSeq = {
    Props.mode match {
      case Props.RunModes.Production =>
        (for (p <- S.attr("prod")) yield {
          <script src={p} lift="with-resource-id"></script>
        }) openOr in
      case _ =>
        (for (p <- S.attr("dev")) yield {
          if (p.endsWith(".jsm")) {
            findFiles(p).map { file =>
              <script src={adaptPath(file)}></script>
            }
          } else {
            <script src={p} lift="with-resource-id"></script>
          }
        }) openOr in
    }
  }

}


object Html5Shim {
  def render = Unparsed("""
<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
<!--[if lt IE 7]> <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js lt-ie9" lang="en"> <![endif]-->
<!-- Consider adding a manifest.appcache: h5bp.com/d/Offline -->
<!--[if gt IE 8]><!--><html class="no-js" lang="en"> <!--<![endif]-->
<!--[if lt IE 9]>
<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->""")
}