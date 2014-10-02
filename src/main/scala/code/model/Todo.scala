package code.model

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.mapper._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject
import scala.xml.UnprefixedAttribute

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
      println("IS JObject: "+obj)
      obj match {
        case Failure(msg, Full(ex), _) => ex.printStackTrace() 
        case _ =>
      }
      obj.flatMap(o => if (o.id.get > 0) findById(o.id.get).map(db => copyChanges(db, o)) else Full(o))
    case _ => Empty
  }

  def unapply(in: JValue): Option[OwnerType] = apply(in)

}

object Todo extends Todo with LongKeyedMetaMapper[Todo] with JsonConverter[Todo] {

  def findById(id: Long): Box[Todo] = Todo.find(id)

  def copyChanges(orig: Todo, neo: Todo): Todo = {
    orig.text(neo.text.get).priority(neo.priority.get).done(neo.done.get)
  }

  def findAllByUser(): List[JValue] = {
    val re = ((for (user <- User.currentUser) yield {
      findAll(By(Todo.userId, user.id.get))
    }) openOr Nil) or {
      val re = Todo.create.text("My First Todo") :: Nil
      re
    }
    re.map(toJson)
  }

  override def filterJson(json: JObject): JValue = {
    val toRemove = List("$persisted", "userId", "createdAt", "updatedAt")
    json.filterField { field =>
      (!toRemove.exists { _ == field.name })
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
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object priority extends MappedInt(this) {
    override def defaultValue = 1
    override def displayName = "Priority"
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object dueDate extends MappedDateTime(this) {
    override def defaultValue = 1.day later
    override def displayName = "Due"
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object done extends MappedBoolean(this) {
    override def defaultValue = false
    override def displayName = "Done ?"
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

//  override def save =  {
//    (for (user <- User.currentUser) yield {
//      this.userId(user.id.is)
//      super.save
//    }) openOr false
//  }

}