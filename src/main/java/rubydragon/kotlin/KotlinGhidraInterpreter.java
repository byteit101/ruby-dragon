// SPDX-License-Identifier: Apache-2.0

/*
 * Copyright 2021 Joel E. Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rubydragon.kotlin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import generic.jar.ResourceFile;
import ghidra.app.plugin.core.console.CodeCompletion;
import ghidra.app.plugin.core.interpreter.InterpreterConsole;
import ghidra.app.script.GhidraScript;
import ghidra.app.script.GhidraState;
import ghidra.program.flatapi.FlatProgramAPI;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.util.ProgramLocation;
import ghidra.program.util.ProgramSelection;
import rubydragon.MissingDragonDependency;
import rubydragon.ScriptableGhidraInterpreter;

/**
 * A Kotlin intepreter for Ghidra.
 */
public class KotlinGhidraInterpreter extends ScriptableGhidraInterpreter {
	private Thread replThread;
	private ScriptEngine engine;
	private BufferedReader replReader;
	private SimpleScriptContext context;

	/**
	 * Creates a new interpreter, with no input stream.
	 *
	 * @throws MissingDragonDependency If dependencies are missing for a Kotlin
	 *                                 interpeter.
	 */
	public KotlinGhidraInterpreter() throws MissingDragonDependency {
		// needed to avoid dll loading issues on Windows
		System.setProperty("idea.io.use.nio2", "true");

		context = new SimpleScriptContext();

		ScriptEngineManager scriptManager = new ScriptEngineManager();
		engine = scriptManager.getEngineByExtension("kts");

		if (engine == null) {
			String errorMessage = "A Kotlin interpreter could not be created due to missing "
					+ "dependencies. Try running RubyDragon.DownloadAllDependenciesScript "
					+ "(via the Script Manager), restarting Ghidra, and trying again.";
			throw new MissingDragonDependency(errorMessage);
		}

		engine.setContext(context);

		replThread = new Thread(() -> {
			while (replReader != null) {
				try {
					try {
						Object result = engine.eval(replReader.readLine());

						if (result != null) {
							context.getWriter().write(result + "\n");
							context.getWriter().flush();
						}
					} catch (ScriptException e) {
						context.getErrorWriter().write(e.getMessage() + "\n");
						context.getErrorWriter().flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Creates a new interpreter, and ties the streams for the provided console to
	 * the new interpreter.
	 *
	 * @param console The console to bind to the interpreter streams.
	 *
	 * @throws MissingDragonDependency If dependencies are missing for a Kotlin
	 *                                 interpeter.
	 */
	public KotlinGhidraInterpreter(InterpreterConsole console) throws MissingDragonDependency {
		this();
		setStreams(console);
	}

	/**
	 * Should end the interpreter and release all resources.
	 */
	@Override
	public void dispose() {
		replReader = null;
	}

	/**
	 * Get a list of completions for the given command prefix.
	 *
	 * Currently not implemented, and will always return an empty list.
	 *
	 * @param cmd The beginning of a command to try to complete.
	 *
	 * @return A list of possible code completions.
	 */
	public List<CodeCompletion> getCompletions(String cmd) {
		return new ArrayList<CodeCompletion>();
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
	 * @throws IOException              if the script could not be read
	 * @throws FileNotFoundException    if the script file wasn't found
	 */
	@Override
	public void runScript(GhidraScript script, String[] scriptArguments, GhidraState scriptState)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		ResourceFile scriptFile = script.getSourceFile();
		InputStreamReader scriptReader = new InputStreamReader(scriptFile.getInputStream());
		loadState(scriptState);

		engine.put("script", script);
		engine.put("args", scriptArguments);
		try {
			engine.eval(scriptReader);
		} catch (ScriptException e) {
			engine.getContext().getErrorWriter().write(e.getMessage() + "\n");
			engine.getContext().getErrorWriter().flush();
		}

		updateState(scriptState);
	}

	/**
	 * Sets the error output stream for this interpreter.
	 */
	@Override
	public void setErrWriter(PrintWriter errOut) {
		context.setErrorWriter(errOut);
	}

	/**
	 * Sets the input stream for this interpreter.
	 */
	@Override
	public void setInput(InputStream input) {
		replReader = new BufferedReader(new InputStreamReader(input));
	}

	/**
	 * Sets the output stream for this interpreter.
	 */
	@Override
	public void setOutWriter(PrintWriter output) {
		context.setWriter(output);
	}

	/**
	 * Starts an interactive session with the current input/output/error streams.
	 */
	@Override
	public void startInteractiveSession() {
		replThread.start();
	}

	/**
	 * Updates the current address pointed to by the "currentAddress" binding in the
	 * interpreter.
	 *
	 * @param address The new current address in the program.
	 */
	@Override
	public void updateAddress(Address address) {
		if (address != null) {
			engine.put("currentAddress", address);
		}
	}

	/**
	 * Updates the highlighted selection pointed to by the "currentHighlight"
	 * variable.
	 *
	 * @param sel The new highlighted selection.
	 */
	@Override
	public void updateHighlight(ProgramSelection sel) {
		if (sel != null) {
			engine.put("currentHighlight", sel);
		}
	}

	/**
	 * Updates the location in the "currentLocation" variable as well as the address
	 * in the "ghidra/current-address" variable.
	 *
	 * @param loc The new location in the program.
	 */
	@Override
	public void updateLocation(ProgramLocation loc) {
		if (loc != null) {
			engine.put("currentLocation", loc);
			updateAddress(loc.getAddress());
		}
	}

	/**
	 * Updates the program pointed to by the "currentProgram" binding, as well as
	 * the "currentAPI" binding to a FlatProgramAPI instance.
	 *
	 * @param program The new current program.
	 */
	@Override
	public void updateProgram(Program program) {
		if (program != null) {
			engine.put("currentProgram", program);
			engine.put("currentAPI", new FlatProgramAPI(program));
		}
	}

	/**
	 * Updates the selection pointed to by the "currentSelection" binding.
	 *
	 * @param sel The new selection.
	 */
	@Override
	public void updateSelection(ProgramSelection sel) {
		if (sel != null) {
			engine.put("currentSelection", sel);
		}
	}

	/**
	 * Updates a state with the current selection/location/etc. variables from the
	 * interpreter.
	 *
	 * @param scriptState The state to update.
	 */
	@Override
	public void updateState(GhidraState scriptState) {
		Program currentProgram = (Program) engine.get("currentProgram");
		scriptState.setCurrentProgram(currentProgram);

		ProgramLocation currentLocation = (ProgramLocation) engine.get("currentLocation");
		scriptState.setCurrentLocation(currentLocation);

		Address addr = (Address) engine.get("currentAddress");
		scriptState.setCurrentAddress(addr);

		ProgramSelection highlight = (ProgramSelection) engine.get("currentHighlight");
		scriptState.setCurrentHighlight(highlight);

		ProgramSelection sel = (ProgramSelection) engine.get("currentSelection");
		scriptState.setCurrentSelection(sel);
	}

}
