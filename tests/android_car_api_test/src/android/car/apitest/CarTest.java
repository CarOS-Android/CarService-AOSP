/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.car.apitest;

import static com.google.common.truth.Truth.assertThat;

import static org.testng.Assert.assertThrows;

import android.car.Car;
import android.car.ICar;
import android.car.annotation.AddedIn;
import android.car.annotation.AddedInOrBefore;
import android.car.annotation.MinimumPlatformSdkVersion;
import android.car.hardware.CarSensorManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.test.suitebuilder.annotation.SmallTest;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SmallTest
public class CarTest {
    private static final long DEFAULT_WAIT_TIMEOUT_MS = 3000;

    private final Context mContext = InstrumentationRegistry.getInstrumentation()
            .getTargetContext();

    private final Semaphore mConnectionWait = new Semaphore(0);

    private ICar mICar;

    private final ServiceConnection mConnectionListener = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CarApiTestBase.assertMainThread();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CarApiTestBase.assertMainThread();
            mICar = ICar.Stub.asInterface(service);
            mConnectionWait.release();
        }
    };

    private void waitForConnection(long timeoutMs) throws InterruptedException {
        mConnectionWait.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @AddedIn(majorVersion = 31)
    @MinimumPlatformSdkVersion(30)
    private static class AnnotationTest1 {
        @AddedIn(majorVersion = 31)
        @MinimumPlatformSdkVersion(29)
        public int val;

        @AddedIn(majorVersion = 31)
        public void method1() {
        }
    }

    @AddedIn(majorVersion = 31, minorVersion = 0)
    @MinimumPlatformSdkVersion(30)
    private static class AnnotationTest2 {
        @AddedIn(majorVersion = 31, minorVersion = 0)
        @MinimumPlatformSdkVersion(29)
        public int val;

        @AddedIn(majorVersion = 31, minorVersion = 0)
        public void method1() {
        }
    }


    @AddedInOrBefore(majorVersion = 31)
    @MinimumPlatformSdkVersion(30)
    private static class AnnotationTest3 {
        @AddedInOrBefore(majorVersion = 31)
        @MinimumPlatformSdkVersion(29)
        public int val;

        @AddedInOrBefore(majorVersion = 31)
        public void method1() {
        }
    }

    @AddedInOrBefore(majorVersion = 31, minorVersion = 0)
    @MinimumPlatformSdkVersion(30)
    private static class AnnotationTest4 {
        @AddedInOrBefore(majorVersion = 31, minorVersion = 0)
        @MinimumPlatformSdkVersion(29)
        public int val;

        @AddedInOrBefore(majorVersion = 31, minorVersion = 0)
        public void method1() {
        }
    }

    @Test
    public void testCarConnection() throws Exception {
        Car car = Car.createCar(mContext, mConnectionListener);
        assertThat(car.isConnected()).isFalse();
        assertThat(car.isConnecting()).isFalse();
        car.connect();
        // TODO fix race here
        // assertTrue(car.isConnecting()); // This makes test flaky.
        waitForConnection(DEFAULT_WAIT_TIMEOUT_MS);
        assertThat(car.isConnected()).isTrue();
        assertThat(car.isConnecting()).isFalse();
        CarSensorManager carSensorManager =
                (CarSensorManager) car.getCarManager(Car.SENSOR_SERVICE);
        assertThat(carSensorManager).isNotNull();
        CarSensorManager carSensorManager2 =
                (CarSensorManager) car.getCarManager(Car.SENSOR_SERVICE);
        assertThat(carSensorManager2).isSameInstanceAs(carSensorManager);
        Object noSuchService = car.getCarManager("No such service");
        assertThat(noSuchService).isNull();
        // double disconnect should be safe.
        car.disconnect();
        car.disconnect();
        assertThat(car.isConnected()).isFalse();
        assertThat(car.isConnecting()).isFalse();
    }

    @Test
    public void testDoubleConnect() throws Exception {
        Car car = Car.createCar(mContext, mConnectionListener);
        assertThat(car.isConnected()).isFalse();
        assertThat(car.isConnecting()).isFalse();
        car.connect();
        assertThrows(IllegalStateException.class, () -> car.connect());
        car.disconnect();
    }

    @Test
    public void testConstructorWithICar() throws Exception {
        Car car = Car.createCar(mContext, mConnectionListener);
        car.connect();
        waitForConnection(DEFAULT_WAIT_TIMEOUT_MS);
        assertThat(mICar).isNotNull();
        Car car2 = new Car(mContext, mICar, null);
        assertThat(car2.isConnected()).isTrue();
    }

    @Test
    public void testApiVersion() throws Exception {
        int ApiVersionTooHigh = 1000000;
        int MinorApiVersionTooHigh = 1000000;
        assertThat(Car.isApiVersionAtLeast(Car.API_VERSION_MAJOR_INT)).isTrue();
        assertThat(Car.isApiVersionAtLeast(ApiVersionTooHigh)).isFalse();

        assertThat(Car.isApiVersionAtLeast(Car.API_VERSION_MAJOR_INT - 1,
                MinorApiVersionTooHigh)).isTrue();
        assertThat(Car.isApiVersionAtLeast(Car.API_VERSION_MAJOR_INT,
                Car.API_VERSION_MINOR_INT)).isTrue();
        assertThat(Car.isApiVersionAtLeast(Car.API_VERSION_MAJOR_INT,
                MinorApiVersionTooHigh)).isFalse();
        assertThat(Car.isApiVersionAtLeast(ApiVersionTooHigh, 0)).isFalse();

        assertThat(Car.isApiAndPlatformVersionAtLeast(Car.API_VERSION_MAJOR_INT,
                Build.VERSION.SDK_INT)).isTrue();
        assertThat(Car.isApiAndPlatformVersionAtLeast(Car.API_VERSION_MAJOR_INT,
                Build.VERSION.SDK_INT + 1)).isFalse();
        assertThat(Car.isApiAndPlatformVersionAtLeast(Car.API_VERSION_MAJOR_INT,
                Car.API_VERSION_MINOR_INT, Build.VERSION.SDK_INT)).isTrue();
        assertThat(Car.isApiAndPlatformVersionAtLeast(Car.API_VERSION_MAJOR_INT,
                Car.API_VERSION_MINOR_INT, Build.VERSION.SDK_INT + 1)).isFalse();
    }
}
