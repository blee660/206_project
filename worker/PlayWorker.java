package worker;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * The PWorker class is a worker class which carries out necessary bash commands
 * that are essential in playing the media.
 * The commands are delivered from Java to Bash through the use of ProcessBuilder class
 * 
 * @author Jenny Lee
 *
 */

public class PlayWorker extends SwingWorker<String, Integer> {

	private String _fName;
	
	private Process _process;
	private ProcessBuilder _builder;
	
	private int _complete;
	private int _exit;
	
	//constructor for the class
	public PlayWorker(String fName, String newName) {
		_fName = fName;
		
		//check the file type since the command varies depending on the file type
		if (_fName.contains(".mp3")) {
			_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + _fName + 
					" | grep Audio && echo \"Successful\" || echo \"Invalid\"");
			
		} else if (_fName.contains(".mp4") || _fName.endsWith(".avi") || _fName.endsWith(".mov")) {
			_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + _fName + 
					" | grep Media && echo \"Successful\" || echo \"Invalid\"");
		}
		
		_builder.redirectErrorStream(true);
	}
	
	@Override
	protected String doInBackground() throws Exception {
		_process = _builder.start();

		// output information and progress to console
		InputStream stdout = _process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));

		String line = null;
		
	
		while ((line = stdoutBuffered.readLine()) != null ) {
			
			//if depending on the file type of the input file, assign _complete a value
			if(line.equals("Successful")){
				_complete = 0;
			}else if (line.equals("Error")){
				_complete = 1;
			}
		}
		_exit = _process.exitValue();
		return line;
	}
	
	@Override
	protected void done() {
		//do nothing when _complete is 0
		if(_complete == 0){
		}
		//if _complete is 1, display an error message
		else if(_complete == 1){
			JOptionPane.showMessageDialog(null, "Error Encountered: Cannot Play the file");
		}
		//when _exit is not 0, dusplay an error message
		else if(_exit != 0){
			JOptionPane.showMessageDialog(null, "Error Encountered: Play Failed");
		}
	}
	

}