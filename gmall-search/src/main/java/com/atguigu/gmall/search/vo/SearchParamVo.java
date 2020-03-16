package com.atguigu.gmall.search.vo;


import lombok.Data;

@Data
public class SearchParamVo {

    private String keyword;

    private Long[] brandId;

    private Long[] categoryId;

    private String[] props;

    private String order;

    private Boolean store;

    private Double priceFrom;
    private Double priceto;

    private Integer pageNum = 1 ;
    private final Integer pageSize = 20 ;


}
