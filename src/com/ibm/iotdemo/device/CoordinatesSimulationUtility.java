package com.ibm.iotdemo.device;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.json.JSONObject;

public class CoordinatesSimulationUtility implements SimulationUtilityInterface {

	protected double latitudeInitial;
	protected double longitudeInitial;

	protected double latitude;
	protected double longitude;

	public CoordinatesSimulationUtility(double latitudeInitial, double longitudeInitial) {
		this.latitudeInitial = latitudeInitial;
		this.longitudeInitial = longitudeInitial;

		latitude = latitudeInitial;
		longitude = longitudeInitial;
	}

	@Override
	public Object[] getNextData() {

		latitude = latitude + (Math.random() - 0.5) / 1000;

		if (latitude > 90)
			latitude = 90;
		if (latitude < -90)
			latitude = -90;

		longitude = longitude + (Math.random() - 0.2) / 1000;

		if (longitude > 180)
			longitude = 180;
		if (longitude < -180)
			longitude = -180;

		return new Object[] { latitude, longitude };
	}

	@Override
	public JSONObject convertDataToJason(Object[] obj) {
		JSONObject jsonTop = new JSONObject();
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("latitude", obj[0]);
			jsonObj.put("longitude", obj[1]);
			jsonTop.put("coordinates", jsonObj);
			jsonTop.put("event", "data");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonTop;
	}

	@Override
	public void reset() {
		latitude = latitudeInitial;
		longitude = longitudeInitial;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
