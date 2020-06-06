package org.sakuram.relation.util;

public class SecurityContext {
    private static ThreadLocal<Long> currentTenant = new InheritableThreadLocal<>();
    private static ThreadLocal<Long> currentUser = new InheritableThreadLocal<>();

    public static Long getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(Long tenant) {
        currentTenant.set(tenant);
    }

    public static Long getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(Long user) {
        currentUser.set(user);
    }

    public static void clear() {
        currentTenant.set(null);
        currentUser.set(null);
    }
}
