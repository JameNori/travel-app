<template>
  <div class="test-api-client">
    <h3>🧪 Test API Client</h3>
    <div class="test-section">
      <h4>1. Base URL</h4>
      <button @click="testBaseURL">Test Base URL</button>
      <pre v-if="baseURLResult">{{ baseURLResult }}</pre>
    </div>

    <div class="test-section">
      <h4>2. Request Interceptor (Token)</h4>
      <div class="token-controls">
        <input
          v-model="testToken"
          type="text"
          placeholder="Enter test token"
          class="token-input"
        />
        <button @click="setTestToken">Set Token</button>
        <button @click="clearToken">Clear Token</button>
      </div>
      <p>Current token: {{ currentToken || "None" }}</p>
    </div>

    <div class="test-section">
      <h4>3. API Call Test (Public)</h4>
      <button @click="testApiCall">Test GET /trips (Public)</button>
      <pre v-if="apiResult">{{ apiResult }}</pre>
    </div>

    <div class="test-section">
      <h4>3b. API Call Test (Protected)</h4>
      <button @click="testProtectedApiCall">
        Test GET /trips/mine (Protected)
      </button>
      <pre v-if="protectedApiResult">{{ protectedApiResult }}</pre>
    </div>

    <div class="test-section">
      <h4>4. Response Interceptor (401 Test)</h4>
      <button @click="test401Response">Test 401 Handling</button>
      <pre v-if="error401Result">{{ error401Result }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import api from "../api/client";

const baseURLResult = ref<string | null>(null);
const testToken = ref("");
const currentToken = ref<string | null>(null);
const apiResult = ref<any>(null);
const protectedApiResult = ref<any>(null);
const error401Result = ref<any>(null);

function testBaseURL() {
  const baseURL = api.defaults.baseURL;
  const envURL = import.meta.env.VITE_API_BASE_URL;
  baseURLResult.value = JSON.stringify(
    {
      "api.defaults.baseURL": baseURL,
      "import.meta.env.VITE_API_BASE_URL": envURL,
      match: baseURL === envURL,
    },
    null,
    2
  );
}

function setTestToken() {
  if (testToken.value) {
    localStorage.setItem("token", testToken.value);
    currentToken.value = testToken.value;
  } else {
    localStorage.setItem("token", "test-token-123");
    currentToken.value = "test-token-123";
  }
  testToken.value = "";
}

function clearToken() {
  localStorage.removeItem("token");
  currentToken.value = null;
}

async function testApiCall() {
  apiResult.value = "Loading...";
  try {
    const response = await api.get("/trips");
    apiResult.value = JSON.stringify(
      {
        success: true,
        status: response.status,
        dataLength: Array.isArray(response.data) ? response.data.length : "N/A",
        headers: {
          authorization: response.config.headers?.Authorization || "None",
        },
      },
      null,
      2
    );
  } catch (error: any) {
    apiResult.value = JSON.stringify(
      {
        success: false,
        error: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        headers: {
          authorization: error.config?.headers?.Authorization || "None",
        },
      },
      null,
      2
    );
  }
}

async function testProtectedApiCall() {
  protectedApiResult.value = "Loading...";
  try {
    const response = await api.get("/trips/mine");
    protectedApiResult.value = JSON.stringify(
      {
        success: true,
        status: response.status,
        dataLength: Array.isArray(response.data) ? response.data.length : "N/A",
        headers: {
          authorization: response.config.headers?.Authorization || "None",
        },
        note: "Protected endpoint accessible with token",
      },
      null,
      2
    );
  } catch (error: any) {
    protectedApiResult.value = JSON.stringify(
      {
        success: false,
        error: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        headers: {
          authorization: error.config?.headers?.Authorization || "None",
        },
        note:
          error.response?.status === 401
            ? "Correctly rejected - endpoint requires auth (no token or invalid token)"
            : "Unexpected error",
      },
      null,
      2
    );
  }
}

async function test401Response() {
  error401Result.value = "Testing...";
  // ใส่ token ที่ไม่ถูกต้อง
  localStorage.setItem("token", "invalid-token-for-401-test");

  try {
    // เรียก API ที่ต้อง auth (ถ้า backend return 401)
    await api.get("/trips/mine");
    error401Result.value = JSON.stringify(
      {
        note: "Backend did not return 401. Token might be valid or endpoint is public.",
      },
      null,
      2
    );
  } catch (error: any) {
    const tokenCleared = !localStorage.getItem("token");
    error401Result.value = JSON.stringify(
      {
        status: error.response?.status,
        tokenCleared: tokenCleared,
        message:
          error.response?.status === 401
            ? "401 handled correctly - token cleared"
            : "Expected 401 but got different status",
      },
      null,
      2
    );
  }
}

// อ่าน token ปัจจุบันเมื่อ component mount
currentToken.value = localStorage.getItem("token");
</script>

<style scoped>
.test-api-client {
  margin-top: 2rem;
  padding: 1.5rem;
  background: #f0f0f0;
  border-radius: 8px;
  border: 2px dashed #999;
}

.test-api-client h3 {
  color: #1e40af;
  margin-bottom: 1rem;
}

.test-section {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: white;
  border-radius: 4px;
}

.test-section h4 {
  color: black;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.test-section button {
  padding: 0.5rem 1rem;
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
  margin-bottom: 0.5rem;
}

.test-section button:hover {
  background: #1e3a8a;
}

.token-controls {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.token-input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  flex: 1;
  min-width: 200px;
}

pre {
  background: black;
  padding: 0.75rem;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 0.85rem;
  margin-top: 0.5rem;
}
</style>
