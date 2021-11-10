package osu.halfEdgeMesh;

/// \file CellBase.java
/// Class for HalfEdgeMesh cells.

/*
* Copyright (C) 2021 Rephael Wenger
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
	
}
