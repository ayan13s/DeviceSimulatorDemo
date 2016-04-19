package com.ibm.iotdemo.device;

public interface SimulationCallbackInterface {
	public void onSimulationStarted(Object... params);
	public void onDataGenerated(Object... params);
	public void onDataSent(Object... params);
	public void onSimulationStopped(Object... params);
	public void onDataReceived(Object... params);
}

