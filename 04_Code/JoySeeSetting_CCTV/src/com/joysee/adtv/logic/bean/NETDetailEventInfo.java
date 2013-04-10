package com.joysee.adtv.logic.bean;

import java.util.List;

public class NETDetailEventInfo {
	private int nibble1_id; // <一级分类(按内容)
	private int nibble2_id;// <二级分类(按内容)
	private int programId;// <节目ID
	private String programName;// <节目名称
	private String nibble1;// <一级分类(e.g. '电视剧')
	private String nibble2;// <二级分类(e.g. '电视剧\动作')
	private String desc;// <影视简介
	private String imagepath;// <影视封面
	private List<String> directors;// <导演信息
	private List<String> actors;// <演员信息
	
	public int getNibble1_id() {
		return nibble1_id;
	}



	public void setNibble1_id(int nibble1_id) {
		this.nibble1_id = nibble1_id;
	}



	public int getNibble2_id() {
		return nibble2_id;
	}



	public void setNibble2_id(int nibble2_id) {
		this.nibble2_id = nibble2_id;
	}



	public int getProgramId() {
		return programId;
	}



	public void setProgramId(int programId) {
		this.programId = programId;
	}



	public String getProgramName() {
		return programName;
	}



	public void setProgramName(String programName) {
		this.programName = programName;
	}



	public String getNibble1() {
		return nibble1;
	}



	public void setNibble1(String nibble1) {
		this.nibble1 = nibble1;
	}



	public String getNibble2() {
		return nibble2;
	}



	public void setNibble2(String nibble2) {
		this.nibble2 = nibble2;
	}



	public String getDesc() {
		return desc;
	}



	public void setDesc(String desc) {
		this.desc = desc;
	}



	public String getImagepath() {
		return imagepath;
	}



	public void setImagepath(String imagepath) {
		this.imagepath = imagepath;
	}



	public List<String> getDirectors() {
		return directors;
	}



	public void setDirectors(List<String> directors) {
		this.directors = directors;
	}



	public List<String> getActors() {
		return actors;
	}



	public void setActors(List<String> actors) {
		this.actors = actors;
	}



	@Override
	public String toString() {
		return "NETDetailEventInfo [nibble1_id=" + nibble1_id + ", nibble2_id="
				+ nibble2_id + ", programId=" + programId + ", programName="
				+ programName + ", nibble1=" + nibble1 + ", nibble2=" + nibble2
				+ ", desc=" + desc + ", imagepath=" + imagepath
				+ ", directors=" + directors + ", actors=" + actors + "]";
	}

}
