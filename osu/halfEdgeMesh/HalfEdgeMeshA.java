package osu.halfEdgeMesh;

/** Instantiation of HalfEdgeMeshBase from VertexA, HalfEdgeA, CellA, HalfEdgeMeshFactoryA. */
public class HalfEdgeMeshA extends HalfEdgeMeshBase<VertexA,HalfEdgeA,CellA> {
	
	/** Constructor.
	 * <ul> <li> Constructor must create and store a factory
	 * 		for use in creating mesh vertices, half edges, and cells. </ul>
	 */
	public HalfEdgeMeshA()
	{ factory = new HalfEdgeMeshFactoryA(); }
}
