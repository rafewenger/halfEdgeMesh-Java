package TestHalfEdgeMesh;

import java.io.*;
import java.util.*;

import osu.halfEdgeMesh.*;

/** Test program to test class HalfEdgeMesh and associated classes and routines.
 * <ul>
 * 		<li> Reads a .off file into a HalfEdgeMesh.
 * 		<li> Runs check mesh, check manifold and check orientation routines.
 * 		<li> Writes the mesh to a .off file.
 * </ul>
 * @author Rephael Wenger
 */
public class TestHalfEdgeMesh {

	static String input_filename;
	static String output_filename;
	static boolean flag_silent = false;
	static boolean flag_no_warn = false;
	static boolean flag_time = false;

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
				System.err.println("Error detected in mesh data structure.");
				if (error_info.Message() != null && !(error_info.Message().equals(""))) {
					System.err.println(error_info.Message()); 
				}
				System.exit(-1);
			}
			
			
			boolean flag_warning = false;
			if (!flag_no_warn) 
			{ flag_warning = warn_non_manifold_or_not_oriented(mesh); }
			
			if (!flag_silent) {
				if (!flag_warning) {
					System.out.println("Mesh data structure passed check.");
				}
			}
		
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
				print_time("Time to check mesh: ", (time3-time2));
				print_time("Time to write file: ", (end_time-time3));
				print_time("Total time:         ", (end_time-begin_time));
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	// *** SUBROUTINES ****
	
	protected static void parse_command_line(String[] argv)
	{
		int iarg = 0;
		
		while (iarg < argv.length &&
				argv[iarg].charAt(0) == '-') {
			String s= argv[iarg];
			if (s.equals("-s"))
			{ flag_silent = true; }
			else if (s.equals("-no_warn")) 
			{ flag_no_warn = true; }
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
		out.println("Usage: TestHalfEdgeMesh [-s] [-no_warn] [-time] [-h] <input filename> [<output_filename>]");
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
		System.out.println("TestHalfEdgeMesh - Test the HalfEdgeMesh and associated classes\n" +
				"  and I/O routines by reading a .off file to the mesh,\n" +
				"  running check mesh, check manifold and check orientation routines\n" +
				"  and then writing the mesh to a .off file.");
		System.out.println();
		System.out.println("Options:");
		System.out.println("-s:        Silent. Output only warnings and error messages.");
		System.out.println("-no_warn:  Do not output non-manifold or inconsistent orientation warnings.");
		System.out.println("-time:     Report run time.");
		System.out.println("-h:        Output this help message and exit.");
		System.exit(0);
	}
	
	/// Print a warning message if the mesh is not a manifold or not oriented.
	/// - Returns true if mesh is not a manifold or not oriented
	static boolean warn_non_manifold_or_not_oriented(HalfEdgeMeshA mesh)
	{
		ManifoldInfo manifold_info = mesh.CheckManifold();
		
		if (!manifold_info.FlagManifold()) {
			if (!manifold_info.FlagManifoldVertices()) {
				int iv = manifold_info.VertexIndex();
				System.err.println
				("Warning: Non manifold vertex " + 
					String.valueOf(iv) + ".");
			}
			
			if (!manifold_info.FlagManifoldEdges()) {
				int ihalf_edge = manifold_info.HalfEdgeIndex();
				HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
				System.err.println
				("Warning: Non manifold edge (" +
					half_edge.EndpointsStr(",") + ").");
			}
			
			return true;
		}
		else {
			OrientationInfo orientation_info = mesh.CheckOrientation();
			
			if (!orientation_info.IsOriented()) {
				int ihalf_edge = orientation_info.HalfEdgeIndex();
				HalfEdgeBase half_edge = mesh.HalfEdge(ihalf_edge);
				System.err.println
				("Warning inconsistent orientation of cells incident on edge (" +
						half_edge.EndpointsStr(",") + ").");
				
				return true;
			}
		}
		
		return false;
	}
	
}
