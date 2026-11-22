import { apiRequest } from "@/shared/api/client";
import { isMockApi } from "@/shared/api/config";
import { CERTIFICATES } from "../data/mock";
import type { Certificate } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

/** TODO: o endpoint /certificados ainda não existe no backend. */
export async function fetchCertificates(): Promise<Certificate[]> {
  if (!isMockApi) return apiRequest<Certificate[]>("/certificados");

  await delay(400);
  return CERTIFICATES.map((c) => ({ ...c }));
}
