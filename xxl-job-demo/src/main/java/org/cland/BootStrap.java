package org.cland;

import com.xxl.job.core.executor.impl.XxlJobSimpleExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cland.service.jobhandler.SampleXxlJob;

import java.util.Arrays;
import java.util.Properties;

import static org.cland.utils.PropertiesUtil.load;

@Slf4j
public class BootStrap {
    static XxlJobSimpleExecutor xxlJobExecutor = null;

    @SneakyThrows
    public static void main(String[] args) {
        // load executor prop
        Properties xxlJobProp = load("xxl-job-executor.properties");

        // init executor
        xxlJobExecutor = new XxlJobSimpleExecutor();
        xxlJobExecutor.setAdminAddresses(xxlJobProp.getProperty("xxl.job.admin.addresses"));
        xxlJobExecutor.setAccessToken(xxlJobProp.getProperty("xxl.job.accessToken"));
        xxlJobExecutor.setAppname(xxlJobProp.getProperty("xxl.job.executor.appname"));
        xxlJobExecutor.setAddress(xxlJobProp.getProperty("xxl.job.executor.address"));
        xxlJobExecutor.setIp(xxlJobProp.getProperty("xxl.job.executor.ip"));
        xxlJobExecutor.setPort(Integer.valueOf(xxlJobProp.getProperty("xxl.job.executor.port")));
        xxlJobExecutor.setLogPath(xxlJobProp.getProperty("xxl.job.executor.logpath"));
        xxlJobExecutor.setLogRetentionDays(Integer.valueOf(xxlJobProp.getProperty("xxl.job.executor.logretentiondays")));

        // registry job bean
        xxlJobExecutor.setXxlJobBeanList(Arrays.asList(new SampleXxlJob()));

        // start executor
        try {
            xxlJobExecutor.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            destoryXxlJobExecutor();
        }));

        synchronized (BootStrap.class) {
            BootStrap.class.wait();
        }
    }

    /**
     * destory
     */
    public static void destoryXxlJobExecutor() {
        if (xxlJobExecutor != null) {
            xxlJobExecutor.destroy();
        }
    }
}
