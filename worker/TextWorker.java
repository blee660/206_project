package worker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class TextWorker extends SwingWorker<String, Integer> implements ActionListener{

	private String _vidName;
	private String _newName;
	private String _timeS;
	private String _timeF;
	private String _textS;
	private String _textE;
	private String _font;
	private String _fontSize;
	private String _fontColor;
	private JProgressBar _prog;

	private int _complete;
	private int _exit;

	private ProcessBuilder _builder;
	private Process _process;
	
	private final JOptionPane pane = new JOptionPane("Adding Text...");
	private final JDialog dialog = pane.createDialog("Add Text to Video");

	public TextWorker(String name, String newName, String timeS, String timeF, String textS, String textE, String font, String fontSize, String fontColor, JProgressBar p){
		_vidName = name;
		_newName = newName;
		_timeS = timeS;
		_timeF = timeF;
		_textS = textS;
		_textE = textE;
		_fontSize = fontSize;
		_fontColor = fontColor;
		_prog = p;

		//open a JOptionPane while executing
		Thread t = new Thread(new Runnable(){
			public void run(){
				dialog.setVisible(true);
			}
		});
		
		if(font.equals("Normal")){
				_font = "Ubuntu-L";
		}else if(font.equals("Italics")){
			_font = "Ubuntu-LI";
		}else if(font.equals("Bold")){
			_font = "Ubuntu-B";
		}else if(font.equals("Bold + Italics")){
			_font = "Ubuntu-BI";
		}
		
			t.start();
			//use ProcessBuilder class to use bash commands
			_builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i "+ _vidName +" -vf \"drawtext="
					+ "fontfile='/usr/share/fonts/truetype/ubuntu-font-family/"+ _font +".ttf': text='"+ _textS +"':x=(main_w-text_w)/2: "
					+ "fontsize="+ _fontSize +":fontcolor="+ _fontColor +":draw='lt(t,"+ _timeS +")':,drawtext=fontfile='/usr/share/fonts/truetype/ubuntu-font-family/"+ _font +".ttf': "
					+ "text='" + _textE + "':x=(main_w-text_w)/2: fontsize="+ _fontSize +":fontcolor="+ _fontColor +":draw='gt(t," + _timeF + ")':\" -c:a copy " + "\"" +_newName +"\"" + ".mp4" + " && echo \"Successful\" || echo \"Error\"");

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
			System.out.println(line);
			
			if(line.equals("Successful")){
				_complete = 0;
			}else if (line.equals("Error")){
				_complete = 1;
			}
		
			if (line.contains("frame=")){
				int num = Integer.parseInt(line.substring(line.indexOf("time="), line.indexOf("bitrate=") - 3).replaceAll("[^0-9]", ""));
				//publish values
				publish(num);
			}
			
		}
		
		_exit = _process.exitValue();
		return line;
	}

	@Override
	//update progress bar in 'chunks' according to status
		public void process(List<Integer> chunks){
			for (int num: chunks){
				_prog.setValue(num);
			}
		}
	
	@Override
	protected void done() {
		
	//if addition is completed successfully, display a message
		if(_complete == 0){
			dialog.setVisible(false);
			dialog.dispose();
			JOptionPane.showMessageDialog(null, "Add Text Complete");
		}
		//if error is found, display an error message
		else if(_complete == 1 || _exit != 0){
			dialog.setVisible(false);
			dialog.dispose();
			JOptionPane.showMessageDialog(null, "Error Encountered: Error adding text to video");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_process.destroy();
		
	}

}