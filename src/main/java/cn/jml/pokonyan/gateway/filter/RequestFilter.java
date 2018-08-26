package cn.jml.pokonyan.gateway.filter;

import cn.jml.pokonyan.gateway.common.constants.Constants;
import cn.jml.pokonyan.gateway.common.utils.DateUtil;
import cn.jml.pokonyan.gateway.common.utils.LogUtil;
import cn.jml.pokonyan.gateway.common.utils.WebUtil;
import cn.jml.pokonyan.gateway.remote.IScottMapService;
import cn.jml.pokonyan.gateway.remote.entity.request.ScottMapRequest;
import cn.jml.pokonyan.gateway.remote.entity.response.ScottMapResponse;
import cn.jml.pokonyan.gateway.repository.mysql.UserAccessInfoDao;
import cn.jml.pokonyan.gateway.repository.mysql.entity.UserAccessInfoEntity;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 请求执行前需要经过的过滤器
 *
 * @author Justmemoryl   Email: justmemoryl@foxmail.com
 * @version 1.0 created at 2018/8/26 10:27 主要功能为：添加访问用户的信息到数据库
 **/
@Slf4j
public class RequestFilter extends ZuulFilter {
    @Autowired
    private UserAccessInfoDao repository;
    @Autowired
    private IScottMapService IScottMapService;

    /**
     * 过滤器执行的时间
     * <p>
     * 定义filter的类型，有pre、route、post、error四种<br />
     * PRE： 这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择请求的微服务、记录调试信息等。<br />
     * ROUTING：这种过滤器将请求路由到微服务。这种过滤器用于构建发送给微服务的请求，并使用Apache HttpClient或Netfilx Ribbon请求微服务。<br />
     * POST：这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。<br />
     * ERROR：在其他阶段发生错误时执行该过滤器。 除了默认的过滤器类型，Zuul还允许我们创建自定义的过滤器类型。例如，我们可以定制一种STATIC类型的过滤器，直接在Zuul中生成响应，而不将请求转发到后端的微服务。
     * </p>
     *
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤器执行的优先级，数字越小，优先级越高
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 表示是否需要执行该filter，true表示执行，false表示不执行,默认false
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 拦截器具体要执行的操作
     *
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        saveUserAccessInfo(request);
        return null;
    }

    /**
     * 保存用户访问信息到数据库
     */
    public void saveUserAccessInfo(HttpServletRequest request) {
        String ip = WebUtil.getOuterNetIp(request);
        ScottMapResponse locationInfo = getLocationInfoByIPFromScottMap(ip);
        UserAccessInfoEntity userAccessInfoEntity = resolveLocationInfo(ip, locationInfo);
        if (userAccessInfoEntity != null) {
            try {
                repository.save(userAccessInfoEntity);
            } catch (Exception e) {
                LogUtil.error(e, log, "用户访问信息入库失败，原因：%s", e.getMessage());
            }
        }
    }

    /**
     * 根据用户公网IP地址获取用户详细位置信息
     *
     * @param ip 用户外网IP
     * @return
     */
    private ScottMapResponse getLocationInfoByIPFromScottMap(String ip) {
        ScottMapResponse result = new ScottMapResponse();
        ScottMapRequest request = new ScottMapRequest();
        request.setIp(ip);
        request.setKey(Constants.SCOTTMAP_API_KEY);
        request.setOutput("json");
        try {
            result = IScottMapService.getLocationInfoByIP(request);
        } catch (Exception e) {
            LogUtil.error(e, log, "调用高德地图API失败，原因：%s", e.getMessage());
        }
        return result;
    }

    /**
     * 解析高德地图返回的地址信息，方便入库
     *
     * @param ip           用户外网IP
     * @param locationInfo 高德地图返回的地址信息
     * @return
     */
    private UserAccessInfoEntity resolveLocationInfo(String ip, ScottMapResponse locationInfo) {
        UserAccessInfoEntity result = new UserAccessInfoEntity();
        result.setIp(ip);
        if ("1".equals(locationInfo.getStatus())) {
            result.setProvince(locationInfo.getProvince());
            result.setCity(locationInfo.getCity());
            result.setAdcode(locationInfo.getAdcode());
            result.setRectangle(locationInfo.getRectangle());
            result.setTime(DateUtil.formatFullStandardDateTime(new Date()));
            return result;
        } else {
            LogUtil.error(log, "调用高德地图API成功，返回码异常 || Code：%s || Info：%s || InfoCode: %s", locationInfo.getStatus(), locationInfo.getInfo(),
                    locationInfo.getInfocode());
        }
        return null;
    }
}
