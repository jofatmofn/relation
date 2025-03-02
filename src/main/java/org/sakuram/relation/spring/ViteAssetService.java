package org.sakuram.relation.spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ViteAssetService {
    private final Map<String, String> assetMap = new HashMap<>();

    public ViteAssetService() throws IOException {
    	ClassPathResource resource = new ClassPathResource("static/.vite/manifest.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode manifest = objectMapper.readTree(resource.getInputStream());
        
        manifest.fields().forEachRemaining(entry -> {
            String file = entry.getValue().get("file").asText();
            assetMap.put(entry.getKey(), file);
        });
    }

    public String getAsset(String fileName) {
        return assetMap.getOrDefault(fileName, fileName);
    }

}
