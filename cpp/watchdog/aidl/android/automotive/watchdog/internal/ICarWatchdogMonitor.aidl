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

package android.automotive.watchdog.internal;

import android.automotive.watchdog.internal.ProcessIdentifier;

/**
 * Callback that the CarWatchdog monitor must implement.
 */
oneway interface ICarWatchdogMonitor {
  /**
   * Called when the client has not responded within the given timeout.
   * Watchdog server calls this method, requesting the monitor to dump process information of the
   * clients.
   *
   * @param pids                List of process identifiers of the clients.
   */
  void onClientsNotResponding(in List<ProcessIdentifier> processIdentifiers);
}
