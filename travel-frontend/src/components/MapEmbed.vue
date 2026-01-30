<template>
  <div class="map-embed">
    <div ref="mapContainerRef" class="map-embed__container"></div>
    <a
      v-if="googleMapsUrl"
      :href="googleMapsUrl"
      target="_blank"
      rel="noopener noreferrer"
      class="map-embed__open-google"
    >
      เปิดใน Google Maps
    </a>
  </div>
</template>

<script setup lang="ts">
import "leaflet/dist/leaflet.css";
import { ref, onMounted, onUnmounted, watch, computed } from "vue";
import type { Map as LeafletMap, Marker as LeafletMarker } from "leaflet";

interface Props {
  latitude: number;
  longitude: number;
  zoom?: number;
}

const props = withDefaults(defineProps<Props>(), {
  zoom: 14,
});

const mapContainerRef = ref<HTMLElement | null>(null);
let map: LeafletMap | null = null;
let marker: LeafletMarker | null = null;

const googleMapsUrl = computed(
  () => `https://www.google.com/maps?q=${props.latitude},${props.longitude}`,
);

function initMap() {
  if (typeof window === "undefined" || !mapContainerRef.value) return;

  import("leaflet").then((mod) => {
    const L = mod.default;

    map = L.map(mapContainerRef.value!).setView(
      [props.latitude, props.longitude],
      props.zoom,
    );

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);

    const customIcon = L.divIcon({
      className: "map-embed-marker",
      html: "<span></span>",
      iconSize: [24, 24],
      iconAnchor: [12, 24],
    });
    marker = L.marker([props.latitude, props.longitude], {
      icon: customIcon,
    }).addTo(map);
  });
}

function destroyMap() {
  if (map) {
    map.remove();
    map = null;
    marker = null;
  }
}

onMounted(() => {
  initMap();
});

onUnmounted(() => {
  destroyMap();
});

watch(
  () => [props.latitude, props.longitude] as const,
  ([lat, lng]) => {
    if (map && lat != null && lng != null) {
      map.setView([lat, lng], props.zoom);
      if (marker) marker.setLatLng([lat, lng]);
    }
  },
);
</script>

<style scoped>
@reference "tailwindcss";

.map-embed {
  @apply w-full rounded-xl overflow-hidden;
}

.map-embed__container {
  @apply w-full h-64;
  min-height: 16rem;
}

.map-embed__open-google {
  @apply mt-2 inline-block text-sm font-medium no-underline;
  color: var(--color-brand-600);
}

.map-embed__open-google:hover {
  color: var(--color-brand-800);
}

/* Leaflet marker (divIcon) - ป้องกัน default icon path 404 ใน Vite */
:deep(.map-embed-marker) {
  background: var(--color-brand-600);
  width: 24px;
  height: 24px;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
  border: 2px solid white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}
:deep(.map-embed-marker span) {
  display: block;
}
</style>
