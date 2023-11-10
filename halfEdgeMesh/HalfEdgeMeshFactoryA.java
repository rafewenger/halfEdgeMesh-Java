package halfEdgeMesh;

/** Instantiation of abstract class HalfEdgeMeshFactoryBase.
 *  <ul> <li> Creates objects of type VertexA, HalfEdgeA, CellA. </ul> */
public class HalfEdgeMeshFactoryA 
	extends HalfEdgeMeshFactoryBase<VertexA,HalfEdgeA,CellA> {

	/** Create new vertex of type VertexA. */
	public VertexA NewVertex()
	{ return (new VertexA()); }

	/** Create new half edge of type HalfEdgeA. */
	public HalfEdgeA NewHalfEdge()
	{ return new HalfEdgeA(); }
	
	/** Create new cell of type CellA. */
	public CellA NewCell()
	{ return new CellA(); }

}
