package com.zfoo.protocol;

import com.esotericsoftware.kryo.Kryo;
import com.google.protobuf.ByteString;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.packet.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author linda
 * @create 2023-03-18-16:14
 **/
public class BaseSpeedTest {

    public static final int threadNum = Runtime.getRuntime().availableProcessors() - 1;
    public static final ExecutorService[] executors = new ExecutorService[threadNum];

    // kryo协议注册
    public static final ThreadLocal<Kryo> kryos = new ThreadLocal<>() {
        @Override
        protected Kryo initialValue() {
            var kryo = new Kryo();
            kryo.register(VeryBigObject.class);
            kryo.register(ComplexObject.class);
            kryo.register(NormalObject.class);
            kryo.register(SimpleObject.class);
            kryo.register(ObjectA.class);
            kryo.register(ObjectB.class);
            kryo.register(byte[].class);
            kryo.register(Byte[].class);
            kryo.register(short[].class);
            kryo.register(Short[].class);
            kryo.register(int[].class);
            kryo.register(Integer[].class);
            kryo.register(long[].class);
            kryo.register(Long[].class);
            kryo.register(float[].class);
            kryo.register(Float[].class);
            kryo.register(double[].class);
            kryo.register(Double[].class);
            kryo.register(boolean[].class);
            kryo.register(Boolean[].class);
            kryo.register(char[].class);
            kryo.register(Character[].class);
            kryo.register(String[].class);
            kryo.register(ObjectA[].class);
            kryo.register(ArrayList.class);
            kryo.register(HashSet.class);
            kryo.register(HashMap.class);
            // 关闭循环引用，提高性能
            kryo.setReferences(false);
            return kryo;
        }
    };

    static {
        var op = GenerateOperation.NO_OPERATION;

        // 这行加上，会在protocol目录下，生成jsProtocol文件夹及其对应的js协议文件
//        op.getGenerateLanguages().add(CodeLanguage.Cpp);
//        op.getGenerateLanguages().add(CodeLanguage.JavaScript);
//        op.getGenerateLanguages().add(CodeLanguage.TypeScript);
//        op.getGenerateLanguages().add(CodeLanguage.Lua);
//        op.getGenerateLanguages().add(CodeLanguage.CSharp);
//        op.getGenerateLanguages().add(CodeLanguage.GdScript);

        // 需要protocol协议的字段里面都加上JProtobuf注解才能用
//        op.setProtocolParam("protobuf=protobuf.xml");
//        op.getGenerateLanguages().add(CodeLanguage.Protobuf);

        // zfoo协议注册(其实就是：将Set里面的协议号和对应的类注册好，这样子就可以根据协议号知道是反序列化为哪个类)
        ProtocolManager.initProtocolAuto(Set.of(ComplexObject.class, NormalObject.class, SimpleObject.class, VeryBigObject.class), op);

        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
    }

    // -------------------------------------------以下为测试用例---------------------------------------------------------------
    // 简单类型
    public static final byte byteValue = 99;
    public static final short shortValue = 9999;
    public static final int intValue = 99999999;
    public static final long longValue = 9999999999999999L;
    public static final float floatValue = 99999999.9F;
    public static final double doubleValue = 99999999.9D;
    public static final char charValue = 'c';
    public static final String charValueString = "c";
    public static final String stringValue = "hello";

