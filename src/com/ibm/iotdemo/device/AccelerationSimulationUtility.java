package com.ibm.iotdemo.device;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.json.JSON;
import org.apache.commons.json.JSONObject;

public class AccelerationSimulationUtility implements SimulationUtilityInterface {

	protected double ax;
	protected double ay;
	protected double az;

	@Override
	public Object[] getNextData() {

		ax = Math.random() * 5.0 - 2.5;
		ay = Math.random() * 5.0 - 2.5;
		az = Math.random() * 5.0 - 2.5;

		return new Object[] { ax, ay, az };
	}

	@Override
	public JSONObject convertDataToJason(Object[] obj) {
		JSONObject jsonTop = new JSONObject();
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("ax", obj[0]);
			jsonObj.put("ay", obj[1]);
			jsonObj.put("az", obj[2]);
			jsonTop.put("acceleration", jsonObj);
			jsonTop.put("event", "data");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonTop;
	}

	@Override
	public void reset() {
		ax = 0;
		ay = 0;
		az = 0;
	}

	public double getAx() {
		return ax;
	}

	public double getAy() {
		return ay;
	}

	public double getAz() {
		return az;
	}

}
