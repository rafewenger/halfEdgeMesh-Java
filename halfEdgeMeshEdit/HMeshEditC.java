package halfEdgeMeshEdit;

import halfEdgeMesh.*;

/** Instantiation of HMeshEditBase from VertexA, HalfEdgeA, CellA, HalfEdgeMeshFactoryA. */
public class HMeshEditC extends HMeshEditBase<VertexA,HalfEdgeA,CellA> {
	
	/** Constructor.
	 * <ul> <li> Constructor must create and store a factory
	 * 		for use in creating mesh vertices, half edges, and cells. </ul>
	 */
	public HMeshEditC()
	{ factory = new HalfEdgeMeshFactoryA(); }

}
