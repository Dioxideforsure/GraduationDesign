package com.kuopan.DAO;

import com.kuopan.Entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * To put in User Information Mapper 接口
 * </p>
 *
 * @author Kuo
 * @since 2026-01-13
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    // Apply the change from XML, when adding space or reducing space.
    @Update("update user_info set occu_space = occu_space + #{occu_space} where user_id = #{id}")
    int updateRemainSpaceByGUID(@Param("id") String id, @Param("occu_space") Long occuSpace);

    @Update("update user_info set occu_space = if(occu_space > #{space}, occu_space - #{space}, 0) where user_id = #{id}")
    int decreaseOccuSpaceByUserId(@Param("id") String id, @Param("space") Long space);

    @Select("select occu_space, total_space from user_info where user_id = #{id}")
    UserInfo selectUserSpaceByUserId(@Param("id") String id);
}
