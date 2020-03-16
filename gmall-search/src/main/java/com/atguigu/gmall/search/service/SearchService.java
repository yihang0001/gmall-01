package com.atguigu.gmall.search.service;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchResponseAttrVo;
import com.atguigu.gmall.search.vo.SearchResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Attr;

import javax.xml.transform.Source;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public SearchResponseVo search(SearchParamVo searchParamVo) {

        try {

        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(searchParamVo));
        SearchResponse response = this.restHighLevelClient.search(null, RequestOptions.DEFAULT);
        SearchResponseVo responseVo = parseResult(response);

            responseVo.setPageNum(searchParamVo.getPageNum());
            responseVo.setPageSize(searchParamVo.getPageSize());

            return  responseVo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 构建查询条件
     * @return
     */

    private SearchSourceBuilder buildDsl(SearchParamVo searchParamVo){

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //1 查询和过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1 匹配查询
        String keyword = searchParamVo.getKeyword();
        if(StringUtils.isEmpty(keyword)){
            return null;
        }
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        //1.2 构建过滤
        //1.2.1 品牌过滤
        Long[] brandId = searchParamVo.getBrandId();
        if(brandId !=null && brandId.length!=0){
        boolQueryBuilder.filter(QueryBuilders.termQuery("brandId",brandId));
        }
        //1.2.2 分类过滤
        Long[] categoryId = searchParamVo.getCategoryId();
        if(categoryId !=null && categoryId.length!=0){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId",categoryId));
        }
        //1.2.3 价格区间
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceto = searchParamVo.getPriceto();
        if(priceFrom != null || priceto != null){
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
        if(priceFrom != null){
            rangeQuery.gte(priceFrom);
        }
        if(priceto != null){
            rangeQuery.lte(priceto);
        }
        boolQueryBuilder.filter(rangeQuery);
        }
        //1.2.4 有货
        Boolean store = searchParamVo.getStore();
        if(store != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
        //1.2.5 规格参数的嵌套
        String[] props = searchParamVo.getProps();
        if(props !=null && props.length != 0){
            for(String  prop : props) {
                String[] attr = StringUtils.split(prop, ":");
                if(attr == null || attr.length != 2){
                    continue;
                }
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",attr[0]));
                boolQuery.must(QueryBuilders.termQuery("attrs.attrValue",StringUtils.split(attr[1],"-")));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs",boolQuery, ScoreMode.None));
            }
        }
        //2 排序
        String order = searchParamVo.getOrder();
        if(StringUtils.isNotBlank(order)){
            String[] sorts = StringUtils.split(order, ":");
            if(sorts != null && sorts.length == 2){
                String sortFiled = "_score";
                switch (sorts[0]){
                    case "1" : sortFiled = "price"; break;
                    case "2" : sortFiled = "sale"; break;
                    case "3" : sortFiled = "createTime"; break;
                    default:
                        break;
                }
                sourceBuilder.sort(sortFiled,StringUtils.equals("desc",sorts[1])? SortOrder.DESC:SortOrder.ASC);
            }
        }

        //3 分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum - 1 )*pageSize);
        sourceBuilder.size(pageSize);
        //4 高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title")
                    .preTags("<font style='color:red'>").postTags("</font>")
        );
        //5 聚合
        //5.1 品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        //5.2 分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //5.3 规格参数嵌套聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")))));


        sourceBuilder.fetchSource(new String[]{"skuId","title","price","pic"},null);
        //System.out.println(sourceBuilder.toString());
        return  sourceBuilder;
    }

    /**
     * 解析结果集
     * @param response
     * @return
     */

    private SearchResponseVo parseResult(SearchResponse response){

        SearchResponseVo responseVo = new SearchResponseVo();

        SearchHits hits = response.getHits();

        responseVo.setTotal(hits.totalHits);

        SearchHit[] hitsHits = hits.getHits();
        List<GoodsVO> goodsVos = new ArrayList<>();
        for(SearchHit hitsHit : hitsHits){

            String GoodsVoJson = hitsHit.getSourceAsString();

            GoodsVO goodsVO = JSON.parseObject(GoodsVoJson, GoodsVO.class);

            HighlightField highlightField = hitsHit.getHighlightFields().get("title");
            Text fragment = highlightField.getFragments()[0];
            goodsVO.setTitle(fragment.string());
            goodsVos.add(goodsVO);
        }
        responseVo.setData(goodsVos);

        Map<String, Aggregation> aggMap = response.getAggregations().getAsMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(buckets)){
            List<String> brandValues = buckets.stream().map(bucket -> {
                HashMap<String, Object> map = new HashMap<>();
                long brandId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                map.put("id", brandId);

                ParsedStringTerms brandNameAgg = (ParsedStringTerms) bucket.getAggregations().get("brandNameAgg");
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());

                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            SearchResponseAttrVo brandVo = new SearchResponseAttrVo();

        brandVo.setAttrName("品牌");
        brandVo.setAttrValues(brandValues);
        responseVo.setBrand(brandVo);
        }

        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categorybuckets = categoryIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(categorybuckets)){
            List<String> categoryValues = categorybuckets.stream().map(bucket -> {
                HashMap<String, Object> map = new HashMap<>();
                long categoryId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                map.put("id", categoryId);

                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) bucket.getAggregations().get("categoryNameAgg");
                map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);

            }).collect(Collectors.toList());
            SearchResponseAttrVo categoryVo = new SearchResponseAttrVo();
            categoryVo.setAttrName("分类");
            categoryVo.setAttrValues(categoryValues);
            responseVo.setCategory(categoryVo);
        }

        ParsedNested attrsAgg = (ParsedNested) aggMap.get("attrsAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(attrIdAggBuckets)){
            List<SearchResponseAttrVo> attrVos = attrIdAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
                attrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());

                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> ValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(ValueAggBuckets)) {
                    List<String> attrValues = ValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    attrVo.setAttrValues(attrValues);
                }
                return attrVo;
            }).collect(Collectors.toList());
            responseVo.setAttrs(attrVos);
        }

        return  responseVo;
    }
}
