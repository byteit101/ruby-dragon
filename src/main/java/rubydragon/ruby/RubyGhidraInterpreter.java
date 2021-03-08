package rubydragon.ruby;

import java.io.InputStream;
import java.io.PrintWriter;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import ghidra.app.plugin.core.interpreter.InterpreterConsole;
import ghidra.app.script.GhidraState;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.util.ProgramLocation;
import ghidra.program.util.ProgramSelection;
import rubydragon.GhidraInterpreter;

/**
 * A Ruby interpreter for Ghidra, built using JRuby.
 */
public class RubyGhidraInterpreter implements GhidraInterpreter {
	private ScriptingContainer container;
	private Thread irbThread;

	public RubyGhidraInterpreter() {
		container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
		irbThread = new Thread(() -> {
			while (true) {
				container.runScriptlet("require 'irb';IRB.start");
			}
		});
	}

	public RubyGhidraInterpreter(InterpreterConsole console) {
		this();
		setStreams(console);
	}

	@Override
	public void dispose() {
		// do nothing
	}

	public void loadState(GhidraState state) {
		updateHighlight(state.getCurrentHighlight());
		updateLocation(state.getCurrentLocation());
		updateSelection(state.getCurrentSelection());
		updateProgram(state.getCurrentProgram());

		// this has to happen after the location update
		// since it clobbers the current address right now
		updateAddress(state.getCurrentAddress());
	}

	/**
	 * Runs the given script with the arguments and state provided.
	 * 
	 * The provided state is loaded into the interpreter at the beginning of
	 * execution, and the values of the globals are then exported back into the
	 * state after it completes.
	 * 
	 * If the script cannot be found but the script is not running in headless mode,
	 * the user will be prompted to ignore the error, which will cause the function
	 * to simply continue instead of throwing an IllegalArgumentException.
	 * 
	 * @throws IllegalArgumentException if the script does not exist
	 */
	public void runScript(String scriptName, InputStream script, String[] scriptArguments, GhidraState scriptState)
			throws IllegalArgumentException {
		loadState(scriptState);
		container.runScriptlet(script, scriptName);
		updateState(scriptState);
	}

	/**
	 * Sets the error output stream for this interpreter.
	 */
	public void setErrWriter(PrintWriter errOut) {
		container.setError(errOut);
	}

	/**
	 * Sets the input stream for this interpreter.
	 */
	public void setInput(InputStream input) {
		container.setInput(input);
	}

	/**
	 * Sets the output stream for this interpreter.
	 */
	public void setOutWriter(PrintWriter output) {
		container.setOutput(output);
	}

	/**
	 * Sets the input, output, and error streams for this interpreter to those of
	 * the provided console.
	 * 
	 * @param console The console to tie the interpreter streams to.
	 */
	@Override
	public void setStreams(InterpreterConsole console) {
		setInput(console.getStdin());
		setOutWriter(console.getOutWriter());
		setErrWriter(console.getErrWriter());
	}

	@Override
	public void startInteractiveSession() {
		irbThread.start();
	}

	public void updateAddress(Address address) {
		container.put("$current_address", address);
	}

	@Override
	public void updateHighlight(ProgramSelection sel) {
		container.put("$current_highlight", sel);
	}

	@Override
	public void updateLocation(ProgramLocation loc) {
		if (loc == null) {
			container.remove("$current_location");
		} else {
			container.put("$current_location", loc);
			updateAddress(loc.getAddress());
		}
	}

	@Override
	public void updateSelection(ProgramSelection sel) {
		container.put("$current_selection", sel);
	}

	public void updateState(GhidraState scriptState) {
		scriptState.setCurrentProgram((Program) container.get("$current_program"));
		scriptState.setCurrentLocation((ProgramLocation) container.get("$current_location"));
		scriptState.setCurrentAddress((Address) container.get("$current_address"));
		scriptState.setCurrentHighlight((ProgramSelection) container.get("$current_highlight"));
		scriptState.setCurrentSelection((ProgramSelection) container.get("$current_selection"));
	}

	@Override
	public void updateProgram(Program program) {
		container.put("$current_program", program);
	}
}
