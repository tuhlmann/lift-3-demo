package code.model

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.mapper._
import net.liftmodules.mapperauth.model.share.MapperWithId
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject

trait JsonConverter[OwnerType <: Mapper[OwnerType]] extends MetaMapper[OwnerType] {
  self: OwnerType =>

  def filterJson(in: JObject): JValue = in

  def toJson(in: OwnerType): JValue = {
    filterJson(encodeAsJSON_!(in))
  }

  def apply(json: JValue): Box[OwnerType] = json match {
    case JObject(j) => tryo { decodeFromJSON_!(j, false) }
    case _ => Empty
  }

  def unapply(in: JValue): Option[OwnerType] = apply(in)

}

object Todo extends Todo with LongKeyedMetaMapper[Todo] with JsonConverter[Todo] {

  def findAllByUser(): List[JValue] = {
    val re = ((for (user <- User.currentUser) yield {
      findAll(By(Todo.userId, user.id.is))
    }) openOr Nil) or {
      val re = Todo.create.text("My First Todo") :: Nil
      re
    }
    re.map(toJson)
  }

  override def filterJson(json: JObject): JValue = {
    json.transform {
      case JField("$persisted", _) | JField("userId", _) | JField("createdAt", _) | JField("updatedAt", _)=> JNothing
    }
  }

  /**
   * Deletes the item with id and returns the
   * deleted item or Empty if there's no match
   */
  def delete(id: Long): Box[Todo] = {
    println("XX In Delete")
    (for (ret <- Todo.find(id)) yield {
      println("XX Item found")
      ret.delete_!
      ret
    })
  }


}

class Todo private() extends MapperWithId[Todo] with UserId[Todo] with CreatedUpdated {
  def getSingleton = Todo

  object text extends MappedString(this, 1000) {
    override def validations = valMinLen(1, "Text empty.") _ :: super.validations
    override def displayName = "Text"
  }

  object priority extends MappedInt(this) {
    override def defaultValue = 1
    override def displayName = "Priority"
  }

  object dueDate extends MappedDateTime(this) {
    override def defaultValue = 1.day later
    override def displayName = "Due"
  }

  object done extends MappedBoolean(this) {
    override def defaultValue = false
    override def displayName = "Done ?"
  }

  override def save =  {
    (for (user <- User.currentUser) yield {
      this.userId(user.id.is)
      super.save
    }) openOr false
  }

}