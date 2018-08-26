package cn.jml.pokonyan.gateway.repository.mysql;

import cn.jml.pokonyan.gateway.repository.mysql.entity.UserAccessInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccessInfoDao extends JpaRepository<UserAccessInfoEntity, String> {
    /**
     * 保存用户访问信息到MySQL
     *
     * @param userAccessInfoEntity
     * @return
     */
    UserAccessInfoEntity save(UserAccessInfoEntity userAccessInfoEntity);

}
