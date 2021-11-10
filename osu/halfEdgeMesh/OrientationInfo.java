package osu.halfEdgeMesh;

/** Orientation information */
public class OrientationInfo {

	/** True if all adjacent cells are consistently oriented.*/
	public boolean is_oriented;
	
	/** Index of half edge where half_edge.Cell() and
	 * half_edge.NextEdgeAroundEdge().Cell() have inconsistent
	 * orientation.
	 */
	public int half_edge_index;
	
	/* Constructor. Default is mesh is oriented. */
	public OrientationInfo() {
		// Default is true.
		is_oriented = true;
		half_edge_index = 0;
	}

	/** Return true if mesh is oriented. */
	public boolean IsOriented()
	{ return is_oriented; }
	
	/** Return index of half edge whose corresponding edge is
	 * incident on two cells with inconsistent orientations.
	 */
	public int HalfEdgeIndex()
	{ return half_edge_index; }
	
	/** Set the flag to not oriented and the half edge index to ihalf_edge.*/
	public void SetNotOriented(int ihalf_edge)
	{
		is_oriented = false;
		half_edge_index = ihalf_edge;
	}
}