    // 数组类型
    public static final boolean[] booleanArray = new boolean[]{true, false, true, false, true};
    public static final byte[] byteArray = new byte[]{Byte.MIN_VALUE, -99, 0, 99, Byte.MAX_VALUE};
    public static final short[] shortArray = new short[]{Short.MIN_VALUE, -99, 0, 99, Short.MAX_VALUE};
    public static final int[] intArray = new int[]{Integer.MIN_VALUE, -99999999, -99, 0, 99, 99999999, Integer.MAX_VALUE};
    public static final int[] intArray1 = new int[]{Integer.MIN_VALUE, -99999999, -99, 0, 99, 99999999, Integer.MAX_VALUE - 1};
    public static final int[] intArray2 = new int[]{Integer.MIN_VALUE, -99999999, -99, 0, 99, 99999999, Integer.MAX_VALUE - 2};
    public static final long[] longArray = new long[]{Long.MIN_VALUE, -9999999999999999L, -99999999L, -99L, 0L, 99L, 99999999L, 9999999999999999L, Long.MAX_VALUE};
    public static final float[] floatArray = new float[]{Float.MIN_VALUE, -99999999.9F, -99.9F, 0F, 99.9F, 99999999.9F, Float.MAX_VALUE};
    public static final double[] doubleArray = new double[]{Double.MIN_VALUE, -99999999.9F, -99.9D, 0D, 99.9D, 99999999.9F, Double.MAX_VALUE};
    public static final char[] charArray = new char[]{'a', 'b', 'c', 'd', 'e'};
    public static final String[] stringArray = new String[]{"a", "b", "c", "d", "e"};

