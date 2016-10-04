package com.mapr.fs.s3;

import com.mapr.fs.Config;
import com.mapr.fs.PluginConfiguration;
import com.mapr.fs.utils.ConfigUtil;
import com.mapr.fs.dao.S3PluginDao;
import com.mapr.fs.dao.dto.SourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class S3Replicator {


    public static void main(String[] args) throws Exception {
        // Load configuration for S3 : bucket, volumes, ....
        // TODO : move from configuration to DB+API
        BasicConfigurator.configure();
        ConfigUtil.setConfigPath(args);
        Config conf = new Config("s3.");
        Properties properties = conf.getProperties();

        String accessKey = properties.getProperty("aws_access_key_id");
        String secretKey = properties.getProperty("aws_secret_access_key");

        startConsuming(accessKey, secretKey);
    }

    private static void startConsuming(String accessKey, String secretKey) throws IOException, InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        ConcurrentHashMap<String, PluginConfiguration> running = new ConcurrentHashMap<>();
        S3PluginDao dao = new S3PluginDao();

        List<String> keys = new LinkedList<>();

        while (true) {

            for (SourceDTO dto : dao.getAll()) {
                for (String volumeName : dto.getVolumes().keySet()) {

                    String bucketVolumeKey = dto.getBucket() + volumeName;
                    keys.add(bucketVolumeKey);
                    PluginConfiguration pluginConfiguration = getPluginConfiguration(accessKey, secretKey, dto, volumeName);

                    if (!running.containsKey(bucketVolumeKey)) {
                        runThreadForConsuming(service, running, volumeName, pluginConfiguration);
                        running.put(bucketVolumeKey, pluginConfiguration);
                    } else {
                        if (!running.get(bucketVolumeKey).equals(pluginConfiguration)) {
                            running.remove(bucketVolumeKey);
                            runThreadForConsuming(service, running, volumeName, pluginConfiguration);
                        }
                    }
                }
            }

            running = checkForDeleting(running, keys);
            keys.clear();
            Thread.sleep(1000);
        }
    }

    private static ConcurrentHashMap<String, PluginConfiguration> checkForDeleting(ConcurrentHashMap<String, PluginConfiguration> running, List<String> keys) {
        running.keySet().stream()
                .filter(key -> !keys.contains(key))
                .forEach(running::remove);
        return running;
    }

    private static void runThreadForConsuming(ExecutorService service, ConcurrentHashMap<String, PluginConfiguration> running,
                                              String volumeName, PluginConfiguration pluginConfiguration) {
        service.submit(() -> {
            try {
                new Gateway(volumeName, running, pluginConfiguration).processEvents();
            } catch (IOException e) {
                log.error("Cannot create Gateway" + e.getMessage());
            }
        });
        log.info("S3 Plugin Configuration " + pluginConfiguration);
    }

    private static PluginConfiguration getPluginConfiguration(String accessKey, String secretKey,
                                                              SourceDTO dto, String volumeName) {

        PluginConfiguration pluginConfiguration = new PluginConfiguration();

        pluginConfiguration.setAccessKey(accessKey);
        pluginConfiguration.setSecretKey(secretKey);
        pluginConfiguration.setVolumeName(volumeName);
        pluginConfiguration.setBucketName(dto.getBucket());
        pluginConfiguration.setCreateEnabled(dto.getVolumes().get(volumeName).isCreateEnabled());
        pluginConfiguration.setDeleteEnabled(dto.getVolumes().get(volumeName).isDeleteEnabled());
        pluginConfiguration.setModifyEnabled(dto.getVolumes().get(volumeName).isModifyEnabled());
        pluginConfiguration.setRenameEnabled(dto.getVolumes().get(volumeName).isRenameEnabled());
        return pluginConfiguration;
    }
}