package com.ibm.iotdemo.mqtt;

import java.io.Serializable;

public class ServerAuthVO implements Serializable {
		String appKey;
		String authToken;
		String uid;
		boolean isOTPNeeded;
		boolean isOTPDone;
		boolean isFromFile;

		public String getAppKey() {
			return appKey;
		}
		public void setAppKey(String appKey) {
			this.appKey = appKey;
		}
		public String getAuthToken() {
			return authToken;
		}
		public void setAuthToken(String authToken) {
			this.authToken = authToken;
		}
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
		public boolean isOTPNeeded() {
			return isOTPNeeded;
		}
		public void setOTPNeeded(boolean isOTPNeeded) {
			this.isOTPNeeded = isOTPNeeded;
		}
		public boolean isOTPDone() {
			return isOTPDone;
		}
		public void setOTPDone(boolean isOTPDone) {
			this.isOTPDone = isOTPDone;
		}
		public boolean isFromFile() {
			return isFromFile;
		}
		public void setFromFile(boolean isFromFile) {
			this.isFromFile = isFromFile;
		}
}
