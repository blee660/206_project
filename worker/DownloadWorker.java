package worker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * This DWorker class is a worker class that supports the DownloadFrame class.
 * This is the class that actually downloads the file using bash command.
 * 
 * @author Jenny Lee
 *
 */

public class DownloadWorker extends SwingWorker<String,Integer> implements ActionListener{

	//DWorker carries out tasks on appropriate threads
	private boolean _success;
	private String _URL;
	private int _override;
	private Process process;
	private ProcessBuilder _builder;
	private int _complete;
	private JProgressBar _progressBar = new JProgressBar();

	//constructor	
	public DownloadWorker(String url, int override, JProgressBar prog){

		_URL = url;
		_override = override;
		_progressBar = prog;

		//standard wget command
		if (override == 0){
			_builder = new ProcessBuilder("/bin/bash", "-c", "wget " + _URL + " && echo \"Complete\" || echo \"Error Encountered\"");
		}

		//command for wget which will resume cancelled download
		if (override == 1){
			_builder = new ProcessBuilder("/bin/bash", "-c", "wget -c " + _URL + " && echo \"Complete\" || echo \"Error Encountered\"");
		}
		
		_builder.redirectErrorStream(true);
	}



	@Override
	protected String doInBackground() throws Exception {
		//start the download process
		process = _builder.start();

		//print messages to console
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));

		String line = null;

		//iterate through all the lines being printed
		while ((line = stdoutBuffered.readLine()) != null ) {
			
			System.out.println(line);
			//if the download was successful
			if(line.equals("Complete")){
				_success = true;
			}else{
				_success = false;
			}

			//find relevant output on console which shows progress of download
			if (line.contains("..........")){
				int num = Integer.parseInt(line.substring(line.indexOf(".........."), line.indexOf('%')).replaceAll("[^0-9]", ""));
				//publish values
				publish(num);
			}
		}
		//determine whether download process was successful or not
		_complete = process.exitValue();
		return line;
	
	}

	@Override
	//update progress bar in 'chunks' according to status
	public void process(List<Integer> chunks){
		for (int num: chunks){
			_progressBar.setValue(num);
		}
	}

	//method which gets called when the pause button is pressed while downloading
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(process != null){
			process.destroy();
		}
	}

	@Override
	protected void done(){
		if(_complete == 0){
			if(_success == true){
				//if exit status was 0, download was successful. 
				JOptionPane.showMessageDialog(null, "Download Completed");
			}
			//if exit status is bigger than 0, Download was unsuccessful
		} else if(_complete > 0){
			JOptionPane.showMessageDialog(null, "Error Encountered");
		}
	}
}




