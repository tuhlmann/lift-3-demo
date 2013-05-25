package code
package snippet

import net.liftmodules.extras.BootstrapScreen

/*
 * Base all LiftScreens off this. Currently configured to use bootstrap.
 */
abstract class BaseScreen extends BootstrapScreen {
  override def defaultToAjax_? = true
}

