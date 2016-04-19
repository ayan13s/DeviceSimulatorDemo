
package com.ibm.iotdemo.security.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ibm.iotdemo.mqtt.ServerAuthVO;

public class IOTSecurityUtil {

	/**
	 * This method reads the properties from the config file
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getMACAdress(String dId) {
		String strMACAdress = null;
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			byte[] mac = network.getHardwareAddress();

			System.out.print("Current MAC address : ");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			System.out.println("Mac address for " + dId + "is" + strMACAdress);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return strMACAdress;
	}

	public static byte[] encryptString(String strMsg, String strKey, String iv) {
		byte[] encrypted = null;
		IvParameterSpec ivspec = null;
		SecretKeySpec keyspec;
		try {
			// Create key and cipher
			ivspec = new IvParameterSpec(iv.getBytes());
			keyspec = new SecretKeySpec(strKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// encrypt the text
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			encrypted = cipher.doFinal(strMsg.getBytes("UTF-8"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return encrypted;
	}

	public static String decryptString(byte[] byteMsg, String strKey, String iv) {
		String strReturn = null;
		IvParameterSpec ivspec = null;
		SecretKeySpec keyspec;
		;
		try {
			// Create key and cipher
			ivspec = new IvParameterSpec(iv.getBytes());
			keyspec = new SecretKeySpec(strKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// decrypt the text
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			strReturn = new String(cipher.doFinal(byteMsg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strReturn;
	}

	public static String encryptEncodeString(String strMsg, String strKey,
			String iv) {
		String strReturn = null;
		IvParameterSpec ivspec = null;
		SecretKeySpec keyspec;
		;
		try {
			// Create key and cipher
			ivspec = new IvParameterSpec(iv.getBytes());
			keyspec = new SecretKeySpec(strKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// encrypt the text
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(strMsg.getBytes("UTF-8"));
			strReturn = Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strReturn;
	}

	public static String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decryptDecodeString(byte[] strMsg, String strKey,
			String iv) {
		String strReturn = null;
		IvParameterSpec ivspec = null;
		SecretKeySpec keyspec;
		;
		try {
			// Create key and cipher
			ivspec = new IvParameterSpec(iv.getBytes());
			keyspec = new SecretKeySpec(strKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// decrypt the text
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			strReturn = new String(cipher.doFinal(Base64.getDecoder().decode(
					strMsg)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strReturn;
	}
	
	public static String generateOTP() {
		Random r = new Random();
		String otp = new String();
		for (int i = 0; i < 8; i++) {
			otp += r.nextInt(10);
		}
		System.out.println("Generated OTP - " + otp);
		return otp;
	}

	public static boolean saveAuthObjInFile(String strFileName, ServerAuthVO serverObj) {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		boolean isSuccess = false;
		try {
			fout = new FileOutputStream(strFileName);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(serverObj);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	public static ServerAuthVO readAuthObjFromFile(String strFileName) {
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		ServerAuthVO authVO = null;
		try {
			fin = new FileInputStream(strFileName);
			ois = new ObjectInputStream(fin);
			authVO = (ServerAuthVO) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
				fin.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return authVO;
	}
}