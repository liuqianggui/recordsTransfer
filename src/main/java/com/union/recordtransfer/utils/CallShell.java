package com.union.recordtransfer.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Stun_Me
 * @version 1.0
 * @date 2022/4/21 0:37
 * shell脚本工具类
 */
@Slf4j
public class CallShell {


    /**
     *
     * @param script 脚本名称
     * @param args 参数
     * @param workspace 脚本路径
     */
    public String callScript(String script, String args, String... workspace){
        Process process ;
        String result = null;
        try {
            String cmd = "sh " + script + " " + args;
            log.info(cmd);
            File dir = null;
            if(workspace[0] != null){
                dir = new File(workspace[0]);
            }
            String[] evnp = {"val=2", "call=Bash Shell"};
            process = Runtime.getRuntime().exec(cmd, evnp, dir);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((result = input.readLine()) != null) {
                log.info("[{}][脚本调用]返回结果:{}",Thread.currentThread().getName(),result);
                return result;
            }
            input.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


}
