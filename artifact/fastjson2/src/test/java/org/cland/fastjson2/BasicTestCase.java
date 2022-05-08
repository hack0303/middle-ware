package org.cland.fastjson2;

import com.alibaba.fastjson2.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class BasicTestCase {


    @Test
    public void testBasicUseCase(){
        String str="{\"id\":18}";
        JSONObject jsonObject=JSON.parseObject(str);
        assert 18==jsonObject.getInteger("id");
        String arrayStr="[\"id\",18]";
        JSONArray jsonArray=JSON.parseArray(arrayStr);
        assert "id".equals(jsonArray.getString(0));
        assert 18==jsonArray.getInteger(1);
        Product product = new Product();
        product.id = 1001;
        product.name = "DataWorks";

        log.info("@#>obj str is:{}",JSON.toJSONString(product));
        log.info("@#>obj str is:{}",JSON.toJSONString(product, JSONWriter.Feature.BeanToArray));
    }

    @Data
    static class Product {
        public int id;
        public String name;
    }

    @Test
    public void testSr(){
        String str="{\"id\":18}";
       JSONObject jsonObject=JSON.parseObject(str);
       log.info("{}",jsonObject.getInteger("id"));
       byte[] bytes=JSONB.toBytes(str);
       log.info("@#>jsonb is {}",new String(bytes));
    }

    @Test
    public void testDr(){
        String str="{\"id\":18}";
        Product product=JSON.parseObject(str.getBytes(StandardCharsets.UTF_8),Product.class);
        log.info("@#>产品ID",product.getId());
    }

    @Test
    public void testJSONPath(){
        String str="{\"id\":18}";
        JSONPath jsonPath=JSONPath.of("$.id");
        JSONReader jsonReader=JSONReader.of(str);
        assert 18==jsonPath.extractInt32(jsonReader);
    }
}
