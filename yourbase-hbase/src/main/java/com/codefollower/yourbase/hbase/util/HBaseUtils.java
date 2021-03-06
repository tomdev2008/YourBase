/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.yourbase.hbase.util;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.MetaScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import com.codefollower.yourbase.constant.ErrorCode;
import com.codefollower.yourbase.hbase.zookeeper.ZooKeeperAdmin;
import com.codefollower.yourbase.message.DbException;
import com.codefollower.yourbase.value.Value;
import com.codefollower.yourbase.value.ValueBoolean;
import com.codefollower.yourbase.value.ValueByte;
import com.codefollower.yourbase.value.ValueBytes;
import com.codefollower.yourbase.value.ValueDate;
import com.codefollower.yourbase.value.ValueDecimal;
import com.codefollower.yourbase.value.ValueDouble;
import com.codefollower.yourbase.value.ValueFloat;
import com.codefollower.yourbase.value.ValueInt;
import com.codefollower.yourbase.value.ValueJavaObject;
import com.codefollower.yourbase.value.ValueLong;
import com.codefollower.yourbase.value.ValueNull;
import com.codefollower.yourbase.value.ValueShort;
import com.codefollower.yourbase.value.ValueString;
import com.codefollower.yourbase.value.ValueStringFixed;
import com.codefollower.yourbase.value.ValueStringIgnoreCase;
import com.codefollower.yourbase.value.ValueTime;
import com.codefollower.yourbase.value.ValueTimestamp;
import com.codefollower.yourbase.value.ValueUuid;

public class HBaseUtils {
    public static final String HBASE_DB_NAME = "hbasedb";
    private static final Configuration conf = HBaseConfiguration.create();
    private static final Random random = new Random(System.currentTimeMillis());
    private static HConnection hConnection;
    private static HBaseAdmin admin;

    private HBaseUtils() {
        // utility class
    }

    public static Configuration getConfiguration() {
        return conf;
    }

    public static byte[] toBytes(String s) {
        return Bytes.toBytes(s);
    }

    public static String toString(byte[] b) {
        return Bytes.toString(b);
    }

    public static Value toValue(byte[] b, int type) {
        if (b == null)
            return ValueNull.INSTANCE;
        switch (type) {
        case Value.NULL:
            return ValueNull.INSTANCE;
        case Value.BYTES:
            return ValueBytes.get(b);
        case Value.UUID:
            return ValueUuid.get(toString(b));
        case Value.JAVA_OBJECT:
            return ValueJavaObject.get(b);
        case Value.BOOLEAN:
            return ValueBoolean.get(Bytes.toBoolean(b));
        case Value.BYTE:
            return ValueByte.get((byte) Bytes.toShort(b));
        case Value.DATE:
            return ValueDate.get(new Date(Bytes.toLong(b)));
        case Value.TIME:
            return ValueTime.get(new Time(Bytes.toLong(b)));
        case Value.TIMESTAMP:
            return ValueTimestamp.get(new Timestamp(Bytes.toLong(b)));
        case Value.DECIMAL:
            return ValueDecimal.get(Bytes.toBigDecimal(b));
        case Value.DOUBLE:
            return ValueDouble.get(Bytes.toDouble(b));
        case Value.FLOAT:
            return ValueFloat.get(Bytes.toFloat(b));
        case Value.INT:
            return ValueInt.get(Bytes.toInt(b));
        case Value.LONG:
            return ValueLong.get(Bytes.toLong(b));
        case Value.SHORT:
            return ValueShort.get(Bytes.toShort(b));
        case Value.STRING:
            return ValueString.get(toString(b));
        case Value.STRING_IGNORECASE:
            return ValueStringIgnoreCase.get(toString(b));
        case Value.STRING_FIXED:
            return ValueStringFixed.get(toString(b));
        case Value.BLOB:
            return ValueBytes.get(b);
        case Value.CLOB:
            return ValueBytes.get(b);
        case Value.ARRAY:
            return ValueBytes.get(b);
        case Value.RESULT_SET:
            return ValueBytes.get(b);
        default:
            throw DbException.get(ErrorCode.UNKNOWN_DATA_TYPE_1, "type=" + type);
        }
    }

