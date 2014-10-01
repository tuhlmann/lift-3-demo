package code

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import net.liftweb._
import net.liftweb.db.DB
import net.liftweb.db.SuperConnection
import org.scalatest.BeforeAndAfterAllConfigMap
import org.scalatest.ConfigMap

/**
  * Creates a Mongo instance named after the class.
  * Therefore, each Spec class shares the same database.
  * Database is dropped after.
  */
trait MapperTestKit extends BeforeAndAfterAllConfigMap {
  this: WordSpec =>

  def dbName = "test_"+this.getClass.getName
    .replace(".", "_")
    .toLowerCase

  //def defaultServer = new ServerAddress("127.0.0.1", 27017)

  // If you need more than one db, override this
  //def dbs: List[(MongoIdentifier, ServerAddress, String)] = List((DefaultMongoIdentifier, defaultServer, dbName))

  def debug = false

  override def beforeAll(configMap: ConfigMap) {
    // define the dbs
//    dbs foreach { case (id, srvr, name) =>
//      MongoDB.defineDb(id, new Mongo(srvr), name)
//    }
  }

  override def afterAll(configMap: ConfigMap) {
    if (!debug) {
      // drop the databases
//      dbs foreach { case (id, _, _) =>
//        MongoDB.use(id) { db => db.dropDatabase }
//      }
    }

    // clear the mongo instances
    //MongoDB.close
    for (sc <- DB.currentConnection) {
      sc.connection.close()
    }
  }
}
