package code.config

import net.liftweb.common.Loggable
import net.liftweb.db.DB
import net.liftweb.db.StandardDBVendor
import net.liftweb.util.Props
import net.liftweb.http.LiftRules
import net.liftweb.util.DefaultConnectionIdentifier
import net.liftweb.mapper.Schemifier
import net.liftweb.http.S
import code.model.User
import net.liftmodules.mapperauth.model.ExtSession
import net.liftmodules.mapperauth.model.Permission
import net.liftmodules.mapperauth.model.Role
import code.model.Todo

object MapperConfig extends Loggable {

  def init() {

    // Set up a database connection
    // We check first if the connection information is available over JNDI,
    // if not we read the settings from a property file (default.props depending on the RunMode)
    // or, if not settings are found we fall back to the H2 database
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
                   Props.get("db.url") openOr "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
                   Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Mapper is Lift's own persistence framework targeted towards relational databases
    // You don't have to use Mapper, you can use JPA or something different.
    // But Mapper provides a nice way to automatically detect changes in you
    // model classes and can transform the appropriate actions in the database model
    Schemifier.schemify(true, Schemifier.infoF _, Permission, Role, User, ExtSession, Todo)

//    Schemifier.schemify(true, Schemifier.infoF _, Association, Garden, Member, Invoice)

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)

  }

}