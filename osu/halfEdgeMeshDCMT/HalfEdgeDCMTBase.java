package osu.halfEdgeMeshDCMT;

import osu.halfEdgeMesh.*;

/** Half edge class for decimation (DCMT). */
public class HalfEdgeDCMTBase extends HalfEdgeBase {

	// *** Get functions ***
	
	/** Return next half edge in cell. 
	 *  - Override to return type HalEdgeDCMTBase. 
	 */
	public HalfEdgeDCMTBase NextHalfEdgeInCell()
	{ return ((HalfEdgeDCMTBase) next_half_edge_in_cell); }
	
	
	/** Return previous half edge in cell. 
	 *  - Override to return type HalfEdgeDCMTBase.
	 */
	public HalfEdgeDCMTBase PrevHalfEdgeInCell()
	{ return ((HalfEdgeDCMTBase) prev_half_edge_in_cell); }
	
	/** Return next half edge around edge.
	 *  - Override to return type HalfEdgeDCMTBase.
	 */
	public HalfEdgeDCMTBase NextHalfEdgeAroundEdge()
	{ return ((HalfEdgeDCMTBase) next_half_edge_around_edge); }
	
	/** Return from vertex.
	 *  - Override to return type VertexDCMTBase.
	 */
	public VertexDCMTBase FromVertex()
	{ return ((VertexDCMTBase) from_vertex); }
	
	/** Return to vertex.
	 *  - Override to return type VertexDCMTBase.
	 */
	public VertexDCMTBase ToVertex()
	{ return ((VertexDCMTBase) super.ToVertex()); }
	
	/** Return cell containing half edge.
	 *  - Override to return type CellDCMTBase.
	 */
	public CellDCMTBase Cell()
	{ return ((CellDCMTBase) cell); }
	
	
	// *** Set functions ***
	// (Needed because osu.halfEdgeMeshDCMT is a different package than osu.halfEdgeMesh.)
	
	/** Set cell.
	 *  - HalfEdgeMeshDCMTBase does not have access to HalfEdgeBase cell.
	 */
	protected void SetCell(CellDCMTBase cell)
	{ this.cell = cell; }
	

	// *** Functions to compute mesh information ***
	
	/** Compute length squared of the half edge */
	public double ComputeLengthSquared() {
		return ComputeCoord.compute_squared_distance
				(FromVertex().coord, ToVertex().coord);
	}
	
	
	/** Compute cosine of angle (v0,v1,v2) where v1 is 
	 * half_edge->FromVertex() and v0 and v2 are the two vertices
	 * adjacent to v1.
	 */
	public double ComputeCosAngleAtFromVertex
	(FlagZero flag_zero) 
	{	
		VertexBase v0 = PrevHalfEdgeInCell().FromVertex();
		VertexBase v1 = FromVertex();
		VertexBase v2 = ToVertex();
		
		return ComputeCoord.compute_cos_triangle_angle
				(v0.coord, v1.coord, v2.coord, flag_zero);
	}
}
