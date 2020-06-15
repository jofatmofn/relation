package org.sakuram.relation.util;

import org.sakuram.relation.bean.AppUser;
import org.sakuram.relation.bean.Tenant;
import org.springframework.stereotype.Component;

@Component
public class SecurityContext {
	
    private static ThreadLocal<Long> currentTenantId = new InheritableThreadLocal<>();
    private static ThreadLocal<Tenant> currentTenant = new InheritableThreadLocal<>();
    private static ThreadLocal<Long> currentUserId = new InheritableThreadLocal<>();
    private static ThreadLocal<AppUser> currentUser = new InheritableThreadLocal<>();

    public static Long getCurrentTenantId() {
        return currentTenantId.get();
    }

    public static void setCurrentTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    public static Tenant getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(Tenant tenant) {
        currentTenant.set(tenant);
    }

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static AppUser getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(AppUser appUser) {
        currentUser.set(appUser);
    }

    public static void clear() {
        currentTenantId.set(null);
        currentUserId.set(null);
    }
}
