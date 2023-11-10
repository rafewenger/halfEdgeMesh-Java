package halfEdgeMesh;

import java.io.PrintStream;

/// \file CellBase.java
/// Class for HalfEdgeMesh cells.

/*
* Copyright (C) 2021-2023 Rephael Wenger
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* (LGPL) as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import java.util.*;


/** Base class for half edge mesh cells (polygons.) */
public abstract class CellBase {

	/// index: Unique non-negative integer identifying the cell.
	protected int index;
	
	/// Some half edge in the cell.
	protected HalfEdgeBase half_edge;
	
	/// Number of cell vertices.
	protected int num_vertices;
	
	protected void Init()
	{
		half_edge = null;
		num_vertices = 0;
	}
	
	/** Construct and initialize cell. */
	public CellBase() { Init(); }
	
	/** Return cell index. */
	public int Index()
	{ return(index); }
	
	/** Return one of the half edges in the cell. */
	public HalfEdgeBase HalfEdge()
	{ return(half_edge); }
	
	/** Return the number of cell vertices. */
	public int NumVertices()
	{ return(num_vertices); }
	
	/** Return true if cell has exactly 3 vertices.	 */
	public boolean IsTriangle()
	{ return (NumVertices() == 3); }
	
	/** Return list of cell vertex indices */
	public List<Integer> GetVertexIndices()
	{
		List<Integer> ivlist = new ArrayList<Integer>();
		HalfEdgeBase half_edge = HalfEdge();
		for (Integer i = 0; i < NumVertices(); i++) {
			ivlist.add(half_edge.FromVertexIndex());
			half_edge = half_edge.NextHalfEdgeInCell();
		}
		
		return ivlist;
	}

	
	/** Compute centroid of cell */
	public float [] ComputeCentroid()
	{
		float [] centroid = new float[VertexBase.DIMENSION];
		
		HalfEdgeBase half_edge = HalfEdge();
		for (Integer i = 0; i < NumVertices(); i++) {
			VertexBase v = half_edge.FromVertex();
			for (Integer d = 0; d < VertexBase.Dimension(); d++) {
				centroid[d] = centroid[d] + v.Coord(d);
			}
			half_edge = half_edge.NextHalfEdgeInCell();
		}
		
		if (NumVertices() > 0) {
			for (Integer d = 0; d < VertexBase.Dimension(); d++) {
				centroid[d] = centroid[d]/NumVertices();
			}
		}
	
		return centroid;
	}
	
	
	// *** Print routines ***
	
	
	/** Print cell vertices.
	 *  @param out Output stream.
	 *  @param prefix Print prefix at beginning of each line.
	 */
	public void PrintVertices(PrintStream out, String prefix)
	{
		out.print(prefix);
		out.print("Cell " + String.valueOf(Index()));
		out.print(" vertices:");
		int numv = NumVertices();
		HalfEdgeBase half_edge = HalfEdge();
		for (int j = 0; j < numv; j++) {
			int iv = half_edge.FromVertexIndex();
			out.print("  " + String.valueOf(iv));
			half_edge = half_edge.NextHalfEdgeInCell();
		}
		out.println();
	}

}
