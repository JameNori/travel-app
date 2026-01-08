import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import "./style.css";

const app = createApp(App);
// ต้อง use Pinia ก่อน router เพื่อให้ router guard สามารถเข้าถึง store ได้
app.use(createPinia());
app.use(router);
app.mount("#app");
