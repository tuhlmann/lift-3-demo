package code.model

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.mapper._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject

trait JsonConverter[OwnerType <: Mapper[OwnerType] with IdPK] extends MetaMapper[OwnerType] {
  self: OwnerType =>

  def filterJson(in: JObject): JValue = in

  def toJson(in: OwnerType): JValue = {
    filterJson(encodeAsJSON_!(in))
  }

  def findById(id: Long): Box[OwnerType]
  def copyChanges(orig: OwnerType, neo: OwnerType): OwnerType

  def apply(json: JValue): Box[OwnerType] = json match {
    case JObject(j) =>
      val obj = tryo { decodeFromJSON_!(j, false) }
      obj.flatMap(o => if (o.id.is > 0) findById(o.id.is).map(db => copyChanges(db, o)) else Full(o))
    case _ => Empty
  }

  def unapply(in: JValue): Option[OwnerType] = apply(in)

}

object Todo extends Todo with LongKeyedMetaMapper[Todo] with JsonConverter[Todo] {

  def findById(id: Long): Box[Todo] = Todo.find(id)

  def copyChanges(orig: Todo, neo: Todo): Todo = {
    orig.text(neo.text.is).priority(neo.priority.is).done(neo.done.is)
  }

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
    (for (ret <- Todo.find(id)) yield {
      ret.delete_!
      ret
    })
  }


}

class Todo private() extends LongKeyedMapper[Todo] with IdPK with UserId[Todo] with CreatedUpdated {
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

//  override def save =  {
//    (for (user <- User.currentUser) yield {
//      this.userId(user.id.is)
//      super.save
//    }) openOr false
//  }

}