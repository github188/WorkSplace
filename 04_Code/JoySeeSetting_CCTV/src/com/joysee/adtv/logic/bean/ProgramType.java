package com.joysee.adtv.logic.bean;

public class ProgramType{
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ProgramType [id=" + id + ", name=" + name + "]";
	}
	
}
