package worker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

//a worker that supports the VideoEditor class
public class AudioWorker extends SwingWorker<String, Integer>{

	private ProcessBuilder _builder;
	private Process _process;

	private int _complete;
	private int _exit;
	
	private JProgressBar prog;

	public AudioWorker(String vid, String aud, String newName, String act, String overlay, JProgressBar p){
		
		prog = p;
		
		//if the action is replace, perform replace action using bash commands through ProcessBuilder class
		if(act.equals("Replace")){
			_builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + vid + " -i " + aud + " -c:v copy -map 0:0 -map 1:0 -t " + overlay 
					+ " " + "\"" + newName  +  "\"" + ".mp4" + " && echo \"Successful\" || echo \"Error\"");
		}
		//if the action chosen is to overlay, do the same as above
		else if(act.equals("Overlay")){
			_builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + vid + " -i " + aud + 
			" -filter_complex [0:a][1:a]amix[out] -map \"[out]\" -map 0:v -c:v copy"
			+ " -t " + overlay + " -strict experimental " + "\"" + newName  + "\"" + ".mp4 && echo \"Successful\" || echo \"Error\"");
		}

		_builder.redirectErrorStream(true);
	}

	@Override
	protected String doInBackground() throws Exception {
		// Start overlay/replace process
		_process = _builder.start();

		// output information and progress to console
		InputStream stdout = _process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));

		String line = null;

		while ((line = stdoutBuffered.readLine()) != null ) {
			//assign appropriate value to the _complete variable depending on the result from the process
			if(line.equals("Successful")){
				_complete = 0;
			}else if (line.equals("Error")){
				_complete = 1;
			}
			if (line.contains("frame= ")){
				int num = Integer.parseInt(line.substring(line.indexOf("time="), line.indexOf("bitrate=") - 3).replaceAll("[^0-9]", ""));
				//publish values
				publish(num);
			}
			
			System.out.println(line);
		}
		
		_exit = _process.exitValue();
		return line;
		
	}
	
	@Override
	//update progress bar in 'chunks' according to status
		public void process(List<Integer> chunks){
			for (int num: chunks){
				prog.setValue(num);
			}
		}

	@Override
	protected void done() {
		//display a message if the process is completed successfully
		if(_complete == 0){
			JOptionPane.showMessageDialog(null, "Completed");
		}
		//display an error message otherwise
		else if(_complete == 1 || _exit != 0){
			JOptionPane.showMessageDialog(null, "Error Encountered");
		}
	}
	
	

}