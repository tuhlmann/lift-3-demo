package code.service

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import code.model.Todo
import net.liftweb.json.Extraction.decompose

/**
 * A REST implementation that corresponds to Angular's Resource REST module
 * It's not used because we can now use the Roundtrip Promises from Lift 3
 * but this is how it would look like if you need to use REST.
 */
object TodoApi extends RestHelper with Loggable {

  serve ("api" / "todo" prefix {

    case Nil JsonGet _ =>
      decompose(Todo.findAllByUser())

    case Nil JsonPost Todo(item) -> _ =>
      logger.info("Item parsed: "+item);
      item.save;
      Todo.toJson(item)

    case Todo(item) :: Nil JsonDelete _ => Todo.delete(item.id.get).map(Todo.toJson)

  })

}
