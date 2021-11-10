package osu.halfEdgeMesh;

/** Manifold information.
 * <ul>
 * 		<li> Flag indicating if all edges are incident on at most two cells.
 * 		<li> Flag indicating if all cells incident on a vertex form a fan.
 * 		<li> Index of a half edge whose corresponding edge is incident
 * 			on three or more cells.
 * 		<li> Index of a vertex whose incident cells do not form a fan.
 * </ul>
 */
public class ManifoldInfo {

	/** True if all edges are incident on at most two cells. */
	public boolean flag_manifold_edges;
	
	/** True if all cells incident on a vertex form a fan. */
	public boolean flag_manifold_vertices;
	
	/** Index of half edge where more than two cells are incident
	 * on the corresponding edge.
	 */
	public int half_edge_index;
	
	/** Index of vertex where cells incident on the vertex DO NOT
	 * form a fan.
	 */
	public int vertex_index;
	
	/** Constructor. Default to mesh being a manifold. */
	public ManifoldInfo()
	{
		// Default to mesh being a manifold.
		flag_manifold_edges = true;
		flag_manifold_vertices = true;
		half_edge_index = 0;
		vertex_index = 0;
	}
	
	/** Return true if all edges are incident on at most two cells.*/
	public boolean FlagManifoldEdges()
	{ return flag_manifold_edges; }
	
	/** Return true if all cells incident on a vertex form a fan. */
	public boolean FlagManifoldVertices()
	{ return flag_manifold_vertices; }
	
	/** Return the index of a half edge whose corresponding edge
	 * 	is incident on three or more cells.
	 */
	public int HalfEdgeIndex()
	{ return half_edge_index; }

	/** Return the index of a vertex whose incident cells do not
	 * 	form a fan.
	 */
	public int VertexIndex()
	{ return vertex_index; }

	/** Return true if FlagManifoldEdge() and FlagManifoldVertices()
	 * are both true.
	 */
	public boolean FlagManifold()
	{ return (FlagManifoldEdges() && FlagManifoldVertices()); }
	
	/** Set flag_manifold_edges to false and the half edge index to ihalf_edge.*/
	public void SetNonManifoldEdges(int ihalf_edge)
	{
		flag_manifold_edges = false;
		half_edge_index = ihalf_edge;
	}

	/** Set flag_manifold_vertices to false and the vertex index to iv.*/
	public void SetNonManifoldVertices(int iv)
	{
		flag_manifold_vertices = false;
		vertex_index = iv;
	}
}
