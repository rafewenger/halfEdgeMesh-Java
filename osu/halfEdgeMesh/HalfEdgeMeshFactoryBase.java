package osu.halfEdgeMesh;

/** Abstract base class for half edge mesh factory.
 * <ul> <li> Factory creates mesh vertices, half edges and cells.</ul>
 * 
 * @param <VERTEX_TYPE> Type of vertex created by the factory.
 * @param <HALF_EDGE_TYPE> Type of half edge created by the factory.
 * @param <CELL_TYPE> Type of cell created by the factory.
 */
public abstract class HalfEdgeMeshFactoryBase<VERTEX_TYPE,HALF_EDGE_TYPE,CELL_TYPE> {

	/// New vertex.
	public abstract VERTEX_TYPE NewVertex();
	
	/// New edge.
	public abstract HALF_EDGE_TYPE NewHalfEdge();
	
	// New cell.
	public abstract CELL_TYPE NewCell();
}
