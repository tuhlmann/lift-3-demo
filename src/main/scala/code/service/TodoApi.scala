package code.service

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction
import java.util.Date
import code.model.Todo
import net.liftweb.mapper.By

object TodoApi extends RestHelper {

  serve ("api" / "todo" prefix {
    case Nil JsonGet _ => anyToJValue(Todo.findAllByUser())
    case Nil JsonPost Todo(item) -> _ => println("Item parsed: "+item); item.save; Todo.toJson(item)
    case Todo(item) :: Nil JsonDelete _ => Todo.delete(item.id).map(Todo.toJson)

  })

}
