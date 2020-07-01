package com.ylink.aml.modular.system.util.chart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Vector;

/**
 * @author qy
 *
 *  系列:名字和数据集合 构成一条曲线</br> 可以将serie看作一根线或者一根柱子：
 *
 *  <p>
 *  参照JS图表来描述数据：</br> series: [{ name: 'Tokyo', data: [7.0, 6.9, 9.5, 14.5]
 *  },</br> { name: 'New York', data: [-0.2, 0.8, 5.7, 11.3} ]</br>
 *  </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Series implements Serializable {

    private static final long serialVersionUID = -4403344101835355645L;

    private String name;

    private Vector<Object> data;


    /**
     *
     * @param name
     *            名称（线条名称）
     * @param array
     *            数据（线条上的所有数据值）
     */
    public Series(String name, Object[] array) {
        this.name = name;
        if (array != null) {
            data = new Vector<Object>(array.length);
            for (int i = 0; i < array.length; i++) {
                data.add(array[i]);
            }
        }
    }


}
