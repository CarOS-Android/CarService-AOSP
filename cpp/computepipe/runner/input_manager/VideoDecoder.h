// Copyright 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
#ifndef COMPUTEPIPE_RUNNER_INPUT_MANAGER_VIDEODECODER_H_
#define COMPUTEPIPE_RUNNER_INPUT_MANAGER_VIDEODECODER_H_

#include <media/NdkMediaExtractor.h>

#include <atomic>
#include <memory>
#include <queue>
#include <string>
#include <thread>

#include "InputManager.h"
#include "types/Status.h"

namespace android {
namespace automotive {
namespace computepipe {
namespace runner {
namespace input_manager {

class VideoDecoder {
  public:
    explicit VideoDecoder(const proto::InputStreamConfig& config,
                          std::shared_ptr<InputEngineInterface> engineInterface);

    ~VideoDecoder();
    /**
     * Get the platback frame rate (frame rate at source * speed multiplier).
     */
    float getPlaybackFrameRate();
    /**
     * Set the timestamp in micros since epoch, at which first frame must be generated.
     * All subsequest frames must be generated at timestampMicros +
     * (fraeIndex / framesPerMicrosecond). This ensures that frames generated by multiple
     * VideoDecoder follow same timestamp when they have same initial timestamp and frame rate.
     */
    void setInitialTimestamp(int64_t timestampMicros);

    /**
     * Starts video decoding.
     */
    Status startDecoding();
    /**
     * Blocking call to stop video decoding and release all resources.
     */
    void stopDecoding();

  private:
    Status initializeMediaExtractor();
    Status initializeMediaDecoder();
    void releaseResources();
    void sendEosFlag();

    void addFramesToCodec();
    void popFramesFromCodec();
    bool readDecodedFrame(int64_t frameTimeMicros);

    void decoderThreadFunction();

    std::shared_ptr<InputEngineInterface> mEngine;
    proto::InputStreamConfig mConfig;
    std::string mVideoPath;
    int64_t mStartTimeMicros;

    // Thread to perform video decoding.
    std::unique_ptr<std::thread> mDecoderThead;

    std::atomic<bool> mStopThread = false;

    // Media decoder resources - Owned by mDecoderThead when thread is running.
    AMediaExtractor* mExtractor = nullptr;
    AMediaCodec* mCodec = nullptr;
    int mFd;
    int mIsFdOpen = false;
    bool mExtractorFinished = false;

    // Count of buffers queued to decoder.
    int mCountQueuedBuffers = 0;
    // Stores pair of decoded buffer ix, and buffer info.
    std::queue<std::pair<int, AMediaCodecBufferInfo>> mDecodedBuffers;

    float mPlaybackFrameRate = 0;
    int mLoopbackCount = 1;
};

}  // namespace input_manager
}  // namespace runner
}  // namespace computepipe
}  // namespace automotive
}  // namespace android

#endif  // COMPUTEPIPE_RUNNER_INPUT_MANAGER_VIDEODECODER_H_
