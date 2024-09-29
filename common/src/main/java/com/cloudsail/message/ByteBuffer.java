package com.cloudsail.message;

import java.util.Arrays;

public class ByteBuffer {
    public int flag;
    public int length;
    public byte[] data;
    public ByteBuffer(int length)
    {
        flag = -1;
        this.length = length;
        data = new byte[length];
    }
    public int Append(byte[] tmp)
    {
        if (tmp.length + flag + 1 > data.length)
        {
            return -1;
        }
        System.arraycopy(tmp, 0, data, flag + 1, tmp.length);
        flag += tmp.length;
        return flag;
    }
    public void Clean()
    {
        data = new byte[length];
        flag = -1;
    }
    public byte[] Cut(int offset, int length)
    {
        byte[] bytes = new byte[length];
        System.arraycopy(data, offset, bytes, 0, length);
        int tmp = -1;
        for (int i = offset + length; i <= flag; i++)
        {
            tmp = i - length;
            data[tmp] = data[i];
            // flag = i - length;
        }
        flag = tmp;
        Arrays.fill(data, flag + 1, this.data.length, (byte) 0);
        return bytes;
    }
    public byte Get(int index)
    {
        return this.data[index];
    }
}
