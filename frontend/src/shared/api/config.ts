export const API_URL = process.env.EXPO_PUBLIC_API_URL ?? "";

export const isMockApi = API_URL === "";

export const REQUEST_TIMEOUT_MS = 15_000;

export const PAGE_SIZE = 10;
