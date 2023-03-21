package com.zfoo.protocol;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.packet.JSimpleObject;
import com.zfoo.protocol.packet.ProtobufObject;
import com.zfoo.protocol.packet.SimpleObject;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author linda
 * @create 2023-03-18-16:29
 **/
public class RandomSpeedTest extends SpeedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomSpeedTest.class);

    public static final Random random = new Random();

    public static final String zfoo = "zfoo";
    public static final String protobuf = "protobuf";
    public static final String jprotobuf = "jprotobuf";

    public static final List<SimpleObject> simpleObjects = new ArrayList<>(1024);
    public static final List<ProtobufObject.ProtobufSimpleObject> protobufSimpleObjects = new ArrayList<>(1024);
    public static final List<JSimpleObject> jSimpleObjects = new ArrayList<>(1024);

    @Before
    public void beforeTest() {
        // netty的ByteBuf做了更多的安全检测，java自带的ByteBuffer并没有做安全检测，为了公平，把不需要的检测去掉
        // java通过ByteBuffer.allocate(1024 * 8)构造出来的是使用了unsafe的HeapByteBuffer，为了公平，使用netty中带有unsafe操作的UnpooledUnsafeHeapByteBuf
        System.setProperty("io.netty.buffer.checkAccessible", "false");
        System.setProperty("io.netty.buffer.checkBounds", "false");
    }

    @Ignore
    @Test
    public void singleThreadRandomBenchmarks() {
        int times = 1;
        System.out.println(StringUtils.format("[单线程随机性能测试-->[benchmark:{}]]", times));

        beforeRandomSimple(times);

        zfooRandomSimpleTest();
        protobufRandomSimpleTest();
        kryoRandomSimpleTest();
        jprotobufRandomSimpleTest();
    }

    /**
     * 初始化随机简单对象数据
     *
     * @param times 次数
     */
    public void beforeRandomSimple(int times) {
        for (int i = 0; i < times; i++) {
            int c = random.nextInt();
            boolean g = random.nextBoolean();

            // zfoo,kryo
            SimpleObject simpleObject = new SimpleObject();
            simpleObject.setC(c);
            simpleObject.setG(g);

            // protobuf
            var builder = ProtobufObject.ProtobufSimpleObject.newBuilder();
            builder.setC(c);
            builder.setG(g);
            ProtobufObject.ProtobufSimpleObject protobufSimpleObject = builder.build();

            // jprotobuf
            JSimpleObject jSimpleObject = new JSimpleObject();
            jSimpleObject.setC(c);
            jSimpleObject.setG(g);

            simpleObjects.add(simpleObject);
            protobufSimpleObjects.add(protobufSimpleObject);
            jSimpleObjects.add(jSimpleObject);
        }
    }

    /**
     * zfoo随机简单对象测试
     */
    public void zfooRandomSimpleTest() {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        long totalSize = 0;
        long startTime = System.currentTimeMillis();
        for (SimpleObject object : simpleObjects) {
            buffer.clear();
            // 把对象序列化到buffer中
            ProtocolManager.write(buffer, object);

            // 从buffer中反序列化出对象
            var packet = ProtocolManager.read(buffer);

            // zfoo包含了2个字节的包id，其他方式没算，所以这里也不算
            int length = buffer.writerIndex() - 2;
            totalSize += length;
        }

        System.out.println(StringUtils.format("[zfoo] [随机简单对象]--- [thread:{}] [totalSize:{}] [time:{}]", Thread.currentThread().getName(), totalSize, System.currentTimeMillis() - startTime));
    }

    /**
     *
     */
    public void protobufRandomSimpleTest() {
        try {
            var buffer = new byte[1024 * 8];
            long totalSize = 0;

            // 序列化和反序列化简单对象
            long startTime = System.currentTimeMillis();
            for (ProtobufObject.ProtobufSimpleObject object : protobufSimpleObjects) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                object.writeTo(codedOutputStream);
                var length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = object.parseFrom(codeInput);
                totalSize += length;
            }
            System.out.println(StringUtils.format("[protobuf] [随机简单对象]--- [thread:{}] [totalSize:{}] [time:{}]", Thread.currentThread().getName(), totalSize, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void kryoRandomSimpleTest() {
        try {
            var kryo = kryos.get();

            var output = new Output(1024 * 8);
            var input = new Input(output.getBuffer());
            long totalSize = 0;

            // 序列化和反序列化
            long startTime = System.currentTimeMillis();
            for (SimpleObject object : simpleObjects) {
                input.reset();
                output.reset();
                kryo.writeObject(output, object);
                var mess = kryo.readObject(input, object.getClass());
                totalSize += output.position();
            }

            System.out.println(StringUtils.format("[kryo] [随机简单对象]--- [thread:{}] [totalSize:{}] [time:{}]", Thread.currentThread().getName(), totalSize, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("JDK17 运行kryo会报错，等kryo修复bug");
        }
    }

    public void jprotobufRandomSimpleTest() {
        try {
            Codec<JSimpleObject> codec = ProtobufProxy.create(JSimpleObject.class);

            // 序列化和反序列化简单对象
            byte[] bytes = new byte[1];
            long startTime = System.currentTimeMillis();
            long totalSize = 0;
            for (JSimpleObject object : jSimpleObjects) {
                // 序列化
                bytes = codec.encode(object);
                // 反序列化
                var newObj = codec.decode(bytes);
                totalSize += bytes.length;
            }

            System.out.println(StringUtils.format("[jprotobuf] [随机简单对象]--- [thread:{}] [totalSize:{}] [time:{}]", Thread.currentThread().getName(), totalSize, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void int278() {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        ByteBufUtils.writeIntShow(buffer, 278);
    }
}