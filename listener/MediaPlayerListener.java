package listener;
import GUI.PlayFrame;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 * This class is a helper (listener) class that is used to support the
 * functionality of our seek bar in PlayFrame
 * 
 * @author Jenny Lee
 *
 */

public class MediaPlayerListener extends MediaPlayerEventAdapter{
	private PlayFrame _pf;
	
	
	public MediaPlayerListener(PlayFrame pf){
		_pf = pf;
	}
	
	//update the seek bar as the time passes
	public void timeChanged(MediaPlayer mp, long time){
		long total = mp.getLength();
		
		int t = ((int)(1000*time/total));
		
		_pf.updateSeekBar(t);
	}
	
	//place the seek var back to start when the media is finished
	public void finished(MediaPlayer mp){
		mp.setRepeat(true);
		
	}
	

}