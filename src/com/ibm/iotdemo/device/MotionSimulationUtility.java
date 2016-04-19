package com.ibm.iotdemo.device;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.json.JSONObject;

public class MotionSimulationUtility implements SimulationUtilityInterface {

	protected double	oa;
	protected double	ob;
	protected double	og;
	
	@Override
	public Object[] getNextData() {

        oa = oa + Math.random() * 10 - 5;
        ob = ob + Math.random() * 10 - 5;
        og = og + Math.random() * 10 - 5;

		if(oa > 500) oa = 500; if(oa < -500) oa = -500;
		if(ob > 500) ob = 500; if(ob < -500) ob = -500;
		if(og > 500) og = 500; if(og < -500) og = -500;

		return new Object[] { oa, ob, og };
    }
	
	@Override
	public JSONObject convertDataToJason(Object[] obj) {
		JSONObject jsonTop = new JSONObject();
		JSONObject jsonObj = new JSONObject();
		try{
			jsonObj.put("oa", obj[0]);
			jsonObj.put("ob", obj[1]);
			jsonObj.put("og", obj[2]);
			jsonTop.put("motion", jsonObj);
			jsonTop.put("event", "data");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return jsonTop;
    }

	@Override
	public void reset() {
        oa = 0;
        ob = 0;
        og = 0;
	}

	public double getOa() {
		return oa;
	}

	public double getOb() {
		return ob;
	}

	public double getOg() {
		return og;
	}
}
