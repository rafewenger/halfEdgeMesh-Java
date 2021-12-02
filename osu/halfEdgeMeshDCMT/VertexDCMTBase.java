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
	
	
	// *** Internal methods ***
	
	
	/** Give access to _MoveBoundaryHalfEdgeToHalfEdgeFrom0(). */
	protected void _MoveBoundaryHalfEdgeToHalfEdgeFrom0()
	{ super._MoveBoundaryHalfEdgeToHalfEdgeFrom0(); }
	
	
	/** Set visited_flag to flag in all neighbors of this. */
	protected void 
		_SetVisitedFlagsInAdjacentVertices(boolean flag)
	{
		// *** DEBUG ***
		/*
		System.out.printf("Calling _SetVisitedFlagsInAdjacentVertices on vertex %d%n",
							Index());
		System.out.printf("  flag: %b%n", flag);
		*/
		
		for (int k = 0; k < NumHalfEdgesFrom(); k++) {
			HalfEdgeDCMTBase half_edgeA = KthHalfEdgeFrom(k);
			half_edgeA.ToVertex().visited_flag = flag;
			
			HalfEdgeDCMTBase half_edgeB =
				half_edgeA.PrevHalfEdgeInCell();
			
			// Set half_edgeB.FromVertex().visited_flag in case of
			//   boundary edges or cells with arbitrary orientations.
			half_edgeB.FromVertex().visited_flag = flag;
			
			// *** DEBUG ***
			/*
			System.out.printf("Marked vertices %d and %d visited_flag = %b.%n",
							half_edgeA.ToVertexIndex(),
							half_edgeB.FromVertexIndex(),
							flag);
							*/
		}
	}
	
	
	/** Set visited_flag to false in all neighbors of this. */
	protected void _ClearVisitedFlagsInAdjacentVertices()
	{ _SetVisitedFlagsInAdjacentVertices(false);	}
	
}
