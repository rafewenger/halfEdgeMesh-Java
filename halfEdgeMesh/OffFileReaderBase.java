package halfEdgeMesh;

/// \file OffFileReaderBase.java
/// Class for reading HalfEdgeMesh data structure from off file.

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

/** Class to read off files into HalfEdgeMesh. 
 *  @param <MESH_TYPE> Type of mesh.
 */
public abstract class OffFileReaderBase<MESH_TYPE extends HalfEdgeMeshBase> {
	
	/// Get first line that is not blank or a comment.
	/// - First non-blank character in a comment line is '#'.
	/// - Returns null if there is no such line.
	protected String get_next_non_comment_line(BufferedReader reader) throws IOException
	{
		String line = reader.readLine();
		while (line != null) {
			line.trim();
			if (line.length() > 0) {
				if (line.charAt(0) != '#') 
				{ return line; }
			}
			
			// Line was empty or a comment.
			// Read the next line.
			line = reader.readLine();
		}
		
		return line;
	}
	
	
	/** Read off file.
	 * <ul> <li> Precondition: Dimension of vertices is 3. </ul>
	 */
	public void ReadFile(InputStream infile, MESH_TYPE mesh) throws IOException, Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(infile));
		
		String line = reader.readLine();
		if (line == null)
		{ throw new IOException("Read error. File is empty."); }
		
		line.trim();
		
		if (!line.equals("OFF"))
		{ throw new IOException("Read error. File does not begin with OFF."); }
		
		line = get_next_non_comment_line(reader);
		if (line == null)
		{ throw new IOException("Read error. File does not contain line with number of vertices and polygons."); }
		
		String[] listX = line.split("\\s+");
		
		if (listX.length == 0)
		{ throw new IOException("Incorrect OFF file format. File missing number of vertices and polygons."); }
		else if (listX.length == 1)
		{ throw new IOException("Incorrect OFF file format. File missing number of polygons."); }
		
		int numv = Integer.parseInt(listX[0]);
		int numpoly = Integer.parseInt(listX[1]);
		
		mesh.AddVertices(numv);
		for (int iv = 0; iv < numv; iv++) {
			line = get_next_non_comment_line(reader);
			if (line == null)
			{ throw new IOException("Read error. File is missing some vertex coordinates."); }
			
			listX = line.split("\\s+");
			if (listX.length < 3) {
				String msg = "Read error. Error reading vertex " +
						String.valueOf(iv) + " coordinates.";
				throw new IOException(msg);
			}
	
			float coord[] = { Float.parseFloat(listX[0]), 
					Float.parseFloat(listX[1]), Float.parseFloat(listX[2]) };
			
			mesh.SetCoord(iv, coord);

		}
		
		
		for (int ipoly = 0; ipoly < numpoly; ipoly++) {
			line = get_next_non_comment_line(reader);
			if (line == null)
			{ throw new IOException("Read error. File is missing some polygon vertices."); }
			
			listX = line.split("\\s+");
			if (listX.length < 1) {
				String msg = "Read error. Error reading polygon " +
						String.valueOf(ipoly) + " vertices.";
				throw new IOException(msg);
			}
			
			int num_poly_vert = Integer.parseInt(listX[0]);
			
			if (listX.length < num_poly_vert+1) {
				String msg = "Read error. Error reading polygon " +
						String.valueOf(ipoly) + " vertices.";
				throw new IOException(msg);
			}

			ArrayList<Integer> cell_vlist = new ArrayList<Integer>();
			for (int k = 1; k < num_poly_vert+1; k++)
			{ cell_vlist.add(Integer.parseInt(listX[k])); }

			mesh.AddCell(ipoly, cell_vlist);
		}
	}
	
	
	/** Open and read off file.
	 * 	<ul> <li> Precondition: Dimension of vertices is 3. </ul>
	 *  @param input_filename Input file name.
	 */
	public void OpenAndReadFile(String input_filename, MESH_TYPE mesh)
	{
		try {
			File input_file = new File(input_filename);
			FileInputStream file_stream = new FileInputStream(input_file);
			ReadFile(file_stream, mesh);
		}
		catch (IOException e) {
			System.err.println("Error reading file " + input_filename + ".");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		catch (Exception e) {
			System.err.println("Error reading file " + input_filename + ".");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
}
