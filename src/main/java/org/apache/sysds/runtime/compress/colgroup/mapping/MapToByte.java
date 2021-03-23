/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.runtime.compress.colgroup.mapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.sysds.utils.MemoryEstimates;

public class MapToByte implements IMapToData {

	private final byte[] _data;

	public MapToByte(int size) {
		_data = new byte[size];
	}

	private MapToByte(byte[] data) {
		_data = data;
	}

	@Override
	public int getIndex(int n) {
		return _data[n] & 0xFF;
	}

	@Override
	public void fill(int v) {
		Arrays.fill(_data, (byte) v);
	}

	@Override
	public long getInMemorySize() {
		return getInMemorySize(_data.length);
	}

	public static long getInMemorySize(int dataLength) {
		long size = 16; // object header
		size += MemoryEstimates.byteArrayCost(dataLength);
		return size;
	}

	@Override
	public long getExactSizeOnDisk() {
		return 4 + _data.length;
	}

	@Override
	public void set(int n, int v) {
		_data[n] = (byte) v;
	}

	@Override
	public int size() {
		return _data.length;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(_data.length);
		for(int i = 0; i < _data.length; i++)
			out.writeByte(_data[i]);
	}

	public static MapToByte readFields(DataInput in) throws IOException {
		final int length = in.readInt();
		final byte[] data = new byte[length];
		for(int i = 0; i < length; i++)
			data[i] = in.readByte();
		return new MapToByte(data);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(byte c : _data) {
			sb.append((int) c);
			sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
}
