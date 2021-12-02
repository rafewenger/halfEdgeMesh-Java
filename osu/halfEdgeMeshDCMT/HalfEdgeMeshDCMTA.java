package osu.halfEdgeMeshDCMT;

import osu.halfEdgeMesh.*;

/** Instantiation of HalfEdgeMeshDCMTBase from VertexA, HalfEdgeA, CellA. */
public class HalfEdgeMeshDCMTA 
	extends HalfEdgeMeshDCMTBase<VertexDCMTA,HalfEdgeDCMTA,CellDCMTA>	{

	/** Constructor.
	 * <ul> <li> Constructor must create and store a factory
	 * 		for use in creating mesh vertices, half edges, and cells. </ul>
	 */
	public HalfEdgeMeshDCMTA()
	{ factory = new HalfEdgeMeshDCMTFactoryA(); }
}
