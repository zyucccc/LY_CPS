package registre.interfaces;

//nous redefinissons registrationCI
//car nous avons cree notre propre class Direction
//et nous ne utilisons pas celui du package fourni(Direction dans sorbonne_u.cps.sensor_network.interfaces)
//du coup pour correspondre a notre propre class Direction
//nous devons redefinir registrationCI

/**
 * nous redefinissons registrationCI
 * car nous avons cree notre propre class Direction
 * et nous ne utilisons pas celui du package fourni(Direction dans sorbonne_u.cps.sensor_network.interfaces)
 * du coup pour correspondre a notre propre class Direction
 * nous devons redefinir registrationCI
 */

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import registre.RegistreComponent;
import request.ast.Direction;

import java.util.Set;

// -----------------------------------------------------------------------------
/**
 * The class <code>RegistrationCI</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-12-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		RegistrationCI
extends		OfferedCI,
			RequiredCI
{
	/**
	 * return true if {@code nodeIdentifier} corresponds to a already registered
	 * sensor node.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code nodeIdentifier != null && !nodeIdentifier.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param nodeIdentifier	a node identifier to be tested.
	 * @return					true if {@code nodeIdentifier} corresponds to a already registered sensor node.
	 * @throws Exception		<i>to do</i>.
	 */
	public boolean			registered(String nodeIdentifier)throws Exception;

	/**
	 * register the new node and return a set of nodes information to which this
	 * new node can connect given its range and the range of the returned nodes.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code nodeInfo != null && !registered(nodeInfo.nodeIdentifier())}
	 * post	{@code return.stream().allMatch(n -> n != null)}
	 * post	{@code ret.stream().allMatch(n -> n.nodeIdentifier().equals(nodeInfo.nodeIdentifier()))}
	 * post	{@code ret.stream().allMatch(n -> nodeInfo.nodePosition().distance(n.nodePosition()) > nodeInfo.nodeRange() && nodeInfo.nodePosition().distance(n.nodePosition()) > n.nodeRange())}
	 * </pre>
	 *
	 * @param nodeInfo		the node information of the new node.
	 * @return				a set of already registered nodes to which the new node must connect.
	 * @throws Exception	<i>to do</i>.
	 */
	public Set<NodeInfoI>	register(
		NodeInfoI nodeInfo
		) throws Exception;

	    

	/**
	 * find a new neighbour for the given node in the given direction and return
	 * its connection info, or null if none exists.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param nodeInfo		sensor node seeking for a new neighbour.
	 * @param d				direction in which the neighbour is sought.
	 * @return				the node information of a new neighbour in the required direction or null if none is found.
	 * @throws Exception	<i>to do</i>.
	 */
	public NodeInfoI		findNewNeighbour(NodeInfoI nodeInfo, Direction d)
	throws Exception;

	/**
	 * unregister a sensor node.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code registered(nodeIdentifier)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param nodeIdentifier	a node identifier of an already registered sensor node.
	 * @throws Exception		<i>to do</i>.
	 */
	public void			unregister(String nodeIdentifier) throws Exception;
}
// -----------------------------------------------------------------------------
