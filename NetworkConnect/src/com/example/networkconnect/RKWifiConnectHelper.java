package com.example.networkconnect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class RKWifiConnectHelper {

	private Handler mHandler;
	private WifiManager mWifiManager;
	
	public static final int SECURITY_NONE = 0;
	private static final int SECURITY_WEP = 1;
	private static final int SECURITY_PSK = 2;

	Multimap<String, RKAccessPoint> apMap;
	private boolean connecting;
	private WifiInfo lastInfo;
	private List<RKAccessPoint> accessPoints;
	private NetworkInfo lastNetworkInfo;
	private IRKWifiConnectCallback callback;
	private static int mScanId;
	private WifiConfiguration config;

	public WifiConfiguration getCurrentConfig(){
		return config;
	}
	
	public RKWifiConnectHelper(Handler mHandler, WifiManager mWifiManager) {
		this.mHandler = mHandler;
		this.mWifiManager = mWifiManager;
	}

	public boolean connect(String ssid, String pwd) {
		if(!mWifiManager.isWifiEnabled()){
			mWifiManager.setWifiEnabled(true);
		}
		if(ssid ==null || ssid.length()==0){
			if(callback!=null){
				callback.ssidNotFind();
			}
			return false;
		}
		List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
		if(networks!=null){
			for (WifiConfiguration wifiConfiguration : networks) {
				mWifiManager.removeNetwork(wifiConfiguration.networkId);
			}
		}
		List<RKAccessPoint> ssids = null;
		RKAccessPoint ap = null;
		if ((ssids = apMap.getAll(ssid)) == null || ssids.isEmpty()) {
			if(callback!=null){
				callback.ssidNotFind();
			}
			return false;
		}
		for (int i = 0; i < ssids.size(); i++) {
			ap = ssids.get(i);
			if (ap == null) {
				continue;
			}
			if (ap != null) {
				break;
			}
		}
		if(pwd == null ||pwd.length() == 0){
			if(ap.getSecurity()!=RKAccessPoint.SECURITY_NONE){
				if(callback!=null) callback.pwdError();
				return false;
			}
		}
		config = getConfig(ssid, pwd,ap);
		boolean connect = connect(config);
		mWifiManager.startScan();
		return connect;
	}
	
	public void registCallback(IRKWifiConnectCallback callback){
		this.callback = callback;
	}
	
	private boolean connect(WifiConfiguration config){
		if(config == null) return false;
		int networkId = mWifiManager.addNetwork(config);
		return connect(networkId);
	}
	
	private boolean connect(int networkId){
		if(networkId != -1){
			return mWifiManager.enableNetwork(networkId, true);
		}
		return false;
	}

	public void disconnect() {
		List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
		if(list!=null){
			for (WifiConfiguration wifi : list) {
				mWifiManager.removeNetwork(wifi.networkId);
			}
			mWifiManager.saveConfiguration();
		}
	}
	
	private WifiConfiguration getConfig(String ssid, String pwd, RKAccessPoint ap) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		switch (ap.getSecurity()) {
			case SECURITY_NONE: {
				config.allowedKeyManagement.set(KeyMgmt.NONE);
			}
				break;
			case SECURITY_WEP: {
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				int len = pwd.length();
				if ((len == 10) || (len == 26) || (len == 58)
						&& pwd.matches("[0-9A-Fa-f]*")) {
					config.wepKeys[0] = pwd;
				} else {
					config.wepKeys[0] = '"' + pwd + '"';
				}
			}
				break;
			case SECURITY_PSK: {
				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
				if (pwd.matches("[0-9A-Fa-f]{64}")) {
					config.preSharedKey = pwd;
				} else {
					config.preSharedKey = '"' + pwd + '"';
				}
			}
			break;
		}
		return config;
	}
	
	public void updateAccessPoints(){
		if(apMap ==null){
			apMap = new Multimap<String, RKAccessPoint>();
		}
		accessPoints = new ArrayList<RKAccessPoint>();
		List<ScanResult> results = mWifiManager.getScanResults();
		if (results != null) {
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (RKAccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                	RKAccessPoint accessPoint = new RKAccessPoint(result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.getSsid(), accessPoint);
                }
            }
        }
		if(connecting){
			List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
			for (WifiConfiguration config : configs) {
				List<RKAccessPoint> all = apMap.getAll(RKAccessPoint.removeDoubleQuotes(config.SSID));
				if(all==null || all.isEmpty()){
					callback.ssidNotFind();
				}
			}
		}
	}
	
	public void updateNetworkInfo(NetworkInfo mNetworkInfo){
		if(mNetworkInfo!=null){
			connecting = mNetworkInfo.isConnectedOrConnecting();
			this.lastNetworkInfo = mNetworkInfo;
			lastInfo = mWifiManager.getConnectionInfo();
		}
	}
	
	public void release(){
		connecting = false;
		apMap = null;
		config = null;
	}

	private static class Multimap<K, V> {
		private final HashMap<K, List<V>> store = new HashMap<K, List<V>>();
		/** retrieve a non-null list of values with key K */
		List<V> getAll(K key) {
			List<V> values = store.get(key);
			return values != null ? values : Collections.<V> emptyList();
		}

		void put(K key, V val) {
			List<V> curVals = store.get(key);
			if (curVals == null) {
				curVals = new ArrayList<V>(3);
				store.put(key, curVals);
			}
			curVals.add(val);
		}
	}
}
