package code
package snippet

import config.Site
import model.{User, LoginCredentials}
import scala.xml._
import net.liftweb._
import common._
import http.{DispatchSnippet, S, SHtml, StatefulSnippet}
import http.js.JsCmd
import http.js.JsCmds._
import util._
import Helpers._
import net.liftmodules.extras.{Gravatar, SnippetHelper}
import net.liftmodules.mapperauth.LoginRedirect
import net.liftmodules.mapperauth.model.ExtSession

sealed trait UserSnippet extends SnippetHelper with Loggable {

  protected def user: Box[User]

  protected def serve(snip: User => NodeSeq): NodeSeq =
    (for {
      u <- user ?~ "User not found"
    } yield {
      snip(u)
    }): NodeSeq

  protected def serve(html: NodeSeq)(snip: User => CssSel): NodeSeq =
    (for {
      u <- user ?~ "User not found"
    } yield {
      snip(u)(html)
    }): NodeSeq

  def header(xhtml: NodeSeq): NodeSeq = serve { user =>
    <div id="user-header">
      {gravatar(xhtml)}
      <h3>{name(xhtml)}</h3>
    </div>
  }

  def gravatar(xhtml: NodeSeq): NodeSeq = {
    val size = S.attr("size").map(toInt) openOr Gravatar.defaultSize.vend

    serve { user =>
      Gravatar.imgTag(user.email.get, size)
    }
  }

  def username(xhtml: NodeSeq): NodeSeq = serve { user =>
    Text(user.username.get)
  }

  def name(xhtml: NodeSeq): NodeSeq = serve { user =>
    if (user.name.get.length > 0)
      Text("%s (%s)".format(user.name.get, user.username.get))
    else
      Text(user.username.get)
  }

  def title(xhtml: NodeSeq): NodeSeq = serve { user =>
    <lift:head>
      <title lift="Menu.title">{"Lift Bootstrap Master: %*% - "+user.username.get}</title>
    </lift:head>
  }
}

object CurrentUser extends UserSnippet {
  protected def user = User.currentUser
}

object ProfileLocUser extends UserSnippet {

  protected def user = Site.profileLoc.currentValue

  import java.text.SimpleDateFormat

  val df = new SimpleDateFormat("MMM d, yyyy")

  def profile(html: NodeSeq): NodeSeq = serve(html) { user =>
    val editLink: NodeSeq =
      if (User.currentUser.filter(_.id.get == user.id.get).isDefined)
        <a href={Site.editProfile.url} class="btn btn-info"><i class="icon-edit icon-white"></i> Edit Your Profile</a>
      else
        NodeSeq.Empty

    "#id_avatar *" #> Gravatar.imgTag(user.email.get) &
    "#id_name *" #> <h3>{user.name.get}</h3> &
    "#id_location *" #> user.location.get &
    "#id_whencreated" #> df.format(user.whenCreated.toDate).toString &
    "#id_bio *" #> user.bio.get &
    "#id_editlink *" #> editLink
  }
}

object UserLogin extends Loggable {

  def render = {
    // form vars
    var password = ""
    var hasPassword = false
    var remember = User.loginCredentials.is.isRememberMe

    val radios = SHtml.radioElem[Boolean](
      Seq(false, true),
      Full(hasPassword)
    )(it => it.foreach(hasPassword = _))

    def doSubmit(): JsCmd = {
      S.param("email").map(e => {
        val email = e.toLowerCase.trim
        // save the email and remember entered in the session var
        User.loginCredentials(LoginCredentials(email, remember))

        if (hasPassword && email.length > 0 && password.length > 0) {
          User.findByEmail(email) match {
            case Full(user) if (user.password.match_?(password)) =>
              logger.debug("pwd matched")
              User.logUserIn(user, true)
              if (remember) User.createExtSession(user.id.get.toString)
              else ExtSession.deleteExtCookie()
              RedirectTo(LoginRedirect.openOr(Site.home.url))
            case _ =>
              S.error("Invalid credentials")
              Noop
          }
        }
        else if (hasPassword && email.length <= 0 && password.length > 0) {
          S.error("id_email_err", "Please enter an email")
          Noop
        }
        else if (hasPassword && password.length <= 0 && email.length > 0) {
          S.error("id_password_err", "Please enter a password")
          Noop
        }
        else if (hasPassword) {
          S.error("id_email_err", "Please enter an email")
          S.error("id_password_err", "Please enter a password")
          Noop
        }
        else if (email.length > 0) {
          // see if email exists in the database
          User.findByEmail(email) match {
            case Full(user) =>
              User.sendLoginToken(user)
              User.loginCredentials.remove()
              S.notice("An email has been sent to you with instructions for accessing your account")
              Noop
            case _ =>
              RedirectTo(Site.register.url)
          }
        }
        else {
          S.error("id_email_err", "Please enter an email address")
          Noop
        }
      }) openOr {
        S.error("id_email_err", "Please enter an email address")
        Noop
      }
    }

    "#id_email [value]" #> User.loginCredentials.is.email &
    "#id_password" #> SHtml.password(password, password = _) &
    "#no_password" #> radios(0) &
    "#yes_password" #> radios(1) &
    "name=remember" #> SHtml.checkbox(remember, remember = _) &
    "#id_submit" #> SHtml.hidden(doSubmit)
  }
}

object UserTopbar {
  def render = {
    User.currentUser match {
      case Full(user) =>
        <ul class="nav navbar-nav" id="user">
          <li class="dropdown" data-dropdown="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
              {Gravatar.imgTag(user.email.get, 20)}
              <span>{user.username.get}</span>
              <b class="caret"></b>
            </a>
            <ul class="dropdown-menu">
              <li><a href={Site.profileLoc.calcHref(user)}><i class="icon-user"></i> Profile</a></li>
              <li><lift:Menu.item name="Account" donthide="true" linktoself="true"><i class="icon-cog"></i> Settings</lift:Menu.item></li>
              <li class="divider"></li>
              <li><lift:Menu.item name="Logout" donthide="true"><i class="icon-off"></i> Log Out</lift:Menu.item></li>
            </ul>
          </li>
        </ul>
      case _ if (S.request.flatMap(_.location).map(_.name).filterNot(it => List("Login", "Register").contains(it)).isDefined) =>
        <ul class="nav navbar-nav">
          <li><a href="/login">Sign In</a></li>
        </ul>
      case _ => NodeSeq.Empty
    }
  }
}


