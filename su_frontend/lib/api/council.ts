import { apiFetch } from "./httpClient";
import { CouncilRequestDto, CouncilResponseDto } from "@/types/council.types";

export const fetchUserCouncils = async (): Promise<CouncilResponseDto[]> => {
  return apiFetch<CouncilResponseDto[]>("/councils");
};

export const joinCouncilByCode = async (joinCode: string): Promise<CouncilResponseDto> => {
  return apiFetch<CouncilResponseDto>(`/councils/join/${joinCode}`, {
    method: "POST",
  });
};

export const createCouncil = async (data: CouncilRequestDto): Promise<CouncilResponseDto> => {
  return apiFetch<CouncilResponseDto>("/councils", {
    method: "POST",
    body: JSON.stringify(data),
  });
};