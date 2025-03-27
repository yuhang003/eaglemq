package com.yuhang.eaglemq.broke.model;

import com.yuhang.eaglemq.broke.utils.ByteConvertUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommitLogMessageModel {

    private Integer size;
    private byte[] content;

    public byte[] convertToBytes() {
        byte[] sizeByte = ByteConvertUtils.intToBytes(size);
        byte[] content = getContent();
        byte[] mergeResultByte = new byte[sizeByte.length + content.length];
        for (int i = 0; i < sizeByte.length; i++) {
            mergeResultByte[i] = sizeByte[i];
        }

        for (int i = 0; i < content.length; i++) {
            mergeResultByte[i + sizeByte.length] = content[i];
        }
        return mergeResultByte;
    }
}
