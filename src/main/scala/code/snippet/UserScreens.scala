package code
package snippet

import config.Site
import model._

import scala.xml._

import net.liftweb._
import common._
import http.{LiftScreen, S}
import util.FieldError
import util.Helpers._

import net.liftmodules.extras.Gravatar

/*
 * Use for editing the currently logged in user only.
 */
sealed trait BaseCurrentUserScreen extends BaseScreen {
  object userVar extends ScreenVar(User.currentUser.openOr(User.create))

  override def localSetup {
    Referer(Site.account.url)
  }
}

object AccountScreen extends BaseCurrentUserScreen {
  addFields(() => userVar.is.accountScreenFields)

  def finish() {
    userVar.is.save
    S.notice("Account settings saved")
  }
}

sealed trait BasePasswordScreen {
  this: LiftScreen =>

  def pwdName: String = "Password"
  def pwdMinLength: Int = 6
  def pwdMaxLength: Int = 32

  val passwordField = password(pwdName, "", trim,
    valMinLen(pwdMinLength, "Password must be at least "+pwdMinLength+" characters"),
    valMaxLen(pwdMaxLength, "Password must be "+pwdMaxLength+" characters or less"),
    ("tabindex" -> "1")
  )
  val confirmPasswordField = password("Confirm Password", "", trim, ("tabindex" -> "1"))

  def passwordsMustMatch(): Errors = {
    if (passwordField.is != confirmPasswordField.is)
      List(FieldError(confirmPasswordField, "Passwords must match"))
    else Nil
  }
}


object PasswordScreen extends BaseCurrentUserScreen with BasePasswordScreen {
  override def pwdName = "New Password"
  override def validations = passwordsMustMatch _ :: super.validations

  def finish() {
    userVar.is.password(passwordField.is)
    //userVar.is.password.hashIt
    userVar.is.save
    S.notice("New password saved")
  }
}

/*
 * Use for editing the currently logged in user only.
 */
object ProfileScreen extends BaseCurrentUserScreen {
  def gravatarHtml =
    <span>
      <div class="gravatar">
        {Gravatar.imgTag(userVar.is.email.get, 60)}
      </div>
      <div class="gravatar">
        <h4>Change your avatar at <a href="http://gravatar.com" target="_blank">Gravatar.com</a></h4>
        <p>
          We're using {userVar.is.email.get}. It may take time for changes made on gravatar.com to appear on our site.
        </p>
      </div>
    </span>

  val gravatar = displayOnly("Picture", gravatarHtml)

  addFields(() => userVar.is.profileScreenFields)

  def finish() {
    userVar.is.save
    S.notice("Profile settings saved")
  }
}

// this is needed to keep these fields and the password fields in the proper order
trait BaseRegisterScreen extends BaseScreen {
  object userVar extends ScreenVar(User.regUser.is)

  addFields(() => userVar.is.registerScreenFields)
}

/*
 * Use for creating a new user.
 */
object RegisterScreen extends BaseRegisterScreen with BasePasswordScreen {
  override def validations = passwordsMustMatch _ :: super.validations

  val rememberMe = builder("", User.loginCredentials.is.isRememberMe, ("tabindex" -> "1"))
    .help(Text("Remember me when I come back later."))
    .make

  override def localSetup {
    Referer(Site.home.url)
  }

  def finish() {
    val user = userVar.is
    user.password(passwordField.is)
    //user.password.hashIt
    user.save
    User.logUserIn(user, true)
    if (rememberMe) User.createExtSession(user.id.get.toString)
    S.notice("Thanks for signing up!")
  }
}
