package code.model

import org.joda.time.DateTime
import net.liftweb._
import net.liftweb.common._
import net.liftweb.util.FieldContainer
import net.liftweb.util.Mailer.From
import net.liftweb.util.Mailer.PlainMailBodyType
import net.liftweb.util.Mailer.Subject
import net.liftweb.util.Mailer.To
import net.liftweb.util.Mailer.sendMail
import net.liftweb.http.LiftResponse
import net.liftweb.http.RedirectWithState
import net.liftweb.http.RedirectState
import net.liftweb.http.Req
import net.liftweb.http.SessionVar
import net.liftweb.http.S
import net.liftweb.http.RedirectResponse
import net.liftweb.mapper.By
import net.liftmodules.mapperauth.ProtoAuthUser
import net.liftweb.mapper.MappedLocale
import net.liftweb.mapper.MappedTimeZone
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedTextarea
import net.liftweb.mapper.MappedLongForeignKey
import net.liftmodules.mapperauth.ProtoAuthUserMeta
import net.liftmodules.mapperauth.MapperAuth
import net.liftweb.mapper.Mapper
import net.liftmodules.mapperauth.model.LoginToken
import net.liftmodules.mapperauth.model.ExtSession
import scala.xml.UnprefixedAttribute


/**
 * A trait to mix in with table definitions that need a user reference
 */
trait UserId[OwnerType <: UserId[OwnerType]] extends Mapper[OwnerType] {
  self: OwnerType =>
  object userId extends MappedLongForeignKey(this, User) {
    override def dbIndexed_? = true
  }
}

class User private () extends ProtoAuthUser[User] {
  def getSingleton = User

  override def userIdAsString: String = id.toString

  object locale extends MappedLocale(this) {
    override def displayName = "Locale"
    override def defaultValue = "en_US"

    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object timezone extends MappedTimeZone(this) {
    override def displayName = "Time Zone"
    override def defaultValue = "America/Chicago"
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object name extends MappedString(this, 64) {
    override def displayName = "Name"

    override def validations =
      valMaxLen(64, "Name must be 64 characters or less") _ ::
      super.validations

    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object location extends MappedString(this, 64) {
    override def displayName = "Location"

    override def validations =
      valMaxLen(64, "Location must be 64 characters or less") _ ::
      super.validations

    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)

  }
  object bio extends MappedTextarea(this, 160) {
    override def displayName = "Bio"

    override def validations =
      valMaxLen(160, "Bio must be 160 characters or less") _ ::
      super.validations

    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  /*
   * FieldContainers for various LiftScreeens.
   */
  def accountScreenFields = new FieldContainer {
    def allFields = List(username, email, locale, timezone)
  }

  def profileScreenFields = new FieldContainer {
    def allFields = List(name, location, bio)
  }

  def registerScreenFields = new FieldContainer {
    def allFields = List(username, email)
  }

  def whenCreated: DateTime = new DateTime(createdAt.is)

//  override def authRoles: Set[String] = {
//    userRoles.names.toSet
//  }


}

object User extends User with ProtoAuthUserMeta[User] with Loggable {

  val DEFAULT_ID = -1l

  def currentUserIdOrDefault = User.currentUser.map(_.id.is).openOr(DEFAULT_ID)

  override def dbTableName = "users"

//  ensureIndex((email.name -> 1), true)
//  ensureIndex((username.name -> 1), true)

  def findByEmail(in: String): Box[User] = find(By(email, in))
  def findByUsername(in: String): Box[User] = find(By(username, in))

  def findByStringId(strId: String): Box[User] =
    try {
      find(By(id, strId.toLong))
    } catch {
      case e: Exception => Empty
    }

  override def onLogIn: List[User => Unit] = List(
      user => User.loginCredentials.remove()
  )

  override def onLogOut: List[Box[User] => Unit] = List(
    x => logger.debug("User.onLogOut called."),
    boxedUser => boxedUser.foreach { u =>
      ExtSession.deleteExtCookie()
    }
  )

  /*
   * Lift Bootstrap Auth vars
   */
  private lazy val siteName           = MapperAuth.siteName.vend
  private lazy val sysUsername        = MapperAuth.systemUsername.vend
  private lazy val indexUrl           = MapperAuth.indexUrl.vend
  private lazy val registerUrl        = MapperAuth.registerUrl.vend
  private lazy val loginTokenAfterUrl = MapperAuth.loginTokenAfterUrl.vend

  /*
   * LoginToken
   */
  override def handleLoginToken: Box[LiftResponse] = {
    val resp = S.param("token").flatMap(LoginToken.findByStringId) match {
      case Full(at) if (at.expires.isExpired) => {
        at.delete_!
        RedirectWithState(indexUrl, RedirectState(() => { S.error("Login token has expired") }))
      }
      case Full(at) => find(at.userId.is).map(user => {
        if (user.validate.length == 0) {
          user.verified(true)
          user.save
          logUserIn(user)
          at.delete_!
          RedirectResponse(loginTokenAfterUrl)
        }
        else {
          at.delete_!
          regUser(user)
          RedirectWithState(registerUrl, RedirectState(() => { S.notice("Please complete the registration form") }))
        }
      }).openOr(RedirectWithState(indexUrl, RedirectState(() => { S.error("User not found") })))
      case _ => RedirectWithState(indexUrl, RedirectState(() => { S.warning("Login token not provided") }))
    }

    Full(resp)
  }

  // send an email to the user with a link for logging in
  def sendLoginToken(user: User): Unit = {
    import net.liftweb.util.Mailer._

    val token = LoginToken.createForUserId(user.id.is)

    val msgTxt =
      """
        |Someone requested a link to change your password on the %s website.
        |
        |If you did not request this, you can safely ignore it. It will expire 48 hours from the time this message was sent.
        |
        |Follow the link below or copy and paste it into your internet browser.
        |
        |%s
        |
        |Thanks,
        |%s
      """.format(siteName, token.url, sysUsername).stripMargin

    sendMail(
      From(MapperAuth.systemFancyEmail),
      Subject("%s Password Help".format(siteName)),
      To(user.fancyEmail),
      PlainMailBodyType(msgTxt)
    )
  }

  /*
   * ExtSession
   */
  def createExtSession(uid: String) = ExtSession.createExtSession(uid)

  /*
  * Test for active ExtSession.
  */
  def testForExtSession: Box[Req] => Unit = {
    ignoredReq => {
      if (currentUserId.isEmpty) {
        ExtSession.handleExtSession match {
          case Full(es) => find(es.userId.is).foreach { user => logUserIn(user, false) }
          case Failure(msg, _, _) => logger.warn("Error logging user in with ExtSession: %s".format(msg))
          case Empty =>
        }
      }
    }
  }

  // used during login process
  object loginCredentials extends SessionVar[LoginCredentials](LoginCredentials(""))
  object regUser extends SessionVar[User](create.email(loginCredentials.is.email))

}

case class LoginCredentials(val email: String, val isRememberMe: Boolean = false)

object SystemUser {
  private val username = "lbs20"
  private val email = "help@localhost.com"

  lazy val user: User = User.find(By(User.username, username)) openOr {
    User.create
      .name(MapperAuth.siteName.vend)
      .username(username)
      .email(email)
      .locale("en_US")
      .timezone("America/Chicago")
      .verified(true)
      .password("abc123") // TODO: set me
      .saveMe
  }
}

