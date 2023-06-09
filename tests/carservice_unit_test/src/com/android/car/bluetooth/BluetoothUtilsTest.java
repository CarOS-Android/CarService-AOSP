/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.car.bluetooth;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.bluetooth.BluetoothDevice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BluetoothUtilsTest {

    @Mock
    private BluetoothDevice mMockBluetoothDevice;

    @Test
    public void testGetDeviceDebugInfo() {
        when(mMockBluetoothDevice.getName()).thenReturn("deviceName");
        when(mMockBluetoothDevice.getAddress()).thenReturn("deviceAddress");

        assertThat(BluetoothUtils.getDeviceDebugInfo(mMockBluetoothDevice))
                .isEqualTo("(name = deviceName, addr = deviceAddress)");
    }

    @Test
    public void testGetDeviceDebugInfo_nullDevice() {
        assertThat(BluetoothUtils.getDeviceDebugInfo(null)).isEqualTo("(null)");
    }
}
