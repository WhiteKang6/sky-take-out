package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional()
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //插入1条数据
        dishMapper.insert(dish);
        //获取菜品id
        Long id=dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors !=null && flavors.size()>0) {
            flavors.forEach(flavor->{
                flavor.setDishId(id);
            });
            //插入菜品口味n条数据
            dishFlavorMapper.insertBatch(flavors);
        }


    }
    /**
     * 菜品分页查询
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        long total=page.getTotal();
        List<DishVO> records=page.getResult();
        PageResult pageResult=new PageResult(total,records);
        return pageResult;
    }
    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除___是否存在起售中的菜品
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE){
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

        }
        List<Long> setmealIds=setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && setmealIds.size()>0){
            //判断当前菜品是否能够删除---是否被套餐关联了？？
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        /*for (Long id : ids) {
            //删除菜品表中的菜品数据
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }*/
        //批量删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);
        //批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);




    }
    /**
     * 通过id查询菜品信息
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //获取菜品信息
        Dish dish = dishMapper.getById(id);
        //获取菜品口味信息.
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);
        //封装到VO对象
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //获取菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //获取口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //保存到数据库
        dishMapper.update(dish);

        //删除菜品关联的口味数据
        Long dishID=dish.getId();
        dishFlavorMapper.deleteByDishId(dishID);

        if (flavors !=null && flavors.size()>0) {
            flavors.forEach(flavor->{
                flavor.setDishId(dishID);
            });
            //插入菜品口味n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish=new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品列表
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }
}
