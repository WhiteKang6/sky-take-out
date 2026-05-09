package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO) ;

    /**
     * 菜品分页查询
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
    /**
     * 菜品批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);
    /**
     * 通过id查询菜品信息
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDTO dishDTO);
}
