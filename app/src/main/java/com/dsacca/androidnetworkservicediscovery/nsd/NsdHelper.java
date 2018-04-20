package com.dsacca.androidnetworkservicediscovery.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class NsdHelper {
    private final static String TAG = "NsdHelper";

    private Context mConText;
    private FindDevice finder;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mServiceInfo, mServiceInfoAus;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;


    public interface FindDevice {
        void onDeviceFound(NsdServiceInfo deviceInfo);
    }

    public void setDeviceFinder(FindDevice finder){
        this.finder = finder;
    }

    public NsdHelper(Context context, NsdServiceInfo serviceInfo) {
        this.mConText = context;
        this.mServiceInfo = serviceInfo;
        mNsdManager = (NsdManager) mConText.getSystemService(Context.NSD_SERVICE);
    }

    public void setCustomResolveListener(NsdManager.ResolveListener mResolveListener) {
        this.mResolveListener = mResolveListener;
    }

    public void setCustomDiscoveryListener(NsdManager.DiscoveryListener mDiscoveryListener) {
        this.mDiscoveryListener = mDiscoveryListener;
    }

    public void setCustomRegistrationListener(NsdManager.RegistrationListener mRegistrationListener) {
        this.mRegistrationListener = mRegistrationListener;
    }

    public void startServiceDiscovery() {
        if(mDiscoveryListener == null)
            initializeDiscoveryListener();
        initializeResolveListener();
        mNsdManager.discoverServices(mServiceInfo.getServiceType(), NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    public void startServiceRegistrationListener() {
        if(mRegistrationListener == null)
            initializeRegistrationListener();
        initializeResolveListener();
        mNsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void stopService() {
        if(mResolveListener != null)
            mNsdManager.unregisterService(mRegistrationListener);
        if(mResolveListener != null)
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Registration failed: Error code:" + errorCode);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Unregistration failed: Error code:" + errorCode);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                mServiceInfo.setServiceName(serviceInfo.getServiceName());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                mNsdManager.unregisterService(mRegistrationListener);
            }
        };
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener =  new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "Service discovery starterd");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Service discovery success" + serviceInfo);
                if (!serviceInfo.getServiceType().equals(mServiceInfo.getServiceType())) {
                    Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().equals(mServiceInfo.getServiceName())) {
                    Log.d(TAG, "Same machine: " + mServiceInfo.getServiceName());
                } else if (serviceInfo.getServiceName().contains("NsdChat")){
                    mNsdManager.resolveService(serviceInfo, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Service lost" + serviceInfo);
            }
        };

    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceInfo.getServiceName())) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mServiceInfoAus = serviceInfo;
                finder.onDeviceFound(mServiceInfo);
            }
        };
    }


}
