package org.swasth.hcx.controllers.v1;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.dto.HeaderAudit;
import org.swasth.hcx.dto.SearchRequestDTO;
import org.swasth.hcx.service.HeaderAuditService;


@RestController
@RequestMapping("/"+ "${hcx_application.api_version}"+"/audit")
public class AuditController {

	private final HeaderAuditService service;
	
	@Autowired
	public AuditController(HeaderAuditService service) {
		this.service =  service;
	}

    @PostMapping("/search")
    public List<HeaderAudit> search(@RequestBody final SearchRequestDTO dto) {
        return service.search(dto);
    }

}
