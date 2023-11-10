package halfEdgeMeshPrograms;

import java.io.*;
import java.util.*;

import halfEdgeMesh.*;

/** Test program for splitting edges in HalfEdgeMesh.
 * <ul>
 * 		<li> Reads a .off file into a HalfEdgeMesh.
 * 		<li> Runs check mesh, check manifold and check orientation routines.
 * 		<li> Splits all edges.
 * 		<li> Rechecks mesh, manifold and orientation properties of the mesh.
 * 		<li> Writes the mesh to a .off file.
 * </ul>
 * @author Rephael Wenger
 */
public class TestHMeshSplitEdges {
	
	static String input_filename;
	static String output_filename;
	static boolean flag_silent = false;
	static boolean flag_no_warn = false;
	static boolean flag_time = false;
	static boolean flag_verbose = false;

	public static void main(String[] argv)
	{
		long begin_time = System.nanoTime();
		
		HalfEdgeMeshA mesh = new HalfEdgeMeshA();
		OffFileReaderA file_reader = new OffFileReaderA();
		OffFileWriterA file_writer = new OffFileWriterA();
		
		parse_command_line(argv);
		
		file_reader.OpenAndReadFile(input_filename, mesh);
		
		long time2 = System.nanoTime();
		
		try {
			
			ErrorInfo error_info = mesh.CheckAll();
			if (error_info.FlagError()) {
				mesh.PrintErrorMessage(System.err, error_info);
				System.err.println("  Exiting...");
				System.exit(-1);
			}
			
			// Check manifold and orientation.
			ManifoldInfo manifold_info = mesh.CheckManifold();
			OrientationInfo orientation_info = mesh.CheckOrientation();
		
			if (!flag_no_warn) {
				if (!manifold_info.FlagManifoldVertices()) {
					mesh.PrintNonManifoldVertex
						(System.err, "Warning:", manifold_info.VertexIndex());
				}
				
				if (!manifold_info.FlagManifoldEdges()) {
					mesh.PrintNonManifoldEdge
						(System.err, "Warning:", manifold_info.HalfEdgeIndex());
				}
				
				if (manifold_info.FlagManifoldEdges() && !orientation_info.IsOriented()) {
					mesh.PrintNotOriented
						(System.err, "Warning:", orientation_info.HalfEdgeIndex());
				}
					
			}
			
			if (!flag_silent) {
				if (manifold_info.FlagManifold() && orientation_info.IsOriented()) {
					System.out.println("Mesh data structure passed check.");
				}
			}
			
			int num_boundary_edges = mesh.CountNumBoundaryEdges();
			
			List<HalfEdgeA> edge_list = mesh.GetEdgeList();
			
			int num_split = 0;
			for (HalfEdgeBase half_edge:edge_list) {
				int ihalf_edge = half_edge.Index();
				
				if (flag_verbose) { 
					System.out.println
						("Splitting edge (" + half_edge.EndpointsStr(",") + ")."); 
				}
				
				mesh.SplitEdge(ihalf_edge);
				num_split++;
			}
			
			if (!flag_silent) {
				System.out.println
					("Split " + String.valueOf(num_split) + " edges.");
			}
			
			
			// Check mesh after split.
			check_mesh_after_split
				(mesh, manifold_info, orientation_info, num_boundary_edges);
			
			long time3 = System.nanoTime();
			
			if (output_filename == null || output_filename.equals("")) 
			{ output_filename = "out.off"; }
			if (output_filename.equals(input_filename))
			{ output_filename = "out2.off"; }
			
			if (!(flag_silent))
			{ System.out.println("Writing file: " + output_filename); }
			
			file_writer.OpenAndWriteFile(output_filename, mesh);
			
			long end_time = System.nanoTime();
			
			if (flag_time) {
				print_time("Time to read file:  ", (time2-begin_time));
				print_time("Time to split edges: ", (time3-time2));
				print_time("Time to write file: ", (end_time-time3));
				print_time("Total time:         ", (end_time-begin_time));
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	// *** SUBROUTINES ****
	
	/** Check mesh after split of edges.
	 *  @param manifold_info Manifold information about original mesh.
	 *  @param orientation_info Orientation information about original mesh.
	 *  @param num_boundary_edges Number of boundary edges.
	 */
	static void check_mesh_after_split
		(HalfEdgeMeshA mesh, 
		ManifoldInfo manifold_info, OrientationInfo orientation_info,
		int num_boundary_edges)
	{
		ErrorInfo error_info = mesh.CheckAll();
		if (error_info.FlagError()) {
			mesh.PrintErrorMessage(System.err, error_info);
			System.err.println("  Exiting...");
			System.exit(-1);
		}
		
		// Check manifold and orientation.
		ManifoldInfo new_manifold_info = mesh.CheckManifold();
		OrientationInfo new_orientation_info = mesh.CheckOrientation();
	
		if (manifold_info.FlagManifold()) {
			
			if (!new_manifold_info.FlagManifold()) {
				System.err.println
					("Error. Input mesh is a manifold but mesh after split is not.");
				
				if (!new_manifold_info.FlagManifoldVertices()) {
					mesh.PrintNonManifoldVertex
						(System.err, "Error:", new_manifold_info.VertexIndex());
				}
			
				if (!new_manifold_info.FlagManifoldEdges()) {
					mesh.PrintNonManifoldEdge
						(System.err, "Error:", new_manifold_info.HalfEdgeIndex());
				}
			}
		}
			
		if (manifold_info.FlagManifold() && orientation_info.IsOriented()) {
			if (new_manifold_info.FlagManifoldEdges() && 
				!new_orientation_info.IsOriented()) {
				mesh.PrintNotOriented
					(System.err, "Error:", new_orientation_info.HalfEdgeIndex());
			}
		}

		int nume = mesh.CountNumBoundaryEdges();
		if (nume != 2*num_boundary_edges) {
			System.err.println
				("Error detected in boundary after splitting edges.");
			System.err.println
				("  Original mesh has " + String.valueOf(num_boundary_edges) +
				" boundary edges.");
			System.err.println
				("  New mesh has " + String.valueOf(nume) +
				" boundary edges.");
			System.err.println
				("  New mesh should have " + String.valueOf(2*num_boundary_edges) +
				" boundary edges.");
		}
	}
	
	
	protected static void parse_command_line(String[] argv)
	{
		int iarg = 0;
		
		while (iarg < argv.length &&
				argv[iarg].charAt(0) == '-') {
			String s= argv[iarg];
			if (s.equals("-s"))
			{ flag_silent = true; }
			else if (s.equals("-verbose"))
			{ flag_verbose = true; }
			else if (s.equals("-time"))
			{ flag_time = true; }
			else if (s.equals("-h"))
			{ help(); }
			else {
				System.err.println("Usage error. Option " + s + " is undefined.");
				usage_error();
			}
			
			iarg++;
		}
	
		if (iarg >= argv.length || iarg+2 < argv.length)
		{ usage_error(); }
		
		input_filename = argv[iarg];
		
		if (iarg+1 < argv.length)
		{ output_filename = argv[iarg+1]; }
	}
	
	static void print_time(String label, long time) {
		double nanoseconds_per_second = 1E9;
		String s = String.format("%.4f", (time/nanoseconds_per_second));
		System.out.println(label + s + " seconds.");
	}
	
	
	static void usage_msg(PrintStream out)
	{
		out.println("Usage: TestHMeshSplitEdges [-s] [-verbose] [-time] [-h] <input filename> [<output filename>]");
	}
	
	
	static void usage_error()
	{
		usage_msg(System.err);
		System.exit(-1);
	}
	
	static void help()
	{
		usage_msg(System.out);
		System.out.println();
		System.out.println("TestHMeshSplitEdges - Test the HalfEdgeMesh split edge operations\n" +
				"  by reading a .off file to the mesh, splitting each of the mesh edges,\n" +
				"  and writing the mesh to a .off file.\n");
		System.out.println();
		System.out.println("Options:");
		System.out.println("-s:        Silent. Output only warnings and error messages.");
		System.out.println("-verbose:  Verbose. Print information at each split.");
		System.out.println("-time:     Report run time.");
		System.out.println("-h:        Output this help message and exit.");
		System.exit(0);
	}
	
}

