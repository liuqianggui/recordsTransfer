package com.union.recordtransfer.servive;

import com.union.recordtransfer.utils.CallShell;
import com.union.recordtransfer.utils.ReadTxtQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.*;


/**
 * @author Stun_Me
 * @version 1.0
 */
@Slf4j
@Component
public class MasterThread implements ApplicationRunner {

    @Value("${path.targetpath}")
    private String targetpath;
    @Value("${path.company}")
    private String company;
    @Value("${path.shellpath}")
    private String shellpath;
    @Value("${path.tomovepath}")
    private String tomovepath;

    @Value("${path.file}")
    private String tailffix;
    /**
     * 创建线程池
     */
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,5
            ,10, TimeUnit.SECONDS
            ,new LinkedBlockingDeque());

    public Queue queue=new ArrayBlockingQueue(10000);


    /**
     *
     * 【完成录音文件迁移及MD5文件的生成】
     * 1)touchMark-->生成打标文件（生成迁移目录下的子目录层级）
     * 2）produceMd5-->生成文件的MD5值并调用迁移录音文件脚本，删除本次生成的MD5脚本
     * 3）moveAudio-->完成录音文件的迁移
     * @param args
     * @throws Exception
     */

    //TODO
    /**
     * 1)后台启动脚本
     * 2）日志输出配置文件，优化日志
     * 3）shell空目录怎么处理
     * 4）配置文件中同时处理多个目录代码
     * 5）配置文件中同时处理多种数据类型
     * @param args
     * @throws Exception
     */

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //执行第一个脚本遍历目录
        String shellTouchMark = shellpath + "/touchmark.sh";
        String shellProduceMd5 = shellpath + "/produceMD5.sh";

        log.info("shell命令:{}", shellTouchMark);
        CallShell call = new CallShell();

        String txtpath = call.callScript(shellTouchMark, tomovepath, shellpath);
        //String txtpath = "C:\\Users\\liuqianggui\\Desktop\\move\\20220422.txt";
        String test="测试提交权限";

        /**生产者-->通过处理目录文件获取到需要处理的子文件
         */

        new Thread(new Runnable() {
                @Override
                public void run() {
                    ReadTxtQueue queueTenplete = new ReadTxtQueue();
                    while (queue.size()>9000){
                        log.info("[{}][队列数据生产]当前队列数量大于9000:[{}]，程序正在休眠...",Thread.currentThread().getName(),queue.size());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    queueTenplete.readTxt(txtpath, queue);
                }
            }).start();
        /**
         * 消费者-->
         * 1)从队列中获取String数据（每一条字符串都是一个文件的文件名）
         * 2）对上一步获取的文件做解析获取文件中的每一行数据（每一行都是一个录音文件的路径）
         * 3） 生成MD5值
         */
        while(true){
          while(queue.size()>0){
              threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    /**
                     * 此处为将以下几个参数传递给<produceMd5.sh>脚本
                     * 1） 本次需要处理的文件目录
                     * 2） 要处理的文件类型，如"tar"
                     * 3)  要迁移的路径
                     * 4） 要迁移的路径第二层级目录
                     */
                    String filewaitingdeal = (String) queue.poll();
                    if(!StringUtils.hasText(filewaitingdeal)||filewaitingdeal==null){
                        return;
                    }
                    log.info("[{}][队列数据消费]当前队列数量:[{}]，正在处理...",Thread.currentThread().getName(),queue.size());
//                    脚本执行参数改变 1 要迁移的路径 2 company 3 迁移的路径 4 要写出的TXT的路径 5 要迁移的目标路径 6 要迁移的文件格式
//                  构建时间参数
                    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                    Date datee = new Date(System.currentTimeMillis());
                    String date=formatter.format(datee);
//                  构建要执行的格式file
                    String[] strArr= tailffix.split(",");
                    StringBuffer file = new StringBuffer();
                    for(int i = 0;i < strArr.length;i++){
                        file.append(strArr[i] + " ");
                    }
                    String Cmd=filewaitingdeal+" "+company+" "+tomovepath+" "+date+""+targetpath+" "+file;
                    log.info("[{}][队列数据消费]，执行shell:[{}]",Thread.currentThread().getName(),Cmd);
                    call.callScript(shellProduceMd5, Cmd, shellpath);
                }
            });
          }
        }

        }
    }


