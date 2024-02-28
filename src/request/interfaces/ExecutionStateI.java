package request.interfaces;

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

import java.io.Serializable;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import request.ast.Direction;

// -----------------------------------------------------------------------------
/**
 * The interface <code>ExecutionStateI</code> declares the methods used to
 * access request execution state information for processing requests.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code !isContinuationSet() || isDirectional() != isFlooding()}
 * </pre>
 * 
 * <p>Created on : 2023-12-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		ExecutionStateI
extends		Serializable
{
	// -------------------------------------------------------------------------
	// Access to the current node for processing the request.
	// -------------------------------------------------------------------------

	/**
	 * access to the current node for processing the request.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current processing node reference.
	 */
	public ProcessingNodeI	getProcessingNode();

	/**
	 * update the processing node in the execution state.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code pn != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param pn	a reference to a node upon which the other methods will be executed after the change.
	 */
	public void			updateProcessingNode(ProcessingNodeI pn);

	// -------------------------------------------------------------------------
	// Accumulated result for the request.
	// -------------------------------------------------------------------------

	/**
	 * return	the currently accumulated query result.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the currently accumulated query result.
	 */
	public QueryResultI	getCurrentResult();

	/**
	 * merge {@code result} with the currently accumulated result for the request.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getCurrentResult() == null || (getCurrentResult().isBooleanRequest() == result.isBooleanRequest() && getCurrentResult().isGatherRequest() == result.isGatherRequest())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param result	query result to be added to the currently accumulated one.
	 */
	public void			addToCurrentResult(QueryResultI result);

	// -------------------------------------------------------------------------
	// Continuation information
	// -------------------------------------------------------------------------

	/**
	 * return true if the continuation of this execution state has been set.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the continuation of this execution state has been set.
	 */
	public boolean		isContinuationSet();

	/**
	 * return true if this continuation is of type directional.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if this continuation is of type directional.
	 */
	public boolean		isDirectional();

	/**
	 * return the directions of this directional continuation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isContinuationSet()}
	 * pre	{@code isDirectional()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the directions of this directional continuation.
	 */
	public Set<Direction>	getDirections();

	/**
	 * return	true if no more hops can be made after the current sensor node.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isContinuationSet()}
	 * pre	{@code isDirectional()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if no more hops can be made after the current sensor node.
	 */
	public boolean		noMoreHops();

	/**
	 * increment the number of hops made since the initial receiving node.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isContinuationSet()}
	 * pre	{@code isDirectional()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			incrementHops();

	/**
	 * return true if this continuation is of type flooding.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if this continuation is of type flooding.
	 */
	public boolean		isFlooding();

	/**
	 * return true if {@code p} is within the maximal distance for this continuation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isContinuationSet()}
	 * pre	{@code isFlooding()}
	 * pre	{@code p != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param p	a position.
	 * @return	true if {@code p} is within the maximal distance for this continuation.
	 */
	public boolean		withinMaximalDistance(PositionI p);
}
// -----------------------------------------------------------------------------
