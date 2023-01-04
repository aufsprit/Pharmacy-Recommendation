package com.example.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetaDto {

    @JsonProperty("total_count")
    /*Json 으로 응답을 받을 때 mapping 을 시켜줌
    검색어에 검색된 수*/
    private Integer totalCount;

}
