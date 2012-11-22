package scala.tools.eclipse.launching

import java.util.{ List => JList, Map => JMap }
import scala.tools.eclipse.debug.ScalaDebugPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.debug.core.ILaunch
import org.eclipse.jdi.Bootstrap
import org.eclipse.jdt.launching.IVMConnector
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.connect.Connector
import scala.tools.eclipse.debug.model.ScalaDebugTarget
import java.io.IOException
import com.sun.jdi.connect.TransportTimeoutException
import org.eclipse.jdi.TimeoutException

/**
 * Attach connector creating a Scala debug session.
 * Added to the platform through extension point.
 */
class SocketAttachConnectorScala extends SocketConnectorScala {
  import SocketConnectorScala._

  // from scala.tools.eclipse.launching.SocketConnectorScala

  override def connector(): AttachingConnector = {
    import scala.collection.JavaConverters._
    Bootstrap.virtualMachineManager().attachingConnectors().asScala.find(_.name() == SocketAttachName).getOrElse(
        throw ScalaDebugPlugin.wrapInCoreException("Unable to find JDI AttachingConnector", null))
  }

  // from org.eclipse.jdt.launching.IVMConnector

  override val getArgumentOrder: JList[String] = {
    import scala.collection.JavaConverters._
    List(HostnameKey, PortKey).asJava
  }

  override val getIdentifier: String = ScalaDebugPlugin.id + ".socketAttachConnector"

  override def getName(): String = "Scala debugger (Socket Attach)"

  override def connect(params: JMap[_, _], monitor: IProgressMonitor, launch: ILaunch) {

    val arguments = generateArguments(params)

    try {
      // connect and create the debug session
      val virtualMachine = connector.attach(arguments)
      ScalaDebugTarget(virtualMachine, launch, null, allowDisconnect = true, allowTerminate(launch))
    } catch {
      case e: TimeoutException =>
        throw ScalaDebugPlugin.wrapInCoreException("Unable to connect to the remote VM", e)
      case e: IOException =>
        throw ScalaDebugPlugin.wrapInCoreException("Unable to connect to the remote VM", e)
    }
  }

  // ------------

}