import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  // ใช้ createWebHistory แทน hash mode เพื่อ URL สะอาด (ไม่มี #) และ SEO ดีกว่า
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "landing",
      // Lazy loading: โหลด component เมื่อ route ถูกเข้าถึงจริง เพื่อลด initial bundle size
      component: () => import("../views/LandingPage.vue"),
    },
    {
      path: "/register",
      name: "register",
      component: () => import("../views/RegisterPage.vue"),
    },
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginPage.vue"),
    },
    {
      path: "/dashboard",
      name: "dashboard",
      component: () => import("../views/DashboardPage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/profile",
      name: "profile",
      component: () => import("../views/ProfilePage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/trips/:id",
      name: "trip-detail",
      component: () => import("../views/TripDetailPage.vue"),
    },
    // Catch-all route ต้องอยู่ท้ายสุด เพื่อ match route ที่ไม่พบทั้งหมด
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: () => import("../views/NotFoundPage.vue"),
    },
  ],
});

/**
 * Navigation Guard
 * ตรวจสอบ authentication ก่อนเข้าถึง protected routes
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth);

  // ถ้า route ต้องการ authentication แต่ user ยังไม่ login
  if (requiresAuth && !authStore.isAuthenticated) {
    // Redirect ไปหน้า login พร้อมเก็บ intended route
    next({
      name: "login",
      query: { redirect: to.fullPath },
    });
  } else {
    // อนุญาตให้เข้าถึง route
    next();
  }
});

export default router;
