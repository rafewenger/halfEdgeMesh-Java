package halfEdgeMeshPrograms;

import java.io.*;
import java.util.*;

import halfEdgeMesh.*;

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
	
}
