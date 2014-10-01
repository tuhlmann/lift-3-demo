package code.snippet

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import code.BaseSpec
import code.lib.DependencyFactory

class HelloWorldSpec extends BaseSpec {
  val session = new LiftSession("", randomString(20), Empty)
  val stableTime = now

  override def withFixture(test: NoArgTest) = {
    S.initIfUninitted(session) {
      DependencyFactory.time.doWith(stableTime) {
        test()
      }
    }
  }

  "HelloWorld Snippet" should {
    "Put the time in the node" in {
      val hello = new HelloWorld
      Thread.sleep(1000) // make sure the time changes

      val str = hello.render(<span>Welcome to your Lift app at <span id="time">Time goes here</span></span>).toString

      str.indexOf(stableTime.toString) should be >= 0
      str.indexOf("Welcome to your Lift app at") should be >= 0
    }
  }
}
