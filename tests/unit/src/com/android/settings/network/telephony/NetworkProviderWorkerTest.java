/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.network.telephony;

import static com.android.settings.slices.CustomSliceRegistry.PROVIDER_MODEL_SLICE_URI;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;

import androidx.lifecycle.Lifecycle;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.wifitrackerlib.WifiEntry;
import com.android.wifitrackerlib.WifiPickerTracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class NetworkProviderWorkerTest {
    private Context mContext;
    private MockNetworkProviderWorker mMockNetworkProviderWorker;

    @Mock
    WifiPickerTracker mMockWifiPickerTracker;
    @Mock
    private SubscriptionManager mSubscriptionManager;
    @Mock
    private ConnectivityManager mConnectivityManager;
    @Mock
    private TelephonyManager mTelephonyManager;

    @Before
    @UiThreadTest
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(ApplicationProvider.getApplicationContext());

        when(mContext.getSystemService(SubscriptionManager.class)).thenReturn(mSubscriptionManager);
        when(mContext.getSystemService(ConnectivityManager.class)).thenReturn(mConnectivityManager);
        when(mContext.getSystemService(TelephonyManager.class)).thenReturn(mTelephonyManager);
        when(mTelephonyManager.createForSubscriptionId(anyInt())).thenReturn(mTelephonyManager);

        mMockNetworkProviderWorker = new MockNetworkProviderWorker(mContext,
                PROVIDER_MODEL_SLICE_URI);
        mMockNetworkProviderWorker.setWifiPickerTracker(mMockWifiPickerTracker);
    }

    @Test
    @UiThreadTest
    public void onConstructor_shouldBeInCreatedState() {
        assertThat(mMockNetworkProviderWorker.getLifecycle().getCurrentState())
                .isEqualTo(Lifecycle.State.CREATED);
    }

    @Test
    @UiThreadTest
    public void onSlicePinned_shouldBeInResumedState() {
        mMockNetworkProviderWorker.onSlicePinned();

        assertThat(mMockNetworkProviderWorker.getLifecycle().getCurrentState())
                .isEqualTo(Lifecycle.State.RESUMED);
    }

    @Test
    @UiThreadTest
    public void onSliceUnpinned_shouldBeInCreatedState() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.onSliceUnpinned();

        assertThat(mMockNetworkProviderWorker.getLifecycle().getCurrentState())
                .isEqualTo(Lifecycle.State.CREATED);
    }

    @Test
    @UiThreadTest
    public void close_shouldBeInDestroyedState() {
        mMockNetworkProviderWorker.close();

        assertThat(mMockNetworkProviderWorker.getLifecycle().getCurrentState())
                .isEqualTo(Lifecycle.State.DESTROYED);
    }

    @Test
    @UiThreadTest
    public void getWifiEntry_connectedWifiKey_shouldGetConnectedWifi() {
        final String key = "key";
        final WifiEntry connectedWifiEntry = mock(WifiEntry.class);
        when(connectedWifiEntry.getKey()).thenReturn(key);
        when(mMockWifiPickerTracker.getConnectedWifiEntry()).thenReturn(connectedWifiEntry);

        assertThat(mMockNetworkProviderWorker.getWifiEntry(key)).isEqualTo(connectedWifiEntry);
    }

    @Test
    @UiThreadTest
    public void getWifiEntry_reachableWifiKey_shouldGetReachableWifi() {
        final String key = "key";
        final WifiEntry reachableWifiEntry = mock(WifiEntry.class);
        when(reachableWifiEntry.getKey()).thenReturn(key);
        when(mMockWifiPickerTracker.getWifiEntries()).thenReturn(Arrays.asList(reachableWifiEntry));

        assertThat(mMockNetworkProviderWorker.getWifiEntry(key)).isEqualTo(reachableWifiEntry);
    }

    @Test
    @UiThreadTest
    public void onSubscriptionsChanged_notifySubscriptionChanged_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.onSubscriptionsChanged();

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onAirplaneModeChanged_airplaneModeOn_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.onAirplaneModeChanged(false);

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onAirplaneModeChanged_airplaneModeOff_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.onAirplaneModeChanged(true);

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onSignalStrengthChanged_notifySignalStrengthChanged_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.onSignalStrengthChanged();

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onMobileDataEnabledChange_notifyMobileDataEnabledChanged_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.onMobileDataEnabledChange();

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onDataConnectivityChange_notifyDataConnectivityChanged_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.onDataConnectivityChange();

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onServiceStateChanged_notifyPhoneStateListener_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.mPhoneStateListener.onServiceStateChanged(new ServiceState());

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onActiveDataSubscriptionIdChanged_notifyPhoneStateListener_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.mPhoneStateListener.onActiveDataSubscriptionIdChanged(1);

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    @Test
    @UiThreadTest
    public void onDisplayInfoChanged_notifyPhoneStateListener_callUpdateSlice() {
        mMockNetworkProviderWorker.onSlicePinned();
        mMockNetworkProviderWorker.receiveNotification(false);

        mMockNetworkProviderWorker.mPhoneStateListener.onDisplayInfoChanged(
                new TelephonyDisplayInfo(14, 0));

        assertThat(mMockNetworkProviderWorker.hasNotification()).isTrue();
    }

    public class MockNetworkProviderWorker extends NetworkProviderWorker {
        private boolean mHasNotification = false;

        MockNetworkProviderWorker(Context context, Uri uri) {
            super(context, uri);
        }

        public void receiveNotification(boolean inputValue) {
            mHasNotification = inputValue;
        }

        public boolean hasNotification() {
            return mHasNotification;
        }

        @Override
        public void updateSlice() {
            super.updateSlice();
            receiveNotification(true);
        }

        public void setWifiPickerTracker(WifiPickerTracker wifiPickerTracker) {
            mWifiPickerTracker = wifiPickerTracker;
        }
    }
}