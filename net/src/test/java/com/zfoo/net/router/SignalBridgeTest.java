/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.router;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.router.route.SignalBridge;
import com.zfoo.scheduler.util.TimeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class SignalBridgeTest {

    private final int executorSize = EventBus.EXECUTORS_SIZE;
    private final int count = 100_0000;
    private final int totalIndex = 10;


    // equal with 32767
    private static final int SIGNAL_MASK = 0B00000000_00000000_01111111_11111111;

    /**
     * key：signalId
     */
    private static final Map<Integer, SignalAttachment> signalAttachmentMap = new ConcurrentHashMap<>(1000);
    private static final AtomicReferenceArray<SignalAttachment> signalAttachmentArray = new AtomicReferenceArray<>(SIGNAL_MASK + 1);

    @Test
    public void testSingleThreadSpeed_ConcurrentHashMap() throws InterruptedException {
        SignalAttachment signalAttachment = new SignalAttachment();
        long now = System.currentTimeMillis();
        int times = 1_0000;
        while (times-- > 0) {
            for (int i = 0; i < SIGNAL_MASK; i++) {
                signalAttachmentMap.put(i, signalAttachment);
            }
            for (int i = 0; i < SIGNAL_MASK; i++) {
                signalAttachmentMap.remove(i);
            }
        }
        System.out.println(System.currentTimeMillis() - now);
    }

    @Test
    public void testSingleThreadSpeed_AtomicReferenceArray() throws InterruptedException {
        SignalAttachment signalAttachment = new SignalAttachment();
        long now = System.currentTimeMillis();
        int times = 1_0000;
        while (times-- > 0) {
            for (int i = 0; i < SIGNAL_MASK; i++) {
                signalAttachmentArray.compareAndSet(i, null, signalAttachment);
            }
            for (int i = 0; i < SIGNAL_MASK; i++) {
                signalAttachmentArray.lazySet(i, null);
            }
        }

        System.out.println(System.currentTimeMillis() - now);
    }

    @Test
    public void test() throws InterruptedException {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 12; i++) {
            arrayTest();
        }
        SignalBridge.status();
        System.out.println(SignalAttachment.ATOMIC_ID.get() + "  totalCost:" + (System.currentTimeMillis() - now));
    }

    public void arrayTest() throws InterruptedException {
        var startTime = TimeUtils.currentTimeMillis();

        var countDownLatch = new CountDownLatch(executorSize);
        for (var i = 0; i < executorSize; i++) {
            EventBus.execute(i, new Runnable() {
                @Override
                public void run() {
                    addAndRemoveArray();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println(TimeUtils.currentTimeMillis() - startTime);
    }

    public void addAndRemoveArray() {
        var signalList = new ArrayList<Integer>(totalIndex);
        for (var i = 0; i < count; i++) {
            signalList.clear();
            for (var j = 0; j < totalIndex; j++) {
                var signalAttachment = new SignalAttachment();
                SignalBridge.addSignalAttachment(signalAttachment);
                signalList.add(signalAttachment.getSignalId());
            }

            for (var signalId : signalList) {
                SignalBridge.removeSignalAttachment(signalId);
            }
        }
    }

    @Test
    public void test2() throws InterruptedException {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 12; i++) {
            mapTest();
        }
        SignalBridge.status();
        System.out.println(SignalAttachment.ATOMIC_ID.get() + "  totalCost:" + (System.currentTimeMillis() - now));
    }

    public void mapTest() throws InterruptedException {
        var startTime = TimeUtils.currentTimeMillis();

        var countDownLatch = new CountDownLatch(executorSize);
        for (var i = 0; i < executorSize; i++) {
            EventBus.execute(i, new Runnable() {
                @Override
                public void run() {
                    addAndRemoveMap();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println(TimeUtils.currentTimeMillis() - startTime);
    }

    public void addAndRemoveMap() {
        var signalList = new ArrayList<Integer>(totalIndex);
        for (var i = 0; i < count; i++) {
            signalList.clear();
            for (var j = 0; j < totalIndex; j++) {
                var signalAttachment = new SignalAttachment();
                signalAttachmentMap.put(signalAttachment.getSignalId(), signalAttachment);
                signalList.add(signalAttachment.getSignalId());
            }

            for (var signalId : signalList) {
                signalAttachmentMap.remove(signalId);
            }
        }
    }
}
