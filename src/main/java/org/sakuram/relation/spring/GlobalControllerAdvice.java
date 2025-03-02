package org.sakuram.relation.spring;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final ViteAssetService viteAssetService;

    public GlobalControllerAdvice(ViteAssetService viteAssetService) {
        this.viteAssetService = viteAssetService;
    }

    @ModelAttribute("viteAssetService")
    public ViteAssetService viteAssetService() {
        return viteAssetService;
    }
}
