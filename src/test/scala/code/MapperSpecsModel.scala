package code

import net.liftweb.util.ConnectionIdentifier
import net.liftweb.util.StringHelpers
import net.liftweb.mapper.MapperRules
import net.liftweb.mapper.BaseMapper
import java.util.Locale
import net.liftweb.mapper.Schemifier
import net.liftweb.util.DefaultConnectionIdentifier
import net.liftmodules.mapperauth.model.Permission
import net.liftmodules.mapperauth.model.Role
import code.model.User

/*
 * This file contains a number of objects that are common to several
 * of the Mapper specs. By placing them here we reduce code duplication
 * and get rid of some timing errors found when we moved to SBT for build.
 *
 * Derek Chen-Becker, Mar 8, 2011
 */

object MapperSpecsModel {
  // These rules are common to all Mapper specs
  def snakify(connid: ConnectionIdentifier, name: String): String = {
    if (connid.jndiName == "snake") {
      StringHelpers.snakify(name)
    } else {
      name.toLowerCase
    }
  }

  MapperRules.columnName = snakify
  MapperRules.tableName = snakify

  // Simple name calculator
  def displayNameCalculator(bm: BaseMapper, l: Locale, name: String) = {
    val mapperName = bm.dbName
    val displayName = name match {
      case "firstName" if l == Locale.getDefault()    => "DEFAULT:" + mapperName + "." + name
      case "firstName" if l == new Locale("da", "DK") => "da_DK:" + mapperName + "." + name
      case _                                          => name
    }
    displayName
  }

  MapperRules.displayNameCalculator.default.set(displayNameCalculator _)

  def setup() {
    // For now, do nothing. Just force this object to load
  }

  def doLog = false

  private def ignoreLogger(f: => AnyRef): Unit = ()

  def cleanup() {
    // Snake connection doesn't create FK constraints (put this here to be absolutely sure it gets set before Schemify)
    MapperRules.createForeignKeys_? = c => {
      c.jndiName != "snake"
    }

    Schemifier.destroyTables_!!(DefaultConnectionIdentifier, if (doLog) Schemifier.infoF _ else ignoreLogger _)
    Schemifier.destroyTables_!!(DbProviders.SnakeConnectionIdentifier, if (doLog) Schemifier.infoF _ else ignoreLogger _, User, Permission, Role)
    Schemifier.schemify(true, if (doLog) Schemifier.infoF _ else ignoreLogger _, DefaultConnectionIdentifier)
    Schemifier.schemify(true, if (doLog) Schemifier.infoF _ else ignoreLogger _, DbProviders.SnakeConnectionIdentifier, User, Permission, Role)
  }
}
