package osu.halfEdgeMeshDCMT;

import osu.halfEdgeMesh.*;

/** Vertex class for decimation (DCMT). */
public abstract class VertexDCMTBase extends VertexBase {
	
	/** Internal flag used for detecting vertex adjacencies. */
	protected boolean visited_flag = false;
	
	/** Return visited_flag */
	protected boolean _IsVisited()
	{ return visited_flag; }


	// *** Public get methods ***
		
	/** Return the kth half edge in the ArrayList of half edges
	 *    whose from vertex is k.
	 *  - Override method in VertexBase to return type VertexDCMTBase. 
	 */
	public HalfEdgeDCMTBase KthHalfEdgeFrom(int k)
	{ return ((HalfEdgeDCMTBase) half_edge_from.get(k)); };
	
	
	/** Return incident half edge whose from vertex is this vertex
	 *    and whose ToVertexIndex() is iv.
	 * <ul> <li> Return null if no half edge found. </ul>
	 * - Override method in VertexBase to return type VertexDCMTBase.
	 */
	public HalfEdgeDCMTBase FindIncidentHalfEdge(int iv)
	{ return ((HalfEdgeDCMTBase) super.FindIncidentHalfEdge(iv)); }
	
	
	/** Return true if vertex is incident on more than two edges. */
	public boolean IsIncidentOnMoreThanTwoEdges()
	{
		int TWO = 2;
		int num_half_edges_from = NumHalfEdgesFrom();
		
		if (num_half_edges_from > TWO) { return true; }
		if (!IsBoundary()) { return false; }
		
		if (num_half_edges_from == TWO) {
			// Boundary vertex in two cells must have 
			//   at least three incident edges.
			return true;
		}
		else {
			// Boundary vertex is in just one cell and has exactly
			//   two incident edges.
			return false;
		}
	}
	
	
	// *** Internal methods ***
	
	
	/** Give access to _MoveBoundaryHalfEdgeToHalfEdgeFrom0(). */
	protected void _MoveBoundaryHalfEdgeToHalfEdgeFrom0()
	{ super._MoveBoundaryHalfEdgeToHalfEdgeFrom0(); }
	
	
	/** Set visited_flag to flag in all neighbors of this. */
	protected void 
		_SetVisitedFlagsInAdjacentVertices(boolean flag)
	{	
		for (int k = 0; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeDCMTBase half_edgeA = KthHalfEdgeFrom(k);
			half_edgeA.ToVertex().visited_flag = flag;
			
			HalfEdgeDCMTBase half_edgeB =
				half_edgeA.PrevHalfEdgeInCell();
			
			// Set half_edgeB.FromVertex().visited_flag in case of
			//   boundary edges or cells with arbitrary orientations.
			half_edgeB.FromVertex().visited_flag = flag;
		}
	}
	
	
	/** Set visited_flag to false in all neighbors of this. */
	protected void _ClearVisitedFlagsInAdjacentVertices()
	{ _SetVisitedFlagsInAdjacentVertices(false);	}

	
	/** Compare first and last half edges in half_edges_from[].
	 *  - Swap half edges if last half edge is a boundary edge
	 *    and first half edge is internal or if both are internal,
	 *    but half_edge_from[ilast.PrevHalfEdgeInCell() is boundary,
	 *    while half_edge_from[0].PrevHalfEdgeInCell() is internal.
	 */
	protected void _ProcessFirstLastHalfEdgesFrom()
	{
		if (NumHalfEdgesFrom() < 2) {
			// No swap
			return;
		}
		
		if (KthHalfEdgeFrom(0).IsBoundary()) {
			// No swap
			return;
		}
		
		int ilast = NumHalfEdgesFrom()-1;
		if (KthHalfEdgeFrom(ilast).IsBoundary()) {
			_SwapHalfEdgesInHalfEdgeFromList(0,ilast);
			return;
		}
		
		if (KthHalfEdgeFrom(0).PrevHalfEdgeInCell().IsBoundary()) {
			// No swap
			return;
		}
		
		if (KthHalfEdgeFrom(ilast).PrevHalfEdgeInCell().IsBoundary()) {
			_SwapHalfEdgesInHalfEdgeFromList(0,ilast);
			return;
		}
	}
}
