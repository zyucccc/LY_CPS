package registre.interfaces;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement
// a simulation of a sensor network in BCM4Java.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

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
