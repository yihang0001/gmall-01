package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.vo.GoodsVO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepostiory extends ElasticsearchRepository<GoodsVO,Long> {
}
