package code.model
import code.DbProviders
import code.BaseSpec
import code.MapperSpecsModel
import org.scalatest.BeforeAndAfterAll
import net.liftmodules.mapperauth.APermission
import net.liftmodules.mapperauth.model.Role
import net.liftmodules.mapperauth.model.Permission

class UserSpec extends BaseSpec with BeforeAndAfterAll {

  MapperSpecsModel.setup()

  def provider = DbProviders.H2MemoryProvider
  def doLog = false
  private def ignoreLogger(f: => AnyRef): Unit = ()

  override def beforeAll = {
    MapperSpecsModel.cleanup
  }

  provider.setupDB

  "User with " + provider.name should {
    "create, validate, save, and retrieve properly" in {

      val userPass = "testpass1"
      // create a new User instance
      val newUser = User.create.email("test@liftweb.net")

      newUser.password(userPass)

      val errs = newUser.validate
      if (errs.length > 1) {
        fail("Validation error: " + errs.mkString(", "))
      }

      newUser.name("Test")
      newUser.username("Test")
      newUser.validate.length should equal(0)

      // save to db
      //newUser.password.hashIt
      newUser.save

      // retrieve from db and compare
      val userFromDb = User.find(newUser.id.get)
      userFromDb.isDefined should equal(true)
      userFromDb.map(u => u.id.get should equal(newUser.id.get))
    }

    "Support password properly" in {

      val userPass = "testpass2"
      // create a new User instance
      val newUser = User.create.email("test2@liftweb.net").name("Test2").username("Test2").password(userPass)

      // check password
      newUser.password.match_?("xxxxx") should equal(false)
      newUser.password.match_?(userPass) should equal(true)

      newUser.validate.length should equal(0)

      // save to db
      newUser.save

      // retrieve from db and compare
      val userFromDb = User.find(newUser.id.get)

      userFromDb.isDefined should equal(true)
      userFromDb.map(u => {
        u.id.get should equal(newUser.id.get)
        u.password.match_?("xxxxx") should equal(false)
        u.password.match_?(userPass) should equal(true)
      })
    }

    "Can add and retrieve permissions" in {

      val userPass = "testpass3"
      val myRoleName = "my_super_role"
      // create a new User instance
      val newUser = User.create.email("test3@liftweb.net").name("Test3").username("Test3").password(userPass)

      // save to db
      newUser.save

      User.hasRole(newUser, myRoleName) should be (false)

      Role.findOrCreateAndSave(myRoleName, "some category", Permission.fromAPermission(APermission("printers")))

      val myRole = Role.find(myRoleName)
      myRole.isDefined === true

      newUser.userRoles.addRole(myRoleName).saveMe

      Permission.createUserPermission(newUser.id.get, APermission("laundry")).saveMe

      // retrieve from db and compare
      val userFromDb = User.find(newUser.id.get)

      userFromDb.isDefined should equal(true)
      userFromDb.map(u => {
//        println("XXX MY USer: "+u.authRoles)
//        println("XXX HAS ROLE: "+u.authRoles.exists{r => println("XX Check %s vs %s".format(r, myRoleName)); r == myRoleName})

        User.hasRole(u, myRoleName) should be (true)
        User.hasPermission(u, APermission.all) should be (false)
        User.hasPermission(u, APermission("printers")) should be (true)
        //println("XX Laundry: "+User.hasPermission(u, APermission("laundry")))
        User.hasPermission(u, APermission("laundry")) should be(true)

        u.id.get should equal(newUser.id.get)
        u.password.match_?("xxxxx") should equal(false)
        u.password.match_?(userPass) should equal(true)
      })
    }


  }



}
