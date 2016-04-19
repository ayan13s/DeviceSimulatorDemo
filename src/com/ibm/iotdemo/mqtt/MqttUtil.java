/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.iotdemo.mqtt;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class MqttUtil {
	//Default Mqtt Server Suffix
	public final static String SERVER_SUFFIX = ".messaging.internetofthings.ibmcloud.com";
	
	public final static String DEFAULT_EVENT_ID = "eid";
	public final static String DEFAULT_CMD_ID = "cid";
	public final static String DEFAULT_DEVICE_TYPE = "MQTTDevice";

	/**
	 * This method reads the properties from the config file
	 * @param filePath
	 * @return
	 */
	public static Properties readProperties(String filePath) {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}
}
