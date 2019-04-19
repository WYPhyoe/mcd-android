/*
 * Copyright 2019 Wai Yan (TechBase Software). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wyp.mcd.component.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class NetworkStatusBroadcastReceiver extends BroadcastReceiver {

    private Context mContext;
    private NetworkStatusBroadcastReceiver.NetworkStatusListener networkStatusListener;

    public NetworkStatusBroadcastReceiver(
            Context mContext,
            NetworkStatusBroadcastReceiver.NetworkStatusListener networkStatusListener) {

        this.mContext = mContext;
        this.networkStatusListener = networkStatusListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (AndroidUtil.isInternetAvailable(this.mContext)) {
            networkStatusListener.onInternetAvailable();
        } else {
            networkStatusListener.onInternetUnavailable();
        }
    }

    public void registerToContext() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.mContext.registerReceiver(this, intentFilter);
    }

    public void unregisterFromContext() {

        this.mContext.unregisterReceiver(this);
    }

    public interface NetworkStatusListener {

        void onInternetAvailable();

        void onInternetUnavailable();
    }
}
