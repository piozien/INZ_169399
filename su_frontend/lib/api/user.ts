import { UserDto } from "@/types/user.types";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function fetchCurrentUser(): Promise<UserDto> {
  const response = await fetch(`${API_URL}/users/me`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Not authenticated");
  }

  return response.json();
}