    public static byte[] toBytes(Value v) {
        int type = v.getType();
        switch (type) {
        case Value.NULL:
            return toBytes("NULL");
        case Value.BYTES:
            return v.getBytes();
        case Value.UUID:
            return toBytes(v.getString());
        case Value.JAVA_OBJECT:
            return v.getBytes();
        case Value.BOOLEAN:
            return Bytes.toBytes(v.getBoolean());
        case Value.BYTE:
            return Bytes.toBytes(v.getShort());
        case Value.DATE:
            return Bytes.toBytes(v.getDate().getTime());
        case Value.TIME:
            return Bytes.toBytes(v.getTime().getTime());
        case Value.TIMESTAMP:
            return Bytes.toBytes(v.getTimestamp().getTime());
        case Value.DECIMAL:
            return Bytes.toBytes(v.getBigDecimal());
        case Value.DOUBLE:
            return Bytes.toBytes(v.getDouble());
        case Value.FLOAT:
            return Bytes.toBytes(v.getFloat());
        case Value.INT:
            return Bytes.toBytes(v.getInt());
        case Value.LONG:
            return Bytes.toBytes(v.getLong());
        case Value.SHORT:
            return Bytes.toBytes(v.getShort());
        case Value.STRING:
            return toBytes(v.getString());
        case Value.STRING_IGNORECASE:
            return toBytes(v.getString());
        case Value.STRING_FIXED:
            return toBytes(v.getString());
        case Value.BLOB:
            return v.getBytes();
        case Value.CLOB:
            return v.getBytes();
        case Value.ARRAY:
            return v.getBytes();
        case Value.RESULT_SET:
            return v.getBytes();
        default:
            throw DbException.get(ErrorCode.UNKNOWN_DATA_TYPE_1, "type=" + type);
        }
    }

    public static String createURL(HRegionLocation regionLocation) {
        return createURL(regionLocation.getHostname(), ZooKeeperAdmin.getTcpPort(regionLocation));
    }

    public static String createURL(ServerName sn) {
        return createURL(sn.getHostname(), ZooKeeperAdmin.getTcpPort(sn));
    }

    public static String createURL(String hostname, int port) {
        StringBuilder url = new StringBuilder(100);
        url.append("jdbc:yourbase:tcp://").append(hostname).append(":").append(port).append("/").append(HBASE_DB_NAME);
        return url.toString();
    }

    public static HBaseAdmin getHBaseAdmin() throws IOException {
        if (admin == null) {
            synchronized (HBaseUtils.class) {
                if (admin == null) {
                    admin = new HBaseAdmin(getConfiguration());
                    if (hConnection == null || hConnection.isClosed()) {
                        hConnection = admin.getConnection();
                    }
                }
            }
        }
        return admin;
    }

    public static HConnection getConnection() throws IOException {
        if (hConnection == null || hConnection.isClosed()) {
            synchronized (HBaseUtils.class) {
                if (hConnection == null || hConnection.isClosed())
                    hConnection = HConnectionManager.createConnection(conf);
            }
        }
        return hConnection;
    }

    public static void reset() throws IOException {
        if (hConnection != null) {
            hConnection.close();
            hConnection = null;
            admin = null;
        }
    }

    public static String getMasterURL() throws IOException {
        return createURL(ZooKeeperAdmin.getMasterAddress());
    }

    public static ServerName getMasterServerName() throws IOException {
        return ZooKeeperAdmin.getMasterAddress();
    }

