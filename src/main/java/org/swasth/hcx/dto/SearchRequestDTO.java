package org.swasth.hcx.dto;

import org.swasth.hcx.dto.PagedRequestDTO;

import java.util.HashMap;
import java.util.Map;

public class SearchRequestDTO extends PagedRequestDTO {

    private Map<String, String> filters;

    public SearchRequestDTO() {}

    public SearchRequestDTO(Map<String, String> filters){
        this.filters = filters;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

}