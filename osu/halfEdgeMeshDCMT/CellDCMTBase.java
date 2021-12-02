package osu.halfEdgeMeshDCMT;

import osu.halfEdgeMesh.*;

/** Cell class for decimation (DCMT). */
public abstract class CellDCMTBase extends CellBase {
	
	/** Return one of the half edges in the cell.
	 *  - Override to return type HalfEdgeDCMTBase.
	 */
	public HalfEdgeDCMTBase HalfEdge()
	{ return ((HalfEdgeDCMTBase) half_edge); }
	
	
	// *** Set functions ***
	// (Needed because osu.halfEdgeMeshDCMT is a different class than osu.halfEdgeMesh.)
	
	/** Set representative cell half edge. */
	protected void SetHalfEdge(HalfEdgeDCMTBase half_edge)
	{ this.half_edge = half_edge; }
	
	/** Decrement number of vertices in cell. */
	protected void DecrementNumVertices()
	{ num_vertices--; }
	
	
	/** Return minimum and maximum cell edge length and
	 *  cell half edges with those lengths.
	 */
	public void ComputeMinMaxEdgeLengthSquared
	(MinMaxInfo min_max_info)
	{
		boolean flag_found = false;
		
		if (NumVertices() == 0) {
			// Empty cell. Set values to defaults and return.
			min_max_info.InitializeToZero();
			return;
		}
		
		HalfEdgeDCMTBase half_edge = HalfEdge();
		min_max_info.minVal = half_edge.ComputeLengthSquared();
		min_max_info.maxVal = min_max_info.minVal;
		min_max_info.imin = half_edge.Index();
		min_max_info.imax = min_max_info.imin;
		
		for (int i = 1; i < NumVertices(); i++) {
			half_edge = half_edge.NextHalfEdgeInCell();
			
			double length_squared = 
					half_edge.ComputeLengthSquared();
			if (length_squared < min_max_info.minVal) {
				min_max_info.minVal = length_squared;
				min_max_info.imin = half_edge.Index();
			}
			
			if (length_squared > min_max_info.maxVal) {
				min_max_info.maxVal = length_squared;
				min_max_info.imax = half_edge.Index();
			}
		}		
	}
	
	
	/** Return cosines of the min and max angles
	 *    between consecutive cell edges.
	 *  - Note: cos_min_angle >= cos_max_angle.
	 *  - The smallest angle is 0 and cos(0) = 1.
	 *  - The largest angle is pi and cos(pi) = -1.
	 */
	public void ComputeCosMinMaxAngle
	(MinMaxInfo min_max_info, FlagZero flag_zero)
	{
		// Initialize.
		min_max_info.InitializeToZero();
		
		if (NumVertices() == 0) {
			// Empty cell. Return.
			return;
		}
	
		HalfEdgeDCMTBase half_edge = HalfEdge();
		boolean flag_found = false;
		for (int i = 0; i < NumVertices(); i++) {

			double cos_angle =
					half_edge.ComputeCosAngleAtFromVertex(flag_zero);
			
			if (!flag_zero.flag) {
			
				if (!flag_found) {
					min_max_info.minVal = cos_angle;
					min_max_info.maxVal = cos_angle;
					min_max_info.imin = half_edge.Index();
					min_max_info.imax = min_max_info.imin;
				}
				else if (cos_angle < min_max_info.minVal) {
					// Note: Large angles have small cos values.
					// acos(min_max_info.minVal) is the LARGEST angle.
					min_max_info.minVal = cos_angle;
					min_max_info.imin = half_edge.Index();
				}
				// Note: min_max_info.minVal <= min_max_info.maxVal, so
				//  if cos_angle < min_max_info.minVal, then
				// (cos_angle > min_max_info.maxVal) is false.
				else if (cos_angle > min_max_info.maxVal) {
					// Note: Small angles have large cos values.
					// acos(min_max_info.maxVal) is the SMALLEST angle.
					min_max_info.maxVal = cos_angle;
					min_max_info.imax = half_edge.Index();
				}
				
				flag_found = true;
			}
			
			half_edge = half_edge.NextHalfEdgeInCell();
		}
	}

}
