/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol;


import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.packet.ProtobufObject;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author godotg
 * @version 3.0
 */
public class SpeedTest extends BaseSpeedTest {

    public static int benchmark = 10_0000;

    /**
     * 单线程性能测试
     * <p>
     * 不使用任何JVM参数：zfoo比protobuf快20%，zfoo比kryo快40%
     * <p>
     * 包体大小：
     * 简单对象，zfoo包体大小8，kryo包体大小5，protobuf包体大小8
     * 常规对象，zfoo包体大小430，kryo包体大小483，protobuf包体大小793
     * 复杂对象，zfoo包体大小2216，kryo包体大小2528，protobuf包体大小5091
     */
    @Ignore
    @Test
    public void singleThreadBenchmarks() {
        if (benchmark <= 0 || benchmark >= 10_0000_0000) {
            return;
        }
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
        System.out.println(StringUtils.format("[单线程性能测试-->[benchmark:{}]]", benchmark));

        zfooTest();
        protobufTest();
        kryoTest();

        // 递归执行，多跑几遍
        benchmark = benchmark * 2;
        singleThreadBenchmarks();
    }

    /**
     * 多线程性能测试
     */
    @Ignore
    @Test
    public void multipleThreadBenchmarks() throws InterruptedException {
        if (benchmark <= 0 || benchmark >= 10_0000_0000) {
            return;
        }
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
        System.out.println(StringUtils.format("[多线程性能测试-->[benchmark:{}]]", benchmark));

        zfooMultipleThreadTest();
        protobufMultipleThreadTest();
        kryoMultipleThreadTest();

        benchmark = benchmark * 2;
        multipleThreadBenchmarks();
    }

    @Ignore
    @Test
    public void zfooTest() {
        // netty的ByteBuf做了更多的安全检测，java自带的ByteBuffer并没有做安全检测，为了公平，把不需要的检测去掉
        // java通过ByteBuffer.allocate(1024 * 8)构造出来的是使用了unsafe的HeapByteBuffer，为了公平，使用netty中带有unsafe操作的UnpooledUnsafeHeapByteBuf
        System.setProperty("io.netty.buffer.checkAccessible", "false");
        System.setProperty("io.netty.buffer.checkBounds", "false");

        zfooTest(simpleObject, "简单对象");
        zfooTest(normalObject, "常规对象");
        zfooTest(complexObject, "复杂对象");
    }

    public void zfooTest(Object obj, String objName) {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);

        // 序列化和反序列化
        IPacket iPacket = (IPacket) obj;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            // 把对象序列化到buffer中
            ProtocolManager.write(buffer, iPacket);

            // 从buffer中反序列化出对象
            var packet = ProtocolManager.read(buffer);
        }

        System.out.println(StringUtils.format("[zfoo] [{}] [thread:{}] [size:{}] [time:{}]", objName, Thread.currentThread().getName(), buffer.writerIndex(), System.currentTimeMillis() - startTime));
    }

    @Ignore
    @Test
    public void jprotobufTest() throws IOException {
        jprotobufTest(jSimpleObject, "简单对象");
    }

    public void jprotobufTest(Object obj, String objName) throws IOException {
        Codec codec = ProtobufProxy.create(obj.getClass());

        // 序列化和反序列化简单对象
        byte[] bytes = new byte[1];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            // 序列化
            bytes = codec.encode(obj);
            // 反序列化
            var newObj = codec.decode(bytes);
        }

        System.out.println(StringUtils.format("[jprotobuf] [{}] [thread:{}] [size:{}] [time:{}]", objName, Thread.currentThread().getName(), bytes.length, System.currentTimeMillis() - startTime));
    }

    @Ignore
    @Test
    public void kryoTest() {
        kryoTest(simpleObject, "简单对象");
        kryoTest(normalObject, "常规对象");
        kryoTest(complexObject, "复杂对象");
    }

    public void kryoTest(Object obj, String objName) {
        try {
            var kryo = kryos.get();

            var output = new Output(1024 * 8);
            var input = new Input(output.getBuffer());

            // 序列化和反序列化
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                input.reset();
                output.reset();
                kryo.writeObject(output, obj);
                var mess = kryo.readObject(input, obj.getClass());
            }

            System.out.println(StringUtils.format("[kryo] [{}] [thread:{}] [size:{}] [time:{}]", objName, Thread.currentThread().getName(), output.position(), System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("JDK17 运行kryo会报错，等kryo修复bug");
        }
    }

    @Ignore
    @Test
    public void protobufTest() {
        try {
            var buffer = new byte[1024 * 8];
            var length = 0;

            // 序列化和反序列化简单对象
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                protobufSimpleObject.writeTo(codedOutputStream);
                length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = ProtobufObject.ProtobufSimpleObject.parseFrom(codeInput);
            }
            System.out.println(StringUtils.format("[protobuf] [简单对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), length, System.currentTimeMillis() - startTime));

            // 序列化和反序列化常规对象
            startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                protobufNormalObject.writeTo(codedOutputStream);
                length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = ProtobufObject.ProtobufNormalObject.parseFrom(codeInput);
            }
            System.out.println(StringUtils.format("[protobuf] [常规对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), length, System.currentTimeMillis() - startTime));

            // 序列化和反序列化复杂对象
            startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                protobufComplexObject.writeTo(codedOutputStream);
                length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = ProtobufObject.ProtobufComplexObject.parseFrom(codeInput);
            }
            System.out.println(StringUtils.format("[protobuf] [复杂对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), length, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Ignore
    @Test
    public void zfooMultipleThreadTest() throws InterruptedException {
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            executors[i].execute(() -> {
                zfooTest();
                countdown.countDown();
            });
        }
        countdown.await();
    }

    @Ignore
    @Test
    public void kryoMultipleThreadTest() throws InterruptedException {
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            executors[i].execute(() -> {
                kryoTest();
                countdown.countDown();
            });
        }
        countdown.await();
    }

    @Ignore
    @Test
    public void protobufMultipleThreadTest() throws InterruptedException {
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            executors[i].execute(() -> {
                protobufTest();
                countdown.countDown();
            });
        }
        countdown.await();
    }

    /**
     * 简单和复杂对象的序列化和反序列化测试，这个其实是基于ProtoManager.initProtocol初始化协议后执行的
     */
    @Test
    public void cmEnhanceMessTest() {
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        // 简单对象序列化和反序列化测试
        // 序列化：把normalObject序列化一下写到buffer中
        ProtocolManager.write(buffer, normalObject);
        // 反序列化：从buffer中反序列化为协议包
        var packet = ProtocolManager.read(buffer);

        buffer.clear();

        // 复杂对象序列化和反序列化测试
        ProtocolManager.write(buffer, complexObject);
        packet = ProtocolManager.read(buffer);

        buffer.clear();
    }
}
