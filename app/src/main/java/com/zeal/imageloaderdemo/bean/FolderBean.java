package com.zeal.imageloaderdemo.bean;

/**
 * Created by zeal on 16/11/27.
 * 封装了文件夹的相关信息
 */
public class FolderBean {

    private String folderName;//文件的名称

    private int count;//文件夹中文件的个数

    private String firstFilePath;//文件夹中第一个文件的路径

    private String folderPath;//文件夹的路径

    public String getFolderName() {
        return folderName;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFirstFilePath() {
        return firstFilePath;
    }

    public void setFirstFilePath(String firstFilePath) {
        this.firstFilePath = firstFilePath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
        //设置文件夹的名称
        int dotIndex = folderPath.lastIndexOf("/");
        this.folderName = folderPath.substring(dotIndex);
    }
}

