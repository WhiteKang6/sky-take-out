package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sky.vo.DishVO;

import java.util.List;

@Mapper
public interface DishMapper {





    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);



    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(value= OperationType.INSERT)
    void insert(Dish dish);
    /**
     * 菜品分页查询
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    /**
     * 删除菜品
     * @param id
     */
    @Delete("delete from dish where id=#{id} ")
    void deleteById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 更新菜品信息
     * @param dish
     */
    @AutoFill(value=OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品列表
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 根据套餐id查询菜品列表
     * @param setmealId
     * @return
     */
    @Select("select dish.* from dish left outer join setmeal_dish on dish.id = setmeal_dish.dish_id " +
            "where setmeal_id=#{setmealId} ")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据分类id查询起售中的菜品列表,
     */
    List<Dish> list(Dish dish);
}
