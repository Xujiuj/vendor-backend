package org.dromara.carbon.vendor.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 统一维度数据 Service 接口
 * <p>按 dimensionCode 路由到对应的维度表，提供通用 CRUD。</p>
 *
 * @author carbon
 */
public interface ICvDimensionDataService {

    /**
     * 分页查询维度数据
     *
     * @param dimensionCode 维度编码
     * @param pageQuery     分页参数
     * @return 分页结果
     */
    TableDataInfo<?> queryPageList(String dimensionCode, PageQuery pageQuery);

    /**
     * 根据 ID 查询单条维度数据
     *
     * @param dimensionCode 维度编码
     * @param id            主键
     * @return 维度数据（Map 形式）
     */
    Map<String, Object> queryById(String dimensionCode, Long id);

    /**
     * 新增维度数据
     *
     * @param dimensionCode 维度编码
     * @param bo            业务数据（Map 形式）
     * @return 影响行数
     */
    int insertByBo(String dimensionCode, Map<String, Object> bo);

    /**
     * 修改维度数据
     *
     * @param dimensionCode 维度编码
     * @param bo            业务数据（Map 形式）
     * @return 影响行数
     */
    int updateByBo(String dimensionCode, Map<String, Object> bo);

    /**
     * 批量删除维度数据
     *
     * @param dimensionCode 维度编码
     * @param ids           主键集合
     * @return 影响行数
     */
    int deleteByIds(String dimensionCode, Collection<Long> ids);

    /**
     * 查询维度数据列表（不分页）
     *
     * @param dimensionCode 维度编码
     * @return 维度数据列表
     */
    List<Map<String, Object>> queryList(String dimensionCode);
}