    /**
     * 随机获取一个可用的RegionServer URL
     * 
     * @return
     * @throws IOException
     */
    public static String getRegionServerURL() throws IOException {
        List<ServerName> servers = ZooKeeperAdmin.getOnlineServers();
        ServerName sn = servers.get(random.nextInt(servers.size()));
        return createURL(sn);
    }

    public static String getRegionServerURL(String tableName, String rowKey) throws IOException {
        return getRegionServerURL(Bytes.toBytes(tableName), Bytes.toBytes(rowKey));
    }

    public static String getRegionServerURL(byte[] tableName, byte[] rowKey) throws IOException {
        HRegionLocation regionLocation = getConnection().locateRegion(tableName, rowKey);
        return createURL(regionLocation);
    }

    public static HBaseRegionInfo getHBaseRegionInfo(String tableName, String rowKey) throws IOException {
        return getHBaseRegionInfo(Bytes.toBytes(tableName), Bytes.toBytes(rowKey));
    }

    public static HBaseRegionInfo getHBaseRegionInfo(byte[] tableName, byte[] rowKey) throws IOException {
        HRegionLocation regionLocation = getConnection().locateRegion(tableName, rowKey);
        return new HBaseRegionInfo(regionLocation);
    }

    //-----------------以下代码来自org.apache.hadoop.hbase.client.HTable---------------------------//

    public static List<byte[]> getStartKeysInRange(byte[] tableName, byte[] startKey, byte[] endKey) throws IOException {
        Pair<byte[][], byte[][]> startEndKeys = getStartEndKeys(tableName);
        byte[][] startKeys = startEndKeys.getFirst();
        byte[][] endKeys = startEndKeys.getSecond();

        if (startKey == null) {
            startKey = HConstants.EMPTY_START_ROW;
        }
        if (endKey == null) {
            endKey = HConstants.EMPTY_END_ROW;
        }

        List<byte[]> rangeKeys = new ArrayList<byte[]>();
        for (int i = 0; i < startKeys.length; i++) {
            if (Bytes.compareTo(startKey, startKeys[i]) >= 0) {
                if (Bytes.equals(endKeys[i], HConstants.EMPTY_END_ROW) || Bytes.compareTo(startKey, endKeys[i]) < 0) {
                    rangeKeys.add(startKey);
                }
            } else if (Bytes.equals(endKey, HConstants.EMPTY_END_ROW) || //
                    Bytes.compareTo(startKeys[i], endKey) <= 0) { //原先代码是<=，因为coprocessorExec的语义是要包含endKey的
                rangeKeys.add(startKeys[i]);
            } else {
                break; // past stop
            }
        }

        return rangeKeys;
    }

    /**
     * Gets the starting and ending row keys for every region in the currently
     * open table.
     * <p>
     * This is mainly useful for the MapReduce integration.
     * @return Pair of arrays of region starting and ending row keys
     * @throws IOException if a remote or network exception occurs
     */
    public static Pair<byte[][], byte[][]> getStartEndKeys(byte[] tableName) throws IOException {
        NavigableMap<HRegionInfo, ServerName> regions = getRegionLocations(tableName);
        final List<byte[]> startKeyList = new ArrayList<byte[]>(regions.size());
        final List<byte[]> endKeyList = new ArrayList<byte[]>(regions.size());

        for (HRegionInfo region : regions.keySet()) {
            startKeyList.add(region.getStartKey());
            endKeyList.add(region.getEndKey());
        }

        return new Pair<byte[][], byte[][]>(startKeyList.toArray(new byte[startKeyList.size()][]),
                endKeyList.toArray(new byte[endKeyList.size()][]));
    }

    /**
     * Gets all the regions and their address for this table.
     * <p>
     * This is mainly useful for the MapReduce integration.
     * @return A map of HRegionInfo with it's server address
     * @throws IOException if a remote or network exception occurs
     */
    public static NavigableMap<HRegionInfo, ServerName> getRegionLocations(byte[] tableName) throws IOException {
        return MetaScanner.allTableRegions(conf, tableName, false);
    }

}
