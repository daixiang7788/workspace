package com.example.networkconnect;

import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class RKAccessPoint {

	public static final int SECURITY_NONE = 0;
	private static final int SECURITY_WEP = 1;
	private static final int SECURITY_PSK = 2;
	private static final int SECURITY_EAP = 3;

	private static final int PSK_UNKNOWN = 0;
	private static final int PSK_WPA = 1;
	private static final int PSK_WPA2 = 2;
	private static final int PSK_WPA_WPA2 = 3;

	private static final int INVALID_NETWORK_ID = -1;

	private String ssid;
	private String bssid;
	private int security;
	private int networkId;
	private int pskType = PSK_UNKNOWN;

	public String getSsid() {
		return ssid;
	}

	public String getBssid() {
		return bssid;
	}

	public int getSecurity() {
		return security;
	}

	public int getNetworkId() {
		return networkId;
	}

	public int getPskType() {
		return pskType;
	}

	public WifiConfiguration getmConfig() {
		return mConfig;
	}

	public NetworkInfo getmNetworkInfo() {
		return mNetworkInfo;
	}

	public int getmRssi() {
		return mRssi;
	}

	public WifiInfo getmInfo() {
		return mInfo;
	}

	public ScanResult getScanResult() {
		return mScanResult;
	}

	private WifiConfiguration mConfig;
	private NetworkInfo mNetworkInfo;
	private int mRssi = Integer.MAX_VALUE;;
	private WifiInfo mInfo;
	private ScanResult mScanResult;

	public boolean update(ScanResult result) {
		if (matches(result)) {
			if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
				int oldLevel = getLevel();
	            int oldRssi = getRssi();
	            mRssi = (getRssi() + oldRssi)/2;
	            int newLevel = getLevel();
			}
			if (security == SECURITY_PSK) {
				pskType = getPskType(result);
			}
			return true;
		}
		return false;
	}

	private int getRssi() {
		int rssi = Integer.MIN_VALUE;
		if (mScanResult.level > rssi) {
            rssi = mScanResult.level;
        }
		return rssi;
	}

	public RKAccessPoint(ScanResult mScanResult) {
		loadResult(mScanResult);
	}

	private void loadConfig(WifiConfiguration config) {
		ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
		bssid = config.BSSID;
		security = getSecurity(config);
		networkId = config.networkId;
		mRssi = Integer.MAX_VALUE;
		mConfig = config;
	}

	private void loadResult(ScanResult result) {
		ssid = result.SSID;
		bssid = result.BSSID;
		security = getSecurity(result);
		if (security == SECURITY_PSK)
			pskType = getPskType(result);
		networkId = -1;
		mRssi = result.level;
		mScanResult = result;
	}

	public static String removeDoubleQuotes(String string) {
		int length = string.length();
		if ((length > 1) && (string.charAt(0) == '"')
				&& (string.charAt(length - 1) == '"')) {
			return string.substring(1, length - 1);
		}
		return string;
	}

	public boolean matches(ScanResult result) {
		return ssid.equals(result.SSID) && security == getSecurity(result);
	}

	static int getSecurity(WifiConfiguration config) {
		if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
			return SECURITY_PSK;
		}
		if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
				|| config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
			return SECURITY_EAP;
		}
		return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
	}

	public int getLevel() {
		if (mRssi == Integer.MAX_VALUE) {
			return -1;
		}
		return WifiManager.calculateSignalLevel(mRssi, 4);
	}

	private static int getPskType(ScanResult result) {
		boolean wpa = result.capabilities.contains("WPA-PSK");
		boolean wpa2 = result.capabilities.contains("WPA2-PSK");
		if (wpa2 && wpa) {
			return PSK_WPA_WPA2;
		} else if (wpa2) {
			return PSK_WPA2;
		} else if (wpa) {
			return PSK_WPA;
		} else {
			return PSK_UNKNOWN;
		}
	}

	private static int getSecurity(ScanResult result) {
		if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}

	static String convertToQuotedString(String string) {
		return "\"" + string + "\"";
	}

	protected void generateOpenNetworkConfig() {
		if (security != SECURITY_NONE)
			throw new IllegalStateException();
		if (mConfig != null)
			return;
		mConfig = new WifiConfiguration();
		mConfig.SSID = convertToQuotedString(ssid);
		mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
	}

	public int compareTo(RKAccessPoint other) {
		// Active one goes first.
		if (isActive() && !other.isActive())
			return -1;
		if (!isActive() && other.isActive())
			return 1;

		// Reachable one goes before unreachable one.
		if (mRssi != Integer.MAX_VALUE && other.mRssi == Integer.MAX_VALUE)
			return -1;
		if (mRssi == Integer.MAX_VALUE && other.mRssi != Integer.MAX_VALUE)
			return 1;

		// Configured one goes before unconfigured one.
		if (networkId != INVALID_NETWORK_ID
				&& other.networkId == INVALID_NETWORK_ID)
			return -1;
		if (networkId == INVALID_NETWORK_ID
				&& other.networkId != INVALID_NETWORK_ID)
			return 1;

		// Sort by signal strength.
		int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
		if (difference != 0) {
			return difference;
		}
		// Sort by ssid.
		return ssid.compareToIgnoreCase(other.ssid);
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (mInfo != null)
			result += 13 * mInfo.hashCode();
		result += 19 * mRssi;
		result += 23 * networkId;
		result += 29 * ssid.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RKAccessPoint))
			return false;
		return (this.compareTo((RKAccessPoint) other) == 0);
	}

	public boolean isActive() {
		return mNetworkInfo != null
				&& (networkId != INVALID_NETWORK_ID || mNetworkInfo.getState() != State.DISCONNECTED);
	}
}