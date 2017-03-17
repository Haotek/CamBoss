package com.tutk.P2PCam264.object;

import android.graphics.Bitmap;

public class VideoFile {
	private String videoPath;
	private String videoName;	
	private String videoDuration;
	private Bitmap videoImage;
	
	public void setPath(String path){
		videoPath = path;
	}
	
	public void setName(String name){
		videoName = name;
	}
	
	public void setImage(Bitmap bmp){
		videoImage = bmp;
	}
	
	public void setDuration(String duration){
		videoDuration = duration;
	}
	
	public String getPath(){
		return videoPath;
	}
	
	public String getName(){
		return videoName;
	}
	
	public Bitmap getImage(){
		return videoImage;
	}
	
	public String getDuration(){
		return videoDuration;
	}
}
