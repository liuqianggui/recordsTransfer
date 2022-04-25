package com.union.recordtransfer.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

/**
 * @author Stun_Me
 * @version 1.0
 *
 */
@Slf4j
public class ReadTxtQueue {

    public void readTxt(String filePath,Queue queue) {


        StringBuilder lineTxt = new StringBuilder();

       //获取目录txt信息
        String content="";
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                // 遍历目录txt内容
                while ((content = br.readLine()) != null) {
                    lineTxt.append(content).append("\n");
                    while(queue.size()>9000){
                        log.info("[{}][队列数据生产]当前队列线程数量:[{}]，大于9000，需等待线程消费数据，线程休眠中...",Thread.currentThread().getName(),queue.size());
                        Thread.sleep(10000);
                    }
                    queue.add(content);
                }
                br.close();
            } else {
                log.info("[{}][队列数据生产]当前所查询文件不存在--->[{}]...",Thread.currentThread().getName(),filePath);

            }
        } catch (Exception e) {
            log.error("文件读取错误!{}",e);
        }

    }



}
