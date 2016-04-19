package com.ibm.iotdemo.device;

import org.apache.commons.json.JSONObject;

public interface SimulationUtilityInterface {
	public Object[] getNextData();
	public JSONObject convertDataToJason(Object[] obj);
	public void reset();
}
