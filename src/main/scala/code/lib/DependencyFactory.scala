package code.lib

import java.util.Date
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.Factory


/**
 * A factory for generating new instances of Date.  You can create
 * factories for each kind of thing you want to vend in your application.
 * An example is a payment gateway.  You can change the default implementation,
 * or override the default implementation on a session, request or current call
 * stack basis.
 */
object DependencyFactory extends Factory {
  implicit object time extends FactoryMaker(now _)

  /**
   * objects in Scala are lazily created.  The init()
   * method creates a List of all the objects.  This
   * results in all the objects getting initialized and
   * registering their types with the dependency injector
   */
  private def init() {
    List(time)
  }
  init()
}

/*
  def changeSessionImplementation() {
    DependencyFactory.time.session.set(() => new Date())
  }

  def changeRequestImplementation() {
    DependencyFactory.time.request.set(() => new Date())
  }

  def changeJustForCall(d: Date) {
    DependencyFactory.time.doWith(d) {
      // perform some calculations here
    }
  }
}
*/
