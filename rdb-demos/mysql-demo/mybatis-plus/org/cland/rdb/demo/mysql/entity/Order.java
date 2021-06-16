package org.cland.rdb.demo.mysql.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;

/**
 * 
 *
 * @author hack0303
 * @since 2021-06-15
 */
@TableName("t_order")
public class Order implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

        /**
     * 订单号
     */
        @TableId(type = IdType.UUID)
            private String id;
    
                    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    }
