package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {


    private SearchResponseAttrVo brand;
    private SearchResponseAttrVo category;


    private List<SearchResponseAttrVo> attrs;

    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    private List<GoodsVO> data;

}
