package halfEdgeMesh;

/// \file OffFileWriterBase.java
/// Class for writing HalfEdgeMesh data structure to off file.

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

import java.io.*;
import java.util.*;


/** Class to write mesh to off file.
 * @param <MESH_TYPE> Type of mesh. */
public abstract class OffFileWriterBase<MESH_TYPE extends HalfEdgeMeshBase> {

	/** Write mesh to off file.
	 * 	<ul> <li> Precondition: Dimension of vertices is 3. </ul>
	 */
	public void WriteFile(PrintStream outfile, MESH_TYPE mesh) throws IOException, Exception
	{
		// Write OFF header.
		outfile.println("OFF");
		
		// Write number of vertices and polygons.
		outfile.println(String.valueOf(mesh.MaxVertexIndex()+1) + " " +
				String.valueOf(mesh.NumCells()) + " 0");
		outfile.println("");
		
		StringBuffer buffer = new StringBuffer();
		
		for (int iv = 0; iv < mesh.MaxVertexIndex()+1; iv++) {
			VertexBase v = mesh.Vertex(iv);
			if (v == null)
			{ buffer.append("0.0 0.0 0.0\n"); }
			else {
				buffer.append(v.CoordStr());
				buffer.append("\n");
			}
		}
		buffer.append("\n");
		outfile.print(buffer);
		
		buffer.delete(0, buffer.length());
		// Write polygon vertices.
		int num_poly = 0;
		Set<Integer> poly_indices = mesh.CellIndices();
		List<Integer> poly_list = new ArrayList<Integer>(poly_indices);
		Collections.sort(poly_list);
		for (int ipoly: poly_list) {
			CellBase poly = mesh.Cell(ipoly);
			
			if (poly == null) { continue; }
			
			if (num_poly > mesh.NumCells()) {
				// Error. Number of cells in mesh.cell_hashtable does not equal NumCells()?
				throw new IOException("Error in OffFileWriteBase::WriteFile. Incorrect mesh.NumCells().");
			}
			
			String s = String.valueOf(poly.NumVertices()) + " ";
			HalfEdgeBase half_edge = poly.HalfEdge();
			for (int k = 0; k < poly.NumVertices(); k++) {
				s += " " + String.valueOf(half_edge.FromVertexIndex());
				half_edge = half_edge.NextHalfEdgeInCell();
			}
			buffer.append(s);
			buffer.append("\n");
			num_poly++;
		}
		outfile.print(buffer);
		
		return;
	}
	
	
	/** Open and write mesh to off file.
	 * 	<ul> <li> Precondition: Dimension of vertices is 3. </ul>
	 * @param output_filename Output file name.
	 */
	public void OpenAndWriteFile(String output_filename, MESH_TYPE mesh)
	{
		try {
			FileOutputStream file_stream = new FileOutputStream(output_filename);
			PrintStream print_stream = new PrintStream(file_stream, false);
			WriteFile(print_stream, mesh);
		}
		catch (IOException e) {
			System.err.println("Error writing file " + output_filename + ".");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		catch (Exception e) {
			System.err.println("Error writing file " + output_filename + ".");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}	
}
