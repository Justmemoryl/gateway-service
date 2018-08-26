package cn.jml.pokonyan.gateway.remote.impl;

import cn.jml.pokonyan.gateway.common.constants.Constants;
import cn.jml.pokonyan.gateway.common.utils.LogUtil;
import cn.jml.pokonyan.gateway.remote.IScottMapService;
import cn.jml.pokonyan.gateway.remote.entity.request.ScottMapRequest;
import cn.jml.pokonyan.gateway.remote.entity.response.ScottMapResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jml on 2018/6/19 23:09
 **/
@Slf4j
@Component
public class IScottMapServiceImpl implements IScottMapService {

    @Override
    public ScottMapResponse getLocationInfoByIP(ScottMapRequest request) {
        BufferedReader bIn = null;
        ScottMapResponse result;
        try {
            URL url = new URL(Constants.SCOTTMAP_API_URL + "?ip=" + request.getIp() + "&key=" + request.getKey() + "&output=" + request.getOutput());
            URLConnection conn = url.openConnection();
            conn.connect();
            bIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = bIn.readLine()) != null) {
                sb.append(line);
            }
            result = JSON.parseObject(sb.toString(), ScottMapResponse.class);
            return result;
        } catch (Exception e) {
            LogUtil.error(e, log, "向高德地图API发送请求失败，原因：%s", e.getMessage());
        } finally {
            if (bIn != null) {
                try {
                    bIn.close();
                } catch (IOException e) {
                    LogUtil.error(e, log, "关闭BufferedReader失败，原因：%s", e.getMessage());
                }
            }
        }
        return null;
    }
}
