/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.test.uiautomator.aupt.camera;

import android.support.test.aupt.AuptTestCase;
import android.platform.test.helpers.GoogleCameraHelperImpl;

/**
 * Tests for the camera
 */
public class CameraStressTest extends AuptTestCase {
    private GoogleCameraHelperImpl mHelper;
    private int videoTimeMS = 5 * 1000;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mHelper = new GoogleCameraHelperImpl(getInstrumentation());
        if (getParams().containsKey("video-duration")) {
            videoTimeMS = Integer.parseInt(getParams().getString("video-duration"));
        }
    }

    public void testCameraStressStillCaptureBack() {
        mHelper.open();
        mHelper.goToBackCamera();
        mHelper.goToCameraMode();
        mHelper.capturePhoto();
        mHelper.exit();
    }

    public void testCameraStressStillCaptureFront() {
        mHelper.open();
        mHelper.goToFrontCamera();
        mHelper.goToCameraMode();
        mHelper.capturePhoto();
        mHelper.exit();
    }

    public void testCameraStressVideoBasicBack() {
        mHelper.open();
        mHelper.goToBackCamera();
        mHelper.goToVideoMode();
        mHelper.captureVideo(videoTimeMS);
        mHelper.exit();
    }

    public void testCameraStressVideoBasicFront() {
        mHelper.open();
        mHelper.goToFrontCamera();
        mHelper.goToVideoMode();
        mHelper.captureVideo(videoTimeMS);
        mHelper.exit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
