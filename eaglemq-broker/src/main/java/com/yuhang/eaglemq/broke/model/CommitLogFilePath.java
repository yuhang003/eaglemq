package com.yuhang.eaglemq.broke.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitLogFilePath {
    private String fileName;
    private String filePath;
}
