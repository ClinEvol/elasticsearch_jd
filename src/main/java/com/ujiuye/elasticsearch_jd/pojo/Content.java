package com.ujiuye.elasticsearch_jd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Content {
    private String name;
    private String img;
    private String price;
}
