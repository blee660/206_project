package worker;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * EWorker class is a worker class that performs all the bash command works using the 
 * ProcessBuilder classes. This class calls upon the avconv bash command in order to
 * extract audio from a video. 
 * 
 * @author Jenny Lee
 *
 */

public class ExtractWorker extends SwingWorker<String, Integer> {

	//declare and initialise the field to be used in the script
	private String _fName;
	private String _time;
	private String _length;
	private String _newName;
	private boolean _audio;
	private String _temp = "temp";
	private int _totalLength;

	private Process process;
	private ProcessBuilder _builder;

	private final JOptionPane pane = new JOptionPane("Extracting...");
	private final JDialog dialog = pane.createDialog("EXTRACT");

	private int _complete;

	private int _exit;

	//constructor which takes in 4 parameters, all of which MUST not be null
	public ExtractWorker(String fName, String time, String length, String newName, boolean audio) {

		_fName = fName;
		_time = time;
		_length = length;
		_newName = newName;
		_audio = audio;
		//bash command for extracting files
		Thread t = new Thread(new Runnable(){
			public void run(){
				dialog.setVisible(true);
			}
		});


		//if the file is an audio type
		if(_fName.endsWith(".mp3") || _fName.endsWith(".aac")){
			t.start();
			
			if(_time != null && _length != null){
				_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + "\"" + _fName + "\"" +  
						" | grep Audio && echo \"Successful\" || echo \"Invalid\" ; avconv -i " + "\"" +  _fName +  "\"" + 
						" -ss " + _time + " -t " + _length + " -acodec copy " + _newName + 
						".mp3 && echo \"Successful\" || echo \"Error\"");
		
			}
			//if the file is a video type	
		} else if (_fName.endsWith(".mp4") || _fName.endsWith(".avi") || _fName.endsWith(".mov")) {
			t.start();
			//when the start time and the end time of the extraction are specified
			if (_time != null && _length != null){
				
				if(_fName.endsWith(".mp4")){
				_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + _fName + 
						" | grep Media && echo \"Successful\" || echo \"Invalid\" ; avconv -i " + _fName + 
						" | grep Audio ; avconv -i " + _fName + " -acodec copy -vn -y " + _temp + 
						".aac && echo \"Successful\" || echo \"Error\" ; avconv -i " + _temp + ".aac" + 
						" -aq 2 " + _temp + ".mp3 ; rm " + _temp + ".aac ; avconv -i " + _temp + 
						".mp3 -ss " + _time + " -t " + _length + " -acodec copy " + _newName + 
						".mp3 ; rm " + _temp + ".mp3 && echo \"Successful Conversion\" || echo \"Error\"");

				}else if(_fName.endsWith(".avi")){
				_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + "\"" + _fName + "\"" + 
						" | grep MPEG && echo \"Successful\" || echo \"Invalid\" ; avconv -i " + "\"" + _fName + "\"" + 
						" | grep Audio ; avconv -i " + "\"" + _fName + "\"" + " -acodec copy -vn -y " + _temp + 
						".mp3 && echo \"Successful\" || echo \"Error\" ; avconv -i " + _temp + 
						".mp3 -ss " + _time + " -t " + _length + " -acodec copy " + _newName + 
						".mp3 ; rm " + _temp + ".mp3 && echo \"Successful Conversion\" || echo \"Error\"");
				}

				//when the times are not specified, just extract the whole audio of the video.
			}else{
				if(_fName.endsWith(".mp4")){

					_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + "\"" + _fName + "\"" + 
							" | grep Media && echo \"Successful\" || echo \"Invalid\" ; avconv -i " + "\"" + _fName + "\"" + " -an " + _newName + 
							".mp4 && echo \"Successful\" || echo \"Error\"");
				}else if(_fName.endsWith(".avi")){
					_builder = new ProcessBuilder("/bin/bash", "-c", "file -b " + "\"" + _fName + "\"" + 
							" | grep MPEG && echo \"Successful\" || echo \"Invalid\" ; avconv -i " + "\"" + _fName + "\"" + " -an " + _newName + 
							".avi && echo \"Successful\" || echo \"Error\"");
				}
				
			}
			//if not an audio or a video, reject
		} else {
			_builder = new ProcessBuilder("/bin/bash", "-c", "echo \"Invalid\"");
		}

		_builder.redirectErrorStream(true);
		
	}

	@Override
	protected String doInBackground() throws Exception {

		// Start extraction process
		process = _builder.start();

		// output information and progress to console
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));

		String line = null;
		boolean firstLength = false;

		//calculate the total length of the extracted file.
		int eHour = 60 * 60 * 100 * Integer.parseInt(_length.substring(0, 2)) - 60 * 60 * 100 * Integer.parseInt(_time.substring(0, 2));
		int eMin = 60 * 100 * Integer.parseInt(_length.substring(3, 5)) - 60 * 100 * Integer.parseInt(_time.substring(3, 5));
		int eSec = 100 * Integer.parseInt(_length.substring(6, 8)) - 100 * Integer.parseInt(_time.substring(6, 8));
		int totalETime = eHour + eMin + eSec;

		while ((line = stdoutBuffered.readLine()) != null) {

			System.out.println(line);

			//calculate the total length of the original file
			if (line.contains("Duration: ") && !firstLength) {
				int x = line.indexOf("Duration: ");
				String length = line.substring(x + 10, x + 21);
				int hour = 60 * 60 * 100 * Integer.parseInt(length.substring(0,2));
				int min = 60 * 100 * Integer.parseInt(length.substring(3,5));
				int sec = 100 * Integer.parseInt(length.substring(6,8));
				int cs = Integer.parseInt(length.substring(9,11));
				int totalLength = hour + min + sec + cs;
				_totalLength = totalLength;
				firstLength = true;
			}


			if(line.equals("Successful")){
				_complete = 0;
			}else if (line.equals("Error")){
				_complete = 1;
			}else if(line.equals("Invalid")){
				_complete = 2;
				JOptionPane.showMessageDialog(null, "Error Encountered: \nFile is not a valid Audio/Video file");
				this.cancel(true);
			} 
		}

		//the extracted file's length cannot be greater than the original file's length
		if (totalETime > _totalLength) {
			JOptionPane.showMessageDialog(null, "Extraction time exceeds the file length");
			_complete = 3;
		}
		_exit = process.exitValue();
		return line;
	}

	@Override
	protected void done() {
		//display message box informing completion
		if(_complete == 0){
			dialog.setVisible(false);
			dialog.dispose();
			JOptionPane.showMessageDialog(null, "Extraction Complete");
			//display error message if the file does not contain audio component	
		}else if(_complete == 1){
			dialog.setVisible(false);
			dialog.dispose();
			JOptionPane.showMessageDialog(null, "Error Encountered: \nFile does not contain audio component. \nExtraction failed");
			//display error message if the file cannot be read
		}else if(_complete == 2){
			dialog.setVisible(false);
			dialog.dispose();
	
		} 
		//display error message for other failure cases
		else if(_exit != 0 || _complete == 3){
			dialog.setVisible(false);
			dialog.dispose();
			JOptionPane.showMessageDialog(null, "Error Encountered: Extraction failed");
		}
	}
}