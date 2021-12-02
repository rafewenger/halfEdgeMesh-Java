package osu.halfEdgeMeshDCMT;

import osu.halfEdgeMesh.*;

/** Instantiation of abstract class HalfEdgeMeshFactoryBase.
 *  <ul> <li> Creates objects of type VertexA, HalfEdgeA, CellA. </ul> */
public class HalfEdgeMeshDCMTFactoryA 
	extends HalfEdgeMeshFactoryBase<VertexDCMTA,HalfEdgeDCMTA,CellDCMTA> {

	/** Create new vertex of type VertexA. */
	public VertexDCMTA NewVertex()
	{ return (new VertexDCMTA()); }

	/** Create new half edge of type HalfEdgeA. */
	public HalfEdgeDCMTA NewHalfEdge()
	{ return new HalfEdgeDCMTA(); }
	
	/** Create new cell of type CellA. */
	public CellDCMTA NewCell()
	{ return new CellDCMTA(); }

}