    public static final ObjectA objectA = new ObjectA();
    public static final ObjectB objectB = new ObjectB();
    public static final Map<Integer, String> mapWithInteger = new HashMap<>(Map.of(Integer.MIN_VALUE, "a", -99, "b", 0, "c", 99, "d", Integer.MAX_VALUE, "e"));
    public static final List<Integer> listWithInteger = new ArrayList<>(ArrayUtils.toList(intArray));
    public static final List<Integer> listWithInteger1 = new ArrayList<>(ArrayUtils.toList(intArray1));
    public static final List<Integer> listWithInteger2 = new ArrayList<>(ArrayUtils.toList(intArray2));
    public static final List<ObjectA> listWithObject = new ArrayList<>(List.of(objectA, objectA, objectA));
    public static final List<List<ObjectA>> listListWithObject = new ArrayList<>(List.of(listWithObject, listWithObject, listWithObject));
    public static final List<List<Integer>> listListWithInteger = new ArrayList<>(List.of(listWithInteger, listWithInteger, listWithInteger));
    public static final List<List<List<Integer>>> listListListWithInteger = new ArrayList<>(List.of(listListWithInteger, listListWithInteger, listListWithInteger));
    public static final List<String> listWithString = new ArrayList<>(ArrayUtils.toList(stringArray));
    public static final Set<Integer> setWithInteger = new HashSet<>(ArrayUtils.toList(intArray));
    public static final Set<Set<List<Integer>>> setSetListWithInteger = new HashSet<>(Set.of(new HashSet<>(Set.of(listWithInteger)), new HashSet<>(Set.of(listWithInteger1)), new HashSet<>(Set.of(listWithInteger2))));
    public static final Set<Set<ObjectA>> setSetWithObject = new HashSet<>(Set.of(new HashSet<>(Set.of(objectA))));
    public static final Set<String> setWithString = new HashSet<>(ArrayUtils.toList(stringArray));
    public static final Map<Integer, ObjectA> mapWithObject = new HashMap<>(Map.of(1, objectA, 2, objectA, 3, objectA));
    public static final Map<ObjectA, List<Integer>> mapWithList = new HashMap<>(Map.of(objectA, listWithInteger));
    public static final Map<List<List<ObjectA>>, List<List<List<Integer>>>> mapWithListList = new HashMap<>(Map.of(new ArrayList<>(List.of(listWithObject, listWithObject, listWithObject)), listListListWithInteger));
    public static final List<Map<Integer, String>> listMap = new ArrayList<>(List.of(mapWithInteger, mapWithInteger, mapWithInteger));
    public static final Set<Map<Integer, String>> setMapWithInteger = new HashSet<>(Set.of(mapWithInteger));
    public static final Map<List<Map<Integer, String>>, Set<Map<Integer, String>>> mapListSet = new HashMap<>(Map.of(listMap, setMapWithInteger));
    public static final Byte[] byteBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(byteArray), Byte.class);
    public static final Short[] shortBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(shortArray), Short.class);
    public static final Integer[] integerArray = ArrayUtils.listToArray(ArrayUtils.toList(intArray), Integer.class);
    public static final Long[] longBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(longArray), Long.class);
    public static final List<Long> listWithLong = ArrayUtils.toList(longArray);
    public static final Float[] floatBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(floatArray), Float.class);
    public static final List<Float> listWithFloat = ArrayUtils.toList(floatArray);
    public static final Double[] doubleBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(doubleArray), Double.class);
    public static final List<Double> listWithDouble = ArrayUtils.toList(doubleArray);
    public static final Boolean[] booleanBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(booleanArray), Boolean.class);
    public static final List<Boolean> listWithBoolean = ArrayUtils.toList(booleanArray);
    public static final Character[] charBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(charArray), Character.class);

    public static final ComplexObject complexObject = new ComplexObject();
    public static final NormalObject normalObject = new NormalObject();
    public static final SimpleObject simpleObject = new SimpleObject();

    public static ProtobufObject.ProtobufComplexObject protobufComplexObject = null;
    public static ProtobufObject.ProtobufNormalObject protobufNormalObject = null;
    public static ProtobufObject.ProtobufSimpleObject protobufSimpleObject = null;

    public static final JSimpleObject jSimpleObject = new JSimpleObject();

    static {
        objectA.setA(Integer.MAX_VALUE);
        objectA.setM(mapWithInteger);
        objectA.setObjectB(objectB);
        objectB.setFlag(false);
    }

    static {
        complexObject.setA(byteValue);
        complexObject.setAa(byteValue);
        complexObject.setAaa(byteArray);
        complexObject.setAaaa(byteBoxArray);
        complexObject.setB(shortValue);
        complexObject.setBb(shortValue);
        complexObject.setBbb(shortArray);
        complexObject.setBbbb(shortBoxArray);
        complexObject.setC(intValue);
        complexObject.setCc(intValue);
        complexObject.setCcc(intArray);
        complexObject.setCccc(integerArray);
        complexObject.setD(longValue);
        complexObject.setDd(longValue);
        complexObject.setDdd(longArray);
        complexObject.setDddd(longBoxArray);
        complexObject.setE(floatValue);
        complexObject.setEe(floatValue);
        complexObject.setEee(floatArray);
        complexObject.setEeee(floatBoxArray);
        complexObject.setF(doubleValue);
        complexObject.setFf(doubleValue);
        complexObject.setFff(doubleArray);
        complexObject.setFfff(doubleBoxArray);
        complexObject.setG(true);
        complexObject.setGg(true);
        complexObject.setGgg(booleanArray);
        complexObject.setGggg(booleanBoxArray);
        complexObject.setH(charValue);
        complexObject.setHh(charValue);
        complexObject.setHhh(charArray);
        complexObject.setHhhh(charBoxArray);
        complexObject.setJj(stringValue);
        complexObject.setJjj(stringArray);
        complexObject.setKk(objectA);
        complexObject.setKkk(new ObjectA[]{objectA, objectA});

        complexObject.setL(listWithInteger);
        complexObject.setLl(listListListWithInteger);
        complexObject.setLll(listListWithObject);
        complexObject.setLlll(listWithString);
        complexObject.setLllll(listMap);

        complexObject.setM(mapWithInteger);
        complexObject.setMm(mapWithObject);
        complexObject.setMmm(mapWithList);
        complexObject.setMmmm(mapWithListList);
        complexObject.setMmmmm(mapListSet);

        complexObject.setS(setWithInteger);
        complexObject.setSs(setSetListWithInteger);
        complexObject.setSss(setSetWithObject);
        complexObject.setSsss(setWithString);
        complexObject.setSssss(setMapWithInteger);

        normalObject.setA(byteValue);
        normalObject.setAaa(byteArray);
        normalObject.setB(shortValue);
        normalObject.setC(intValue);
        normalObject.setD(longValue);
        normalObject.setE(floatValue);
        normalObject.setF(doubleValue);
        normalObject.setG(true);
        normalObject.setJj(stringValue);
        normalObject.setKk(objectA);

        normalObject.setL(listWithInteger);
        normalObject.setLl(listWithLong);
        normalObject.setLll(listWithObject);
        normalObject.setLlll(listWithString);

        normalObject.setM(mapWithInteger);
        normalObject.setMm(mapWithObject);

        normalObject.setS(setWithInteger);
        normalObject.setSsss(setWithString);

        simpleObject.setC(intValue);
        simpleObject.setG(true);

        jSimpleObject.setC(intValue);
        jSimpleObject.setG(true);

        // protobuf相关
        var protobufObjectB = ProtobufObject.ObjectB.newBuilder().setFlag(false).build();
        var protobufObjectA = ProtobufObject.ObjectA.newBuilder().setA(Integer.MAX_VALUE).putAllM(mapWithInteger).setObjectB(protobufObjectB).build();
        var protobufListInteger = ProtobufObject.ListInteger.newBuilder().addAllA(listWithInteger).build();
        var protobufListListInteger = ProtobufObject.ListListInteger.newBuilder().addAllA(List.of(protobufListInteger, protobufListInteger, protobufListInteger)).build();
        var protobufListListListInteger = ProtobufObject.ListListListInteger.newBuilder().addAllA(List.of(protobufListListInteger, protobufListListInteger, protobufListListInteger)).build();
        var protobufListObjectA = ProtobufObject.ListObjectA.newBuilder().addAllA(List.of(protobufObjectA, protobufObjectA, protobufObjectA)).build();
        var protobufListListObjectA = ProtobufObject.ListListObjectA.newBuilder().addAllA(List.of(protobufListObjectA, protobufListObjectA, protobufListObjectA)).build();
        var rawProtobufListListObjectA = List.of(protobufListObjectA, protobufListObjectA, protobufListObjectA);
        var rawProtobufListListListInteger = List.of(protobufListListInteger, protobufListListInteger, protobufListListInteger);
        var rawProtobufListWithObject = new ArrayList<>(List.of(protobufObjectA, protobufObjectA, protobufObjectA));
        var protobufMapIntegerString = ProtobufObject.MapIntegerString.newBuilder().putAllA(mapWithInteger).build();
        var rawProtobufListMapIntegerString = List.of(protobufMapIntegerString, protobufMapIntegerString, protobufMapIntegerString);
        var protobufMapObjectA = ProtobufObject.MapObjectA.newBuilder().setKey(protobufObjectA).setValue(protobufListInteger).build();
        var protobufMapListListObjectA = ProtobufObject.MapListListObjectA.newBuilder().setKey(protobufListListObjectA).setValue(protobufListListListInteger).build();
        var protobufListMapIntegerStringKey = ProtobufObject.ListMapIntegerString.newBuilder().addAllA(rawProtobufListMapIntegerString).build();
        var protobufListMapIntegerStringValue = ProtobufObject.ListMapIntegerString.newBuilder().addAllA(List.of(protobufMapIntegerString)).build();
        var protobufMapListMapInteger = ProtobufObject.MapListMapInteger.newBuilder().setKey(protobufListMapIntegerStringKey).setValue(protobufListMapIntegerStringValue).build();


        var protobufComplexBuilder = ProtobufObject.ProtobufComplexObject.newBuilder();
        var protobufNormalBuilder = ProtobufObject.ProtobufNormalObject.newBuilder();
        var protobufSimpleBuilder = ProtobufObject.ProtobufSimpleObject.newBuilder();
        protobufComplexBuilder.setA(byteValue);
        protobufComplexBuilder.setAa(byteValue);
        protobufComplexBuilder.setAaa(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setAaaa(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setB(shortValue);
        protobufComplexBuilder.setBb(shortValue);
        protobufComplexBuilder.setBbb(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setBbbb(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setC(intValue);
        protobufComplexBuilder.setCc(intValue);
        protobufComplexBuilder.addAllCcc(listWithInteger);
        protobufComplexBuilder.addAllCccc(listWithInteger);
        protobufComplexBuilder.setD(longValue);
        protobufComplexBuilder.setDd(longValue);
        protobufComplexBuilder.addAllDdd(listWithLong);
        protobufComplexBuilder.addAllDddd(listWithLong);
        protobufComplexBuilder.setE(floatValue);
        protobufComplexBuilder.setEe(floatValue);
        protobufComplexBuilder.addAllEee(listWithFloat);
        protobufComplexBuilder.addAllEeee(listWithFloat);
        protobufComplexBuilder.setF(doubleValue);
        protobufComplexBuilder.setFf(doubleValue);
        protobufComplexBuilder.addAllFff(listWithDouble);
        protobufComplexBuilder.addAllFfff(listWithDouble);
        protobufComplexBuilder.setG(true);
        protobufComplexBuilder.setGg(true);
        protobufComplexBuilder.addAllGgg(listWithBoolean);
        protobufComplexBuilder.addAllGggg(listWithBoolean);
        protobufComplexBuilder.setH(charValueString);
        protobufComplexBuilder.setHh(charValueString);
        protobufComplexBuilder.addAllHhh(listWithString);
        protobufComplexBuilder.addAllHhhh(listWithString);
        protobufComplexBuilder.setJj(stringValue);
        protobufComplexBuilder.addAllJjj(listWithString);
        protobufComplexBuilder.setKk(protobufObjectA);
        protobufComplexBuilder.addAllKkk(rawProtobufListWithObject);
        protobufComplexBuilder.addAllL(listWithInteger);
        protobufComplexBuilder.addAllLl(rawProtobufListListListInteger);
        protobufComplexBuilder.addAllLll(rawProtobufListListObjectA);
        protobufComplexBuilder.addAllLlll(listWithString);
        protobufComplexBuilder.addAllLllll(rawProtobufListMapIntegerString);
        protobufComplexBuilder.putAllM(mapWithInteger);
        protobufComplexBuilder.putAllMm(Map.of(1, protobufObjectA, 2, protobufObjectA, 3, protobufObjectA));
        protobufComplexBuilder.addMmm(protobufMapObjectA);
        protobufComplexBuilder.addMmmm(protobufMapListListObjectA);
        protobufComplexBuilder.addMmmmm(protobufMapListMapInteger);
        protobufComplexBuilder.addAllS(listWithInteger);
        protobufComplexBuilder.addAllSs(rawProtobufListListListInteger);
        protobufComplexBuilder.addAllSss(rawProtobufListListObjectA);
        protobufComplexBuilder.addAllSsss(listWithString);
        protobufComplexBuilder.addAllSssss(rawProtobufListMapIntegerString);
        protobufComplexObject = protobufComplexBuilder.build();

        protobufNormalBuilder.setA(byteValue);
        protobufNormalBuilder.setAaa(ByteString.copyFrom(byteArray));
        protobufNormalBuilder.setB(shortValue);
        protobufNormalBuilder.setC(intValue);
        protobufNormalBuilder.setD(intValue);
        protobufNormalBuilder.setE(longValue);
        protobufNormalBuilder.setF(doubleValue);
        protobufNormalBuilder.setG(true);
        protobufNormalBuilder.setJj(stringValue);
        protobufNormalBuilder.setKk(protobufObjectA);
        protobufNormalBuilder.addAllL(listWithInteger);
        protobufNormalBuilder.addAllLl(listWithLong);
        protobufNormalBuilder.addAllLll(rawProtobufListWithObject);
        protobufNormalBuilder.addAllLlll(listWithString);
        protobufNormalBuilder.putAllM(mapWithInteger);
        protobufNormalBuilder.putAllMm(Map.of(1, protobufObjectA, 2, protobufObjectA, 3, protobufObjectA));
        protobufNormalBuilder.addAllS(listWithInteger);
        protobufNormalBuilder.addAllSsss(listWithString);
        protobufNormalObject = protobufNormalBuilder.build();

        protobufSimpleBuilder.setC(intValue);
        protobufSimpleBuilder.setG(true);
        protobufSimpleObject = protobufSimpleBuilder.build();
    }
}