package code

import java.io.File

import scala.xml.XML

import net.liftweb.common.Full
import net.liftweb.util.Html5

class HtmlSourceSpec extends BaseSpec {

  "HTML Sources" should {

    "be well-formed" in {
      /**
        * Tests to make sure the project's HTML files are well-formed.
        *
        * Finds every *.html and *.xml file in src/main/webapp (and its
        * subdirectories) and tests to make sure they are well-formed.
        */
      var failed: List[File] = Nil

      def handledXml(file: String) = file.endsWith(".xml")

      def handledXHtml(file: String) =
        file.endsWith(".html") || file.endsWith(".htm") || file.endsWith(".xhtml")

      def wellFormed(file: File) {
        if (file.isDirectory)
          for (f <- file.listFiles) wellFormed(f)

        if (file.isFile && handledXml(file.getName)) {
          try {
            XML.loadFile(file)
          } catch {
            case e: org.xml.sax.SAXParseException => failed = file :: failed
          }
        }
        if (file.isFile && handledXHtml(file.getName)) {
          println("Test File "+file.getAbsolutePath())
          Html5.parse(new java.io.FileInputStream(file.getAbsolutePath)) match {
            case Full(_) => // file is ok
            case _ => failed = file :: failed
          }
        }
      }

      wellFormed(new File("src/main/webapp"))

      val numFails = failed.size
      if (numFails > 0) {
        val fileStr = if (numFails == 1) "file" else "files"
        val msg = "Malformed HTML in " + numFails + " " + fileStr + ": " + failed.mkString(", ")
        fail(msg)
      }

      numFails should equal (0)
    }
  }
}
