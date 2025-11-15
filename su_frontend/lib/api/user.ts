import { User } from "@/types/user.types";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function fetchCurrentUser(): Promise<User> {
  const response = await fetch(`${API_URL}/api/users/me`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Not authenticated");
  }

  return response.json();
}
